import {
  createContext,
  type PropsWithChildren,
  useContext,
  useEffect,
  useState,
} from "react";
import LoadingPage from "../components/pageComponents/LoadingPage";
import api from "../api";

export type User = {
  id: string;
  username: string;
  profilePic: string;
  createdDate: Date;
};

type UserContextType = {
  user: User | null;
  login: (userData: User) => void;
  logout: VoidFunction;
};

export const UserContext = createContext<UserContextType>({
  user: null,
  login: (userData: User) => {},
  logout: () => {},
});

const LOCAL_STORAGE_KEY = "strife_user_data";

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

type UseAuthStatusProps = {
  onRequestFinished: VoidFunction;
  logout: UserContextType["logout"];
};

/**
 * Chechs the authentication status of the user in the backend by making an API call.
 * @param onRequestFinished - Callback function to be called when the auth status check is complete success or fail
 * @param logout - function to log the user out locally if they are not authenticated
 */
const useAuthStatus = ({ onRequestFinished, logout }: UseAuthStatusProps) => {
  useEffect(() => {
    api
      .get("/user/auth-status")
      .then((response) => {
        if (response.data === false) {
          console.log("User not authenticated, logging out");
          logout();
        }
      })
      .catch(() => {
        logout();
      })
      .finally(() => {
        onRequestFinished();
      });
  }, []);
};

export const UserContextProvider = ({ children }: PropsWithChildren) => {
  const [user, setUser] = useState<User | null>(getStoredUserData);
  const [isLoading, setIsLoading] = useState(true);
  const login = (userData: User) => setUser(userData);
  const logout = () => setUser(null);

  useAuthStatus({ onRequestFinished: () => setIsLoading(false), logout });

  useEffect(() => {
    try {
      if (user) {
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(user));
      } else {
        localStorage.removeItem(LOCAL_STORAGE_KEY);
      }
    } catch (error) {
      console.error("Error occurred while accessing localStorage:", error);
    }
  }, [user]);

  if (isLoading === true) {
    return <LoadingPage />;
  }

  return <UserContext value={{ user, login, logout }}>{children}</UserContext>;
};

export const useUserContext = () => useContext(UserContext);
