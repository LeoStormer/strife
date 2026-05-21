import { useLayoutEffect, useRef } from "react";

/**
 * Returns focus to the element that was active before the component mounted, on unmount if active.
 * Or when `isActive` changes from true to false.
 * @param isActive Whether to return focus on unmount. Defaults to true.
 */
export function useReturnFocus(isActive: boolean = true) {
  const returnFocusRef = useRef<HTMLElement | null>(null);

  useLayoutEffect(() => {
    if (isActive) {
      // Capture the element that had focus BEFORE this became active
      if (!returnFocusRef.current) {
        returnFocusRef.current = document.activeElement as HTMLElement;
      }
    } else {
      // If we are transitioning from active to inactive, return focus
      if (
        returnFocusRef.current &&
        document.body.contains(returnFocusRef.current)
      ) {
        returnFocusRef.current.focus();
        returnFocusRef.current = null; // Reset
      }
    }
  }, [isActive]);

  // Handle actual unmounting
  useLayoutEffect(() => {
    return () => {
      if (
        isActive &&
        returnFocusRef.current &&
        document.body.contains(returnFocusRef.current)
      ) {
        returnFocusRef.current.focus();
      }
    };
  }, [isActive]);
}
