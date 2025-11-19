import {
  useRef,
  type MouseEventHandler,
  type PropsWithChildren,
  type RefObject,
} from "react";
import styles from "./Button.module.css";
import StyleComposer from "../../../utils/StyleComposer";
import { useTooltipDispatchContext } from "../../../contexts/TooltipContext";

function Pill({ isHidden = false }) {
  const className = StyleComposer(styles.pill, {
    [styles.hidden as string]: isHidden,
  });

  return <div className={className}></div>;
}

export type ButtonProps = PropsWithChildren<{
  isSelected?: boolean | undefined;
  isPillHidden?: boolean | undefined;
  onClick?: MouseEventHandler<HTMLButtonElement>;
  tooltipText: string;
}>;

function Button({
  isSelected = false,
  isPillHidden,
  children,
  onClick,
  tooltipText,
}: ButtonProps) {
  const buttonRef = useRef<HTMLDivElement>(null);
  const { showTooltip, hideTooltip } = useTooltipDispatchContext();
  const buttonClass = StyleComposer(styles.button, {
    [styles.selected as string]: isSelected,
  });

  const handleMouseEnter = () => {
    if (buttonRef.current) {
      showTooltip({
        text: tooltipText,
        targetRef: buttonRef as RefObject<HTMLDivElement>,
        tailStyle: "left",
        renderDirection: "right",
      });
    }
  };

  const handleMouseLeave = () => {
    hideTooltip();
  };

  return (
    <div ref={buttonRef} className={styles.wrapper}>
      <Pill isHidden={isPillHidden} />
      <button
        className={buttonClass}
        onClick={onClick}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        {children}
      </button>
    </div>
  );
}

export default Button;
