import { createContext, type ReactNode, useState } from "react";

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

export default PageNameContext;
