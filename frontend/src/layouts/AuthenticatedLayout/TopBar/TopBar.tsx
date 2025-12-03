import React, { useContext } from "react";
import PageNameContext from "../../../contexts/PageNameContext";
import styles from "./TopBar.module.css";

function TopBar() {
  const pageName = useContext(PageNameContext);

  return (
    <div className={styles.container}>
      {`Page Name = ${pageName}`}
      <button>Inbox</button>
      <button>Help</button>
    </div>
  );
}

export default TopBar;
