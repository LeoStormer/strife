import { useSortable } from "@dnd-kit/sortable";
import { Arguments } from "@dnd-kit/sortable/dist/hooks/useSortable";
import { CSS } from "@dnd-kit/utilities";
import { Key, ReactNode } from "react";

type SortableProps = Arguments & {
  children?: ReactNode;
  key: Key;
};

function SortableListItem({ children, key, ...useProps }: SortableProps) {
  const { attributes, listeners, setNodeRef, transform, transition } =
    useSortable(useProps);

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <li key={key} ref={setNodeRef} style={style} {...attributes} {...listeners}>
      {children}
    </li>
  );
}

export default SortableListItem;
