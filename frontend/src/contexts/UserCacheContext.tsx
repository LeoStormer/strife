import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  type PropsWithChildren,
} from "react";
import { useUserContext, type User } from "./UserContext";
import api from "../api";
import { HttpStatusCode, isAxiosError, isCancel } from "axios";

const CACHE_EXPIRATION_MS = 5 * 60 * 1000;
const CACHE_CLEAR_INTERVAL = 10 * 1000;

type UserCacheContextType = {
  getUser: (userId: string) => Promise<User>;
};

const UserCacheContext = createContext<UserCacheContextType>({
  getUser: async (userId: string) => {
    return {
      id: userId,
      username: `Deleted_User_${userId}`,
      profilePic: "",
      createdDate: new Date(),
    };
  },
});

type CacheEntry = {
  user: User | null;
  promise: Promise<User> | null;
  controller: AbortController | null;
  lastAccessed: number;
};

export function UserCacheContextProvider({ children }: PropsWithChildren) {
  const cacheRef = useRef<Record<string, CacheEntry>>({});
  const { user: localUser } = useUserContext();

  const getUser = useCallback(
    async (userId: string): Promise<User> => {
      if (userId === localUser?.id) {
        return localUser;
      }

      const now = performance.now();
      const entry = cacheRef.current[userId];
      if (entry?.user) {
        entry.lastAccessed = now;
        return entry.user;
      }

      if (entry?.promise) {
        return entry.promise;
      }

      const controller = new AbortController();
      const userPromise = api
        .get(`/user/${userId}`, { signal: controller.signal })
        .then((response) => {
          const user: User = response.data;
          cacheRef.current[userId] = {
            user: user,
            promise: null,
            controller: null,
            lastAccessed: performance.now(),
          };
          return user;
        })
        .catch((error) => {
          if (
            isAxiosError(error) &&
            error.response?.status === HttpStatusCode.NotFound
          ) {
            const deletedUser: User = {
              id: userId,
              username: `Deleted_User_${userId}`,
              profilePic: "",
              createdDate: new Date(),
            };
            cacheRef.current[userId] = {
              user: deletedUser,
              promise: null,
              controller: null,
              lastAccessed: performance.now(),
            };
            return deletedUser;
          }

          if (isCancel(error)) {
            console.log("User fetch request aborted");
          }

          delete cacheRef.current[userId];
          throw error;
        });

      cacheRef.current[userId] = {
        user: null,
        promise: userPromise,
        controller,
        lastAccessed: now,
      };

      return userPromise;
    },
    [localUser],
  );

  useEffect(() => {
    const interval = setInterval(() => {
      const now = performance.now();
      const cache = cacheRef.current;

      Object.entries(cache).forEach(
        ([userId, { lastAccessed, controller }]) => {
          if (now - lastAccessed > CACHE_EXPIRATION_MS) {
            controller?.abort();
            delete cache[userId];
          }
        },
      );
    }, CACHE_CLEAR_INTERVAL);

    return () => clearInterval(interval);
  }, []);

  return <UserCacheContext value={{ getUser }}>{children}</UserCacheContext>;
}

export const useUserCacheContext = () => useContext(UserCacheContext);

export default UserCacheContext;
