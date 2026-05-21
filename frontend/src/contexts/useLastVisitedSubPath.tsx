import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import useLocalStorage from "./useLocalStorage";
import { useUserContext } from "./UserContext";

type UseLastVisitedPathProps = {
  storageKey: string;
  basePath: string;
  defaultPath?: string | ((signal: AbortSignal) => Promise<string>) | undefined;
  isEnabled?: boolean;
};

/**
 * Redirects to the last visited path if the current location is the `basePath`.
 * Redirects instead to the `defaultPath` if no last visited path is stored.
 *
 * @param {string} props.storageKey - The key used to store the last visited path in `localStorage`.
 * @param {string} props.basePath - The entry path that triggers redirection (e.g., '/servers/@me').
 * @param {string} props.defaultPath - The fallback path (e.g., `/servers/@me/friends`).
 * @param {boolean} [props.isEnabled=true] - Whether the hook is enabled.
 */
export const useLastVisitedPath = ({
  storageKey,
  basePath,
  defaultPath,
  isEnabled = true,
}: UseLastVisitedPathProps) => {
  const { user } = useUserContext();
  const navigate = useNavigate();
  const location = useLocation();

  const [lastVisitedPath, setLastVisitedPath] = useLocalStorage<
    string | undefined
  >({
    storageKey,
    initialValue: undefined,
    userId: user?.id,
    syncToLocalStorage: isEnabled,
  });

  useEffect(() => {
    if (!isEnabled || !location.pathname.includes(basePath)) {
      return;
    }

    const isBasePath = new RegExp(basePath + "$").test(location.pathname);

    if (!isBasePath) {
      setLastVisitedPath(location.pathname);
      return;
    }

    const controller = new AbortController();
    const getTargetPath = async () => {
      if (lastVisitedPath) {
        return lastVisitedPath;
      }

      // get the default path
      if (typeof defaultPath === "function") {
        return await defaultPath(controller.signal);
      }

      return defaultPath;
    };

    getTargetPath()
      .then((targetPath) => {
        if (targetPath && targetPath !== location.pathname) {
          navigate(targetPath, { replace: true });
        }
      })
      .catch(console.warn);

    return () => {
      controller.abort();
    };
  }, [
    location.pathname,
    storageKey,
    basePath,
    defaultPath,
    isEnabled,
    lastVisitedPath,
    setLastVisitedPath,
  ]);
};
