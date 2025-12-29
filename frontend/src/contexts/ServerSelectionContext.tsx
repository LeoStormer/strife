import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { useLocation } from "react-router-dom";
import api from "../api";

export type Server = {
  id: string;
  name: string;
  defaultChannelId: string;
  icon?: string;
  description?: string;
};

export type ServerItem = Server & {
  type: "server";
  folderId?: string | undefined;
};

export type Folder = {
  id: string;
  serverOrder: string[];
  type: "folder";
};

type MoveItem = {
  (activeId: string, overId: "last", targetFolderId?: string | undefined): void;
  (activeId: string, overId: string, targetFolderId?: string | undefined): void;
};

type ServerSelectionContextType = {
  servers: Record<string, ServerItem | Folder>;
  rootOrder: string[];
  isLoading: boolean;
  selectedId: string | null;
  getServer: (serverId: string) => Server | null;
  moveItem: MoveItem;
  createFolder: (servers: string[], index?: number) => void;
};

export const ServerSelectionContext =
  createContext<ServerSelectionContextType | null>(null);

export const getServerIdFromPath = (path: string) => {
  const regex = /(?<=servers\/)([0-9a-fA-F]+)(?=\/|$)/;
  const match = path.match(regex);
  return match?.at(0);
};

const getServersFromApi = async (): Promise<Server[]> => {
  if (process.env.NODE_ENV === "production") {
    return await api.get("/user/servers");
  }

  //TODO: Remove after implementing
  return [
    { id: "1", name: "Server 1", defaultChannelId: "1" },
    { id: "2", name: "Server 2", defaultChannelId: "1" },
    { id: "3", name: "Server 3", defaultChannelId: "1" },
    { id: "4", name: "Server 4", defaultChannelId: "1" },
    { id: "5", name: "Server 5", defaultChannelId: "1" },
  ];
};

const FOLDER_STORAGE_KEY = "SERVERBAR_FOLDERS";
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

const saveFoldersToLocalStorage = (folders: Folder[]) => {
  try {
    localStorage.setItem(FOLDER_STORAGE_KEY, JSON.stringify(folders));
  } catch (error) {
    console.log(`Failed to save folders to localStorage: ${error}`);
  }
};

const ROOT_ORDER_KEY = "SERVERBAR_ORDER";
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

const saveRootOrderToLocalStorage = (rootOrder: string[]) => {
  try {
    localStorage.setItem(ROOT_ORDER_KEY, JSON.stringify(rootOrder));
  } catch (error) {
    console.log(`Failed to save root order to localStorage: ${error}`);
  }
};

const getUserJoinedServers = (
  serversFromApi: Server[]
): ServerSelectionContextType["servers"] => {
  const serverSet = new Set(serversFromApi.map((server) => server.id));
  const folders = getLocalFolders()
    .map((folder) => {
      const filteredServerOrder = folder.serverOrder.filter((id) =>
        serverSet.has(id)
      );
      return { ...folder, serverOrder: filteredServerOrder };
    })
    .filter((folder) => folder.serverOrder.length > 0);

  const serverItems: Record<string, ServerItem | Folder> = {};
  serversFromApi.forEach((server) => {
    serverItems[server.id] = { ...server, type: "server" };
  });
  folders.forEach((folder) => {
    serverItems[folder.id] = folder;
  });

  return serverItems;
};

const reconcileRootOrder = (
  servers: ServerSelectionContextType["servers"],
  rootOrder: string[]
) => {
  const cleanedRootOrder = rootOrder.filter((id) => servers[id] != undefined);
  const alreadyOrdered = new Set(cleanedRootOrder);
  const serversInFolders = new Set(
    Object.values(servers)
      .filter((item) => item.type === "folder")
      .flatMap((item) => item.serverOrder)
  );
  const newServers = Object.keys(servers).filter(
    (id) => !(alreadyOrdered.has(id) || serversInFolders.has(id))
  );
  return [...cleanedRootOrder, ...newServers];
};

