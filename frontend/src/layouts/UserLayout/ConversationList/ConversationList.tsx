import { Suspense, use, useState, type MouseEventHandler } from "react";
import styles from "./ConversationList.module.css";
import panelStyles from "../../../styles/SelectionPanel.module.css";
import { Link, type LinkProps } from "react-router-dom";
import { FRIENDS_PAGE_PATH } from "../../../constants";
import type { IconName } from "../../../../types/name";
import Icon from "../../../components/Icon";
import TooltipTrigger from "../../../components/TooltipTrigger";
import {
  useUserContext,
  useUserList,
  type User,
} from "../../../contexts/UserContext";
import {
  useConversations,
  type Conversation,
} from "../../../contexts/useConversations";
import Skeleton from "react-loading-skeleton";
import ProfilePicture from "../../../components/ProfilePicture";
import { ErrorBoundary } from "react-error-boundary";

type CategoryLinkProps = {
  icon?: IconName;
  category: string;
  to: LinkProps["to"];
};

const CategoryLink = ({ icon, category, to }: CategoryLinkProps) => {
  return (
    <Link className={styles.button} to={to}>
      {icon ? <Icon name={icon} /> : null}
      {category}
    </Link>
  );
};

const ConversationsHeader = ({
  startConversation,
}: {
  startConversation: (userIds: string[]) => void;
}) => {
  const { getAllProps } = TooltipTrigger<HTMLButtonElement>({
    tooltipText: "Create DM",
    tailStyle: "down",
  });
  // TODO: Implement create group conversation flow

  return (
    <div className={styles.conversationHeader}>
      <h2>Direct Messages</h2>
      <button {...getAllProps()} className={styles.createConversationButton}>
        <Icon name='plus-lg' />
      </button>
    </div>
  );
};

const ProfileGridContent = ({ users }: { users: User[] }) => {
  return (
    <div className={styles.profileDisplayGrid}>
      {users.map((user) => {
        return (
          <ProfilePicture
            key={user.id}
            profilePic={user.profilePic}
            alt={user.username}
          />
        );
      })}
    </div>
  );
};

type ConversationLinkProps = {
  conversation: Conversation;
  closeConversation: MouseEventHandler;
};

const LeaveConversationButton = ({
  closeConversation,
}: Pick<ConversationLinkProps, "closeConversation">) => {
  return (
    <button
      className={styles.leaveConversationButton}
      onClick={closeConversation}
    >
      <Icon name='x-lg' />
    </button>
  );
};

const ConversationLinkError = ({
  closeConversation,
}: Pick<ConversationLinkProps, "closeConversation">) => {
  return (
    <Link
      to={"#"}
      className={`${styles.button} ${styles.conversationLink} ${styles.error}`}
    >
      <div className={styles.profileDisplayGrid}>
        <Icon name='error-circle' className={styles.error} />
      </div>
      <div className={styles.error}>Failed to load conversation</div>
      <LeaveConversationButton closeConversation={closeConversation} />
    </Link>
  );
};

const ConversationLink = ({
  conversation,
  closeConversation,
}: ConversationLinkProps) => {
  const { user } = useUserContext();
  const otherIds = conversation.userIds.filter((id) => id !== user?.id);
  const truncatedOtherIds = otherIds.slice(0, 4);
  const isGroupDM = truncatedOtherIds.length > 1;
  const otherUsers = useUserList(truncatedOtherIds);
  const conversationName = otherUsers
    .map((result) => result.user.username)
    .join(", ");

  // throw new Error("Test error boundary");
  // TODO: Create right click menu
  return (
    <Link
      className={`${styles.button} ${styles.conversationLink}`}
      to={`/servers/@me/${conversation.id}`}
    >
      <ProfileGridContent users={otherUsers.map((result) => result.user)} />
      <div className={styles.conversationDetails}>
        <span>{conversationName}</span>
        {isGroupDM && (
          <span
            className={styles.status}
          >{`${otherUsers.length} Members`}</span>
        )}
      </div>
      <LeaveConversationButton closeConversation={closeConversation} />
    </Link>
  );
};

function ConversationList() {
  const { conversations, startConversation, leaveConversation } =
    useConversations();

  return (
    <nav aria-label='Conversations' className={panelStyles.container}>
      <button className={styles.button}>Find or start a conversation</button>
      <div className={panelStyles.scrollingContainer}>
        <CategoryLink
          icon='person-greeting'
          category='Friends'
          to={FRIENDS_PAGE_PATH}
        />
        <CategoryLink category='Turbo' to='#' />
        <CategoryLink category='Shop' to='#' />
        <CategoryLink category='Quests' to='#' />
        <div className={styles.separator} />
        <ConversationsHeader startConversation={startConversation} />
        {conversations.map((conversation) => {
          const closeConversation: MouseEventHandler = (e) => {
            e.preventDefault();
            // TODO: If the user is currently viewing this conversation, navigate them to a different page
            return leaveConversation(conversation.id);
          };

          return (
            <ErrorBoundary
              key={conversation.id}
              fallback={<ConversationLinkError closeConversation={closeConversation} />}
            >
              <Suspense
                fallback={
                  <Skeleton
                    containerClassName={styles.linkSkeleton as string}
                  />
                }
              >
                <ConversationLink
                  key={conversation.id}
                  conversation={conversation}
                  closeConversation={closeConversation}
                />
              </Suspense>
            </ErrorBoundary>
          );
        })}
      </div>
    </nav>
  );
}

export default ConversationList;
