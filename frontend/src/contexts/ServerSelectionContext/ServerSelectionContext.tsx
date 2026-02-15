import {
  createContext,
  type PropsWithChildren,
  useContext,
  useEffect,
} from "react";
import { useLocation, useNavigate } from "react-router-dom";
import useServerManager, { type MoveItem } from "./useServerManager";
import useServerPersistence from "./useServerPersistence";
import useServerSync from "./useServerSync";
import { HOME_PAGE_PATH } from "../../constants";
import type {
  Server,
  ServerItem,
  Folder,
  ServerItemOrFolderRecord,
} from "./serverReducer";

export type { Server, ServerItem, Folder };

type ServerSelectionContextType = {
  servers: ServerItemOrFolderRecord;
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

export const ServerSelectionContextProvider = ({
  children,
}: PropsWithChildren) => {
  const {
    state: { servers, rootOrder },
    overwriteState,
    addServer,
    removeServer,
    moveItem,
    createFolder,
  } = useServerManager();
  const location = useLocation();
  const navigate = useNavigate();
  const serverIdFromPath = getServerIdFromPath(location.pathname);
  const selectedId =
    serverIdFromPath && servers[serverIdFromPath] ? serverIdFromPath : null;

  const getServer = (serverId: string) => {
    const item = servers[serverId];
    return item?.type === "server" ? item : null;
  };

  const { isLoading, error } = useServerSync({ overwriteState, addServer, removeServer });
  useServerPersistence({ servers, rootOrder, isLoading });

  useEffect(() => {
    if (error) {
      alert("Server experiencing issues. Please try again later.");
      navigate(HOME_PAGE_PATH);
    }
  }, [error, navigate]);

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
      "useServerSelectionContext must be called from a descendant of a ServerSelectionContextProvider",
    );
  }

  return context;
};
