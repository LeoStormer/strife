import React from "react";
import styles from "./ConversationPage.module.css";
import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import ChannelViewer from "../../ChannelViewer";

const PAGE_NAME = "Direct Messages";

const TitleBar = () => {
  return <div>UserName</div>;
};

const UserProfilePanel = () => {
  return <div className={styles.profileContainer}>User Profile</div>;
};

function ConversationPage() {
  usePageNameDispatchContext({ pageName: PAGE_NAME });

  const sendMessage = async (message: string) => {};

  return (
    <ChannelViewer
      titlePanel={<TitleBar />}
      sidePanel={<UserProfilePanel />}
      sendMessage={sendMessage}
      channelName='@Username'
      messages={[]}
    />
  );
}

export default ConversationPage;
