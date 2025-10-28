import { Outlet } from "react-router-dom";
import ChannelList from "../components/ChannelList";

/**
 * Adds a list of channels contained by the currently selected server.
 */
function ServerLayout() {
  return (
    <>
      <ChannelList />
      <Outlet />
    </>
  );
}

export default ServerLayout;
