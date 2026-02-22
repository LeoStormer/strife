import { useCallback, useReducer } from "react";
import {
  serverReducer,
  type Server,
  type ServerItemOrFolderRecord,
} from "./serverReducer";

export type MoveItem = {
  (activeId: string, overId: "last", targetFolderId?: string | undefined): void;
  (activeId: string, overId: string, targetFolderId?: string | undefined): void;
};

function useServerManager() {
  const [state, dispatch] = useReducer(serverReducer, {
    servers: {},
    rootOrder: [],
  });

  const overwriteState = useCallback(
    (servers: ServerItemOrFolderRecord, rootOrder: string[]) => {
      dispatch({ type: "OVERWRITE", servers, rootOrder });
    },
    [dispatch],
  );

  const addServer = useCallback(
    (server: Server) => {
      dispatch({ type: "ADD_SERVER", server });
    },
    [dispatch],
  );

  const removeServer = useCallback(
    (serverId: string) => {
      dispatch({ type: "REMOVE_SERVER", serverId });
    },
    [dispatch],
  );

  const moveItem: MoveItem = useCallback(
    (activeId: string, overId: string, targetFolderId?: string | undefined) => {
      dispatch({ type: "MOVE_ITEM", activeId, overId, targetFolderId });
    },
    [dispatch],
  );

  const createFolder = useCallback(
    (serverOrder: string[], index: number = Infinity) => {
      dispatch({ type: "CREATE_FOLDER", serverOrder, index });
    },
    [dispatch],
  );

  return {
    state,
    overwriteState,
    addServer,
    removeServer,
    moveItem,
    createFolder,
  };
}

export type ServerManager = ReturnType<typeof useServerManager>;

export default useServerManager;
