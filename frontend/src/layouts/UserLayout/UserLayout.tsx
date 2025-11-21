import React from "react";
import ConversationList from "./ConversationList";
import { Outlet } from "react-router-dom";

/**
 * Adds a section listing the authenticated user's conversations. 
 */
function UserLayout() {
  return (
    <>
      <ConversationList />
      <Outlet />
    </>
  );
}

export default UserLayout;
