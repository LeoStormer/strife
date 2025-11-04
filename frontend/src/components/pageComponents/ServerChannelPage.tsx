import React, { useContext, useEffect } from "react";
import { PageNameDispatchContext } from "../../contexts/PageNameContext";
import { useServerSelectionContext } from "../../contexts/ServerSelectionContext";

function ServerChannelPage() {
  const setPageName = useContext(PageNameDispatchContext);
  const { getServer, selectedId } = useServerSelectionContext();

  useEffect(() => {
    const serverName = selectedId ? getServer(selectedId)?.name : "";
    setPageName(serverName ?? "Server Page");
  }, [selectedId, getServer, setPageName]);

  return <div>ServerChannelPage</div>;
}

export default ServerChannelPage;
