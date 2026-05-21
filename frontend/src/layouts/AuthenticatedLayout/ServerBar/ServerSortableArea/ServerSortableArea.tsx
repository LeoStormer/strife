import { useRef, useState } from "react";
import {
  closestCenter,
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  pointerWithin,
  TouchSensor,
  useSensor,
  useSensors,
  type CollisionDetection,
  type DragCancelEvent,
  type DragEndEvent,
  type DragStartEvent,
  type KeyboardCoordinateGetter,
} from "@dnd-kit/core";
import { snapCenterToCursor } from "@dnd-kit/modifiers";
import Portal from "../../../../components/Portal";
import ServerIcon from "../../../../components/ServerIcon";
import {
  useServerSelectionContext,
  type Folder,
  type Server,
  type ServerItem,
} from "../../../../contexts/ServerSelectionContext";
import styles from "../ServerBar.module.css";
import type { ServerItemOrFolderRecord } from "../../../../contexts/ServerSelectionContext/serverReducer";
import {
  Mover,
  ServerFolder,
  ServerIconDisplayGrid,
  ServerListItem,
  type DragType,
} from "./ServerSortableComponents";

// TODO: Migrate from legacy dnd-kit to latest dnd-kit/react

const hybridCollision: CollisionDetection = (args) => {
  if (args.pointerCoordinates) {
    return pointerWithin(args);
  }

  return closestCenter(args);
};

type DragNDropData = {
  source: "last" | string;
  type?: "mover" | "combiner";
  folderId?: string | undefined;
};

type KeyboardNode = {
  id: string;
  type: "mover" | "combiner";
  source: string;
  folderId?: string | undefined;
};

const buildKeyboardGraph = (
  rootOrder: string[],
  servers: ServerItemOrFolderRecord,
): KeyboardNode[] => {
  const result: KeyboardNode[] = [];

  const pushServer = (serverId: string, folderId?: string) => {
    result.push({
      id: `Mover(${serverId})`,
      type: "mover",
      source: serverId,
      folderId,
    });

    result.push({
      id: `Combiner(${serverId})`,
      type: "combiner",
      source: serverId,
      folderId,
    });
  };

  for (const itemId of rootOrder) {
    const item = servers[itemId]!;

    if (item.type === "server") {
      pushServer(itemId);
    } else {
      // Folder itself
      result.push({
        id: `Mover(${itemId})`,
        type: "mover",
        source: itemId,
      });

      result.push({
        id: `Combiner(${itemId})`,
        type: "combiner",
        source: "last",
        folderId: itemId,
      });

      // Folder children
      for (const serverId of item.serverOrder) {
        pushServer(serverId, itemId);
      }
    }
  }

  // Final "last" drop zone
  result.push({
    id: `Mover(Last)`,
    type: "mover",
    source: "last",
  });

  return result;
};

