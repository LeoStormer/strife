import { useContext } from "react";
import PageNameContext, {
  type DynamicIconProps,
} from "../../../contexts/PageNameContext";
import styles from "./TopBar.module.css";
import Icon from "../../../components/Icon";
import { Link } from "react-router-dom";
import ServerIcon from "../../../components/ServerIcon";

const DynamicIcon = (props: DynamicIconProps) => {
  if (props.type === "svg") {
    const {type, ...iconProps} = props;
    return <Icon {...iconProps} />;
  }

  if (props.type === "serverIcon") {
    const {type, ...iconProps} = props;
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
