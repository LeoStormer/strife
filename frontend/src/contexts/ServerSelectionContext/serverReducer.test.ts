import { serverReducer, type Folder, type Server, type ServerItem, type ServerManagerAction, type State } from './serverReducer';

const INITIAL_STATE: State = {
    servers: {
        's1': { id: 's1', type: 'server', folderId: 'f1' } as ServerItem,
        's2': { id: 's2', type: 'server', folderId: 'f1' } as ServerItem,
        's3': { id: 's3', type: 'server' } as ServerItem,
        'f1': { id: 'f1', type: 'folder', serverOrder: ['s1', 's2'] } as Folder,
        's4': { id: 's4', type: 'server', folderId: 'f2' } as ServerItem,
        'f2': { id: 'f2', type: 'folder', serverOrder: ['s4'] } as Folder,
    }, rootOrder: ['f1', 's3', 'f2']
};

describe('Basics', () => {
    it('should return the same state for unknown action types', () => {
        const action = { type: 'UNKNOWN_ACTION' } as any;
        const result = serverReducer(INITIAL_STATE, action);
        expect(result).toBe(INITIAL_STATE);
    });

    it('ADD_SERVER: should add a new server to the root', () => {
        const newServer: Server = { id: 's5', name: 'New Server' };
        const state = serverReducer(INITIAL_STATE, { type: 'ADD_SERVER', server: newServer });

        expect(state.servers['s5']).toMatchObject({ id: 's5', name: 'New Server', type: 'server' });
        expect(state.rootOrder).toContain('s5');
        expect(state.rootOrder.length).toBe(4);
    });

    it('OVERWRITE: should replace the entire state', () => {
        const newState: State = { servers: {}, rootOrder: [] };
        const state = serverReducer(INITIAL_STATE, {
            type: 'OVERWRITE',
            servers: newState.servers,
            rootOrder: newState.rootOrder
        });
        expect(state).toEqual(newState);
    });
});

describe('Removal', () => {
    it('REMOVE_SERVER: should remove a standalone server from root', () => {
        const state = serverReducer(INITIAL_STATE, { type: 'REMOVE_SERVER', serverId: 's3' });
        expect(state.servers['s3']).toBeUndefined();
        expect(state.rootOrder).not.toContain('s3');
    });

    it('REMOVE_SERVER: should remove from folder and delete folder if it becomes empty', () => {
        // s4 is the only item in f2
        const state = serverReducer(INITIAL_STATE, { type: 'REMOVE_SERVER', serverId: 's4' });
        expect(state.servers['s4']).toBeUndefined();
        expect(state.servers['f2']).toBeUndefined(); // Auto-cleanup
        expect(state.rootOrder).toEqual(['f1', 's3']);
    });

    it('REMOVE_SERVER: should remove from folder but keep folder if not empty', () => {
        // s1 is in f1 with s2
        const state = serverReducer(INITIAL_STATE, { type: 'REMOVE_SERVER', serverId: 's1' });
        expect(state.servers['s1']).toBeUndefined();
        expect((state.servers['f1'] as Folder).serverOrder).toEqual(['s2']);
    });
});

describe('Folder Creation', () => {
    it('CREATE_FOLDER: should group servers and remove them from original locations', () => {
        const action: ServerManagerAction = { type: 'CREATE_FOLDER', serverOrder: ['s2', 's3'], index: 0 } as const;
        const state = serverReducer(INITIAL_STATE, action);

        // Find new folder
        const newFolderId = state.rootOrder[0]!;
        const newFolder = state.servers[newFolderId] as Folder;

        expect(newFolder.serverOrder).toEqual(['s2', 's3']);
        expect((state.servers['s2'] as ServerItem).folderId).toBe(newFolderId);

        // Check original folder f1 was updated (s2 removed)
        expect((state.servers['f1'] as Folder).serverOrder).toEqual(['s1']);
        // Check s3 was removed from root
        expect(state.rootOrder).not.toContain('s3');
    });

    it('CREATE_FOLDER: should delete source folders if all their children are moved', () => {
        const action: ServerManagerAction = { type: 'CREATE_FOLDER', serverOrder: ['s4'] } as const;
        const state = serverReducer(INITIAL_STATE, action);

        expect(state.servers['f2']).toBeUndefined(); // f2 only had s4
        expect(state.rootOrder).not.toContain('f2');
    });
});


describe('Movement', () => {
    it('should return state unchanged if activeItem is not found', () => {
        const action = { type: 'MOVE_ITEM', activeId: 'non-existent', overId: 'last' } as const;
        const result = serverReducer(INITIAL_STATE, action);
        expect(result).toBe(INITIAL_STATE);
    });

    it('should return state unchanged if overItem is not found (and overId is not "last")', () => {
        const action = { type: 'MOVE_ITEM', activeId: 's1', overId: 'non-existent' } as const;
        const result = serverReducer(INITIAL_STATE, action);
        expect(result).toBe(INITIAL_STATE);
    });

    it('MOVE_ITEM: should reorder items within the root', () => {
        // Move f2 to the front
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 'f2', overId: 'f1'
        });
        expect(state.rootOrder).toEqual(['f2', 'f1', 's3']);
    });

    it('MOVE_ITEM: should move a server into a folder', () => {
        // Move s3 into f1 (over s1)
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 's3', overId: 's1'
        });
        const f1 = state.servers['f1'] as Folder;
        expect(f1.serverOrder).toEqual(['s3', 's1', 's2']);
        expect((state.servers['s3'] as ServerItem).folderId).toBe('f1');
        expect(state.rootOrder).not.toContain('s3');
    });

    it('MOVE_ITEM: should move an item to the "last" position in the root', () => {
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 'f1', overId: 'last'
        });
        expect(state.rootOrder).toEqual(['s3', 'f2', 'f1']);
    });

    it('MOVE_ITEM: should move a server to the "last" position in a folder', () => {
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 's3', overId: 'last', targetFolderId: 'f1'
        });
        expect((state.servers['f1'] as Folder).serverOrder).toEqual(['s1', 's2', 's3']);
    });

    it('MOVE_ITEM: should move a server out of a folder to the root', () => {
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 's1', overId: 's3'
        });
        expect(state.rootOrder).toEqual(['f1', 's1', 's3', 'f2']);
        expect((state.servers['f1'] as Folder).serverOrder).toEqual(['s2']);
        expect((state.servers['s1'] as ServerItem).folderId).toBeUndefined();
    });

    it('MOVE_ITEM: should move a server from one folder to another', () => {
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 's1', overId: 's4', targetFolderId: 'f2'
        });
        expect((state.servers['f1'] as Folder).serverOrder).toEqual(['s2']);
        expect((state.servers['f2'] as Folder).serverOrder).toEqual(['s1', 's4']);
        expect((state.servers['s1'] as ServerItem).folderId).toBe('f2');
    });

    it('MOVE_ITEM: should delete source folder if server moved out and it becomes empty', () => {
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 's4', overId: 's3'
        });
        expect(state.servers['f2']).toBeUndefined();
        expect(state.rootOrder).not.toContain('f2');
        expect(state.rootOrder).toEqual(['f1', 's4', 's3']);
    });

    it('MOVE_ITEM: should guard against moving folders into other folders', () => {
        const state = serverReducer(INITIAL_STATE, {
            type: 'MOVE_ITEM', activeId: 'f1', overId: 's4'
        });

        expect(state).toBe(INITIAL_STATE);
    });
});
