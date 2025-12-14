import {
  type DetailedHTMLProps,
  type HTMLAttributes,
  type RefObject,
} from "react";
import { useTooltipDispatchContext } from "../../contexts/TooltipContext";
import type { RenderDirection, TailStyle } from "../Tooltip";

export type ButtonProps = Omit<
  DetailedHTMLProps<HTMLAttributes<HTMLDivElement>, HTMLDivElement>,
  "onMouseEnter" | "onMouseLeave"
> & {
  targetRef: RefObject<HTMLElement>;
  tooltipText: string;
  tailStyle?: TailStyle;
  renderDirection?: RenderDirection;
};

function Button({
  targetRef,
  tailStyle,
  tooltipText,
  renderDirection,
  ...divProps
}: ButtonProps) {
  const { showTooltip, hideTooltip } = useTooltipDispatchContext();

  const handleMouseEnter = () => {
    if (targetRef.current) {
      showTooltip({
        text: tooltipText,
        targetRef,
        tailStyle,
        renderDirection,
      });
    }
  };

  const handleMouseLeave = () => {
    hideTooltip();
  };

  return (
    <div
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      {...divProps}
    />
  );
}

export default Button;
