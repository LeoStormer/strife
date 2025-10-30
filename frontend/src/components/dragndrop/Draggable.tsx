import { type ReactNode } from "react";
import { useDraggable, type UseDraggableArguments } from "@dnd-kit/core";
import { CSS } from "@dnd-kit/utilities";

type DraggableProps = UseDraggableArguments & {
  children?: ReactNode;
};

function Draggable({ children, ...useDraggableProps }: DraggableProps) {
  const { attributes, listeners, setNodeRef, transform } =
    useDraggable(useDraggableProps);

  const style = transform
    ? { transform: CSS.Transform.toString(transform) }
    : undefined;

  return (
    <div ref={setNodeRef} {...attributes} {...listeners} style={style}>
      {children}
    </div>
  );
}

export default Draggable;
