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
  const targetRef = useRef<HTMLLIElement>(null);
  return (
    <li ref={targetRef} key={id} className={styles.listItem}>
      <Draggable
        id={id}
        transformOverride={restrictSortableToOriginalPosition}
        data={{ index }}
        className={styles.draggable}
      >
        <TooltipTrigger
          targetRef={targetRef as RefObject<HTMLElement>}
          tailStyle='left'
          tooltipText={name}
        >
          <Link
            to={`/servers/${id}`}
            className={StyleComposer(styles.navItem, {
              [styles.selected as string]: selectedServerId === id,
            })}
          >
            <ServerIcon serverName={name} serverIconImage={icon} />
          </Link>
        </TooltipTrigger>
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
  const directMessagesTargetRef = useRef<HTMLLIElement>(null);
  const addServerTargetRef = useRef<HTMLLIElement>(null);
  const discoveryTargetRef = useRef<HTMLLIElement>(null);

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
          ref={directMessagesTargetRef}
          key='direct-messages'
          className={styles.listItem}
        >
          <TooltipTrigger
            targetRef={directMessagesTargetRef as RefObject<HTMLElement>}
            tailStyle='left'
            tooltipText='Direct Messages'
          >
            <Link
              to={USER_LAYOUT_PATH}
              className={StyleComposer(styles.navItem, {
                [styles.selected as string]: isDirectMessagesSelected,
              })}
            >
              <Icon name='person-circle' />
            </Link>
          </TooltipTrigger>
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
            ref={addServerTargetRef}
            key='add-server'
            className={styles.listItem}
          >
            <TooltipTrigger
              targetRef={addServerTargetRef as RefObject<HTMLElement>}
              tailStyle='left'
              tooltipText='Add a Server'
            >
              <button
                onClick={() => setIsAddServerSelected(true)}
                className={StyleComposer(styles.navItem, {
                  [styles.selected as string]: isAddServerSelected,
                })}
              >
                <Icon name='plus-lg' />
              </button>
            </TooltipTrigger>
            <Mover moverId='Last' index={servers.length} />
          </li>
          <Modal style={{ pointerEvents: "none" }}>
            <DragOverlay modifiers={[snapCenterToCursor]}>
              <div className={styles.dragOverlay}>{draggingServerIcon}</div>
            </DragOverlay>
          </Modal>
        </DndContext>
        <li
          ref={discoveryTargetRef}
          key='server-discovery'
          className={styles.listItem}
        >
          <TooltipTrigger
            targetRef={discoveryTargetRef as RefObject<HTMLElement>}
            tailStyle='left'
            tooltipText='Discover'
          >
            <Link
              to={DISCOVERY_LAYOUT_PATH}
              className={StyleComposer(styles.navItem, {
                [styles.selected as string]: isDiscoverySelected,
              })}
            >
              <Icon name='compass' />
            </Link>
          </TooltipTrigger>
        </li>
      </ul>
      {isAddServerSelected ? (
        <AddServerModal deselectButton={() => setIsAddServerSelected(false)} />
      ) : null}
    </nav>
  );
}

export default ServerBar;
