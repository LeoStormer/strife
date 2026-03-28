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
import Draggable, {
  type TransformOverride,
} from "../../../components/dragndrop/Draggable";
import Droppable from "../../../components/dragndrop/Droppable";
import Portal from "../../../components/Portal";
import Icon from "../../../components/Icon";
import ServerIcon from "../../../components/ServerIcon";
import {
  useServerSelectionContext,
  type Folder,
  type Server,
  type ServerItem,
} from "../../../contexts/ServerSelectionContext";
import StyleComposer from "../../../utils/StyleComposer";
import TooltipTrigger from "../../../components/TooltipTrigger";
import { Link } from "react-router-dom";
import styles from "./ServerBar.module.css";
import type { ServerItemOrFolderRecord } from "../../../contexts/ServerSelectionContext/serverReducer";

const restrictSortableToOriginalPosition: TransformOverride = (transform) => {
  void transform;
  return { transform: undefined };
};

const hybridCollision: CollisionDetection = (args) => {
  if (args.pointerCoordinates) {
    return pointerWithin(args);
  }

  return closestCenter(args);
};

type DragType = "server" | "folder" | null;

type DragNDropData = {
  source: "last" | string;
  type?: "mover" | "combiner";
  folderId?: string | undefined;
};

type MoverProps = {
  moverId: string;
  dragType: DragType;
  sourceId?: string | undefined;
  isDragging?: boolean;
  folderId?: string | undefined;
  isEnabled?: boolean;
};

function Mover({
  moverId,
  sourceId,
  isDragging = false,
  dragType,
  folderId,
  isEnabled = true,
}: MoverProps) {
  return (
    <Droppable
      className={StyleComposer(styles.droppable, {
        [styles.dragging as string]: isDragging,
        [styles.notAllowed as string]:
          dragType === "folder" && folderId != undefined,
      })}
      id={`Mover(${moverId})`}
      disabled={!isEnabled}
      data={{ source: sourceId, type: "mover", folderId }}
    />
  );
}

type ServerListItemProps = {
  server: Server;
  selectedServerId: string | null;
  draggingId: string | null;
  dragType: DragType;
  folderId?: string | undefined;
  isPillHidden?: boolean;
  isDNDEnabled?: boolean;
};

function ServerListItem({
  server,
  selectedServerId,
  draggingId,
  dragType,
  folderId,
  isPillHidden = false,
  isDNDEnabled = true,
}: ServerListItemProps) {
  const { id, name, icon } = server;
  const { getTargetProps, getTriggerProps } = TooltipTrigger<HTMLLIElement>({
    tooltipText: name,
    tailStyle: "left",
  });

  return (
    <li {...getTargetProps()} className={styles.listItem}>
      <Draggable
        id={id}
        transformOverride={restrictSortableToOriginalPosition}
        disabled={!isDNDEnabled}
        data={{ source: id, folderId }}
        className={styles.draggable}
      >
        <Link
          {...getTriggerProps()}
          tabIndex={0}
          to={`/servers/${id}`}
          className={StyleComposer(styles.navItem, {
            [styles.selected as string]: selectedServerId === id,
            [styles.pillHidden as string]: isPillHidden,
          })}
        >
          <ServerIcon serverName={name} serverIconImage={icon} />
        </Link>
      </Draggable>
      <Mover
        moverId={id}
        sourceId={id}
        isDragging={draggingId === id}
        dragType={dragType}
        folderId={folderId}
        isEnabled={isDNDEnabled}
      />
      <Droppable
        className={StyleComposer(`${styles.droppable} ${styles.combiner}`, {
          [styles.dragging as string]: draggingId === id,
          [styles.notAllowed as string]: dragType === "folder",
        })}
        id={`Combiner(${id})`}
        data={{ source: id, type: "combiner", folderId }}
        disabled={!isDNDEnabled}
      />
    </li>
  );
}

function ServerIconDisplayGrid({ servers }: { servers: Server[] }) {
  return (
    <div className={styles.iconDisplay}>
      {servers.map(({ name, icon, id }) => (
        <ServerIcon
          className={styles.icon}
          serverIconImage={icon}
          serverName={name}
          key={`icon-${id}`}
        />
      ))}
    </div>
  );
}

type ServerFolderProps = Omit<ServerListItemProps, "server" | "folderId"> & {
  servers: Server[];
  id: string;
};

function ServerFolder({
  id,
  servers,
  selectedServerId,
  draggingId,
  dragType,
  isDNDEnabled = true,
}: ServerFolderProps) {
  const [isOpen, setIsOpen] = useState(false);
  const names = servers.map((s) => s.name).reduce((s1, s2) => `${s1} | ${s2}`);
  const { getTargetProps, getTriggerProps } = TooltipTrigger<HTMLLIElement>({
    tooltipText: names,
    tailStyle: "left",
  });

  const folderClass = StyleComposer(`${styles.folder} ${styles.list}`, {
    [styles.open as string]: isOpen,
    [styles.pillHidden as string]: isOpen,
  });

  return (
    <li {...getTargetProps()} className={styles.listItem}>
      <Mover
        moverId={id}
        sourceId={id}
        dragType={dragType}
        isEnabled={isDNDEnabled}
        isDragging={draggingId === id}
      />
      <Droppable
        className={StyleComposer(`${styles.droppable} ${styles.combiner}`, {
          [styles.dragging as string]: draggingId === id,
          [styles.notAllowed as string]: dragType === "folder",
        })}
        id={`Combiner(${id})`}
        data={{ source: "last", type: "combiner", folderId: id }}
        disabled={!isDNDEnabled}
      />
      <Draggable
        id={id}
        data={{ source: id }}
        transformOverride={restrictSortableToOriginalPosition}
        disabled={!isDNDEnabled}
        className={styles.draggable}
      >
        <ul
          {...(!isOpen ? getTriggerProps() : {})}
          className={folderClass}
          onClick={() => {
            if (!isOpen) {
              setIsOpen(true);
            }
          }}
        >
          <div className={styles.wrapper}>
            <li className={styles.listItem}>
              <button
                {...getTriggerProps()}
                className={styles.folderButton}
                onClick={() => setIsOpen(false)}
              >
                <Icon name='folder-fill' />
              </button>
              <ServerIconDisplayGrid servers={servers} />
            </li>
            {servers.map((server, i) => (
              <ServerListItem
                folderId={id}
                selectedServerId={selectedServerId}
                server={server}
                draggingId={draggingId}
                dragType={dragType}
                isPillHidden={!isOpen}
                isDNDEnabled={isOpen}
                key={server.id}
              />
            ))}
          </div>
        </ul>
      </Draggable>
    </li>
  );
}

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
