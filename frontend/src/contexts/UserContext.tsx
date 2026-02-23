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
export const REGISTRATION_SUCCESS_BUT_LOGIN_FAILED_ERROR =
  "REGISTRATION_SUCCESS_BUT_LOGIN_FAILED";

const getStoredUserData = (): User | null => {
  try {
    const storedData = localStorage.getItem(LOCAL_STORAGE_KEY);
    if (!storedData) {
      return null;
    }

    const userData = JSON.parse(storedData);
    userData.createdDate = new Date(userData.createdDate);
    return userData;
  } catch (error) {
    console.warn("Failed to parse user data from localStorage: ", error);
  }

  return null;
};

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

const useUserPersistence = (user: User | null, isLoading: boolean) => {
  useEffect(() => {
    if (isLoading) {
      return;
    }

    try {
      if (user) {
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(user));
      } else {
        localStorage.removeItem(LOCAL_STORAGE_KEY);
      }
    } catch (error) {
      console.error("Error occurred while accessing localStorage:", error);
    }
  }, [user, isLoading]);
};

export const UserContextProvider = ({ children }: PropsWithChildren) => {
  const storedUserData = getStoredUserData();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(storedUserData !== null);

  // Inside UserContextProvider:
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
      localStorage.removeItem(LOCAL_STORAGE_KEY);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const storedUser = getStoredUserData();
    if (storedUser) {
      // verify with backend that the stored user data is still valid before setting user state
      api
        .get("/user/auth-status", { signal: controller.signal })
        .then(() => {
          // session is valid, set user state from localStorage
          setUser(storedUser);
          setIsLoading(false);
        })
        .catch((error) => {
          // interceptor alread handles logging out on 401, so we just stop loading and let it do its thing
          if (isCancel(error)) {
            return;
          }

          setIsLoading(false);
        });
    } else {
      setIsLoading(false);
    }

    return () => {
      controller.abort();
    };
  }, []);

  useUserPersistence(user, isLoading);
  useAuthenticationInterceptor(logout);

  return (
    <UserContext value={{ user, register, login, logout, isLoading }}>
      {children}
    </UserContext>
  );
};

export const useUserContext = () => useContext(UserContext);
