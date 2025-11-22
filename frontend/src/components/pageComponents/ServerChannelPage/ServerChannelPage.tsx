import React, { useContext, useEffect } from "react";
import { PageNameDispatchContext } from "../../../contexts/PageNameContext";
import { useServerSelectionContext } from "../../../contexts/ServerSelectionContext";
import styles from "./ServerChannelPage.module.css"

function ServerChannelPage() {
  const setPageName = useContext(PageNameDispatchContext);
  const { getServer, selectedId } = useServerSelectionContext();

  useEffect(() => {
    const serverName = getServer(selectedId as string)?.name as string;
    setPageName(serverName);
  }, [selectedId, getServer, setPageName]);

  return <div className={styles.container}>ServerChannelPage</div>;
}

export default ServerChannelPage;
