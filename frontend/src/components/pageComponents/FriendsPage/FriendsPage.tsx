import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import styles from "./FriendsPage.module.css";
import FriendList from "./FriendList";
import ActivityFeed from "./ActivityFeed";
import FriendFilterBar from "./FriendFilterBar";
import ThreePanelContentGrid from "../../ThreePanelContentGrid";
import useLocalStorage from "../../../contexts/useLocalStorage";
import { useUserContext } from "../../../contexts/UserContext";
import AddFriendTab from "./AddFriendPanel";
import { useEffect, useState } from "react";
import { type Relationship } from "./FriendList/FriendList";
import api from "../../../api";

const PAGE_NAME = "Friends";
const FILTERS = ["Online", "All", "Pending", "Blocked"];
const storageKey = "FRIENDS_PAGE_ACTIVE_TAB";

//TODO: implement loading state for FriendsList
//TODO: switch to Tanstack Query
/**
 * TODO MIGRATION PLAN: TANSTACK QUERY + SERVER-SIDE FILTERING
 * 
 * STEP 1: INFRASTRUCTURE SETUP
 * - Install: npm install @tanstack/react-query
 * - Provider: Wrap the app in <QueryClientProvider client={queryClient}>.
 * 
 * STEP 2: CREATE CUSTOM HOOK (useRelationships.ts)
 * - useInfiniteQuery: Use ['relationships', activeFilter, debouncedSearch] as the QueryKey.
 * - queryFn: Call GET `/user/relationships?page=${pageParam}&size=20&search=${search}&filter=${filter}`.
 * - getNextPageParam: Logic (lastPage.page + 1 < Math.ceil(lastPage.total / lastPage.size)).
 * - Mutations: Create useMutation for accept, reject, and unblock actions.
 * - Invalidation: Call queryClient.invalidateQueries(['relationships']) in 'onSettled'.
 * 
 * STEP 3: REFACTOR FriendList.tsx (DATA CONSUMPTION)
 * - Search: Implement 'useDebounce(search, 300)' and pass it to the hook.
 * - Filter Removal: Delete the manual 'useMemo' filtering logic; use 'data.pages.flatMap()'.
 * - Pagination: Connect Virtuoso 'endReached' to the hook's 'fetchNextPage' function.
 * - Actions: Replace manual 'api.put/delete' calls with hook's '.mutate()' functions.
 * 
 * STEP 4: REFACTOR FriendsPage.tsx (STATE CLEANUP)
 * - Cleanup: Delete 'relationships' useState, 'loading' useState, 'useEffect', and 'refresh()'.
 * - Integration: Pull 'relationships', 'isLoading', and 'hasNextPage' from the custom hook.
 * - Loading: Implement a Skeleton or Spinner using the hook's 'isLoading' boolean.
 * 
 */
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
  const [relationships, setRelationships] = useState<Relationship[]>([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    const fetchAll = async () => {
      try {
        const response = await api.get("/user/relationships?page=0&size=1000");
        setRelationships(response.data.relationships);
        console.log(response);
        
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, []);

  const refresh = async () => {
    const response = await api.get("/user/relationships?page=0&size=1000");
    setRelationships(response.data.relationships);
  };

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
      {filter ? (
        <FriendList
          relationships={relationships}
          activeFilter={filter}
          onListActionSuccess={refresh}
        />
      ) : (
        <AddFriendTab />
      )}
    </ThreePanelContentGrid>
  );
}

export default FriendsPage;
