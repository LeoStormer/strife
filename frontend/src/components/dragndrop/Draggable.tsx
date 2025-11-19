import { type HTMLAttributes, type PropsWithChildren } from "react";
import { useDraggable, type UseDraggableArguments } from "@dnd-kit/core";
import { CSS, type Transform } from "@dnd-kit/utilities";
import StyleComposer from "../../utils/StyleComposer";

export type TransformOverride = (transform: Transform | null) => {
  transform: string | undefined;
};

type DraggableProps = PropsWithChildren<
  UseDraggableArguments &
    Pick<HTMLAttributes<HTMLDivElement>, "className" | "style"> & {
      transformOverride?: TransformOverride | undefined;
    }
>;

const transformToString: TransformOverride = (transform) => {
  return {
    transform: CSS.Transform.toString(transform),
  };
};

function Draggable({
  children,
  className,
  transformOverride = transformToString,
  style,
  ...useDraggableProps
}: DraggableProps) {
  const { attributes, listeners, setNodeRef, transform, isDragging } =
    useDraggable({
      ...useDraggableProps,
    });

  const classToApply = StyleComposer(className, {
    ["dragging"]: isDragging,
  });

  return (
    <div
      ref={setNodeRef}
      className={classToApply}
      {...attributes}
      {...listeners}
      style={{ ...style, ...transformOverride(transform) }}
    >
      {children}
    </div>
  );
}

export default Draggable;
