import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach } from 'vitest';

// Automatically cleanup after each test
afterEach(() => {
  cleanup();
});

// Mock offsetParent since JSDOM lacks a layout engine
Object.defineProperty(HTMLElement.prototype, 'offsetParent', {
  get() { return this.parentNode; },
});

global.ResizeObserver = class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}
