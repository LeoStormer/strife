import { useRef, type RefObject } from "react";
import type { RenderDirection, TailStyle } from "../Tooltip";
import { useTooltipDispatchContext } from "../../contexts/TooltipContext";

export type TooltipTriggerProps = {
  tooltipText: string;
  tailStyle?: TailStyle;
  renderDirection?: RenderDirection;
};

function TooltipTrigger<T extends HTMLElement>({
  tailStyle,
  tooltipText,
  renderDirection,
}: TooltipTriggerProps) {
  const { showTooltip, hideTooltip } = useTooltipDispatchContext();
  const targetRef = useRef<T | null>(null);

  const onMouseEnter = () => {
    if (targetRef.current) {
      showTooltip({
        text: tooltipText,
        targetRef: targetRef as RefObject<T>,
        tailStyle,
        renderDirection,
      });
    }
  };

  const onMouseLeave = () => {
    hideTooltip();
  };

  const getTargetProps = () => {
    return { ref: targetRef };
  };

  const getTriggerProps = () => {
    return { onMouseEnter, onMouseLeave };
  };

  const getAllProps = () => {
    return { ...getTargetProps(), ...getTriggerProps() };
  };

  return { getTargetProps, getTriggerProps, getAllProps };
}

export default TooltipTrigger;
