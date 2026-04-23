import {
  useEffect,
  useId,
  useRef,
  type JSX,
  type MouseEventHandler,
} from "react";
import Portal from "../Portal";
import styles from "./Modal.module.css";
import StyleComposer from "../../utils/StyleComposer";
import Icon from "../Icon";
import useInertRoot from "../../contexts/useInertRoot";
import useFocusTrap from "../../contexts/useFocusTrap";
import { useReturnFocus } from "../../contexts/useReturnFocus";

export type ModalProps = Exclude<
  JSX.IntrinsicElements["div"],
  "aria-modal" | "role"
> & {
  isOpen?: boolean | undefined;
  onClose?: VoidFunction;
};

const modalStackStore = (() => {
  const stack: { id: string; close: VoidFunction }[] = [];

  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === "Escape") {
      // Logic is centralized: only the top-most modal's close() is called
      const topMost = stack[stack.length - 1];
      topMost?.close();
    }
  };

  return {
    push(id: string, close: VoidFunction) {
      if (stack.some((item) => item.id === id)) {
        return;
      }

      if (stack.length === 0) {
        document.addEventListener("keydown", handleKeyDown);
      }

      stack.push({ id, close });
    },

    pop(id: string) {
      const index = stack.findIndex((item) => item.id === id);
      if (index > -1) {
        stack.splice(index, 1);
      }

      if (stack.length === 0) {
        document.removeEventListener("keydown", handleKeyDown);
      }
    },
  };
})();

function useModalKeyboard(
  isOpen: boolean,
  id: string,
  onClose: VoidFunction = () => {},
) {
  useEffect(() => {
    if (!isOpen) return;

    modalStackStore.push(id, onClose);

    return () => {
      modalStackStore.pop(id);
    };
  }, [isOpen, id, onClose]);
}

function Modal({
  isOpen = true,
  onClose,
  className,
  children,
  ...containerProps
}: ModalProps) {
  const mouseDownOnBackdrop = useRef(false);

  const handleMouseDown: MouseEventHandler<HTMLDivElement> = (e) => {
    mouseDownOnBackdrop.current = e.target === e.currentTarget;
  };

  const handleMouseUp: MouseEventHandler<HTMLDivElement> = (e) => {
    if (mouseDownOnBackdrop.current && e.target === e.currentTarget) {
      onClose?.();
    }
    mouseDownOnBackdrop.current = false;
  };

  const containerClassName = StyleComposer(styles.container, {
    [className as string]: true,
    [styles.closed as string]: !isOpen,
  });

  const trapId = useId();
  useInertRoot(isOpen, trapId);
  const ref = useFocusTrap(isOpen);
  useModalKeyboard(isOpen, trapId, onClose);
  useReturnFocus(isOpen);

  return (
    <Portal
      className={styles.backdrop}
      onMouseDown={handleMouseDown}
      onMouseUp={handleMouseUp}
      inert={!isOpen}
    >
      <div
        {...containerProps}
        ref={ref}
        className={containerClassName}
        onMouseDown={(e) => {
          e.stopPropagation();
          containerProps?.onMouseDown?.(e);
        }}
        onMouseUp={(e) => {
          e.stopPropagation();
          containerProps?.onMouseUp?.(e);
        }}
        role='dialog'
        aria-modal='true'
      >
        {children}
        <button
          className={styles.closeButton}
          onClick={onClose}
          aria-label='Close Modal'
        >
          <Icon name='close' />
        </button>
      </div>
    </Portal>
  );
}

export default Modal;
