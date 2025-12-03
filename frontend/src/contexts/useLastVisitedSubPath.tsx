import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

type UseLastVisitedPathProps = {
  storageKey: string;
  basePath: string;
  defaultPath: string;
};

export const useLastVisitedPath = ({
  storageKey,
  basePath,
  defaultPath,
}: UseLastVisitedPathProps) => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const regex = new RegExp(basePath + "$");
    let lastVisitedPath: string;
    const isBasePath = regex.test(location.pathname);

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
  }, [location]);
};
