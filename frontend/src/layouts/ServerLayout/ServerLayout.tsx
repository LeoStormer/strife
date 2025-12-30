import { Navigate, Outlet } from "react-router-dom";
import ChannelList from "./ChannelList";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";
import { useServerSelectionContext } from "../../contexts/ServerSelectionContext";

/**
 * Adds a list of channels contained by the currently selected server.
 */
function ServerLayout() {
  const {
    getServer,
    selectedId: serverId,
    isLoading,
  } = useServerSelectionContext();

  if (!serverId && !isLoading) {
    return <Navigate to={"/not-found"} replace />;
  }

  const { defaultChannelId } = serverId ? getServer(serverId)! : {};
  const storageKey = `SERVER_PATH(${serverId})`;
  const basePath = `/servers/${serverId}`;
  const defaultPath = `${basePath}/${defaultChannelId}`;

  useLastVisitedPath({
    storageKey,
    basePath,
    defaultPath,
    isEnabled: !isLoading,
  });

  return (
    <>
      <ChannelList />
      <Outlet />
    </>
  );
}

export default ServerLayout;
