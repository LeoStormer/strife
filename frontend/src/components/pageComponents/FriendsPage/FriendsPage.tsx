import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import styles from "./FriendsPage.module.css";
import FriendList from "./FriendList";
import ActivityFeed from "./ActivityFeed";
import FriendFilterBar from "./FriendFilterBar";
import ThreePanelContentGrid from "../../ThreePanelContentGrid";

const PAGE_NAME = "Friends";

function FriendsPage() {
  usePageNameDispatchContext({
    pageName: PAGE_NAME,
    iconProps: { type: "svg", name: "person-greeting" },
  });

  return (
    <ThreePanelContentGrid
      titlePanel={<FriendFilterBar />}
      sidePanel={<ActivityFeed />}
      isSidePanelOpen
    >
      <FriendList />
    </ThreePanelContentGrid>
  );
}

export default FriendsPage;
