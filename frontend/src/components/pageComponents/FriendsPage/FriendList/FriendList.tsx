import { useEffect, useMemo, useState } from "react";
import styles from "./FriendList.module.css";
import Icon from "../../../Icon";
import { Virtuoso } from "react-virtuoso";
import { useUserCacheContext } from "../../../../contexts/UserCacheContext";
import ProfilePicture from "../../../ProfilePicture";
import { type User } from "../../../../contexts/UserContext";
import api from "../../../../api";

export type Relationship =
  | { type: "FRIEND"; user: User }
  | { type: "BLOCKED"; user: User }
  | { type: "PENDING"; user: User; requestId: string }
  | { type: "PENDING_OTHER"; user: User; requestId: string };

type CardProps = {
  relationship: Relationship;
  acceptFriendRequest?: AsyncVoidFunction | undefined;
  rejectFriendRequest?: AsyncVoidFunction | undefined;
  unblockUser?: AsyncVoidFunction | undefined;
  startConversation?: AsyncVoidFunction | undefined;
};

type AsyncVoidFunction = () => Promise<void>;

const PendingActions = ({
  relationship,
  acceptFriendRequest = () => Promise.reject(),
  rejectFriendRequest = () => Promise.reject(),
}: Pick<
  CardProps,
  "acceptFriendRequest" | "rejectFriendRequest" | "relationship"
>) => {
  return (
    <div className={styles.cardActionWrapper}>
      <button
        className={styles.cardAction}
        onClick={acceptFriendRequest}
        disabled={relationship.type !== "PENDING"}
      >
        <Icon name='check-lg' />
      </button>
      <button className={styles.cardAction} onClick={rejectFriendRequest}>
        <Icon name='x-lg' />
      </button>
    </div>
  );
};

const BlockedActions = ({
  unblockUser = () => Promise.reject(),
}: Pick<CardProps, "unblockUser">) => {
  return (
    <div className={styles.cardActionWrapper}>
      <button className={styles.cardAction} onClick={unblockUser}>
        <Icon name='person-x-fill' />
      </button>
    </div>
  );
};

const FriendActions = ({
  startConversation = () => Promise.reject(),
}: Pick<CardProps, "startConversation">) => {
  return (
    <div className={styles.cardActionWrapper}>
      <button className={styles.cardAction} onClick={startConversation}>
        <Icon name='chat-fill' />
      </button>
      <button className={styles.cardAction} disabled>
        <Icon name='kebab-menu' />
      </button>
    </div>
  );
};

const ACTION_MAP = {
  PENDING: PendingActions,
  PENDING_OTHER: PendingActions,
  BLOCKED: BlockedActions,
  FRIEND: FriendActions,
};

const UserDetails = ({ user }: { user: User }) => {
  const { getUser } = useUserCacheContext();

  return (
    <div className={styles.userDetails}>
      <ProfilePicture
        profilePic={user?.profilePic}
        className={styles.profilePicture}
      />
      <div className={styles.username}>{user?.username}</div>
      <span className={styles.status}>Idle</span>
    </div>
  );
};

const RelationshipCard = ({ relationship, ...cardActions }: CardProps) => {
  const Actions = ACTION_MAP[relationship.type];

  return (
    <div className={styles.relationshipCard}>
      <UserDetails user={relationship.user} />
      <Actions relationship={relationship} {...cardActions} />
    </div>
  );
};

type Props = {
  activeFilter: string;
  relationships: Relationship[];
  onListActionSuccess: () => Promise<void>;
};

function FriendList({
  relationships,
  activeFilter,
  onListActionSuccess,
}: Props) {
  const [search, setSearch] = useState<string>("");
  // use a debounced value of search then quary the backend with that

  const acceptFriendRequest = async (requestId: string) => {
    await api.put(`/user/friends/friend-request?requestId=${requestId}`);
    onListActionSuccess();
  };
  const rejectFriendRequest = async (requestId: string) => {
    await api.delete(`/user/friends/friend-request?requestId=${requestId}`);
    onListActionSuccess();
  };
  const unblockUser = async (userId: string) => {
    await api.delete(`/user/unblock-user?receiverId=${userId}`);
    onListActionSuccess();
  };
  const startConversation = async (userId: string) => {
    api.post("/conversation", [userId]);
  };

    // Move towards querying the backend instead
  const filteredRelationships = useMemo(() => {
    let result = relationships.filter((rel) => {
      switch (activeFilter) {
        case "Online":
          return rel.type === "FRIEND"; // && rel.user.status === "online";
        case "All":
          return rel.type === "FRIEND";
        case "Pending":
          return rel.type === "PENDING" || rel.type === "PENDING_OTHER";
        case "Blocked":
          return rel.type === "BLOCKED";
        default:
          return true;
      }
    });

    const query = search.trim().toLowerCase();
    if (query) {
      result = result.filter((rel) =>
        rel.user.username.toLowerCase().includes(query),
      );
    }

    return result;
  }, [relationships, activeFilter, search]);

  return (
    <div
      role='tabpanel'
      id={`${activeFilter} Friends List`}
      className={styles.container}
    >
      <div className={styles.search}>
        <Icon name='search' />
        <input
          type='text'
          value={search}
          placeholder='Search'
          onChange={(e) => {
            setSearch(e.currentTarget.value.trim());
          }}
        />
      </div>
      <h3>{`${activeFilter} — ${relationships.length}`}</h3>
      <Virtuoso
        style={{ flexGrow: "1" }}
        data={filteredRelationships}
        itemContent={(_, data) => (
          <RelationshipCard
            relationship={data}
            acceptFriendRequest={
              data.type === "PENDING"
                ? () => acceptFriendRequest(data.requestId)
                : undefined
            }
            rejectFriendRequest={
              data.type === "PENDING" || data.type === "PENDING_OTHER"
                ? () => rejectFriendRequest(data.requestId)
                : undefined
            }
            unblockUser={() => unblockUser(data.user.id)}
            startConversation={() => startConversation(data.user.id)}
          />
        )}
      />
    </div>
  );
}

export default FriendList;
