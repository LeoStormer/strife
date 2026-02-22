import { useRef, type JSX, type MouseEventHandler } from "react";
import Portal from "../Portal";
import styles from "./Modal.module.css";
import StyleComposer from "../../utils/StyleComposer";
import Icon from "../Icon";

export type ModalProps = JSX.IntrinsicElements["div"] & {
  isOpen?: boolean | undefined;
  onClose?: VoidFunction;
};

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

  return (
    <Portal
      className={styles.backdrop}
      onMouseDown={handleMouseDown}
      onMouseUp={handleMouseUp}
      inert={!isOpen}
    >
      <div
        {...containerProps}
        className={containerClassName}
        onMouseDown={(e) => {
          e.stopPropagation();
          containerProps?.onMouseDown?.(e);
        }}
        onMouseUp={(e) => {
          e.stopPropagation();
          containerProps?.onMouseUp?.(e);
        }}
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
