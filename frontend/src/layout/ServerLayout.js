import React from "react";
import { Outlet } from "react-router-dom";
import ChannelList from "../components/ChannelList";

function ServerLayout() {
  return (
    <div>
      <ChannelList />
      <Outlet />
    </div>
  );
}

export default ServerLayout;
