import { Navigate, Outlet, useLocation } from "react-router-dom";
import ChannelList from "./ChannelList";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";
import {
  useServerSelectionContext,
  getServerIdFromPath,
  type Server,
} from "../../contexts/ServerSelectionContext";

/**
 * Adds a list of channels contained by the currently selected server.
 */
function ServerLayout() {
  const { pathname } = useLocation();
  const { getServer } = useServerSelectionContext();
  const serverId = getServerIdFromPath(pathname);

  if (!serverId) {
    return <Navigate to={"/not-found"} replace />;
  }

  const server = getServer(serverId) as Server;
  const storageKey = `SERVER_PATH(${serverId})`;
  const basePath = `/servers/${serverId}`;
  const defaultPath = `${basePath}/${server.defaultChannelId}`;

  useLastVisitedPath({ storageKey, basePath, defaultPath });

  return (
    <>
      <ChannelList />
      <Outlet />
    </>
  );
}

export default ServerLayout;
