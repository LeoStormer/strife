import { useLayoutEffect, useRef } from "react";

/**
 * Returns focus to the element that was active before the component mounted.
 * @param isActive Whether the component is currently active/visible.
 */
export function useReturnFocus(isActive: boolean) {
  const returnFocusRef = useRef<HTMLElement | null>(null);

  useLayoutEffect(() => {
    if (isActive) {
      returnFocusRef.current = document.activeElement as HTMLElement;
    } else {
      const timer = setTimeout(() => {
        if (
          returnFocusRef.current &&
          document.body.contains(returnFocusRef.current)
        ) {
          returnFocusRef.current.focus();
        }
        returnFocusRef.current = null;
      }, 0);

      return () => clearTimeout(timer);
    }
  }, [isActive]);
}
