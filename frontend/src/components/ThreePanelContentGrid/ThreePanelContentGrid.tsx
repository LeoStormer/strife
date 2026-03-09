import type { PropsWithChildren, ReactNode } from "react";
import styles from "./ThreePanelContentGrid.module.css";
import StyleComposer from "../../utils/StyleComposer";

export type ThreePanelContentGridProps = PropsWithChildren<{
  isSidePanelOpen?: boolean;
  sidePanel?: ReactNode;
  titlePanel?: ReactNode;
}>;

function ThreePanelContentGrid({
  isSidePanelOpen = false,
  sidePanel,
  titlePanel,
  children,
}: ThreePanelContentGridProps) {
  const containerClass = StyleComposer(styles.container, {
    [styles.isSidebarOpen as string]: isSidePanelOpen,
  });

  return (
    <div className={containerClass}>
      <header className={styles.titlePanel}>{titlePanel}</header>
      <main className={styles.contentPanel}>{children}</main>
      <aside className={`${styles.sidePanel}`}>{sidePanel}</aside>
    </div>
  );
}

export default ThreePanelContentGrid;
