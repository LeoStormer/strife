import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

type UseLastVisitedPathProps = {
  storageKey: string;
  basePath: string;
  defaultPath: string;
  isEnabled?: boolean
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
    if (!isEnabled) {
      return;
    }

    const regex = new RegExp(basePath + "$");
    let lastVisitedPath: string;
    const isBasePath = regex.test(location.pathname);

    if (!location.pathname.includes(basePath)) {
      return;
    }

    try {
      lastVisitedPath = localStorage.getItem(storageKey) ?? defaultPath;
      if (!isBasePath) {
        localStorage.setItem(storageKey, location.pathname);
      }
    } catch (error) {
      lastVisitedPath = defaultPath;
    }

    if (isBasePath) {
      navigate(lastVisitedPath, { replace: true });
    }
  }, [location, storageKey, basePath, defaultPath, isEnabled]);
};
