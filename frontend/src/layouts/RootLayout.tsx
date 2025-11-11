import { Outlet } from "react-router-dom";
import { UserContextProvider } from "../contexts/UserContext";
import { ThemeContextProvider } from "../contexts/ThemeContext";
import { TooltipContextProvier } from "../contexts/TooltipContext";

function RootLayout() {
  return (
    <ThemeContextProvider>
      <UserContextProvider>
        <TooltipContextProvier>
          <Outlet />
        </TooltipContextProvier>
      </UserContextProvider>
    </ThemeContextProvider>
  );
}

export default RootLayout;
