import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { type IconName } from "../../types/name";

type PageNameContextType = {
  icon: IconName | null;
  pageName: string;
};

export const PageNameContext = createContext<PageNameContextType>({
  icon: null,
  pageName: "",
});

type PageNameDispatchProps = {
  pageName?: string | undefined;
  icon?: IconName | null;
};

export const PageNameDispatchContext = createContext(
  ({ pageName = "", icon = null }: PageNameDispatchProps) => {}
);

export function PageNameContextProvider({ children }: { children: ReactNode }) {
  const [icon, setIcon] = useState<IconName | null>(null);
  const [pageName, setPageName] = useState("");

  const dispatchPageName = useCallback(
    ({ pageName = "", icon = null }: PageNameDispatchProps) => {
      setIcon(icon);
      setPageName(pageName);
    },
    [setIcon, setPageName]
  );

  return (
    <PageNameContext value={{ icon, pageName }}>
      <PageNameDispatchContext value={dispatchPageName}>
        {children}
      </PageNameDispatchContext>
    </PageNameContext>
  );
}

export const usePageNameDispatchContext = ({
  pageName,
  icon = null,
}: PageNameDispatchProps) => {
  const dispatchPageName = useContext(PageNameDispatchContext);
  useEffect(() => {
    dispatchPageName({ pageName, icon });
  }, [dispatchPageName, pageName]);
};

export default PageNameContext;
