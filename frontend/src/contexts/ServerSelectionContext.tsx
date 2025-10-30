import {
  createContext,
  type Dispatch,
  type PropsWithChildren,
  type SetStateAction,
  useContext,
  useState,
} from "react";

type Server = {
  id: string;
  name: string;
  icon?: string;
  description?: string;
};

type ServerSelectionContextType = {
  servers: Server[];
  selectedId: string | null;
  setServers: Dispatch<SetStateAction<Server[]>>;
  selectServer: (serverId: string) => void;
  getServer: (serverId: string) => Server | null;
};

export const ServerSelectionContext =
  createContext<ServerSelectionContextType | null>(null);

export const ServerSelectionContextProvider = ({
  children,
}: PropsWithChildren) => {
  const [servers, setServers] = useState<Server[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const serverMap = new Map(servers.map((server) => [server.id, server]));

  const selectServer = (serverId: string | null) => {
    if (serverId !== null && !serverMap.has(serverId)) {
      throw new Error("User does not have access to this server");
    }
    setSelectedId(serverId);
  };

  const getServer = (serverId: string) => serverMap.get(serverId) ?? null;

  return (
    <ServerSelectionContext
      value={{ servers, setServers, selectedId, selectServer, getServer }}
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
