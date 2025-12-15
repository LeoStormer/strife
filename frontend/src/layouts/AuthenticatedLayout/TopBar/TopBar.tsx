import { useContext } from "react";
import PageNameContext, {
  type DynamicIconProps,
} from "../../../contexts/PageNameContext";
import styles from "./TopBar.module.css";
import Icon, { type IconProps } from "../../../components/Icon";
import { Link } from "react-router-dom";
import ServerIcon, {
  type ServerIconProps,
} from "../../../components/ServerIcon";

const DynamicIcon = ({ type, ...props }: DynamicIconProps) => {
  if (type === "svg") {
    const iconProps = props as IconProps;
    return <Icon {...iconProps} />;
  }

  if (type === "serverIcon") {
    const iconProps = props as ServerIconProps;
    return <ServerIcon {...iconProps} />;
  }

  return null;
};

function TopBar() {
  const { iconProps, pageName } = useContext(PageNameContext);
  return (
    <header className={styles.container}>
      {iconProps ? (
        <DynamicIcon {...iconProps} className={styles.pageIcon} />
      ) : null}
      <p className={styles.pageTitle}>{pageName}</p>
      <div className={styles.iconButtonContainer}>
        <button className={styles.iconButton}>
          <Icon name='inbox-fill' />
        </button>
        <Link to='#' className={styles.iconButton}>
          <Icon name='question-circle-fill' />
        </Link>
      </div>
    </header>
  );
}

export default TopBar;
