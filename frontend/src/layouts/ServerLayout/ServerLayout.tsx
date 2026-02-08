import { Navigate, Outlet } from "react-router-dom";
import ChannelList from "./ChannelList";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";
import { useServerSelectionContext } from "../../contexts/ServerSelectionContext";
import api from "../../api";
import { useCallback } from "react";

/**
 * Adds a list of channels contained by the currently selected server.
 */
function ServerLayout() {
  const { selectedId: serverId, isLoading } = useServerSelectionContext();
  const storageKey = `SERVER_PATH(${serverId})`;
  const basePath = `/servers/${serverId}`;
  const defaultPath = useCallback(
    async (signal: AbortSignal) => {
      const { data } = await api.get(`/api/server/${serverId}/defaultChannel`, {
        signal,
      });
      return `${basePath}/${data.id}`;
    },
    [serverId, basePath],
  );

  useLastVisitedPath({
    storageKey,
    basePath,
    defaultPath,
    isEnabled: !isLoading && serverId != null,
  });

  if (!isLoading && !serverId) {
    return <Navigate to={"/not-found"} replace />;
  }

  return (
    <>
      <ChannelList />
      <Outlet />
    </>
  );
}

export default ServerLayout;
