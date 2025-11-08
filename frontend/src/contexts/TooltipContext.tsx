import {
  createContext,
  useContext,
  useRef,
  useState,
  type Dispatch,
  type PropsWithChildren,
  type RefObject,
  type SetStateAction,
} from "react";

export type TailStyle = "up" | "down" | "left" | "right" |"none"

type TooltipContextType = {
  text: string;
  isVisible: boolean;
  targetRef: RefObject<HTMLElement | null>;
  tailStyle: TailStyle;
};

const TooltipContext = createContext<TooltipContextType | null>(null);

type TooltipDispatchContextType = {
  setText: Dispatch<SetStateAction<string>>;
  setVisible: Dispatch<SetStateAction<boolean>>;
  targetRef: RefObject<HTMLElement | null>;
  setTailStyle: Dispatch<SetStateAction<TailStyle>>
};
const TooltipDispatchContext = createContext<TooltipDispatchContextType | null>(
  null
);

export const TooltipContextProvier = ({ children }: PropsWithChildren) => {
  const [text, setText] = useState("");
  const [isVisible, setVisible] = useState(false)
  const targetRef = useRef<HTMLElement>(null)
  const [tailStyle, setTailStyle] = useState<TailStyle>("none")

  return (
    <TooltipContext value={{ text, isVisible, targetRef, tailStyle }}>
      <TooltipDispatchContext value={{ setText, setVisible, targetRef, setTailStyle }}>
        {children}
      </TooltipDispatchContext>
    </TooltipContext>
  );
};

export const useTooltipContext = () => {
  const context = useContext(TooltipContext);
  if (!context) {
    throw new Error(
      "useTooltipContext must be called within a TooltipContextProvider"
    );
  }

  return context;
};

export const useTooltipDispatchContext = () => {
  const context = useContext(TooltipDispatchContext);
  if (!context) {
    throw new Error(
      "useTooltipContext must be called within a TooltipContextProvider"
    );
  }

  return context;
};
