import { Outlet } from "react-router-dom";
import DiscoveryPathList from "./DiscoveryPathList";
import { usePageNameDispatchContext } from "../../contexts/PageNameContext";
import { DISCOVERY_LAYOUT_PATH, SERVER_DISCOVERY_PATH } from "../../constants";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";

const DISCOVERY_PATH_STORAGE_KEY = "DISCOVERY_PATH";
const PAGE_NAME = "Discovery";

function DiscoveryLayout() {
  usePageNameDispatchContext({
    pageName: PAGE_NAME,
    iconProps: { type: "svg", name: "discover" },
  });

  useLastVisitedPath({
    storageKey: DISCOVERY_PATH_STORAGE_KEY,
    basePath: DISCOVERY_LAYOUT_PATH,
    defaultPath: SERVER_DISCOVERY_PATH,
  });

  return (
    <>
      <DiscoveryPathList />
      <Outlet />
    </>
  );
}

export default DiscoveryLayout;
