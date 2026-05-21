import { render } from "@testing-library/react";
import useInertRoot from "../useInertRoot";

const TestComponent = ({ isActive, id }: { isActive: boolean; id: string }) => {
  useInertRoot(isActive, id);
  return null;
};

beforeEach(() => {
  const root = document.createElement("div");
  root.id = "root";
  document.body.appendChild(root);
});

it("should add the 'inert' attribute to the root when isActive", () => {
  const root = document.getElementById("root") as HTMLDivElement;
  render(
    // <InertRootProvider>
    <TestComponent isActive={true} id='modal-1' />,
    // </InertRootProvider>,
  );

  expect(root.hasAttribute("inert")).toBe(true);
});

it("should remove 'inert' only when all active hooks are unmounted", () => {
  const root = document.getElementById("root") as HTMLDivElement;
  const Wrapper = ({ show1, show2 }: { show1: boolean; show2: boolean }) => (
    // <InertRootProvider>
    <>
      {show1 && <TestComponent isActive={true} id='m1' />}
      {show2 && <TestComponent isActive={true} id='m2' />}
      {/* </InertRootProvider> */}
    </>
  );

  const { rerender } = render(<Wrapper show1={true} show2={true} />);
  expect(root.hasAttribute("inert")).toBe(true);

  rerender(<Wrapper show1={false} show2={true} />);
  expect(root.hasAttribute("inert")).toBe(true);

  rerender(<Wrapper show1={true} show2={false} />);
  expect(root.hasAttribute("inert")).toBe(true);

  rerender(<Wrapper show1={false} show2={false} />);
  expect(root.hasAttribute("inert")).toBe(false);
});
