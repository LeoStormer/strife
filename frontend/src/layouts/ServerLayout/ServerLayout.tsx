import { Navigate, Outlet } from "react-router-dom";
import ChannelList from "./ChannelList";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";
import {
  useServerSelectionContext,
  type Server,
} from "../../contexts/ServerSelectionContext";

/**
 * Adds a list of channels contained by the currently selected server.
 */
function ServerLayout() {
  const { getServer, selectedId: serverId, isLoading } = useServerSelectionContext();

  // TODO: implement loading state
  if (isLoading) {

  }
  
  if (!serverId) {
    return <Navigate to={"/not-found"} replace />;
  }

  const { defaultChannelId } = getServer(serverId) as Server;
  const storageKey = `SERVER_PATH(${serverId})`;
  const basePath = `/servers/${serverId}`;
  const defaultPath = `${basePath}/${defaultChannelId}`;

  useLastVisitedPath({ storageKey, basePath, defaultPath });

  return (
    <>
      <ChannelList />
      <Outlet />
    </>
  );
}

export default ServerLayout;
