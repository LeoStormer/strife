import { useCallback } from 'react'

const useFocusOnMount = () => {
  const setFocusRef = useCallback((node: HTMLElement | null) => {
    if (node) {
      node.focus();
    }
  }, []);

  return setFocusRef;
}

export default useFocusOnMount