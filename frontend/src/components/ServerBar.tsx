import {
  type MouseEventHandler,
  type PropsWithChildren,
  useState,
} from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import { useServerSelectionContext } from "../contexts/ServerSelectionContext";
import ServerIcon from "./ServerIcon";
import {
  closestCenter,
  DndContext,
  type DragEndEvent,
  DragOverlay,
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
import SortableListItem from "./dragndrop/SortableListItem";

type ServerBarButtonProps = PropsWithChildren<{
  isSelected?: boolean | undefined;
  onClick?: MouseEventHandler<HTMLButtonElement>;
}>;

function ServerBarButton({
  isSelected = false,
  children,
  onClick,
}: ServerBarButtonProps) {
  // what is the name of the button
  // create a tooltip that hovers next to this button if hovered over
  // displaying the name of the button
  const buttonClass = `server-bar-button${isSelected ? " selected" : ""}`;

  return (
    <button className={buttonClass} onClick={onClick}>
      {children}
    </button>
  );
}

type NavigateButtonProps = Omit<ServerBarButtonProps, "onClick"> & {
  navigate: () => void;
  setIsSelected: (value: boolean) => void;
};

function NavigateButton({
  isSelected = false,
  setIsSelected,
  navigate,
  children,
}: NavigateButtonProps) {
  const onClickHandler = () => {
    setIsSelected(true);
    navigate();
  };

  return (
    <ServerBarButton onClick={onClickHandler} isSelected={isSelected}>
      {children}
    </ServerBarButton>
  );
}

/**
 * a sidebar with a button to head towards user Layout path, a button for server discovery page, a button Selection of icons representing the Selection of servers a user is in.
 */
function ServerBar() {
  const { servers, setServers, selectedId, selectServer, getServer } =
    useServerSelectionContext();
  const [isButtonSelected, setIsButtonSelected] = useState([
    false,
    false,
    false,
  ]);

  const getButtonSelector = (index: number) => {
    return (isSelected: boolean) =>
      setIsButtonSelected(
        isButtonSelected.map((value, idx) =>
          idx === index ? isSelected : value
        )
      );
  };
  const navigate = useNavigate();

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) {
      return;
    }
    const oldIndex = servers.findIndex((server) => server.id === active.id);
    const newIndex = servers.findIndex((server) => server.id === over.id);
    setServers((servers) => arrayMove(servers, oldIndex, newIndex));
  };

  const sensors = useSensors(useSensor(PointerSensor), useSensor(TouchSensor));

  const serverIconButtons = servers.map((server) => (
    <SortableListItem id={server.id} key={server.id}>
      <NavigateButton
        isSelected={selectedId === server.id}
        setIsSelected={() => selectServer(server.id)}
        navigate={() => navigate(`/servers/${server.id}`)}
      >
        <ServerIcon serverName={server.name} serverIconImage={server.icon} />
      </NavigateButton>
    </SortableListItem>
  ));

  const currentServerIcon = (() => {
    let server = selectedId ? getServer(selectedId) : null;
    return server ? (
      <ServerIcon serverName={server.name} serverIconImage={server.icon} />
    ) : null;
  })();

  //Potential error: selection and dragging may not be the same thing
  return (
    <div className='server-bar'>
      <ul>
        <li key='direct-messages'>
          <NavigateButton
            isSelected={isButtonSelected[0]}
            setIsSelected={getButtonSelector(0)}
            navigate={() => navigate("servers/@me")}
          >
            <label>direct messages</label>
          </NavigateButton>
        </li>
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext
            items={servers.map((server) => server.id)}
            strategy={verticalListSortingStrategy}
          >
            {serverIconButtons}
          </SortableContext>
          {createPortal(
            <DragOverlay>{currentServerIcon}</DragOverlay>,
            document.body
          )}
        </DndContext>
        <li key='add-server'>
          <ServerBarButton
            isSelected={isButtonSelected[1]}
            onClick={() => getButtonSelector(1)(true)}
          >
            <label>add a server</label>
            {/* If selected render a modal */}
          </ServerBarButton>
        </li>
        <li key='server-discovery'>
          <NavigateButton
            isSelected={isButtonSelected[2]}
            setIsSelected={getButtonSelector(2)}
            navigate={() => navigate("/servers/discover")}
          >
            <label>discovery</label>
          </NavigateButton>
        </li>
      </ul>
    </div>
  );
}

export default ServerBar;
