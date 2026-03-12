import { Outlet } from "react-router-dom";
import { UserContextProvider } from "../contexts/UserContext";
import { ThemeContextProvider } from "../contexts/ThemeContext";
import { TooltipContextProvier } from "../contexts/TooltipContext";
import { SkeletonTheme } from "react-loading-skeleton";

function RootLayout() {
  return (
    <SkeletonTheme
      baseColor='var(--on-background-contrast)'
      highlightColor='var(--on-background-contrast-strong)'
    >
      <UserContextProvider>
        <ThemeContextProvider>
          <TooltipContextProvier>
            <Outlet />
          </TooltipContextProvier>
        </ThemeContextProvider>
      </UserContextProvider>
    </SkeletonTheme>
  );
}

export default RootLayout;
