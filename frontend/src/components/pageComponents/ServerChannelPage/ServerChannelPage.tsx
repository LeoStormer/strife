import React from "react";
import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import {
  useServerSelectionContext,
  type Server,
} from "../../../contexts/ServerSelectionContext";
import styles from "./ServerChannelPage.module.css";
import sharedStyles from "../../../styles/ChannelViewer.module.css";

function ChannelTitleBar() {
  return (
    <div className={sharedStyles.channelTitleBarContainer}>ChannelTitleBar</div>
  );
}

function ServerChannelPage() {
  const { getServer, selectedId } = useServerSelectionContext();
  const { name, icon } = getServer(selectedId as string) as Server;

  usePageNameDispatchContext({
    pageName: name,
    iconProps: { type: "serverIcon", serverName: name, serverIconImage: icon },
  });

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
