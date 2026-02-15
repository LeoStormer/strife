import { useEffect } from 'react'
import type { Folder, ServerItemOrFolderRecord } from './serverReducer';
import { FOLDER_STORAGE_KEY, ROOT_ORDER_KEY } from '../../constants';

type Props = {
  servers: ServerItemOrFolderRecord;
  rootOrder: string[];
  isLoading: boolean;
}

const saveFoldersToLocalStorage = (folders: Folder[]) => {
  try {
    localStorage.setItem(FOLDER_STORAGE_KEY, JSON.stringify(folders));
  } catch (error) {
    console.log(`Failed to save folders to localStorage: ${error}`);
  }
};

const saveRootOrderToLocalStorage = (rootOrder: string[]) => {
  try {
    localStorage.setItem(ROOT_ORDER_KEY, JSON.stringify(rootOrder));
  } catch (error) {
    console.log(`Failed to save root order to localStorage: ${error}`);
  }
};

function useServerPersistence({ servers, rootOrder, isLoading }: Props) {
    useEffect(() => {
      if (isLoading) {
        return;
      }
      saveFoldersToLocalStorage(
        Object.values(servers).filter((item) => item.type === "folder"),
      );
    }, [servers, isLoading]);
  
    useEffect(() => {
      if (isLoading) {
        return;
      }
      saveRootOrderToLocalStorage(rootOrder);
    }, [rootOrder, isLoading]);
}

export default useServerPersistence