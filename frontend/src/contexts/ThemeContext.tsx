import {
  createContext,
  useContext,
  useEffect,
  useState,
  type PropsWithChildren,
} from "react";

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
  const [theme, setTheme] = useState(() => {
    try {
      const storedTheme = localStorage.getItem(LOCAL_STORAGE_KEY);
      return storedTheme || "light";
    } catch (error) {
      return "light";
    }
  });

  useEffect(() => {
    try {
      if (theme) {
        localStorage.setItem(LOCAL_STORAGE_KEY, theme);
      } else {
        localStorage.removeItem(LOCAL_STORAGE_KEY);
      }
      document.documentElement.setAttribute('data-theme', theme)
    } catch (error) {
      console.error("Error occurred while accessing localStorage:", error);
    }
  }, [theme]);

  return <ThemeContext value={{ theme, setTheme }}>{children}</ThemeContext>;
};

export const useTheme = () => useContext(ThemeContext);
