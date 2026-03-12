import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import api from "../api";
import { HttpStatusCode, isAxiosError, isCancel } from "axios";
import useLocalStorage from "./useLocalStorage";

export type User = {
  id: string;
  username: string;
  profilePic: string;
  createdDate: Date;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegistrationRequest = {
  email: string;
  username: string;
  password: string;
};

type UserContextType = {
  isLoading: boolean;
  user: User | null;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegistrationRequest) => Promise<void>;
  logout: VoidFunction;
};

export const UserContext = createContext<UserContextType>({
  isLoading: false,
  user: null,
  login: (request: LoginRequest) => Promise.resolve(),
  register: (request: RegistrationRequest) => Promise.resolve(),
  logout: () => {},
});

const mapResponseDataToUser = (data: any): User => {
  return {
    ...data,
    createdDate: new Date(data.createdDate),
  };
};

const LOCAL_STORAGE_KEY = "strife_user_data";
const METADATA_KEY = "strife_users_metadata"
export const REGISTRATION_SUCCESS_BUT_LOGIN_FAILED_ERROR =
  "REGISTRATION_SUCCESS_BUT_LOGIN_FAILED";

const useAuthenticationInterceptor = (logout: VoidFunction) => {
  useEffect(() => {
    const interceptor = api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          console.log("User Session Expired, logging out");
          logout();
        }
        return Promise.reject(error);
      },
    );

    return () => {
      api.interceptors.response.eject(interceptor);
    };
  }, [logout]);
};

const updateMaintenanceMetadata = (currentUserId: string) => {
  const THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000;
  const now = Date.now();

  try {
    const raw = localStorage.getItem(METADATA_KEY);
    const metadata: Record<string, { lastAccess: number }> = raw ? JSON.parse(raw) : {};

    // 1. Update current user's timestamp
    metadata[currentUserId] = { lastAccess: now };

    // 2. Identify and remove data for other users older than 30 days
    Object.entries(metadata).forEach(([userId, data]) => {
      if (userId !== currentUserId && now - data.lastAccess > THIRTY_DAYS_MS) {
        // Find all keys prefixed with this abandoned userId and wipe them
        const prefix = `user_${userId}_`;
        Object.keys(localStorage).forEach((key) => {
          if (key.startsWith(prefix)) {
            localStorage.removeItem(key);
          }
        });
        delete metadata[userId];
      }
    });

    localStorage.setItem(METADATA_KEY, JSON.stringify(metadata));
  } catch (e) {
    console.warn("Maintenance cleanup failed:", e);
  }
};

export const UserContextProvider = ({ children }: PropsWithChildren) => {
  const [user, setUser] = useLocalStorage<User | null>({storageKey: LOCAL_STORAGE_KEY, initialValue: null});
  const [isLoading, setIsLoading] = useState(user !== null);

  const register = useCallback(async (request: RegistrationRequest) => {
    const response = await api.post<User>("/user/register", request);

    if (response.status === HttpStatusCode.Created) {
      throw new Error(REGISTRATION_SUCCESS_BUT_LOGIN_FAILED_ERROR);
    }

    setUser(mapResponseDataToUser(response.data));
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await api.post<User>("/user/login", request);
    setUser(mapResponseDataToUser(response.data));
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.post("/user/logout");
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 401) {
        console.warn("Session was already expired on the server.");
        return; // Swallow 401 as it's an expected state for a logout
      }

      throw error;
    } finally {
      // Always clear local state
      setUser(null);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    if (!user) {
      setIsLoading(false);
      return;
    }

    // verify with backend that the stored user data is still valid
    api
      .get("/user/auth-status", { signal: controller.signal })
      .then(() => {
        updateMaintenanceMetadata(user.id);
        setIsLoading(false);
      })
      .catch((error) => {
        // interceptor alread handles logging out on 401, so we just stop loading and let it do its thing
        if (isCancel(error)) {
          return;
        }

        setIsLoading(false);
      });

    return () => {
      controller.abort();
    };
  }, [user?.id]);

  useAuthenticationInterceptor(logout);

  return (
    <UserContext value={{ user, register, login, logout, isLoading }}>
      {children}
    </UserContext>
  );
};

export const useUserContext = () => useContext(UserContext);
