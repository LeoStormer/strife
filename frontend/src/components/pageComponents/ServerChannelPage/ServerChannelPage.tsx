import React from "react";
import { usePageNameDispatchContext } from "../../../contexts/PageNameContext";
import { useServerSelectionContext } from "../../../contexts/ServerSelectionContext";
import styles from "./ServerChannelPage.module.css";
import ChannelViewer from "../../ChannelViewer";

const TitleBar = () => {
  return <div>channel-name</div>;
};

const ServerMembersPanel = () => {
  return <div className={styles.serverMembersContainer}>Server Members</div>;
};

function ServerChannelPage() {
  const { getServer, selectedId } = useServerSelectionContext();
  const { name, icon } = selectedId ? getServer(selectedId)! : { name: "" };

  usePageNameDispatchContext({
    pageName: name,
    iconProps: { type: "serverIcon", serverName: name, serverIconImage: icon },
  });

  const sendMessage = async (message: string) => {};

  return (
    <ChannelViewer
      titlePanel={<TitleBar />}
      sidePanel={<ServerMembersPanel />}
      sendMessage={sendMessage}
      channelName={`#channel-name`}
      messages={[]}
    />
  );
}

export default ServerChannelPage;
