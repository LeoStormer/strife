
export type Server = {
  id: string;
  name: string;
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

export type ServerItemOrFolderRecord = Record<string, ServerItem | Folder>;

export type State = {
  servers: ServerItemOrFolderRecord;
  rootOrder: string[];
};

type ServerManagerAction =
  | { type: 'OVERWRITE'; servers: ServerItemOrFolderRecord; rootOrder: string[] }
  | { type: 'ADD_SERVER'; server: Server }
  | { type: 'REMOVE_SERVER'; serverId: string }
  | { type: 'MOVE_ITEM'; activeId: string; overId: string; targetFolderId?: string | undefined }
  | { type: 'CREATE_FOLDER'; serverOrder: string[]; index?: number };

const onServerAdded = (state: State, { server }: Extract<ServerManagerAction, { type: 'ADD_SERVER' }>) => {
  const newServerItem: ServerItem = { ...server, type: "server" };
  return {
    servers: {
      ...state.servers,
      [server.id]: newServerItem,
    },
    rootOrder: [...state.rootOrder, server.id],
  };
}

const onServerRemoved = (state: State, { serverId }: Extract<ServerManagerAction, { type: 'REMOVE_SERVER' }>) => {
  const { servers, rootOrder } = state;
  const oldServerItem = servers[serverId] as ServerItem;
  const newServers = { ...servers };

  let emptyFolderId: string | undefined;
  if (oldServerItem.folderId) {
    const folder = servers[oldServerItem.folderId] as Folder;
    if (folder.serverOrder.length === 1) {
      emptyFolderId = folder.id;
      delete newServers[folder.id];
    } else {
      const newServerOrder = folder.serverOrder.filter(id => id !== serverId);
      newServers[folder.id] = { ...folder, serverOrder: newServerOrder };
    }
  }

  delete newServers[serverId];

  return {
    servers: newServers,
    rootOrder: rootOrder.filter(id => id !== serverId && id !== emptyFolderId),
  };
}

const onMove = (state: State, { activeId, overId, targetFolderId }: Extract<ServerManagerAction, { type: 'MOVE_ITEM' }>) => {
  const { servers, rootOrder } = state;
  const activeItem = servers[activeId];
  const overItem = servers[overId];
  if (
    activeItem == undefined ||
    (overItem == undefined && overId !== "last") ||
    (activeItem.type === "folder" && targetFolderId != undefined)
  ) {
    return state;
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
    newRootOrder.splice(activeIndex, 1);
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

    return {
      servers: newServers,
      rootOrder: newRootOrder.filter((id) => id !== emptyFolderId),
    }
  }

  let overIndex = newRootOrder.indexOf(overId);
  if (overIndex === -1) {
    // overitem is in a folder and as such is a ServerItem
    if (activeItem.type === "folder") {
      return state;
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

  return { servers: newServers, rootOrder: newRootOrder.filter((id) => id !== emptyFolderId) };
};

const onCreateFolder = (state: State, { serverOrder, index = state.rootOrder.length }: Extract<ServerManagerAction, { type: 'CREATE_FOLDER' }>) => {
  const folderId = crypto.randomUUID();
  const folder: Folder = {
    id: folderId,
    type: "folder",
    serverOrder,
  };

  const { servers, rootOrder } = state;
  let emptyFolders = new Set();
  let newServers = { ...servers };
  newServers[folderId] = folder;
  serverOrder.forEach((serverId) => {
    const oldServer = servers[serverId] as ServerItem;
    if (oldServer.folderId) {
      const oldFolder = servers[oldServer.folderId] as Folder;
      if (oldFolder.serverOrder.length === 1) {
        emptyFolders.add(oldFolder.id);
        delete newServers[oldFolder.id];
      } else {
        newServers[oldFolder.id] = {
          ...oldFolder,
          serverOrder: oldFolder.serverOrder.filter(
            (id) => id !== serverId,
          ),
        };
      }
    }

    newServers[serverId] = { ...oldServer, folderId: folderId };
  });

  const folderChildren = new Set(serverOrder);
  let newRootOrder = rootOrder.toSpliced(index, 0, folderId);
  newRootOrder = newRootOrder.filter(
    (id) => !(folderChildren.has(id) || emptyFolders.has(id)),
  );
  return { servers: newServers, rootOrder: newRootOrder };
};

export const serverReducer = (state: State, action: ServerManagerAction): State => {
  switch (action.type) {
    case 'OVERWRITE':
      return { servers: action.servers, rootOrder: action.rootOrder };
    case 'ADD_SERVER':
      return onServerAdded(state, action);
    case 'REMOVE_SERVER':
      return onServerRemoved(state, action);
    case 'MOVE_ITEM':
      return onMove(state, action);
    case 'CREATE_FOLDER':
      return onCreateFolder(state, action);
    default:
      return state;
  }
};
