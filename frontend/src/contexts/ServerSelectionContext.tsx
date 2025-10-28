import { createContext, ReactNode, useState } from "react";

type Server = {
  id: string;
  name: string;
  icon: string;
  description: string;
};

interface ServerSelectionContextType {
  servers: Server[];
  selectedId: string | null;
  updateServers: (servers: Server[]) => void;
  selectServer: (serverId: string) => void;
  getServer: (serverId: string) => Server | null;
}

export const ServerSelectionContext = createContext<ServerSelectionContextType>(
  {
    servers: [],
    selectedId: null,
    updateServers: (servers: Server[]) => {},
    selectServer: (serverId: string) => {},
    getServer: (serverId: string) => null,
  }
);

export const ServerSelectionContextProvider = ({
  children,
}: {
  children: ReactNode;
}) => {
  const [servers, setServers] = useState<Server[]>([]);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const serverMap = new Map(servers.map((server) => [server.id, server]));

  const updateServers = (updatedServers: Server[]) => {
    setServers(updatedServers);
  };

  const selectServer = (serverId: string | null) => {
    if (serverId !== null && !serverMap.has(serverId)) {
      throw new Error("User does not have access to this server");
    }
    setSelectedId(serverId);
  };

  const getServer = (serverId: string) => serverMap.get(serverId) ?? null;

  return (
    <ServerSelectionContext
      value={{ servers, updateServers, selectedId, selectServer, getServer }}
    >
      {children}
    </ServerSelectionContext>
  );
};
