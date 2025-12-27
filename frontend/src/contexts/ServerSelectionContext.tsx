import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useState,
} from "react";
import { useLocation } from "react-router-dom";

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

const getUserJoinedServers = (): ServerSelectionContextType["servers"] => {
  if (process.env.NODE_ENV === "production") {
    // get joined servers from api
    // map server array to proper structure
    return {};
  }

  return {
    "1": { id: "1", type: "server", name: "Server 1", defaultChannelId: "1" },
    "2": { id: "2", type: "server", name: "Server 2", defaultChannelId: "1" },
    "3": { id: "3", type: "server", name: "Server 3", defaultChannelId: "1" },
    "4": { id: "4", type: "server", name: "Server 4", defaultChannelId: "1" },
    "5": { id: "5", type: "server", name: "Server 5", defaultChannelId: "1" },
  };
};

const ROOT_ORDER_KEY = "SERVERBAR_ORDER";
const getRootOrder = (): string[] => {
  // try {
  //   const storedData = localStorage.getItem(ROOT_ORDER_KEY)

  //   if (!storedData) {
  //     return []
  //   }
  //   return JSON.parse(storedData)
  // } catch (error) {
  //   return []
  // }
  return ["1", "2", "3", "4", "5"];
};

const reconcileRootOrder = (
  servers: ServerSelectionContextType["servers"],
  rootOrder: string[]
) => {
  const filteredRootOrder = rootOrder.filter((id) => {
    const item = servers[id];
    if (!item) {
      return false;
    }

    if (item.type === "folder") {
    }
  });
};

export const ServerSelectionContextProvider = ({
  children,
}: PropsWithChildren) => {
  const [servers, setServers] = useState<ServerSelectionContextType["servers"]>(
    () => getUserJoinedServers()
  );

  const getServer = useCallback(
    (serverId: string) => {
      const item = servers[serverId];
      if (item && item.type == "server") {
        const { type, ...server } = item;
        return server;
      }

      return null;
    },
    [servers]
  );

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

  return (
    <ServerSelectionContext
      value={{
        servers,
        rootOrder,
        selectedId,
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
