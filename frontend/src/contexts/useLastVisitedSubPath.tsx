import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

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
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (!isEnabled || !location.pathname.includes(basePath)) {
      return;
    }

    const regex = new RegExp(basePath + "$");
    const isBasePath = regex.test(location.pathname);

    if (!isBasePath) {
      // just record current path as the last visited path and return
      try {
        localStorage.setItem(storageKey, location.pathname);
      } catch (error) {
        console.log(error);
      }
      return;
    }

    const controller = new AbortController();
    const getTargetPath = async () => {
      // get last visited sub path from storage
      try {
        const storedPath = localStorage.getItem(storageKey);
        if (storedPath) {
          return storedPath;
        }
      } catch (error) {
        console.warn(error);
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
  }, [location, storageKey, basePath, defaultPath, isEnabled]);
};
