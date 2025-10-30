import { useContext, useEffect } from "react";
import { PageNameDispatchContext } from "../contexts/PageNameContext";

function DiscoveryPage() {
  const setPageName = useContext(PageNameDispatchContext);

  useEffect(() => {
    setPageName("Discovery");
  }, [setPageName]);

  return <div>DiscoveryPage</div>;
}

export default DiscoveryPage;
