import {
  type MouseEventHandler,
  type PropsWithChildren,
  useState,
} from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useServerSelectionContext } from "../../contexts/ServerSelectionContext";
import ServerIcon from "../ServerIcon";
import {
  closestCenter,
  DndContext,
  type DragCancelEvent,
  type DragEndEvent,
  type DragOverEvent,
  DragOverlay,
  type DragStartEvent,
  PointerSensor,
  TouchSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import SortableListItem, {
  type StyleOverride,
} from "../dragndrop/SortableListItem";
import Modal from "../Modal";
import Icon from "../Icon";
import ServerBarButton from "./Button";
import styles from "./ServerBar.module.css";

/**
 * a sidebar with a button to head towards user Layout path, a button for server discovery page, a button Selection of icons representing the Selection of servers a user is in.
 */
// TODO: drop sortable implementation and do what discord does instead
// have a dropable area that occupies top half of server button
// if dragged over there insert before server button
// and second droppable area that occupies lower half of server button
// if dragged over this combine active and over server into a folder
// or figure out how to achieve this using sortable and math
const DISCOVERY_PATH = "/servers/discover";
const DIRECT_MESSAGES_PATH = "/servers/@me/friends";
function ServerBar() {
  const { servers, setServers, selectedId, selectServer, getServer } =
    useServerSelectionContext();
  const [draggingId, setDraggingId] = useState<string | null>(null);
  const location = useLocation();
  const isDirectMessagesSelected =
    location.pathname.includes(DIRECT_MESSAGES_PATH);
  const isDiscoverySelected = location.pathname.includes(DISCOVERY_PATH);
  const [isButtonSelected, setIsButtonSelected] = useState([
    false,
    false,
    false,
  ]);

  const restrictSortableToOriginalPosition: StyleOverride = (
    _transform,
    transition
  ) => {
    return { transition };
  };
  const getButtonSelector = (index: number) => {
    return (isSelected: boolean) =>
      setIsButtonSelected(
        isButtonSelected.map((value, idx) =>
          idx === index ? isSelected : value
        )
      );
  };
  const navigate = useNavigate();

  const handleDragStart = (event: DragStartEvent) => {
    setDraggingId(event.active.id as string);
  };

  const handleDragOver = (event: DragOverEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) {
      return;
    }

    const willCombineIntoFolder = false;
    // see how close Acive is to the center of Over and either display a bar
    // above / below Over or diplay effect showing that they will combine into
    // a folder component. See dnd-kit events to learn how to do this

    if (willCombineIntoFolder) {
      // display effect
    } else {
      // display bar above or below
    }
  };

  const handleDragEnd = (event: DragEndEvent) => {
    setDraggingId(null);
    const { active, over } = event;
    if (!over || active.id === over.id) {
      return;
    }

    const willCombineIntoFolder = false;
    if (willCombineIntoFolder) {
      // Combine them into a folder
    } else {
      const oldIndex = servers.findIndex((server) => server.id === active.id);
      const newIndex = servers.findIndex((server) => server.id === over.id);
      setServers((servers) => arrayMove(servers, oldIndex, newIndex));
    }
  };

  const handleDragCancel = (event: DragCancelEvent) => {
    void event;
    setDraggingId(null);
  };
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { delay: 150, tolerance: 5 },
    }),
    useSensor(TouchSensor)
  );

  const serverListItems = servers.map((server) => (
    <SortableListItem
      id={server.id}
      key={server.id}
      className={styles.listItem}
      styleOverride={restrictSortableToOriginalPosition}
    >
      <ServerBarButton
        isSelected={selectedId === server.id}
        onClick={() => {
          selectServer(server.id);
          navigate(`/servers/${server.id}`);
        }}
        tooltipText={server.name}
      >
        <ServerIcon serverName={server.name} serverIconImage={server.icon} />
      </ServerBarButton>
    </SortableListItem>
  ));

  const draggingServerIcon = (() => {
    let server = draggingId ? getServer(draggingId) : null;
    return server ? (
      <ServerIcon serverName={server.name} serverIconImage={server.icon} />
    ) : null;
  })();

  return (
    <ul className={styles.serverBar}>
      <li key='direct-messages' className={styles.listItem}>
        <ServerBarButton
          isSelected={isDirectMessagesSelected}
          onClick={() => navigate(DIRECT_MESSAGES_PATH)}
          tooltipText='Direct Messages'
        >
          <Icon name='person-circle' />
        </ServerBarButton>
      </li>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
        onDragCancel={handleDragCancel}
      >
        <SortableContext
          items={servers.map((server) => server.id)}
          strategy={verticalListSortingStrategy}
        >
          {serverListItems}
        </SortableContext>
        <Modal>
          <DragOverlay>{draggingServerIcon}</DragOverlay>
        </Modal>
      </DndContext>
      <li key='add-server' className={styles.listItem}>
        <ServerBarButton
          isSelected={isButtonSelected[1]}
          onClick={() => getButtonSelector(1)(true)}
          tooltipText='Add a Server'
        >
          <Icon name='plus-lg' />
          {isButtonSelected[1] ? <Modal>Add a server Modal</Modal> : null}
        </ServerBarButton>
      </li>
      <li key='server-discovery' className={styles.listItem}>
        <ServerBarButton
          isSelected={isDiscoverySelected}
          onClick={() => navigate(DISCOVERY_PATH)}
          tooltipText='Discover'
        >
          <Icon name='compass' />
        </ServerBarButton>
      </li>
    </ul>
  );
}

export default ServerBar;
