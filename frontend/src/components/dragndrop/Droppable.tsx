import { useDroppable, UseDroppableArguments } from "@dnd-kit/core";
import { ReactNode } from "react";

type DroppableProps = UseDroppableArguments & {
  children?: ReactNode;
};

function Droppable({ children, ...useDroppableProps }: DroppableProps) {
  const { setNodeRef } = useDroppable(useDroppableProps);
  return <div ref={setNodeRef}>{children}</div>;
}

export default Droppable;
