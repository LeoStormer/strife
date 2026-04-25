import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useMemo,
} from "react";
import api from "../api";
import { HttpStatusCode, isAxiosError, isCancel } from "axios";
import useLocalStorage from "./useLocalStorage";
import {
  useMutation,
  useQuery,
  useQueryClient,
  useSuspenseQueries,
} from "@tanstack/react-query";

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
  isLoggingIn: boolean;
  isRegistering: boolean;
  user: User | null;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegistrationRequest) => Promise<void>;
  logout: VoidFunction;
};

export const UserContext = createContext<UserContextType>({
  isLoading: false,
  isLoggingIn: false,
  isRegistering: false,
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

const fetchUserById = async (userId: string) => {
  try {
    const response = await api.get(`/user/${userId}`);
    return mapResponseDataToUser(response.data);
  } catch (error) {
    if (
      isAxiosError(error) &&
      error.response?.status === HttpStatusCode.NotFound
    ) {
      return {
        id: userId,
        username: `Deleted_User_${userId}`,
        profilePic: "",
        createdDate: new Date(),
      };
    }
    throw error;
  }
};

const LOCAL_STORAGE_KEY = "strife_user_data";
const METADATA_KEY = "strife_users_metadata";
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
    const metadata: Record<string, { lastAccess: number }> = raw
      ? JSON.parse(raw)
      : {};

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
  const queryClient = useQueryClient();
  const [localUser, setLocalUser] = useLocalStorage<User | null>({
    storageKey: LOCAL_STORAGE_KEY,
    initialValue: null,
  });

  // Verify user session on app load to handle cases where the session might have expired server-side
  const { data: verifiedUser, isLoading: isAuthLoading } = useQuery({
    queryKey: ["user", localUser?.id],
    queryFn: async () => {
      await api.get("/user/auth-status");
      return localUser;
    },
    enabled: !!localUser,
    staleTime: Infinity,
    retry: false,
  });

  const currentUser = verifiedUser ?? localUser;

  useEffect(() => {
    if (currentUser?.id) {
      updateMaintenanceMetadata(currentUser.id);
    }
  }, [currentUser?.id]);

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
      setLocalUser(null);
      queryClient.clear();
    }
  }, [queryClient, setLocalUser]);

  useAuthenticationInterceptor(logout);

  const loginMutation = useMutation({
    mutationFn: (request: LoginRequest) =>
      api.post<User>("/user/login", request),
    onSuccess: (response) => {
      const user = mapResponseDataToUser(response.data);
      setLocalUser(user);
      queryClient.setQueryData(["user", user.id], user);
    },
  });

  const registerMutation = useMutation({
    mutationFn: async (request: RegistrationRequest) => {
      const response = await api.post<User>("/user/register", request);

      if (response.status === HttpStatusCode.Created) {
        throw new Error(REGISTRATION_SUCCESS_BUT_LOGIN_FAILED_ERROR);
      }

      return response;
    },
    onSuccess: (response) => {
      const user = mapResponseDataToUser(response.data);
      setLocalUser(user);
      queryClient.setQueryData(["user", user.id], user);
    },
  });

  const value = useMemo(
    () => ({
      user: currentUser,
      isLoading: isAuthLoading && !!localUser,
      isLoggingIn: loginMutation.isPending,
      isRegistering: registerMutation.isPending,
      login: async (req: LoginRequest) => {
        await loginMutation.mutateAsync(req);
      },
      register: async (req: RegistrationRequest) => {
        await registerMutation.mutateAsync(req);
      },
      logout,
    }),
    [
      currentUser,
      isAuthLoading,
      localUser,
      loginMutation,
      registerMutation,
      logout,
    ],
  );

  return <UserContext value={value}>{children}</UserContext>;
};

export const useUserContext = () => useContext(UserContext);
