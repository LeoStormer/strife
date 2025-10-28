import { Outlet } from "react-router-dom";
import { UserContextProvider } from "../contexts/UserContext";

function RootLayout() {
  return (
    <>
      <UserContextProvider>
        <Outlet />
      </UserContextProvider>
    </>
  );
}

export default RootLayout;
