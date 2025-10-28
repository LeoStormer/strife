import { MouseEventHandler, ReactNode, useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ServerSelectionContext } from "../contexts/ServerSelectionContext";
import ServerIcon from "./ServerIcon";
import {
  closestCenter,
  DndContext,
  DragEndEvent,
  DragOverlay,
  PointerSensor,
  TouchSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import SortableListItem from "./dragndrop/SortableListItem";

type ServerBarButtonProps = {
  isSelected: boolean;
  children?: ReactNode;
  onClick?: MouseEventHandler<HTMLButtonElement>;
};

function ServerBarButton({
  children,
  onClick,
  isSelected = false,
}: ServerBarButtonProps) {
  // is this selected?
  // if selected or hovered over, make button background the active color
  // what is the name of the button
  // create a tooltip that hovers next to this button if hovered over
  const buttonClass = `server-bar-button${isSelected ? ' selected': ''}` ;

  return <button className={buttonClass} onClick={onClick}>{children}</button>;
}

/**
 * a sidebar with a button to head towards user Layout path, a button for server discovery page, a button Selection of icons representing the Selection of servers a user is in.
 */
function ServerBar() {
  const { servers, selectedId, selectServer } = useContext(
    ServerSelectionContext
  );
  const [isButtonSelected, setIsButtonSelected] = useState([
    false,
    false,
    false,
  ]);
  const navigate = useNavigate();

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (over && active.id !== over.id) {
    }
  };

  const sensors = useSensors(useSensor(PointerSensor), useSensor(TouchSensor));

  const serverIconButtons = servers.map((server) => (
    <SortableListItem id={server.id} key={server.id}>
      <ServerBarButton
        isSelected={selectedId === server.id}
        onClick={(e) => {
          navigate(`/servers/${server.id}`);
          selectServer(server.id);
        }}
      >
        <ServerIcon serverName={server.name} serverIconImage={server.icon} />
      </ServerBarButton>
    </SortableListItem>
  ));

  return (
    <div className='server-bar'>
      <ul>
        {[
          <li key='direct-messages'>
            <ServerBarButton isSelected={isButtonSelected[0]}>
              <label>direct messages</label>
            </ServerBarButton>
          </li>,
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
            <DragOverlay></DragOverlay>
          </DndContext>,
          <li key='add-server'>
            <ServerBarButton isSelected={isButtonSelected[1]}>
              <label>add a server</label>
            </ServerBarButton>
          </li>,
          <li key='server-discovery'>
            <ServerBarButton isSelected={isButtonSelected[2]}>
              <label>discovery</label>
            </ServerBarButton>
          </li>,
        ]}
      </ul>
    </div>
  );
}

export default ServerBar;
