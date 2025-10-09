import { Outlet } from "react-router-dom";
import ServerBar from "../components/ServerBar";
import UserStatusController from "../components/UserStatusController";
import TopBar from "../components/TopBar";

function ServerListLayout() {
  return (
    <div>
      <TopBar />
      <ServerBar />
      <UserStatusController />
      <Outlet />
    </div>
  );
}

export default ServerListLayout;
