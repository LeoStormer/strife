import { useState } from "react";
import styles from "./ConversationList.module.css";
import panelStyles from "../../../styles/SelectionPanel.module.css";
import { Link, type LinkProps } from "react-router-dom";
import { FRIENDS_PAGE_PATH } from "../../../constants";
import type { IconName } from "../../../../types/name";
import Icon from "../../../components/Icon";
import TooltipTrigger from "../../../components/TooltipTrigger";
import { useUserContext } from "../../../contexts/UserContext";
import ProfileDisplayGrid from "./ProfileDisplayGrid";

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

const ConversationsHeader = () => {
  const { getAllProps } = TooltipTrigger<HTMLButtonElement>({
    tooltipText: "Create DM",
    tailStyle: "down",
  });

  return (
    <div className={styles.conversationHeader}>
      <h2>Direct Messages</h2>
      <button {...getAllProps()} className={styles.createConversationButton}>
        <Icon name='plus-lg' />
      </button>
    </div>
  );
};

type Conversation = {
  id: string;
  userIds: string[];
  locked: boolean;
};

const ConversationLink = ({ conversation }: { conversation: Conversation }) => {
  const { user } = useUserContext();
  const otherIds = conversation.userIds.filter((id) => id !== user?.id);

  return (
    <Link
      className={`${styles.button} ${styles.conversationLink}`}
      to={`/servers/@me/${conversation.id}`}
    >
      <ProfileDisplayGrid userIds={otherIds} />
      <div></div>
    </Link>
  );
};

function ConversationList() {
  const [conversations, setConversations] = useState<Conversation[]>([
    {
      id: "test",
      userIds: ["68ef270586d79b221c542507", "u1"],
      locked: false,
    },
  ]);

  return (
    <nav aria-label="Conversations" className={panelStyles.container}>
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
        <ConversationsHeader />
        {conversations.map((conversation) => {
          return (
            <ConversationLink
              key={conversation.id}
              conversation={conversation}
            />
          );
        })}
      </div>
    </nav>
  );
}

export default ConversationList;
