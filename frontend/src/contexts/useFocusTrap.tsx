import { useLayoutEffect, useRef, type RefObject } from "react";

// Global stack to track active traps
const trapStack: HTMLElement[] = [];
const getTopMostTrap = () => trapStack[trapStack.length - 1];
const removeTrap = (element: HTMLElement) => {
  const index = trapStack.indexOf(element);
  if (index > -1) trapStack.splice(index, 1);
};

// List of common focusable elements
const FOCUSABLE_SELECTOR = [
  "a[href]",
  "button",
  "input",
  "textarea",
  "select",
  "details",
  '[tabindex]:not([tabindex="-1"])',
]
  .map((s) => `${s}:not([inert], [inert] *)`)
  .join(", ");

const getFocusables = (container: HTMLElement) =>
  Array.from(
    container.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR),
  ).filter(
    (el) =>
      el.offsetParent !== null && // Filter out display: none
      !el.hasAttribute("disabled") && // Filter out disabled buttons/inputs
      el.getAttribute("aria-hidden") !== "true", // Filter out screen-reader hidden items
  );

/**
 * A custom hook that traps keyboard focus within a specific container.
 *
 * When active, it prevents the user from tabbing out of the element,
 * looping focus back to the start or end instead.
 * If a user tries to focus outside the container, the trap will steal focus back.
 * Ideal for modals and drawers.
 *
 * @param isActive - Determines if the focus trap is currently engaged.
 * @returns A `RefObject` to be attached to the container element.
 *
 * @example
 * const containerRef = useFocusTrap(isOpen);
 * return <div ref={containerRef}>{children}</div>;
 */
const useFocusTrap = <T extends HTMLElement = HTMLDivElement>(
  isActive: boolean,
): RefObject<T> => {
  const containerRef = useRef<T | null>(null);

  useLayoutEffect(() => {
    if (!isActive) {
      return;
    }

    const handleFocusIn = (e: FocusEvent) => {
      const target = e.target as HTMLElement | null;
      const container = containerRef.current;
      if (
        !container ||
        getTopMostTrap() !== container ||
        (target && container.contains(target))
      ) {
        return;
      }

      e.preventDefault();
      e.stopImmediatePropagation();
      const focusables = getFocusables(container);
      const first = focusables[0];
      first?.focus();
    };

    document.addEventListener("focusin", handleFocusIn, true);
    return () => {
      document.removeEventListener("focusin", handleFocusIn, true);
    };
  }, [isActive]);

  useLayoutEffect(() => {
    if (!isActive || !containerRef.current) {
      return;
    }

    const handleKeyDown = (e: KeyboardEvent) => {
      const container = containerRef.current;
      if (
        !container ||
        e.key !== "Tab" ||
        // Stack Check: Only the top-most container handles the event
        getTopMostTrap() !== container
      ) {
        return;
      }

      // Stop Propagation: Prevents other global listeners from reacting to this Tab
      const handleFocus = (target?: HTMLElement) => {
        e.preventDefault();
        e.stopImmediatePropagation();
        target?.focus();
      };

      const focusables = getFocusables(container);
      if (focusables.length === 0) {
        handleFocus();
        return;
      }

      const first = focusables[0];
      const last = focusables[focusables.length - 1];

      // Shift + Tab: loop from first to last
      if (e.shiftKey && document.activeElement === first) {
        handleFocus(last);
      }
      // Tab: loop from last to first
      else if (!e.shiftKey && document.activeElement === last) {
        handleFocus(first);
      }
    };

    const container = containerRef.current;
    trapStack.push(container);

    document.addEventListener("keydown", handleKeyDown, true);
    return () => {
      document.removeEventListener("keydown", handleKeyDown, true);
      removeTrap(container);
    };
  }, [isActive, containerRef]);

  return containerRef as RefObject<T>;
};

export default useFocusTrap;
