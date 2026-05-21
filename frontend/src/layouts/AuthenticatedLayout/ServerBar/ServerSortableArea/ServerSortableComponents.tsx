import { Link } from "react-router-dom";
import Droppable from "../../../../components/dragndrop/Droppable";
import StyleComposer from "../../../../utils/StyleComposer";
import Draggable, { type TransformOverride } from "../../../../components/dragndrop/Draggable";
import styles from "../ServerBar.module.css";
import type { Server } from "../../../../contexts/ServerSelectionContext";
import TooltipTrigger from "../../../../components/TooltipTrigger";
import ServerIcon from "../../../../components/ServerIcon";
import { useState } from "react";
import Icon from "../../../../components/Icon";

const restrictSortableToOriginalPosition: TransformOverride = (transform) => {
  void transform;
  return { transform: undefined };
};

export type DragType = "server" | "folder" | null;

type MoverProps = {
  moverId: string;
  dragType: DragType;
  sourceId?: string | undefined;
  isDragging?: boolean;
  folderId?: string | undefined;
  isEnabled?: boolean;
};

export function Mover({
  moverId,
  sourceId,
  isDragging = false,
  dragType,
  folderId,
  isEnabled = true,
}: MoverProps) {
  return (
    <Droppable
      className={StyleComposer(styles.droppable, {
        [styles.dragging as string]: isDragging,
        [styles.notAllowed as string]:
          dragType === "folder" && folderId != undefined,
      })}
      id={`Mover(${moverId})`}
      disabled={!isEnabled}
      data={{ source: sourceId, type: "mover", folderId }}
    />
  );
}

type ServerListItemProps = {
  server: Server;
  selectedServerId: string | null;
  draggingId: string | null;
  dragType: DragType;
  folderId?: string | undefined;
  isPillHidden?: boolean;
  isDNDEnabled?: boolean;
};

export function ServerListItem({
  server,
  selectedServerId,
  draggingId,
  dragType,
  folderId,
  isPillHidden = false,
  isDNDEnabled = true,
}: ServerListItemProps) {
  const { id, name, icon } = server;
  const { getTargetProps, getTriggerProps } = TooltipTrigger<HTMLLIElement>({
    tooltipText: name,
    tailStyle: "left",
  });

  return (
    <li {...getTargetProps()} className={styles.listItem}>
      <Draggable
        id={id}
        transformOverride={restrictSortableToOriginalPosition}
        disabled={!isDNDEnabled}
        data={{ source: id, folderId }}
        className={styles.draggable}
      >
        <Link
          {...getTriggerProps()}
          tabIndex={0}
          to={`/servers/${id}`}
          className={StyleComposer(styles.navItem, {
            [styles.selected as string]: selectedServerId === id,
            [styles.pillHidden as string]: isPillHidden,
          })}
        >
          <ServerIcon serverName={name} serverIconImage={icon} />
        </Link>
      </Draggable>
      <Mover
        moverId={id}
        sourceId={id}
        isDragging={draggingId === id}
        dragType={dragType}
        folderId={folderId}
        isEnabled={isDNDEnabled}
      />
      <Droppable
        className={StyleComposer(`${styles.droppable} ${styles.combiner}`, {
          [styles.dragging as string]: draggingId === id,
          [styles.notAllowed as string]: dragType === "folder",
        })}
        id={`Combiner(${id})`}
        data={{ source: id, type: "combiner", folderId }}
        disabled={!isDNDEnabled}
      />
    </li>
  );
}

type ServerFolderProps = Omit<ServerListItemProps, "server" | "folderId"> & {
  servers: Server[];
  id: string;
};

export function ServerFolder({
  id,
  servers,
  selectedServerId,
  draggingId,
  dragType,
  isDNDEnabled = true,
}: ServerFolderProps) {
  const [isOpen, setIsOpen] = useState(false);
  const names = servers.map((s) => s.name).reduce((s1, s2) => `${s1} | ${s2}`);
  const { getTargetProps, getTriggerProps } = TooltipTrigger<HTMLLIElement>({
    tooltipText: names,
    tailStyle: "left",
  });

  const folderClass = StyleComposer(`${styles.folder} ${styles.list}`, {
    [styles.open as string]: isOpen,
    [styles.pillHidden as string]: isOpen,
  });

  return (
    <li {...getTargetProps()} className={styles.listItem}>
      <Mover
        moverId={id}
        sourceId={id}
        dragType={dragType}
        isEnabled={isDNDEnabled}
        isDragging={draggingId === id}
      />
      <Droppable
        className={StyleComposer(`${styles.droppable} ${styles.combiner}`, {
          [styles.dragging as string]: draggingId === id,
          [styles.notAllowed as string]: dragType === "folder",
        })}
        id={`Combiner(${id})`}
        data={{ source: "last", type: "combiner", folderId: id }}
        disabled={!isDNDEnabled}
      />
      <Draggable
        id={id}
        data={{ source: id }}
        transformOverride={restrictSortableToOriginalPosition}
        disabled={!isDNDEnabled}
        className={styles.draggable}
      >
        <ul
          {...(!isOpen ? getTriggerProps() : {})}
          className={folderClass}
          onClick={() => {
            if (!isOpen) {
              setIsOpen(true);
            }
          }}
        >
          <div className={styles.wrapper}>
            <li className={styles.listItem}>
              <button
                {...getTriggerProps()}
                className={styles.folderButton}
                onClick={() => setIsOpen(false)}
              >
                <Icon name='folder-fill' />
              </button>
              <ServerIconDisplayGrid servers={servers} />
            </li>
            {servers.map((server, i) => (
              <ServerListItem
                folderId={id}
                selectedServerId={selectedServerId}
                server={server}
                draggingId={draggingId}
                dragType={dragType}
                isPillHidden={!isOpen}
                isDNDEnabled={isOpen}
                key={server.id}
              />
            ))}
          </div>
        </ul>
      </Draggable>
    </li>
  );
}

export function ServerIconDisplayGrid({ servers }: { servers: Server[] }) {
  return (
    <div className={styles.iconDisplay}>
      {servers.map(({ name, icon, id }) => (
        <ServerIcon
          className={styles.icon}
          serverIconImage={icon}
          serverName={name}
          key={`icon-${id}`}
        />
      ))}
    </div>
  );
}
