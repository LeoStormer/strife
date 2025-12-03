import { useState, type UIEventHandler } from "react";
import styles from "../../../styles/DiscoveryPage.module.css";
import { hexToRgba } from "../../../utils/hexToRgba";

function ServerDiscoveryPage() {
  const [stickyHeaderAlpha, setStickyHeaderAlpha] = useState(0);
  const rootStyles = window.getComputedStyle(document.documentElement);
  const primaryColor = rootStyles.getPropertyValue("--background-color").trim();
  const stickyHeaderBackgroundColor = hexToRgba(
    primaryColor,
    stickyHeaderAlpha
  );

  const handleScroll: UIEventHandler = (event) => {
    const newAlpha = Math.min(event.currentTarget.scrollTop / 48, 1);
    setStickyHeaderAlpha(newAlpha);
  };

  return (
    <div className={styles.container}>
      <div
        style={{ backgroundColor: stickyHeaderBackgroundColor }}
        className={styles.stickyHeaderContainer}
      >
        SearchBar
      </div>
      <div className={styles.serverViewer} onScroll={handleScroll}>
        <div className={styles.serverViewerHeaderContainer}>
          Find Your Community on Strife
        </div>
        <h2 className={styles.serverGridHeader}>Featured Servers</h2>
        <div className={styles.serverGrid}>
          {Array.from({ length: 12 }, (_, index) => (
            <div className={styles.gridItem}>{index}</div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default ServerDiscoveryPage;
