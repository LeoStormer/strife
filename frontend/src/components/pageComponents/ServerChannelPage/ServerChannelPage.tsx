import React from "react";
import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import { useServerSelectionContext } from "../../../contexts/ServerSelectionContext";
import styles from "./ServerChannelPage.module.css";
import sharedStyles from "../../../styles/ChannelViewer.module.css";

function ChannelTitleBar() {
  return (
    <div className={sharedStyles.channelTitleBarContainer}>ChannelTitleBar</div>
  );
}

function ServerChannelPage() {
  const { getServer, selectedId } = useServerSelectionContext();
  const pageName = getServer(selectedId as string)?.name as string;
  usePageNameDispatchContext({ pageName });

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
