import { useContext, useEffect } from "react";
import { Outlet } from "react-router-dom";
import DiscoveryPathList from "./DiscoveryPathList";
import { PageNameDispatchContext } from "../../contexts/PageNameContext";
import { DISCOVERY_LAYOUT_PATH, SERVER_DISCOVERY_PATH } from "../../constants";
import { useLastVisitedPath } from "../../contexts/useLastVisitedSubPath";

const DISCOVERY_PATH_STORAGE_KEY = "DISCOVERY_PATH";

function DiscoveryLayout() {
  const setPageName = useContext(PageNameDispatchContext);

  useEffect(() => {
    setPageName("Discovery");
  }, [setPageName]);

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
