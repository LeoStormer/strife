import {
  useState,
  type ComponentPropsWithoutRef,
  type ElementType,
} from "react";
import { Link, useLocation } from "react-router-dom";
import { useServerSelectionContext } from "../../../contexts/ServerSelectionContext";
import Icon from "../../../components/Icon";
import TooltipTrigger from "../../../components/TooltipTrigger";
import styles from "./ServerBar.module.css";
import AddServerModal from "./AddServerModal";
import StyleComposer from "../../../utils/StyleComposer";
import { DISCOVERY_LAYOUT_PATH, USER_LAYOUT_PATH } from "../../../constants";
import Skeleton from "react-loading-skeleton";
import "react-loading-skeleton/dist/skeleton.css";
import ServerSortableArea from "./ServerSortableArea";
import type { IconName } from "../../../../types/name";

const NavItemSkeleton = () => {
  return (
    <li className={styles.listItem}>
      <Skeleton width='40px' height='40px' />
    </li>
  );
};

function LoadingServerBar() {
  return (
    <nav className={styles.navContainer}>
      <ul className={styles.list}>
        <NavItemSkeleton />
        <div className={styles.separator}></div>
        {Array(5)
          .fill(0)
          .map((_, index) => (
            <NavItemSkeleton key={index} />
          ))}
        <NavItemSkeleton />
        <NavItemSkeleton />
      </ul>
    </nav>
  );
}

type NavItemProps<T extends ElementType> = {
  tooltipText: string;
  isSelected: boolean;
  iconName: IconName;
  as?: T;
} & ComponentPropsWithoutRef<T>;

const NavItem = <T extends ElementType = "button">({
  as,
  isSelected,
  tooltipText,
  iconName,
  ...props
}: NavItemProps<T>) => {
  const Component = as || "button";
  const { getTargetProps, getTriggerProps } = TooltipTrigger<HTMLLIElement>({
    tooltipText,
    tailStyle: "left",
  });

  return (
    <li {...getTargetProps()} className={styles.listItem}>
      <Component
        {...getTriggerProps()}
        className={StyleComposer(styles.navItem, {
          [styles.selected as string]: isSelected,
        })}
        {...props}
      >
        <Icon name={iconName} />
      </Component>
    </li>
  );
};

/**
 * A sidebar with a button to navigate to user Layout path, a button that
 * navigates to the server discovery page, a sortable list of icon buttons
 * representing the selection of servers a user has joined that navigate to
 * that server's page when clicked.
 */
function ServerBar() {
  const { isLoading } = useServerSelectionContext();

  if (isLoading) {
    return <LoadingServerBar />;
  }

  const [isAddServerSelected, setIsAddServerSelected] = useState(false);
  const location = useLocation();
  const isDirectMessagesSelected = location.pathname.includes(USER_LAYOUT_PATH);
  const isDiscoverySelected = location.pathname.includes(DISCOVERY_LAYOUT_PATH);

  return (
    <nav className={styles.navContainer}>
      <ul className={styles.list}>
        <NavItem
          as={Link}
          tooltipText='Direct Messages'
          isSelected={isDirectMessagesSelected}
          iconName='person-circle'
          to={USER_LAYOUT_PATH}
        />
        <div className={styles.separator}></div>
        <ServerSortableArea />
        <NavItem
          tooltipText='Add a Server'
          isSelected={isAddServerSelected}
          iconName='plus-lg'
          onClick={() => setIsAddServerSelected(true)}
        />
        <NavItem
          as={Link}
          tooltipText='Discover'
          isSelected={isDiscoverySelected}
          iconName='compass'
          to={DISCOVERY_LAYOUT_PATH}
        />
      </ul>
      {isAddServerSelected ? (
        <AddServerModal deselectButton={() => setIsAddServerSelected(false)} />
      ) : null}
    </nav>
  );
}

export default ServerBar;
