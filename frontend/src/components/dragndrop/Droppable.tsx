import { useDroppable, type UseDroppableArguments } from "@dnd-kit/core";
import { type PropsWithChildren } from "react";

type DroppableProps = PropsWithChildren<UseDroppableArguments>;

function Droppable({ children, ...useDroppableProps }: DroppableProps) {
  const { setNodeRef } = useDroppable(useDroppableProps);
  return <div ref={setNodeRef}>{children}</div>;
}

export default Droppable;
