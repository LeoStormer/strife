import {
  createContext,
  useContext,
  useEffect,
  type PropsWithChildren,
} from "react";
import { useUserContext } from "./UserContext";
import useLocalStorage from "./useLocalStorage";

type ThemeContextType = {
  theme: string;
  setTheme: (newTheme: string) => void;
};

export const ThemeContext = createContext<ThemeContextType>({
  theme: "light",
  setTheme: (newTheme: string) => {},
});

const LOCAL_STORAGE_KEY = "THEME";

export const ThemeContextProvider = ({ children }: PropsWithChildren) => {
  const { user } = useUserContext();
  const [theme, setTheme] = useLocalStorage<string>({
    storageKey: LOCAL_STORAGE_KEY,
    initialValue: "light",
    userId: user?.id,
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  return <ThemeContext value={{ theme, setTheme }}>{children}</ThemeContext>;
};

export const useTheme = () => useContext(ThemeContext);
