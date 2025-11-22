import { Navigate, Outlet, useLocation } from "react-router-dom";
import ChannelList from "./ChannelList";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";
import { useServerSelectionContext } from "../../contexts/ServerSelectionContext";

/**
 * Adds a list of channels contained by the currently selected server.
 */
function ServerLayout() {
  const location = useLocation();
  const { getServer } = useServerSelectionContext();
  const regex = /(?<=servers\/)([0-9a-fA-F]+)(?=\/|$)/;
  const match = location.pathname.match(regex); // guaranteed to match something otherwise we wouldn't be on this path
  const serverId = match?.at(0) as string;
  const server = getServer(serverId);
  
  if (!server) {
    return <Navigate to={"/not-found"} replace />;
  }
  
  const storageKey = `SERVER_PATH(${serverId})`;
  const basePath = `/servers/${serverId}`;
  const defaultPath = `${basePath}/${server.defaultChannelId}`; // will be decided by server data's default channel

  useLastVisitedPath({ storageKey, basePath, defaultPath });

  return (
    <>
      <ChannelList />
      <Outlet />
    </>
  );
}

export default ServerLayout;
