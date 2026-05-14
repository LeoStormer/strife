import { Outlet } from "react-router-dom";
import { UserContextProvider } from "../contexts/UserContext";
import { ThemeContextProvider } from "../contexts/ThemeContext";
import { TooltipContextProvier } from "../contexts/TooltipContext";
import { SkeletonTheme } from "react-loading-skeleton";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import api from "../api";

function RootLayout() {
  const [initialHandshakeDone, setInitialHandshakeDone] = useState(false);

  useEffect(() => {
    api
      .get("/auth/csrf")
      .then(() => {
        setInitialHandshakeDone(true);
      })
      .catch((error) => {
        console.log("Initial handshake failed", error);
      });
  }, []);

  if (!initialHandshakeDone) {
    return null;
  }

  const queryClient = new QueryClient();
  return (
    <SkeletonTheme
      baseColor='var(--on-background-contrast)'
      highlightColor='var(--on-background-contrast-strong)'
    >
      <QueryClientProvider client={queryClient}>
        <UserContextProvider>
          <ThemeContextProvider>
            <TooltipContextProvier>
              <Outlet />
            </TooltipContextProvier>
          </ThemeContextProvider>
        </UserContextProvider>
      </QueryClientProvider>
    </SkeletonTheme>
  );
}

export default RootLayout;
