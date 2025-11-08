import { useEffect, useRef, useState, type RefObject } from "react";
import styles from "./ToolTip.module.css";
import Modal from "../Modal";
import StyleComposer from "../../utils/StyleComposer";

type TailStyle = "up" | "down" | "left" | "right" | "none";

type RenderDirection = Omit<TailStyle, "none">;

type TooltipProps = {
  text: string;
  targetRef?: RefObject<HTMLElement>;
  tailStyle?: TailStyle;
  direction?: RenderDirection;
};

const TAIL_LENGTH = 4;

const TAIL_STYLE_TO_DEFAULT_RENDER_DIRECTION: Record<
  TailStyle,
  RenderDirection
> = {
  up: "down",
  down: "up",
  left: "right",
  right: "left",
  none: "right",
};

const TAIL_MAP: Record<TailStyle, string | undefined> = {
  up: styles.up,
  down: styles.down,
  left: styles.left,
  right: styles.right,
  none: undefined,
};

const getTop = (
  direction: RenderDirection,
  tooltipRect: DOMRect,
  targetRect: DOMRect
) => {
  if (direction === "right" || direction === "left") {
    return window.scrollY + targetRect.top;
  }

  if (direction === "down") {
    return window.scrollY + targetRect.bottom + TAIL_LENGTH;
  }

  // direction === up
  return window.scrollY + targetRect.top - tooltipRect.height - TAIL_LENGTH;
};

const getLeft = (
  direction: RenderDirection,
  tooltipRect: DOMRect,
  targetRect: DOMRect
) => {
  if (direction === "right") {
    return window.scrollX + targetRect.right + TAIL_LENGTH;
  }

  if (direction === "up" || direction === "down") {
    return (
      window.scrollX +
      targetRect.left +
      targetRect.width / 2 -
      tooltipRect.width / 2
    );
  }

  //direction == left
  return window.scrollX + targetRect.left - tooltipRect.width - TAIL_LENGTH;
};

function Tooltip({
  text,
  targetRef,
  tailStyle = "left",
  direction = TAIL_STYLE_TO_DEFAULT_RENDER_DIRECTION[tailStyle],
}: TooltipProps) {
  const tooltipRef = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState({ top: 0, left: 0 });

  useEffect(() => {
    if (!(tooltipRef && tooltipRef.current && targetRef && targetRef.current)) {
      return;
    }

    const targetRect = targetRef.current.getBoundingClientRect();
    const tooltipRect = tooltipRef.current.getBoundingClientRect();
    const top = getTop(direction, tooltipRect, targetRect);
    const left = getLeft(direction, tooltipRect, targetRect);

    setPosition({ top: top, left: left });
  }, [text, tailStyle, direction]);

  const bubbleClass = StyleComposer(styles.bubble, {
    [TAIL_MAP[tailStyle] as string]: true,
  });

  return (
    <Modal
      ref={tooltipRef}
      className={styles.container}
      style={{ position: "absolute", top: position.top, left: position.left }}
    >
      <div className={bubbleClass}>
        <p className={styles.content}>{text}</p>
      </div>
    </Modal>
  );
}

export default Tooltip;
