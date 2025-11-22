import {
  createContext,
  type ReactNode,
  useContext,
  useEffect,
  useState,
} from "react";

export const PageNameContext = createContext("");
export const PageNameDispatchContext = createContext((pageName: string) => {});

export function PageNameContextProvider({ children }: { children: ReactNode }) {
  const [pageName, setPageName] = useState("");

  return (
    <PageNameContext value={pageName}>
      <PageNameDispatchContext value={setPageName}>
        {children}
      </PageNameDispatchContext>
    </PageNameContext>
  );
}

type UsePageNameDispatchContextProps = { pageName: string };

export const usePageNameDispatchContext = ({
  pageName,
}: UsePageNameDispatchContextProps) => {
  const setPageName = useContext(PageNameDispatchContext);
  useEffect(() => {
    setPageName(pageName);
  }, [setPageName, pageName]);
};

export default PageNameContext;
