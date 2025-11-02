import { useSortable } from "@dnd-kit/sortable";
import { type Arguments } from "@dnd-kit/sortable/dist/hooks/useSortable";
import { CSS } from "@dnd-kit/utilities";
import { type PropsWithChildren } from "react";

type SortableProps = PropsWithChildren<Arguments>;

function SortableListItem({ children, ...useProps }: SortableProps) {
  const { attributes, listeners, setNodeRef, transform, transition } =
    useSortable(useProps);

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <li ref={setNodeRef} style={style} {...attributes} {...listeners}>
      {children}
    </li>
  );
}

export default SortableListItem;
