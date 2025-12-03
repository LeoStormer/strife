import {
  type MouseEventHandler,
  type PropsWithChildren,
  useState,
} from "react";
import {
  useLocation,
  useNavigate,
  type NavigateFunction,
} from "react-router-dom";
import {
  useServerSelectionContext,
  type Server,
  type ServerSelectionContextType,
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
import ServerBarButton from "./Button";
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
  selectServer: ServerSelectionContextType["selectServer"];
  navigate: NavigateFunction;
  draggingId: string | null;
};

function ServerListItem({
  server,
  index,
  selectedServerId,
  selectServer,
  navigate,
  draggingId,
}: ServerListItemProps) {
  const { id, name, icon } = server;
  return (
    <li key={id} className={styles.listItem}>
      <Draggable
        id={id}
        transformOverride={restrictSortableToOriginalPosition}
        data={{ index }}
        className={styles.draggable}
      >
        <ServerBarButton
          isSelected={selectedServerId === id}
          onClick={() => {
            selectServer(id);
            navigate(`/servers/${id}`);
          }}
          tooltipText={name}
        >
          <ServerIcon serverName={name} serverIconImage={icon} />
        </ServerBarButton>
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
  const { servers, setServers, selectedId, selectServer, getServer } =
    useServerSelectionContext();
  const [draggingId, setDraggingId] = useState<string | null>(null);
  const [isAddServerSelected, setIsAddServerSelected] = useState(false);
  const location = useLocation();
  const isDirectMessagesSelected = location.pathname.includes(USER_LAYOUT_PATH);
  const isDiscoverySelected = location.pathname.includes(DISCOVERY_LAYOUT_PATH);
  const navigate = useNavigate();

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
      selectServer={selectServer}
      draggingId={draggingId}
      navigate={navigate}
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
    <>
      <ul className={styles.serverBar}>
        <li key='direct-messages' className={styles.listItem}>
          <ServerBarButton
            isSelected={isDirectMessagesSelected}
            onClick={() => navigate(USER_LAYOUT_PATH)}
            tooltipText='Direct Messages'
          >
            <Icon name='person-circle' />
          </ServerBarButton>
        </li>
        <DndContext
          sensors={sensors}
          collisionDetection={pointerWithin}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
          onDragCancel={handleDragCancel}
        >
          {serverListItems}
          <li key='add-server' className={styles.listItem}>
            <ServerBarButton
              isSelected={isAddServerSelected}
              onClick={() => setIsAddServerSelected(true)}
              tooltipText='Add a Server'
            >
              <Icon name='plus-lg' />
            </ServerBarButton>
            <Mover moverId='Last' index={servers.length} />
          </li>
          <Modal style={{ pointerEvents: "none" }}>
            <DragOverlay modifiers={[snapCenterToCursor]}>
              <div className={styles.dragOverlay}>{draggingServerIcon}</div>
            </DragOverlay>
          </Modal>
        </DndContext>
        <li key='server-discovery' className={styles.listItem}>
          <ServerBarButton
            isSelected={isDiscoverySelected}
            onClick={() => navigate(DISCOVERY_LAYOUT_PATH)}
            tooltipText='Discover'
          >
            <Icon name='compass' />
          </ServerBarButton>
        </li>
      </ul>
      {isAddServerSelected ? (
        <AddServerModal deselectButton={() => setIsAddServerSelected(false)} />
      ) : null}
    </>
  );
}

export default ServerBar;
