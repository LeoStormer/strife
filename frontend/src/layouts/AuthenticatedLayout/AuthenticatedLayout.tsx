import { Navigate, Outlet } from "react-router-dom";
import ServerBar from "./ServerBar";
import UserStatusController from "./UserStatusController";
import TopBar from "./TopBar";
import { useUserContext } from "../../contexts/UserContext";
import { ServerSelectionContextProvider } from "../../contexts/ServerSelectionContext";
import { PageNameContextProvider } from "../../contexts/PageNameContext";
import styles from "./AuthenticatedLayout.module.css";
import WebsocketContextProvider from "../../contexts/WebsocketContext";

/**
 * The main parent route after a user has authenticated. Adds a top
 * bar, a sidebar listing the servers the authenticated user has joined, and a
 * component showing the status of the authenticated user with a settings
 * button. Any page on this route redirects to the login page if the user isn't
 * logged in.
 */
function AuthenticatedLayout() {
  const { user } = useUserContext();

  if (user === null) {
    return <Navigate to={"/login"} replace />;
  }

  return (
    <div className={styles.container}>
      <WebsocketContextProvider>
        <PageNameContextProvider>
          <ServerSelectionContextProvider>
            <TopBar />
            <ServerBar />
            <Outlet />
          </ServerSelectionContextProvider>
        </PageNameContextProvider>
        <UserStatusController />
      </WebsocketContextProvider>
    </div>
  );
}

export default AuthenticatedLayout;
