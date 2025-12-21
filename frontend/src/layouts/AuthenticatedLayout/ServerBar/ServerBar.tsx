import { type RefObject, useRef, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  useServerSelectionContext,
  type Server,
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
import { arrayMove } from "@dnd-kit/sortable";
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

type MoverProps = {
  moverId: string;
  sourceId?: string | undefined;
  index: number;
  isDragging?: boolean;
};

function Mover({ moverId, sourceId, index, isDragging = false }: MoverProps) {
  return (
    <Droppable
      className={StyleComposer(styles.droppable, {
        [styles.dragging as string]: isDragging,
      })}
      id={`Mover(${moverId})`}
      data={{ source: sourceId, type: "mover", index }}
    />
  );
}

type ServerListItemProps = {
  server: Server;
  index: number;
  selectedServerId: string | null;
  draggingId: string | null;
};

function ServerListItem({
  server,
  index,
  selectedServerId,
  draggingId,
}: ServerListItemProps) {
  const { id, name, icon } = server;
  const { getTargetProps, getTriggerProps } = TooltipTrigger<HTMLLIElement>({
    tooltipText: name,
    tailStyle: "left",
  });
  return (
    <li {...getTargetProps()} key={id} className={styles.listItem}>
      <Draggable
        id={id}
        transformOverride={restrictSortableToOriginalPosition}
        data={{ index }}
        className={styles.draggable}
      >
        <Link
          {...getTriggerProps()}
          to={`/servers/${id}`}
          className={StyleComposer(styles.navItem, {
            [styles.selected as string]: selectedServerId === id,
          })}
        >
          <ServerIcon serverName={name} serverIconImage={icon} />
        </Link>
      </Draggable>
      <Mover
        moverId={id}
        sourceId={id}
        index={index}
        isDragging={draggingId === id}
      />
      <Droppable
        className={StyleComposer(`${styles.droppable} ${styles.combiner}`, {
          [styles.dragging as string]: draggingId === id,
        })}
        id={`Combiner(${id})`}
        data={{ source: id, type: "combiner", index }}
      />
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
  const { servers, setServers, selectedId, getServer } =
    useServerSelectionContext();
  const [draggingId, setDraggingId] = useState<string | null>(null);
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
    setDraggingId(event.active.id as string);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setDraggingId(null);
    const { active, over } = event;
    if (!over || !over.data.current || active.id === over.data.current.source) {
      return;
    }

    const willCombineIntoFolder = over.data.current.type === "combiner";

    if (willCombineIntoFolder) {
      // Combine them into a folder
    } else {
      const oldIndex = active.data.current?.index as number;
      const newIndex = over.data.current?.index as number;
      const shifter = oldIndex < newIndex ? -1 : 0;
      // Treats every index after the oldIndex as if the old didn't exist
      // for the purpose of reinsertion
      setServers((servers) => arrayMove(servers, oldIndex, newIndex + shifter));
    }
  };

  const handleDragCancel = (event: DragCancelEvent) => {
    void event;
    setDraggingId(null);
  };

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    }),
    useSensor(TouchSensor)
  );

  const serverListItems = servers.map((server, index) => (
    <ServerListItem
      server={server}
      index={index}
      selectedServerId={selectedId}
      draggingId={draggingId}
      key={server.id}
    />
  ));

  const draggingServerIcon = (() => {
    let server = draggingId ? getServer(draggingId) : null;
    return server ? (
      <ServerIcon serverName={server.name} serverIconImage={server.icon} />
    ) : null;
  })();

  return (
    <nav className={styles.navContainer}>
      <ul className={styles.serverBar}>
        <li
          {...directMessagesTriggerProps.getTargetProps()}
          key='direct-messages'
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
            key='add-server'
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
            <Mover moverId='Last' index={servers.length} />
          </li>
          <Modal style={{ pointerEvents: "none" }}>
            <DragOverlay modifiers={[snapCenterToCursor]}>
              <div className={styles.dragOverlay}>{draggingServerIcon}</div>
            </DragOverlay>
          </Modal>
        </DndContext>
        <li
          {...discoveryTriggerProps.getTargetProps()}
          key='server-discovery'
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
