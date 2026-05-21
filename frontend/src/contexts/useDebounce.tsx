import { useEffect, useState } from "react";

const useDebounce = <T,>(value: T, delayMillis: number): T => {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timeout = setTimeout(() => setDebouncedValue(value), delayMillis);

    return () => clearTimeout(timeout);
  }, [value, delayMillis]);

  return debouncedValue;
};

export default useDebounce;
