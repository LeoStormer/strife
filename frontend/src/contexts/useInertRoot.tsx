import { useEffect } from "react";

const rootTrapStore = (() => {
  const activeRootTraps = new Set<string>();

  return {
    add(id: string) {
      activeRootTraps.add(id);
      if (activeRootTraps.size === 1) {
        document.getElementById("root")?.setAttribute("inert", "");
      }
    },
    remove(id: string) {
      activeRootTraps.delete(id);
      if (activeRootTraps.size === 0) {
        document.getElementById("root")?.removeAttribute("inert");
      }
    },
  };
})();

/**
 * A hook to manage the inert state of the root element. As long as at least
 * one component has requested the root to be inert, it will remain inert.
 * @param shouldDeactivateRoot whether to set the root element as inert
 * @param id a unique identifier for the component instance
 */
const useInertRoot = (shouldDeactivateRoot: boolean, id: string) => {
  useEffect(() => {
    if (!shouldDeactivateRoot) {
      return;
    }

    rootTrapStore.add(id);
    return () => {
      rootTrapStore.remove(id);
    };
  }, [shouldDeactivateRoot, id]);
};

export default useInertRoot;
