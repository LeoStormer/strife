import React from "react";
import ConversationList from "../components/ConversationList";
import { Outlet } from "react-router-dom";

function UserLayout() {
  return (
    <div>
      <ConversationList />
      <Outlet />
    </div>
  );
}

export default UserLayout;
