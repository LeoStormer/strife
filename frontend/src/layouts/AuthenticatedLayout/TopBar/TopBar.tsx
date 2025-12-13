import React, { useContext } from "react";
import PageNameContext from "../../../contexts/PageNameContext";
import styles from "./TopBar.module.css";
import Icon from "../../../components/Icon";
import { Link } from "react-router-dom";

function TopBar() {
  const { icon, pageName } = useContext(PageNameContext);

  return (
    <header className={styles.container}>
      {icon ? <Icon className={styles.pageIcon} name={icon} /> : null}
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
