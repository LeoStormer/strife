import { useLayoutEffect, useRef, type RefObject } from "react";
import styles from "./Tooltip.module.css";
import Modal from "../Modal";
import StyleComposer from "../../utils/StyleComposer";

export type TailStyle = "up" | "down" | "left" | "right" | "none";

export type RenderDirection = Omit<TailStyle, "none">;

const TAIL_LENGTH = 4;

export const TAIL_STYLE_TO_DEFAULT_RENDER_DIRECTION: Record<
  TailStyle,
  RenderDirection
> = {
  up: "down",
  down: "up",
  left: "right",
  right: "left",
  none: "right",
};

const TAIL_STYLE_MAP: Record<TailStyle, string | undefined> = {
  up: styles.up,
  down: styles.down,
  left: styles.left,
  right: styles.right,
  none: undefined,
};

const getTop = (
  renderDirection: RenderDirection,
  tooltipRect: DOMRect,
  targetRect: DOMRect
) => {
  if (renderDirection === "right" || renderDirection === "left") {
    return window.scrollY + targetRect.top;
  }

  if (renderDirection === "down") {
    return window.scrollY + targetRect.bottom + TAIL_LENGTH;
  }

  // renderDirection === up
  return window.scrollY + targetRect.top - tooltipRect.height - TAIL_LENGTH;
};

const getLeft = (
  renderDirection: RenderDirection,
  tooltipRect: DOMRect,
  targetRect: DOMRect
) => {
  if (renderDirection === "right") {
    return window.scrollX + targetRect.right + TAIL_LENGTH;
  }

  if (renderDirection === "up" || renderDirection === "down") {
    return (
      window.scrollX +
      targetRect.left +
      targetRect.width / 2 -
      tooltipRect.width / 2
    );
  }

  //renderDirection === left
  return window.scrollX + targetRect.left - tooltipRect.width - TAIL_LENGTH;
};

type TooltipProps = {
  text: string;
  targetRef?: RefObject<HTMLElement>;
  tailStyle?: TailStyle;
  renderDirection?: RenderDirection;
  isVisible?: boolean;
};

function Tooltip({
  text,
  targetRef,
  tailStyle = "left",
  renderDirection = TAIL_STYLE_TO_DEFAULT_RENDER_DIRECTION[tailStyle],
  isVisible = false,
}: TooltipProps) {
  const tooltipRef = useRef<HTMLDivElement>(null);

  useLayoutEffect(() => {
    if (!(tooltipRef?.current && targetRef?.current && isVisible)) {
      return;
    }

    const targetRect = targetRef.current.getBoundingClientRect();
    const tooltipRect = tooltipRef.current.getBoundingClientRect();
    const top = getTop(renderDirection, tooltipRect, targetRect);
    const left = getLeft(renderDirection, tooltipRect, targetRect);

    tooltipRef.current.style.top = `${top}px`;
    tooltipRef.current.style.left = `${left}px`;
  }, [text, tailStyle, renderDirection, targetRef, isVisible]);

  const modalClass = StyleComposer(styles.container, {
    [styles.hidden as string]: !isVisible
  })

  const bubbleClass = StyleComposer(styles.bubble, {
    [TAIL_STYLE_MAP[tailStyle] as string]: true,
  });

  return (
    <Modal
      ref={tooltipRef}
      className={modalClass}
    >
      <div className={bubbleClass}>
        <p className={styles.content}>{text}</p>
      </div>
    </Modal>
  );
}

export default Tooltip;
