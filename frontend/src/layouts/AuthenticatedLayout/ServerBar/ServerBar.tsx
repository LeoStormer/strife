import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  useServerSelectionContext,
  type Folder,
  type Server,
  type ServerItem,
} from "../../../contexts/ServerSelectionContext";
import ServerIcon from "../../../components/ServerIcon";
import {
  DndContext,
  type DragCancelEvent,
  type DragEndEvent,
  DragOverlay,
  type DragStartEvent,
  PointerSensor,
  pointerWithin,
  TouchSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import Modal from "../../../components/Modal";
import Icon from "../../../components/Icon";
import TooltipTrigger from "../../../components/TooltipTrigger";
import styles from "./ServerBar.module.css";
import AddServerModal from "./AddServerModal";
import { snapCenterToCursor } from "@dnd-kit/modifiers";
import Droppable from "../../../components/dragndrop/Droppable";
import Draggable, {
  type TransformOverride,
} from "../../../components/dragndrop/Draggable";
import StyleComposer from "../../../utils/StyleComposer";
import { DISCOVERY_LAYOUT_PATH, USER_LAYOUT_PATH } from "../../../constants";

const restrictSortableToOriginalPosition: TransformOverride = (transform) => {
  void transform;
  return { transform: undefined };
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
              <div className={styles.iconDisplay}>
                {servers.map(({ name, icon }) => (
                  <ServerIcon
                    className={styles.icon}
                    serverIconImage={icon}
                    serverName={name}
                  />
                ))}
              </div>
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

/**
 * A sidebar with a button to navigate to user Layout path, a button that
 * navigates to the server discovery page, a sortable list of icon buttons
 * representing the selection of servers a user has joined that navigate to
 * that server's page when clicked.
 */
function ServerBar() {
  const {
    servers,
    rootOrder,
    selectedId,
    getServer,
    moveItem,
    createFolder,
    isLoading,
  } = useServerSelectionContext();

  // TODO if isLoading show a loading state
  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [dragType, setDragType] = useState<DragType>(null);
  const [isAddServerSelected, setIsAddServerSelected] = useState(false);
  const location = useLocation();
  const isDirectMessagesSelected = location.pathname.includes(USER_LAYOUT_PATH);
  const isDiscoverySelected = location.pathname.includes(DISCOVERY_LAYOUT_PATH);
  const directMessagesTriggerProps = TooltipTrigger<HTMLLIElement>({
    tooltipText: "Direct Messages",
    tailStyle: "left",
  });
  const addServerTriggerProps = TooltipTrigger<HTMLLIElement>({
    tooltipText: "Add a Server",
    tailStyle: "left",
  });
  const discoveryTriggerProps = TooltipTrigger<HTMLLIElement>({
    tooltipText: "Discover",
    tailStyle: "left",
  });

  const handleDragStart = (event: DragStartEvent) => {
    const { type } = servers[event.active.id]!;
    setDraggingId(event.active.id as string);
    setDragType(type);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setDraggingId(null);
    setDragType(null);
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
  };

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    }),
    useSensor(TouchSensor)
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
      (serverId) => getServer(serverId)!
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

  const draggingServerIcon = (() => {
    let item = draggingId ? servers[draggingId]! : null;
    if (!item) {
      return null;
    }

    if (item.type === "folder") {
      return (
        <div className={styles.iconDisplay}>
          {item.serverOrder.map((serverId) => {
            const { name, icon } = servers[serverId] as ServerItem;
            return (
              <ServerIcon
                className={styles.icon}
                serverIconImage={icon}
                serverName={name}
              />
            );
          })}
        </div>
      );
    }
    const server = item as ServerItem;
    return (
      <ServerIcon serverName={server.name} serverIconImage={server.icon} />
    );
  })();

  return (
    <nav className={styles.navContainer}>
      <ul className={styles.list}>
        <li
          {...directMessagesTriggerProps.getTargetProps()}
          className={styles.listItem}
        >
          <Link
            {...directMessagesTriggerProps.getTriggerProps()}
            to={USER_LAYOUT_PATH}
            className={StyleComposer(styles.navItem, {
              [styles.selected as string]: isDirectMessagesSelected,
            })}
          >
            <Icon name='person-circle' />
          </Link>
        </li>
        <div className={styles.separator}></div>
        <DndContext
          sensors={sensors}
          collisionDetection={pointerWithin}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
          onDragCancel={handleDragCancel}
        >
          {serverListItems}
          <li
            {...addServerTriggerProps.getTargetProps()}
            className={styles.listItem}
          >
            <button
              {...addServerTriggerProps.getTriggerProps()}
              onClick={() => setIsAddServerSelected(true)}
              className={StyleComposer(styles.navItem, {
                [styles.selected as string]: isAddServerSelected,
              })}
            >
              <Icon name='plus-lg' />
            </button>
            <Mover moverId='Last' sourceId='last' dragType={dragType} />
          </li>
          <Modal style={{ pointerEvents: "none" }}>
            <DragOverlay modifiers={[snapCenterToCursor]}>
              <div className={styles.dragOverlay}>{draggingServerIcon}</div>
            </DragOverlay>
          </Modal>
        </DndContext>
        <li
          {...discoveryTriggerProps.getTargetProps()}
          className={styles.listItem}
        >
          <Link
            {...discoveryTriggerProps.getTriggerProps()}
            to={DISCOVERY_LAYOUT_PATH}
            className={StyleComposer(styles.navItem, {
              [styles.selected as string]: isDiscoverySelected,
            })}
          >
            <Icon name='compass' />
          </Link>
        </li>
      </ul>
      {isAddServerSelected ? (
        <AddServerModal deselectButton={() => setIsAddServerSelected(false)} />
      ) : null}
    </nav>
  );
}

export default ServerBar;
