import { useSortable } from "@dnd-kit/sortable";
import { type Arguments } from "@dnd-kit/sortable/dist/hooks/useSortable";
import { CSS, type Transform } from "@dnd-kit/utilities";
import { type PropsWithChildren } from "react";

export type StyleOverride = (
  transform: Transform | null,
  transition: string | undefined
) => {
  transform?: string | undefined;
  transition?: string | undefined;
};

type SortableProps = PropsWithChildren<
  Arguments & {
    styleOverride?: StyleOverride;
    className?: string | undefined;
  }
>;

const returnAsIs: StyleOverride = (transform, transition) => {
  return {
    transform: CSS.Transform.toString(transform),
    transition,
  };
};

function SortableListItem({
  children,
  styleOverride = returnAsIs,
  className,
  ...useProps
}: SortableProps) {
  const { attributes, listeners, setNodeRef, transform, transition } =
    useSortable(useProps);

  const style = styleOverride(transform, transition);

  return (
    <li
      ref={setNodeRef}
      style={style}
      className={className}
      {...attributes}
      {...listeners}
    >
      {children}
    </li>
  );
}

export default SortableListItem;
