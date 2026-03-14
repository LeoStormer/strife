import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import styles from "./FriendsPage.module.css";
import FriendList from "./FriendList";
import ActivityFeed from "./ActivityFeed";
import FriendFilterBar from "./FriendFilterBar";
import ThreePanelContentGrid from "../../ThreePanelContentGrid";
import useLocalStorage from "../../../contexts/useLocalStorage";
import { useUserContext } from "../../../contexts/UserContext";
import AddFriendTab from "./AddFriendPanel";

const PAGE_NAME = "Friends";
const FILTERS = ["Online", "All", "Pending", "Blocked"];
const storageKey = "FRIENDS_PAGE_ACTIVE_TAB";

function FriendsPage() {
  usePageNameDispatchContext({
    pageName: PAGE_NAME,
    iconProps: { type: "svg", name: "person-greeting" },
  });

  const { user } = useUserContext();
  const [activeTabIndex, setActiveTabIndex] = useLocalStorage({
    storageKey,
    initialValue: 0,
    userId: user!.id,
  });
  const filter = FILTERS[activeTabIndex];

  return (
    <ThreePanelContentGrid
      titlePanel={
        <FriendFilterBar
          filters={FILTERS}
          activeTabIndex={activeTabIndex}
          setActiveTabIndex={setActiveTabIndex}
        />
      }
      sidePanel={<ActivityFeed />}
      isSidePanelOpen
    >
      {filter ? <FriendList activeFilter={filter} /> : <AddFriendTab />}
    </ThreePanelContentGrid>
  );
}

export default FriendsPage;
