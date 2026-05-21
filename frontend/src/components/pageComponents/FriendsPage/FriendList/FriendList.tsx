import { useState } from "react";
import styles from "./FriendList.module.css";
import Icon from "../../../Icon";
import { Virtuoso } from "react-virtuoso";
import ProfilePicture from "../../../ProfilePicture";
import { type User } from "../../../../contexts/UserContext";
import useDebounce from "../../../../contexts/useDebounce";
import useRelationships, {
  type Relationship,
  type UseRelationshipsReturnType,
} from "../../../../contexts/useRelationships";
import Skeleton from "react-loading-skeleton";
import { useConversations } from "../../../../contexts/useConversations";
import { useNavigate } from "react-router-dom";

type ActionFunction = (...args: any[]) => void | Promise<void>;

type CardProps = {
  relationship: Relationship;
  acceptFriendRequest?: ActionFunction | undefined;
  rejectFriendRequest?: ActionFunction | undefined;
  unblockUser?: ActionFunction | undefined;
  startConversation?: ActionFunction | undefined;
};

const PendingActions = ({
  relationship,
  acceptFriendRequest = () => {},
  rejectFriendRequest = () => {},
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
  unblockUser = () => {},
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

const RelationshipCardSkeleton = () => {
  return (
    <div className={styles.relationshipCard}>
      <div className={styles.userDetails}>
        <Skeleton
          circle
          width={32}
          height={32}
          containerClassName={styles.profilepicture as string}
        />
        <div className={styles.username}>
          <Skeleton width={120} height={16} />
        </div>
        <div className={styles.status}>
          <Skeleton width={24} height={14} />
        </div>
      </div>
      <div className={styles.cardActionWrapper}>
        <Skeleton
          circle
          width={30}
          height={30}
          count={2}
          inline
          style={{ marginLeft: 12 }}
        />
      </div>
    </div>
  );
};

const FriendListLoadingSkeleton = () => {
  return (
    <div className={styles.container}>
      <div>
        <Skeleton height={40} />
      </div>
      <h3>
        <Skeleton width={100} />
      </h3>
      {Array.from({ length: 10 }).map((_, i) => (
        <RelationshipCardSkeleton key={i} />
      ))}
    </div>
  );
};

type Props = {
  activeFilter: string;
};

const EmptyState = ({ search }: { activeFilter: string; search: string }) => {
  const isSearching = search.length > 0;
  return (
    <div className={styles.emptyStateContainer}>
      <h3>No results found</h3>
      {isSearching ? <p>We couldn't find anyone named {search}.</p> : null}
    </div>
  );
};

type ListContentProps = Props &
  Omit<UseRelationshipsReturnType, "isLoading"> & {
    startConversation: ActionFunction;
  };

const ListContent = (props: ListContentProps) => {
  const isSearching = props.isFetching && !props.isFetchingNextPage;
  return (
    <>
      <h3>{`${props.activeFilter} — ${props.total}`}</h3>
      <div
        style={{
          flexGrow: "1",
          opacity: isSearching ? 0.6 : 1,
          transition: "opacity 0.2s ease-in-out",
          pointerEvents: isSearching ? "none" : "auto",
        }}
      >
        <Virtuoso
          style={{ flexGrow: 1 }}
          increaseViewportBy={200}
          data={props.relationships}
          endReached={() => {
            if (!props.isFetching && props.hasNextPage) {
              props.fetchNextPage();
            }
          }}
          components={{
            Footer: () =>
              props.isFetchingNextPage ? <RelationshipCardSkeleton /> : null,
          }}
          itemContent={(_, rel) => (
            <RelationshipCard
              relationship={rel}
              acceptFriendRequest={
                rel.type === "PENDING"
                  ? () => props.acceptFriendRequest(rel.requestId)
                  : undefined
              }
              rejectFriendRequest={
                rel.type === "PENDING" || rel.type === "PENDING_OTHER"
                  ? () => props.rejectFriendRequest(rel.requestId)
                  : undefined
              }
              unblockUser={() => props.unblockUser(rel.user.id)}
              startConversation={() => props.startConversation(rel.user.id)}
            />
          )}
        />
      </div>
    </>
  );
};

function FriendList({ activeFilter }: Props) {
  const [search, setSearch] = useState<string>("");
  const debouncedSearch = useDebounce(search, 300);
  const navigate = useNavigate();

  const { relationships, isLoading, ...listProps } = useRelationships({
    filter: activeFilter,
    search: debouncedSearch,
  });

  const { startConversation } = useConversations();

  if (isLoading && relationships.length == 0) {
    return <FriendListLoadingSkeleton />;
  }

  const isEmpty = !isLoading && relationships.length === 0;

  return (
    <div
      role='tabpanel'
      id={`${activeFilter} Friends List`}
      className={styles.container}
    >
      <search aria-label='Search Friends' className={styles.search}>
        <Icon name='search' />
        <input
          type='text'
          value={search}
          placeholder='Search'
          onChange={(e) => {
            setSearch(e.currentTarget.value.trim());
          }}
        />
      </search>
      {isEmpty ? (
        <EmptyState activeFilter={activeFilter} search={debouncedSearch} />
      ) : (
        <ListContent
          {...listProps}
          activeFilter={activeFilter}
          relationships={relationships}
          startConversation={async (userId: string) => {
            const conversation = await startConversation([userId]);
            navigate(`/servers/@me/${conversation.id}`);
          }}
        />
      )}
    </div>
  );
}

export default FriendList;
