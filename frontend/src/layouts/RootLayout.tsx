import { Outlet } from "react-router-dom";
import { UserContextProvider } from "../contexts/UserContext";
import { ThemeContextProvider } from "../contexts/ThemeContext";
import { TooltipContextProvier } from "../contexts/TooltipContext";
import { SkeletonTheme } from "react-loading-skeleton";

function RootLayout() {
  return (
    <ThemeContextProvider>
      <SkeletonTheme
        baseColor='var(--on-background-contrast)'
        highlightColor='var(--on-background-contrast-strong)'
      >
        <UserContextProvider>
          <TooltipContextProvier>
            <Outlet />
          </TooltipContextProvier>
        </UserContextProvider>
      </SkeletonTheme>
    </ThemeContextProvider>
  );
}

export default RootLayout;
