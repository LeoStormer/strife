import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useReturnFocus } from "../useReturnFocus";

const TestComponent = ({ isActive }: { isActive: boolean }) => {
  useReturnFocus(isActive);
  return (
    <div>
      <button id='other' data-testid='other'>
        Other Button
      </button>
    </div>
  );
};

beforeEach(() => {
  const trigger = document.createElement("button");
  trigger.id = "trigger";
  document.body.appendChild(trigger);

  const sibling = document.createElement("button");
  sibling.id = "sibling";
  document.body.appendChild(sibling);
});

it("should return focus to the trigger element on unmount", async () => {
  const user = userEvent.setup();
  const trigger = document.getElementById("trigger") as HTMLButtonElement;
  await user.click(trigger);

  expect(trigger).toHaveFocus();

  const { unmount } = render(<TestComponent isActive={true} />);

  const internalButton = screen.getByTestId("other");
  await user.click(internalButton);

  expect(internalButton).toHaveFocus();

  unmount();

  expect(trigger).toHaveFocus();
});

it("should return focus to the trigger element when isActive changes from true to false", async () => {
  const user = userEvent.setup();
  const trigger = document.getElementById("trigger") as HTMLButtonElement;
  await user.click(trigger);

  expect(trigger).toHaveFocus();

  const { rerender } = render(<TestComponent isActive={true} />);

  const internalButton = screen.getByTestId("other");
  await user.click(internalButton);

  expect(internalButton).toHaveFocus();

  rerender(<TestComponent isActive={false} />);

  expect(trigger).toHaveFocus();
});

it("should not return focus if not active", async () => {
  const user = userEvent.setup();
  const trigger = document.getElementById("trigger") as HTMLButtonElement;
  await user.click(trigger);

  const sibling = document.getElementById("sibling") as HTMLButtonElement;
  expect(trigger).toHaveFocus();

  const { unmount } = render(<TestComponent isActive={false} />);
  const internalButton = screen.getByTestId("other");
  await user.click(internalButton);

  expect(internalButton).toHaveFocus();

  await user.click(sibling);
  expect(sibling).toHaveFocus();

  unmount();

  await new Promise((r) => setTimeout(r, 0));
  expect(sibling).toHaveFocus();
});

it("should return focus through multiple levels of nesting", async () => {
  const NestedComponent = ({ isChildActive }: { isChildActive: boolean }) => {
    useReturnFocus(true);
    return (
      <div>
        <button data-testid='parent'>Nested Button</button>
        {isChildActive && <TestComponent isActive={true} />}
      </div>
    );
  };

  const user = userEvent.setup();
  const trigger = document.getElementById("trigger") as HTMLButtonElement;
  await user.click(trigger);

  // Render Parent
  const { rerender, unmount } = render(
    <NestedComponent isChildActive={false} />,
  );
  const parentButton = screen.getByTestId("parent");
  await user.click(parentButton);

  rerender(<NestedComponent isChildActive={true} />); // Render Child -> focus should go to child
  const childButton = screen.getByTestId("other");
  await user.click(childButton);
  expect(childButton).toHaveFocus();

  rerender(<NestedComponent isChildActive={false} />); // Unrender Child -> focus should return to parent
  await waitFor(() => expect(parentButton).toHaveFocus());

  unmount(); // Parent closes -> focus should go back to trigger
  await waitFor(() => expect(trigger).toHaveFocus());
});
