import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import type { ServerIconProps } from "../components/ServerIcon";
import type { IconProps } from "../components/Icon/Icon";

export type DynamicIconProps =
  | ({ type: "svg" } & IconProps)
  | ({ type: "serverIcon" } & ServerIconProps);

type PageNameContextType = {
  iconProps: DynamicIconProps | null;
  pageName: string;
};

export const PageNameContext = createContext<PageNameContextType>({
  iconProps: null,
  pageName: "",
});

type PageNameDispatchProps = {
  pageName?: string | undefined;
  iconProps?: DynamicIconProps | null;
};

export const PageNameDispatchContext = createContext(
  ({}: PageNameDispatchProps) => {}
);

export function PageNameContextProvider({ children }: PropsWithChildren) {
  const [iconProps, setIconProps] = useState<DynamicIconProps | null>(null);
  const [pageName, setPageName] = useState("");

  const dispatchPageName = useCallback(
    ({ pageName = "", iconProps = null }: PageNameDispatchProps) => {
      setIconProps(iconProps);
      setPageName(pageName);
    },
    [setIconProps, setPageName]
  );

  return (
    <PageNameContext value={{ iconProps, pageName }}>
      <PageNameDispatchContext value={dispatchPageName}>
        {children}
      </PageNameDispatchContext>
    </PageNameContext>
  );
}

export const usePageNameDispatchContext = ({
  pageName,
  iconProps = null,
}: PageNameDispatchProps) => {
  const dispatchPageName = useContext(PageNameDispatchContext);
  useEffect(() => {
    dispatchPageName({ pageName, iconProps });
  }, [dispatchPageName, pageName]);
};

export default PageNameContext;
