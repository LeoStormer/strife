import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type PropsWithChildren,
  type RefObject,
} from "react";
import Tooltip, {
  TAIL_STYLE_TO_DEFAULT_RENDER_DIRECTION,
  type RenderDirection,
  type TailStyle,
} from "../components/Tooltip";

type TooltipState = {
  text: string;
  isVisible: boolean;
  targetRef: RefObject<HTMLElement> | null;
  tailStyle: TailStyle;
  renderDirection: RenderDirection;
};

type TooltipContextType = {
  state: TooltipState;
};

type TooltipDispatchContextType = {
  showTooltip: (props: {
    text: string;
    targetRef: RefObject<HTMLElement>;
    tailStyle?: TailStyle;
    renderDirection?: RenderDirection;
  }) => void;
  hideTooltip: () => void;
};

const TooltipContext = createContext<TooltipContextType | null>(null);

const TooltipDispatchContext = createContext<TooltipDispatchContextType | null>(
  null
);

export const TooltipContextProvier = ({ children }: PropsWithChildren) => {
  const [state, setState] = useState<TooltipState>({
    text: "",
    isVisible: false,
    tailStyle: "none",
    renderDirection: "right",
    targetRef: null,
  });

  const showTooltip: TooltipDispatchContextType["showTooltip"] = useCallback(
    ({
      text,
      tailStyle = "none",
      renderDirection = TAIL_STYLE_TO_DEFAULT_RENDER_DIRECTION[tailStyle],
      targetRef,
    }) => {
      setState({
        text,
        targetRef,
        tailStyle,
        renderDirection,
        isVisible: true,
      });
    },
    []
  );

  const hideTooltip = useCallback(() => {
    setState({ ...state, isVisible: false });
  }, []);

  const dispatchValue = useMemo(
    () => ({ showTooltip, hideTooltip }),
    [showTooltip, hideTooltip]
  );

  return (
    <TooltipContext value={{ state }}>
      <TooltipDispatchContext value={dispatchValue}>
        <TooltipRenderer />
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
      "useTooltipDispatchContext must be called within a TooltipContextProvider"
    );
  }

  return context;
};

export const TooltipRenderer = () => {
  const { state } = useTooltipContext();

  if (!(state.isVisible && state.targetRef)) {
    return null;
  }

  return (
    <Tooltip
      text={state.text}
      tailStyle={state.tailStyle}
      targetRef={state.targetRef}
      renderDirection={state.renderDirection}
    />
  );
};
