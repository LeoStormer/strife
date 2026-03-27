import React from "react";
import styles from "./ActivityFeed.module.css";

function ActivityFeed() {
  return (
    <div className={styles.container}>
      <h2 id='side-panel-title'>Active Now</h2>
      <div className={styles.emptyCard}>
        <h3>It's quiet for now...</h3>
        <p>
          When a friend starts an activity—like playing a game or hanging out on
          voice—we'll show it here!
        </p>
      </div>
    </div>
  );
}

export default ActivityFeed;
