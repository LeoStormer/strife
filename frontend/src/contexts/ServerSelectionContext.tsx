import {
  createContext,
  type Dispatch,
  type PropsWithChildren,
  type SetStateAction,
  useContext,
  useEffect,
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

type ServerSelectionContextType = {
  servers: Server[];
  selectedId: string | null;
  setServers: Dispatch<SetStateAction<Server[]>>;
  getServer: (serverId: string) => Server | null;
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
  const [servers, setServers] = useState<Server[]>([
    { id: "1", name: "Server 1", defaultChannelId: "1" },
    { id: "2", name: "Server 2", defaultChannelId: "1" },
    { id: "3", name: "Server 3", defaultChannelId: "1" },
  ]); // Use api to get these later
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const serverMap = new Map(servers.map((server) => [server.id, server]));
  const getServer = (serverId: string) => serverMap.get(serverId) ?? null;

  const location = useLocation();
  const serverIdFromPath = getServerIdFromPath(location.pathname);

  useEffect(() => {
    if (serverIdFromPath && serverMap.has(serverIdFromPath)) {
      setSelectedId(serverIdFromPath);
    } else {
      setSelectedId(null);
    }
  }, [serverIdFromPath, servers]);

  return (
    <ServerSelectionContext
      value={{ servers, setServers, selectedId, getServer }}
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
