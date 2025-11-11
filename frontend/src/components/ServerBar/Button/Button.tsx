import {
  useRef,
  useState,
  type MouseEventHandler,
  type PropsWithChildren,
  type RefObject,
} from "react";
import styles from "./Button.module.css";
import StyleComposer from "../../../utils/StyleComposer";
import { useTooltipDispatchContext } from "../../../contexts/TooltipContext";

type PillProps = {
  isSelected: boolean;
  isHovered: boolean;
  isHidden: boolean;
};

function Pill({ isSelected, isHovered, isHidden = false }: PillProps) {
  const className = StyleComposer(styles.pill, {
    [styles.hidden as string]: isHidden,
    [styles.hovered as string]: isHovered,
    [styles.selected as string]: isSelected,
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
  isPillHidden = false,
  children,
  onClick,
  tooltipText,
}: ButtonProps) {
  const [isHovered, setIsHovered] = useState(false);
  const buttonRef = useRef<HTMLDivElement>(null);
  const { showTooltip, hideTooltip } = useTooltipDispatchContext();
  const buttonClass = StyleComposer(styles.button, {
    [styles.selected as string]: isHovered || isSelected,
  });

  const handleMouseEnter = () => {
    setIsHovered(true);
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
    setIsHovered(false);
    hideTooltip();
  };

  return (
    <div ref={buttonRef} className={styles.wrapper}>
      <Pill
        isSelected={isSelected}
        isHovered={isHovered}
        isHidden={isPillHidden}
      />
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
