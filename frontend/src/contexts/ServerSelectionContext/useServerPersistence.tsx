import { useEffect } from "react";
import type { Folder, ServerItemOrFolderRecord } from "./serverReducer";
import { FOLDER_STORAGE_KEY, ROOT_ORDER_KEY } from "../../constants";
import useLocalStorage from "../useLocalStorage";

type Props = {
  servers: ServerItemOrFolderRecord;
  rootOrder: string[];
  isLoading: boolean;
  userId: string | undefined;
};

function useServerPersistence({
  servers,
  rootOrder,
  isLoading,
  userId,
}: Props) {
  const [storedFolders, setStoredFolders] = useLocalStorage<Folder[]>({
    storageKey: FOLDER_STORAGE_KEY,
    initialValue: [],
    userId,
  });

  const [storedRootOrder, setStoredRootOrder] = useLocalStorage<string[]>({
    storageKey: ROOT_ORDER_KEY,
    initialValue: [],
    userId,
  });

  useEffect(() => {
    if (isLoading) {
      return;
    }
    const currentFolders = Object.values(servers).filter(
      (item) => item.type === "folder",
    );
    setStoredFolders(currentFolders);
    setStoredRootOrder(rootOrder);
  }, [servers, rootOrder, isLoading, setStoredFolders, setStoredRootOrder]);

  return { storedFolders, storedRootOrder };
}

export default useServerPersistence;
