import { Outlet } from "react-router-dom";
import { UserContextProvider } from "../contexts/UserContext";
import { ThemeContextProvider } from "../contexts/ThemeContext";

function RootLayout() {
  return (
    <ThemeContextProvider>
      <UserContextProvider>
          <Outlet />
      </UserContextProvider>
    </ThemeContextProvider>
  );
}

export default RootLayout;
