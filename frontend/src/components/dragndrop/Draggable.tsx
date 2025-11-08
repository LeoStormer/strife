import { type ReactNode } from "react";
import { useDraggable, type UseDraggableArguments } from "@dnd-kit/core";
import { CSS, type Transform } from "@dnd-kit/utilities";

type StyleOverride = (transform: Transform | null) => {
  transform: string | undefined;
};

type DraggableProps = UseDraggableArguments & {
  children?: ReactNode;
  styleOveride?: StyleOverride | undefined;
};

const returnAsIs: StyleOverride = (transform) => {
  return {
    transform: CSS.Transform.toString(transform),
  };
};

function Draggable({
  children,
  styleOveride = returnAsIs,
  ...useDraggableProps
}: DraggableProps) {
  const { attributes, listeners, setNodeRef, transform } = useDraggable({
    ...useDraggableProps,
  });

  const style = styleOveride(transform);

  return (
    <div ref={setNodeRef} {...attributes} {...listeners} style={style}>
      {children}
    </div>
  );
}

export default Draggable;
