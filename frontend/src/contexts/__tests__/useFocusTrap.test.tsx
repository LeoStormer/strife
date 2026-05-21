import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import useFocusTrap from "../useFocusTrap";
import type { PropsWithChildren } from "react";

const TestComponent = ({
  isActive,
  id,
  children,
}: PropsWithChildren<{
  isActive: boolean;
  id?: string | undefined;
}>) => {
  const ref = useFocusTrap(isActive);
  return (
    <div ref={ref}>
      <button data-testid='first'>{"First" + (id ? ` (${id})` : "")}</button>
      <button data-testid='last'>{"Last" + (id ? ` (${id})` : "")}</button>
      {children}
    </div>
  );
};

describe("Basic Behavior", () => {
  it("should loop to the first element when tabbing from the last element", async () => {
    const user = userEvent.setup();
    render(<TestComponent isActive={true} />);

    const first = screen.getByTestId("first");
    const last = screen.getByTestId("last");

    last.focus();
    await user.tab(); // Focus loops back to First
    expect(document.activeElement).toBe(first);
  });

  it("should loop to the last element when shift-tabbing from the first element", async () => {
    const user = userEvent.setup();
    render(<TestComponent isActive={true} />);

    const first = screen.getByTestId("first");
    const last = screen.getByTestId("last");

    first.focus();
    await user.tab({ shift: true }); // Shift+Tab loops to Last
    expect(document.activeElement).toBe(last);
  });

  it("should steal focus back if user tries to focus outside the container", async () => {
    const user = userEvent.setup();
    render(
      <>
        <TestComponent isActive={true} />
        <button data-testid='outside'>Outside Button</button>
      </>,
    );
    const outsideButton = screen.getByTestId("outside");
    outsideButton.focus();
    await waitFor(() => expect(document.activeElement).toBe(screen.getByTestId("first")));
});
  it("should allow focus to escape when not active", async () => {
    const user = userEvent.setup();
    render(<TestComponent isActive={false} />);

    const first = screen.getByTestId("first");
    const last = screen.getByTestId("last");

    first.focus();
    await user.tab(); // Focus moves to Last
    expect(document.activeElement).toBe(last);
    await user.tab(); // Focus moves to Background Button
    expect(document.activeElement).toBe(document.body);
  });
});

describe("Multiple Trap Behavior", () => {
  beforeEach(() => {
    const button = document.createElement("button");
    button.id = "external";
    document.body.appendChild(button);
  });

  it("focus should be trapped within latest active trap", async () => {
    const user = userEvent.setup();
    render(
      <>
        <TestComponent isActive={true} id='bottom' />
        <TestComponent isActive={true} id='top' />
      </>,
    );

    const externalButton = document.getElementById("external") as HTMLButtonElement;
    const [bottomFirst, topFirst] = screen.getAllByTestId("first");
    const [_bottomLast, topLast] = screen.getAllByTestId("last");
    // Focus enters the latest trap
    topFirst!.focus();

    await user.tab({ shift: true }); // Focus loops to topLast
    expect(topLast).toHaveFocus();
    await user.tab(); // Focus loops back to topFirst
    expect(topFirst).toHaveFocus();

    // Focus enters a trap.
    await user.click(bottomFirst!);
    // latest trap should take precedence and steal focus back if user tries to focus outside of it
    await waitFor(() => expect(topFirst).toHaveFocus());

    // focus enters a 
    await user.click(externalButton);
    await waitFor(() => expect(topFirst).toHaveFocus());
  });

  it("should allow focus to escape when all traps are removed/disabled", async () => {
    render(
      <>
        <TestComponent isActive={false} id='bottom' />
        <TestComponent isActive={false} id='top' />
      </>,
    );

    const [_bottomFirst, _topFirst] = screen.getAllByTestId("first");
    const [_bottomLast, topLast] = screen.getAllByTestId("last");
    topLast!.focus();
    await userEvent.tab(); // Focus should move to body
    expect(document.activeElement).toBe(document.body);
  });
});