function ServerSortableArea() {
  const { servers, rootOrder, getServer, selectedId, moveItem, createFolder } =
    useServerSelectionContext();
  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [dragType, setDragType] = useState<DragType>(null);
  const graphRef = useRef<KeyboardNode[] | null>(null);
  const keyboardIndexRef = useRef<number | null>(null);

  const handleDragStart = (event: DragStartEvent) => {
    const { type } = servers[event.active.id]!;
    setDraggingId(event.active.id as string);
    setDragType(type);

    const graph = buildKeyboardGraph(rootOrder, servers).filter((node) => {
      if (dragType === "folder" && node.type === "combiner") {
        return false;
      }
      return true;
    });
    graphRef.current = graph;
    const startIndex = graph.findIndex(
      (node) => node.source === event.active.id,
    );

    keyboardIndexRef.current = startIndex === -1 ? 0 : startIndex;
  };

  const customKeyboardCoordinates: KeyboardCoordinateGetter = (
    event,
    { context },
  ) => {
    // TODO: filter graph to be aware of folder open state
    // - Closed folders should have their children removed from the graph
    // - Arrows up and down should skip the children of closed folders
    // - If shift is pressed draggable should only see 'combiners'
    // otherwise only see 'movers'
    // - Arrow right should enter a folder, opening it and allowing
    // its children to be dragged over.
    // - Arrow left should exit a folder, closing it and causing its children
    // to be skipped
    const { draggingNodeRect, droppableRects } = context;
    const graph = graphRef.current;
    const currentIndex = keyboardIndexRef.current;
    if (!graph || currentIndex === null) return;

    let nextIndex = currentIndex;
    if (event.code === "ArrowDown") {
      nextIndex = Math.min(graph.length - 1, currentIndex + 1);
    }

    if (event.code === "ArrowUp") {
      nextIndex = Math.max(0, currentIndex - 1);
    }

    const next = graph[nextIndex];
    if (!next) return;

    keyboardIndexRef.current = nextIndex;

    const targetRect = droppableRects.get(next.id);
    return targetRect && draggingNodeRect
      ? {
          x: targetRect.left + (targetRect.width - draggingNodeRect.width) / 2,
          y: targetRect.top + (targetRect.height - draggingNodeRect.height) / 2,
        }
      : undefined;
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setDraggingId(null);
    setDragType(null);
    keyboardIndexRef.current = null;
    const { active, over } = event;
    if (!over || !over.data.current || active.id === over.data.current.source) {
      return;
    }

    const activeData = active.data.current as DragNDropData;
    const overData = over.data.current as DragNDropData;

    if (overData.type === "combiner") {
      if (servers[activeData.source]?.type === "folder") {
        return;
      }

      if (!overData.folderId) {
        const overIndex = rootOrder.indexOf(overData.source as string);
        createFolder([overData.source, activeData.source], overIndex);
        return;
      }

      const folder = servers[overData.folderId] as Folder;
      const nextOverIndex = folder.serverOrder.indexOf(overData.source) + 1;
      if (
        overData.source === "last" ||
        nextOverIndex === folder.serverOrder.length
      ) {
        moveItem(activeData.source, "last", overData.folderId);
        return;
      }

      moveItem(activeData.source, folder.serverOrder[nextOverIndex] as string);
      return;
    }

    if (overData.source == "last") {
      moveItem(activeData.source, overData.source, overData.folderId);
      return;
    }
    moveItem(activeData.source, overData.source);
  };

  const handleDragCancel = (event: DragCancelEvent) => {
    void event;
    setDraggingId(null);
    setDragType(null);
    keyboardIndexRef.current = null;
  };

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    }),
    useSensor(TouchSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: customKeyboardCoordinates,
      keyboardCodes: {
        start: ['Space'],
        end: ['Space'],
        cancel: ['Escape'],
        up: ['ArrowUp'],
        down: ['ArrowDown'],
        left: ['ArrowLeft'],
        right: ['ArrowRight'],
      },
    }),
  );

  const serverListItems = rootOrder.map((itemId) => {
    const item = servers[itemId]!;
    if (item.type === "server") {
      const { type, ...server } = item;
      return (
        <ServerListItem
          server={server}
          selectedServerId={selectedId}
          draggingId={draggingId}
          dragType={dragType}
          key={server.id}
        />
      );
    }
    const { type, ...folder } = item;
    const serverList = folder.serverOrder.map(
      (serverId) => getServer(serverId)!,
    );
    return (
      <ServerFolder
        id={folder.id}
        servers={serverList}
        selectedServerId={selectedId}
        draggingId={draggingId}
        dragType={dragType}
        key={folder.id}
      />
    );
  });

  const getDraggingIcon = () => {
    let item = draggingId ? servers[draggingId]! : null;
    if (!item) {
      return null;
    }

    if (item.type === "folder") {
      const serverItems = item.serverOrder.map(
        (serverId) => servers[serverId] as Server,
      );
      return <ServerIconDisplayGrid servers={serverItems} />;
    }

    const server = item as ServerItem;
    return (
      <ServerIcon serverName={server.name} serverIconImage={server.icon} />
    );
  };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={hybridCollision}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
      onDragCancel={handleDragCancel}
    >
      {serverListItems}
      <div className={styles.ghostWrapper}>
        <Mover moverId='Last' sourceId='last' dragType={dragType} />
      </div>
      <Portal style={{ pointerEvents: "none" }}>
        <DragOverlay modifiers={[snapCenterToCursor]}>
          <div className={styles.dragOverlay}>{getDraggingIcon()}</div>
        </DragOverlay>
      </Portal>
    </DndContext>
  );
}

export default ServerSortableArea;
