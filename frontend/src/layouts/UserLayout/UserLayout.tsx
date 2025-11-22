import React from "react";
import ConversationList from "./ConversationList";
import { Outlet } from "react-router-dom";
import { FRIENDS_PAGE_PATH, USER_LAYOUT_PATH } from "../../constants";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";

const USER_PATH_STORAGE_KEY = "USER_PATH";
/**
 * Adds a section listing the authenticated user's conversations.
 */
function UserLayout() {
  useLastVisitedPath({
    storageKey: USER_PATH_STORAGE_KEY,
    basePath: USER_LAYOUT_PATH,
    defaultPath: FRIENDS_PAGE_PATH,
  });

  return (
    <>
      <ConversationList />
      <Outlet />
    </>
  );
}

export default UserLayout;
