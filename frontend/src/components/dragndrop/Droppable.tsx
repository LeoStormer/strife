import { useDroppable, type UseDroppableArguments } from "@dnd-kit/core";
import { type HTMLAttributes, type PropsWithChildren } from "react";
import StyleComposer from "../../utils/StyleComposer";

type DroppableProps = PropsWithChildren<
  UseDroppableArguments &
    Pick<HTMLAttributes<HTMLDivElement>, "className" | "style">
>;

function Droppable({
  children,
  className,
  style,
  ...useDroppableProps
}: DroppableProps) {
  const { setNodeRef, isOver } = useDroppable(useDroppableProps);
  const droppableClass = StyleComposer(className, {
    ["over"]: isOver,
  });

  return (
    <div ref={setNodeRef} className={droppableClass} style={style}>
      {children}
    </div>
  );
}

export default Droppable;
