import React from "react";
import styles from "./ConversationPage.module.css";
import sharedStyles from "../../../styles/ChannelViewer.module.css";
import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";

const PAGE_NAME = "Direct Messages";

function ConversationPage() {
  usePageNameDispatchContext({ pageName: PAGE_NAME });

  return (
    <div className={sharedStyles.container}>
      <div className={sharedStyles.channelTitleBarContainer}>UserName</div>
      <div className={sharedStyles.flexContainer}>
        <div className={sharedStyles.messageViewAreaContainer}>
          Message View Area
          <div className={sharedStyles.messageInputContainer}>
            Message #channel-name
          </div>
        </div>
        <div className={styles.profileContainer}>User Profile</div>
      </div>
    </div>
  );
}

export default ConversationPage;
