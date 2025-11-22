import React, { useContext, useEffect } from "react";
import { PageNameDispatchContext } from "../../../contexts/PageNameContext";
import { useServerSelectionContext } from "../../../contexts/ServerSelectionContext";
import styles from "./ServerChannelPage.module.css";
import sharedStyles from "../../../styles/ChannelViewer.module.css";

function ChannelTitleBar() {
  return (
    <div className={sharedStyles.channelTitleBarContainer}>ChannelTitleBar</div>
  );
}

function ServerChannelPage() {
  const setPageName = useContext(PageNameDispatchContext);
  const { getServer, selectedId } = useServerSelectionContext();

  useEffect(() => {
    const serverName = getServer(selectedId as string)?.name as string;
    setPageName(serverName);
  }, [selectedId, getServer, setPageName]);

  return (
    <div className={sharedStyles.container}>
      <ChannelTitleBar />
      <div className={sharedStyles.flexContainer}>
        <div className={sharedStyles.messageViewAreaContainer}>
          Message View Area
          <div className={sharedStyles.messageInputContainer}>
            Message #channel-name
          </div>
        </div>
        <div className={styles.serverMembersContainer}>Server Members</div>
      </div>
    </div>
  );
}

export default ServerChannelPage;
