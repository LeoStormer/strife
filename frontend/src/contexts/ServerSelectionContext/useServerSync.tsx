import { useEffect, useState } from "react";
import { useWebsocketContext } from "../WebsocketContext";
import type {
  Folder,
  Server,
  ServerItem,
  ServerItemOrFolderRecord,
} from "./serverReducer";
import { FOLDER_STORAGE_KEY, ROOT_ORDER_KEY } from "../../constants";
import api from "../../api";
import type { ServerManager } from "./useServerManager";

const getServersFromApi = async (): Promise<Server[]> => {
  return api.get("/user/servers").then((response) => response.data);
};

const getLocalFolders = (): Folder[] => {
  try {
    const storedData = localStorage.getItem(FOLDER_STORAGE_KEY);

    if (!storedData) {
      return [];
    }
    return JSON.parse(storedData);
  } catch (error) {
    return [];
  }
};

const getRootOrder = (): string[] => {
  try {
    const storedData = localStorage.getItem(ROOT_ORDER_KEY);

    if (!storedData) {
      return [];
    }
    return JSON.parse(storedData);
  } catch (error) {
    return [];
  }
};

const getUserJoinedServers = (
  serversFromApi: Server[],
): ServerItemOrFolderRecord => {
  const serverSet = new Set<string>();
  const serverItems: Record<string, ServerItem | Folder> = {};

  for (const server of serversFromApi) {
    serverItems[server.id] = { ...server, type: "server" };
    serverSet.add(server.id);
  }

  for (const folder of getLocalFolders()) {
    const filteredServerOrder = folder.serverOrder.filter((id) =>
      serverSet.has(id),
    );

    if (filteredServerOrder.length === 0) {
      continue;
    }

    serverItems[folder.id] = { ...folder, serverOrder: filteredServerOrder };
    filteredServerOrder.forEach((serverId) => {
      const serverItem = serverItems[serverId] as ServerItem;
      serverItems[serverId] = { ...serverItem, folderId: folder.id };
    });
  }

  return serverItems;
};

const reconcileRootOrder = (
  servers: ServerItemOrFolderRecord,
  rootOrder: string[],
) => {
  const cleanedRootOrder = rootOrder.filter((id) => servers[id] != undefined);
  const alreadyOrdered = new Set(cleanedRootOrder);
  const serversInFolders = new Set(
    Object.values(servers)
      .filter((item) => item.type === "folder")
      .flatMap((item) => item.serverOrder),
  );
  const newServers = Object.keys(servers).filter(
    (id) => !(alreadyOrdered.has(id) || serversInFolders.has(id)),
  );
  return [...cleanedRootOrder, ...newServers];
};

type Props = Pick<
  ServerManager,
  "overwriteState" | "addServer" | "removeServer"
>;

type UpdateMessage =
  | { type: "SERVER_ADDED"; server: Server }
  | { type: "SERVER_REMOVED"; serverId: string };

function useServerSync({ overwriteState, addServer, removeServer }: Props) {
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(false);
  const { subscribe, unsubscribe, isConnected } = useWebsocketContext();

  // sync on mount and whenever connection is re-established after a disconnect
  useEffect(() => {
    if (!isConnected) {
      return;
    }

    getServersFromApi()
      .then((serversFromApi) => {
        const userServers = getUserJoinedServers(serversFromApi);
        const storedRootOrder = getRootOrder();
        const reconciledRootOrder = reconcileRootOrder(
          userServers,
          storedRootOrder,
        );
        overwriteState(userServers, reconciledRootOrder);
        setIsLoading(false);
        setError(false);
      })
      .catch(() => {
        setError(true);
      });
  }, [isConnected, overwriteState, setIsLoading, setError]);

  // subscribe to server updates
  useEffect(() => {
    const destination = "/queue/server-updates";
    subscribe(destination, (message) => {
      const update = JSON.parse(message.body) as UpdateMessage;
      if (update.type === "SERVER_ADDED") {
        addServer(update.server);
      } else if (update.type === "SERVER_REMOVED") {
        removeServer(update.serverId);
      }
    });

    return () => unsubscribe(destination);
  }, [subscribe, unsubscribe]);

  return { isLoading, error };
}

export default useServerSync;
