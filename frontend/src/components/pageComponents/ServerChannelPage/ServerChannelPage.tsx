import React, { useContext, useEffect } from "react";
import { PageNameDispatchContext } from "../../../contexts/PageNameContext";
import { useServerSelectionContext } from "../../../contexts/ServerSelectionContext";
import styles from "./ServerChannelPage.module.css";

function ChannelTitleBar() {
  return <div className={styles.channelTitleBarContainer}>ChannelTitleBar</div>;
}

function ServerChannelPage() {
  const setPageName = useContext(PageNameDispatchContext);
  const { getServer, selectedId } = useServerSelectionContext();

  useEffect(() => {
    const serverName = getServer(selectedId as string)?.name as string;
    setPageName(serverName);
  }, [selectedId, getServer, setPageName]);

  return (
    <div className={styles.container}>
      <ChannelTitleBar />
      <div className={styles.flexContainer}>
        <div className={styles.messageViewAreaContainer}>
          Message View Area
          <div className={styles.messageInputContainer}>
            Message #channel-name
          </div>
        </div>
        <div className={styles.serverMembersContainer}>Server Members</div>
      </div>
    </div>
  );
}

export default ServerChannelPage;
