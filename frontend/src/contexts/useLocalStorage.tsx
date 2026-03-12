import {
  useEffect,
  useRef,
  useState,
  type Dispatch,
  type SetStateAction,
} from "react";
import z from "zod";

const replacer = (_: string, value: any) => {
  if (value instanceof Map) {
    return { __type: "Map", value: Array.from(value.entries()) };
  }
  if (value instanceof Set) {
    return { __type: "Set", value: Array.from(value) };
  }

  if (value instanceof Date) {
    return { __type: "Date", value: value.toISOString() };
  }
  return value;
};

const ZOD_ISO_DATE_SCHEMA = z.iso.datetime();
const reviver = (_: string, value: any) => {
  if (value && typeof value === "object" && "__type" in value) {
    if (value.__type == "Map") {
      return new Map(value.value);
    }
    if (value.__type == "Set") {
      return new Set(value.value);
    }
    if (value.__type == "Date") {
      const { success } = ZOD_ISO_DATE_SCHEMA.safeParse(value.value);
      return success ? new Date(value.value) : value.value;
    }
  }

  return value;
};

type Props<T> = {
  storageKey: string;
  initialValue: T;
  userId?: string | undefined;
  syncToLocalStorage?: boolean;
};

const useLocalStorage = <T extends unknown>({
  storageKey,
  initialValue,
  userId,
  syncToLocalStorage = true,
}: Props<T>): [T, Dispatch<SetStateAction<T>>] => {
  const scopedKey = userId ? `USER_${userId}_${storageKey}` : storageKey
  const load = () => {
    try {
      const storedValue = localStorage.getItem(scopedKey);
      return storedValue !== null
        ? JSON.parse(storedValue, reviver)
        : initialValue;
    } catch (error) {
      console.error("LocalStorage Read Error:", error);
      return initialValue;
    }
  };
  const [value, setValue] = useState<T>(load);
  const lastLoadedKey = useRef(scopedKey);

  // skip the initial load, reload if storage key changes
  useEffect(() => {
    if (lastLoadedKey.current === scopedKey) {
      return;
    }

    setValue(load());
    lastLoadedKey.current = scopedKey;
  }, [scopedKey]);

  // write to local storage when value changes if sync enabled
  // debounce to account for high frequency changes.
  useEffect(() => {
    if (!syncToLocalStorage) {
      return;
    }

    const timeoutId = setTimeout(() => {
      try {
        localStorage.setItem(scopedKey, JSON.stringify(value, replacer));
      } catch (error) {
        console.error("LocalStorage Write Error:", error);
      }
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [scopedKey, value, syncToLocalStorage]);

  // if stored value is changed in another tab sync on this one.
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === scopedKey && e.newValue !== null) {
        try {
          setValue(JSON.parse(e.newValue, reviver));
        } catch (err) {
          console.error("External Storage Sync Error:", err);
        }
      }
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, [scopedKey]);

  // immediate save on close just in case user changed value then closed within debounce duration.
  useEffect(() => {
    const handleUnload = () => {
      localStorage.setItem(scopedKey, JSON.stringify(value, replacer));
    };
    window.addEventListener("beforeunload", handleUnload);
    return () => window.removeEventListener("beforeunload", handleUnload);
  }, [scopedKey, value]);

  return [value, setValue];
};

export default useLocalStorage;
