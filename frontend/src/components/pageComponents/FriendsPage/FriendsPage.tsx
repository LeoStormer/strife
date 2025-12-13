import React from "react";
import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import styles from "./FriendsPage.module.css";
import FriendList from "./FriendList";
import ActivityFeed from "./ActivityFeed";
import FriendFilterBar from "./FriendFilterBar";

const PAGE_NAME = "Friends";

function FriendsPage() {
  usePageNameDispatchContext({ pageName: PAGE_NAME, icon: "person-greeting" });

  return (
    <div className={styles.container}>
      <FriendFilterBar />
      <FriendList />
      <ActivityFeed />
    </div>
  );
}

export default FriendsPage;
