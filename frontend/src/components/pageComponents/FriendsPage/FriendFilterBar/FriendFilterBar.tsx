import React from "react";
import styles from "./FriendFilterBar.module.css";
import Icon from "../../../Icon";
import TooltipTrigger from "../../../TooltipTrigger";
import StyleComposer from "../../../../utils/StyleComposer";

type ButtonProps = {
  tabName: string;
  isSelected: boolean;
  onClick: () => void;
};

const TabButton = ({ tabName, isSelected, onClick }: ButtonProps) => {
  const className = StyleComposer(styles.tabButton, {
    [styles.isSelected as string]: isSelected,
  });

  return (
    <button
      role='tab'
      className={className}
      disabled={isSelected}
      onClick={onClick}
    >
      {tabName}
    </button>
  );
};

type Props = {
  filters: string[];
  activeTabIndex: number;
  setActiveTabIndex: (index: number) => void;
};

const FriendFilterBar = ({
  filters,
  activeTabIndex,
  setActiveTabIndex,
}: Props) => {
  const { getAllProps } = TooltipTrigger<HTMLButtonElement>({
    tooltipText: "New Group DM",
    tailStyle: "up",
  });

  return (
    <div className={styles.container}>
      <Icon name='person-greeting' />
      <h2>Friends</h2>
      <Icon name='dot' style={{ color: "var(--on-background-contrast)" }} />
      <div role='tablist' className={styles.tabButtonGroup}>
        {filters.map((tabName, index) => (
          <TabButton
            key={tabName}
            tabName={tabName}
            isSelected={activeTabIndex === index}
            onClick={() => setActiveTabIndex(index)}
          />
        ))}
        <TabButton
          tabName='Add Friend'
          isSelected={activeTabIndex === filters.length}
          onClick={() => setActiveTabIndex(filters.length)}
        />
      </div>
      <button className={styles.newConversationButton} {...getAllProps()}>
        <Icon name='chat-fill-plus' />
      </button>
    </div>
  );
};

export default FriendFilterBar;