export const ServerSelectionContextProvider = ({
  children,
}: PropsWithChildren) => {
  const [servers, setServers] = useState<ServerSelectionContextType["servers"]>(
    {}
  );
  const [isLoading, setIsLoading] = useState(true);

  const getServer = (serverId: string) => {
    const item = servers[serverId];
    if (item && item.type == "server") {
      const { type, ...server } = item;
      return server;
    }

    return null;
  };

  const location = useLocation();
  const serverIdFromPath = getServerIdFromPath(location.pathname);
  const selectedId =
    serverIdFromPath && servers[serverIdFromPath] ? serverIdFromPath : null;
  const [rootOrder, setRootOrder] = useState(() => getRootOrder());

  const moveItem = (
    activeId: string,
    overId: string,
    targetFolderId?: string
  ) => {
    const activeItem = servers[activeId];
    const overItem = servers[overId];
    if (
      !activeItem ||
      (!overItem && overId !== "last") ||
      (activeItem.type === "folder" && targetFolderId)
    ) {
      return;
    }

    const activeIndex = rootOrder.indexOf(activeId);
    let newRootOrder = [...rootOrder];
    let newServers = { ...servers };
    let emptyFolderId: string | undefined;
    if (activeIndex === -1) {
      // active item is in a folder and as such is a ServerItem
      const folder = servers[
        (activeItem as ServerItem).folderId as string
      ] as Folder;

      const newServerOrder = folder.serverOrder.filter((id) => id !== activeId);
      newServers[folder.id] = {
        ...folder,
        serverOrder: newServerOrder,
      };

      if (newServerOrder.length === 0) {
        emptyFolderId = folder.id;
      }

      const { folderId, ...newServerItem } = activeItem as ServerItem;
      newServers[activeId] = newServerItem;
    } else {
      newRootOrder = rootOrder.toSpliced(activeIndex, 1);
    }

    if (overId == "last") {
      if (targetFolderId && newServers[targetFolderId]) {
        const folder = newServers[targetFolderId] as Folder;
        const newServerOrder = [...folder.serverOrder, activeId];
        newServers[activeId] = {
          ...(newServers[activeId] as ServerItem),
          folderId: targetFolderId,
        };
        newServers[targetFolderId] = { ...folder, serverOrder: newServerOrder };
        emptyFolderId =
          targetFolderId === emptyFolderId ? undefined : emptyFolderId;
      } else {
        newRootOrder.push(activeId);
      }

      if (emptyFolderId) {
        delete newServers[emptyFolderId];
      }
      setServers(newServers);
      setRootOrder(newRootOrder.filter((id) => id !== emptyFolderId));
      return;
    }

    let overIndex = newRootOrder.indexOf(overId);
    if (overIndex === -1) {
      // overitem is in a folder and as such is a ServerItem
      if (activeItem.type === "folder") {
        return;
      }

      const folder = newServers[
        (overItem as ServerItem).folderId as string
      ] as Folder;

      overIndex = folder.serverOrder.indexOf(overId);
      newServers[folder.id] = {
        ...folder,
        serverOrder: folder.serverOrder.toSpliced(overIndex, 0, activeId),
      };
      newServers[activeId] = { ...activeItem, folderId: folder.id };
    } else {
      newRootOrder = newRootOrder.toSpliced(overIndex, 0, activeId);
    }

    if (emptyFolderId) {
      delete newServers[emptyFolderId];
    }
    setServers(newServers);
    setRootOrder(newRootOrder.filter((id) => id !== emptyFolderId));
  };

  const createFolder = (serverOrder: string[], index: number = Infinity) => {
    const folder: Folder = {
      id: crypto.randomUUID(),
      type: "folder",
      serverOrder,
    };

    let emptyFolders = new Set();
    setServers((prevServers) => {
      let newServers = { ...prevServers };
      newServers[folder.id] = folder;
      serverOrder.forEach((serverId) => {
        const oldServer = prevServers[serverId] as ServerItem;
        if (oldServer.folderId) {
          const oldFolder = prevServers[oldServer.folderId] as Folder;
          if (oldFolder.serverOrder.length === 1) {
            emptyFolders.add(oldFolder.id);
            delete newServers[oldFolder.id];
          } else {
            newServers[oldFolder.id] = {
              ...oldFolder,
              serverOrder: oldFolder.serverOrder.filter(
                (id) => id !== serverId
              ),
            };
          }
        }

        newServers[serverId] = { ...oldServer, folderId: folder.id };
      });
      return newServers;
    });

    const folderChildren = new Set(serverOrder);
    let newRootOrder = rootOrder.toSpliced(index, 0, folder.id);
    newRootOrder = newRootOrder.filter(
      (id) => !(folderChildren.has(id) || emptyFolders.has(id))
    );
    setRootOrder(newRootOrder);
  };

  useEffect(() => {
    getServersFromApi()
      .then((serversFromApi) => {
        const userServers = getUserJoinedServers(serversFromApi);
        setServers(userServers);
        const storedRootOrder = getRootOrder();
        const reconciledRootOrder = reconcileRootOrder(
          userServers,
          storedRootOrder
        );
        setRootOrder(reconciledRootOrder);
        setIsLoading(false);
      })
      .catch(console.error);
  }, []);

  useEffect(() => {
    if (isLoading) {
      return;
    }
    saveFoldersToLocalStorage(
      Object.values(servers).filter((item) => item.type === "folder")
    );
  }, [servers]);

  useEffect(() => {
    if (isLoading) {
      return;
    }
    saveRootOrderToLocalStorage(rootOrder);
  }, [rootOrder]);

  return (
    <ServerSelectionContext
      value={{
        servers,
        rootOrder,
        selectedId,
        isLoading,
        getServer,
        moveItem,
        createFolder,
      }}
    >
      {children}
    </ServerSelectionContext>
  );
};

export const useServerSelectionContext = () => {
  const context = useContext(ServerSelectionContext);

  if (!context) {
    throw new Error(
      "useServerSelectionContext must be called from a descendant of a ServerSelectionContextProvider"
    );
  }

  return context;
};
