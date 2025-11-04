import {
  createContext,
  type PropsWithChildren,
  useContext,
  useEffect,
  useState,
} from "react";
import { useAuthStatus } from "../hooks/useAuthStatus";
import LoadingPage from "../components/pageComponents/LoadingPage";

export type User = {
  id: string;
  username: string;
  profilePic: string;
  createdDate: Date;
};

type UserContextType = {
  user: User | null;
  login: (userData: User) => void;
  logout: () => void;
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

export const UserContextProvider = ({ children }: PropsWithChildren) => {
  const [user, setUser] = useState<User | null>(getStoredUserData);
  const [isLoading, setIsLoading] = useState(true);
  const login = (userData: User) => setUser(userData);
  const logout = () => setUser(null);
  const onRequestFinished = () => setIsLoading(false);

  useAuthStatus({ onRequestFinished, logout });

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
