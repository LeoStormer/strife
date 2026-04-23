import userEvent from "@testing-library/user-event";
import Modal, { type ModalProps } from "./Modal";
import { render, screen, waitFor } from "@testing-library/react";

beforeEach(() => {
  const triggerButton = document.createElement("button");
  triggerButton.id = "trigger";
  document.body.appendChild(triggerButton);

  const root = document.createElement("div");
  root.id = "root";
  document.body.appendChild(root);

  const modalRoot = document.createElement("div");
  modalRoot.id = "modal-root";
  document.body.appendChild(modalRoot);
});

describe("Closing Modal", () => {
  it("should close when the close button is clicked", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(<Modal onClose={onClose} />);
    const closeButton = screen.getByRole("button", { name: /close modal/i });

    await user.click(closeButton);

    expect(onClose).toHaveBeenCalledOnce();
  });

  it("closes only when clicking directly on the backdrop", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
      <Modal isOpen={true} onClose={onClose}>
        <button data-testid='content' />
      </Modal>,
    );

    const backdrop = screen.getByRole("dialog").parentElement as HTMLElement;
    const content = screen.getByTestId("content");

    await user.click(content);
    expect(onClose).not.toHaveBeenCalled();

    await user.pointer([
      { target: content, keys: "[MouseLeft>]" },
      { target: backdrop, keys: "[/MouseLeft]" },
    ]);
    expect(onClose).not.toHaveBeenCalled();

    await user.pointer([
      { target: backdrop, keys: "[MouseLeft>]" },
      { target: content, keys: "[/MouseLeft]" },
    ]);
    expect(onClose).not.toHaveBeenCalled();

    await user.click(backdrop);
    expect(onClose).toHaveBeenCalled();
  });

  it("closes only the top-most modal on Escape key", async () => {
    const user = userEvent.setup();
    const onCloseFirst = vi.fn();
    const onCloseSecond = vi.fn();

    render(
      <>
        <Modal isOpen={true} onClose={onCloseFirst} />
        <Modal isOpen={true} onClose={onCloseSecond} />
      </>,
    );

    await user.keyboard("{Escape}");

    expect(onCloseSecond).toHaveBeenCalledOnce();
    expect(onCloseFirst).not.toHaveBeenCalled();
  });
});

describe("Root disabling", () => {
  it("should make the root element inert when open", () => {
    const user = userEvent.setup();
    render(<Modal isOpen={true} />);
    const root = document.getElementById("root");
    expect(root).toHaveAttribute("inert");
  });

  it("should remove the inert attribute from the root element when all modals are closed", () => {
    const root = document.getElementById("root");
    expect(root).not.toHaveAttribute("inert");

    const { rerender } = render(
      <>
        <Modal isOpen={true} />
        <Modal isOpen={true} />
      </>,
    );

    expect(root).toHaveAttribute("inert");

    rerender(
      <>
        <Modal isOpen={false} />
        <Modal isOpen={true} />
      </>,
    );
    expect(root).toHaveAttribute("inert");

    rerender(
      <>
        <Modal isOpen={false} />
        <Modal isOpen={false} />
      </>,
    );
    expect(root).not.toHaveAttribute("inert");
  });
});

describe("Focus Management", () => {
  it("should return focus to the trigger when closed", async () => {
    const user = userEvent.setup();
    const triggerButton = document.getElementById("trigger");

    await user.click(triggerButton!);
    expect(triggerButton).toHaveFocus();

    const { rerender } = render(
      <Modal isOpen={true} onClose={() => rerender(<Modal isOpen={false} />)}>
        <button data-testid='inside'>Inside Button</button>
      </Modal>,
    );

    const closeButton = screen.getByRole("button", { name: /close modal/i });
    const insideButton = screen.getByTestId("inside");

    await user.click(insideButton);
    expect(insideButton).toHaveFocus();

    await user.click(closeButton);
    await waitFor(() => expect(triggerButton).toHaveFocus());
  });

  it("should return focus to the next modal in the stack when the top modal is closed", async () => {
    const user = userEvent.setup();

    const triggerButton = document.getElementById("trigger");
    await user.click(triggerButton!);
    expect(triggerButton).toHaveFocus();

    const { rerender } = render(
      <Modal isOpen={true}>
        <button data-testid='first'>First Modal Button</button>
        <Modal isOpen={false}>
          <button data-testid='second'>Second Modal Button</button>
        </Modal>
      </Modal>,
    );

    const firstModalButton = screen.getByTestId("first");
    await user.click(firstModalButton);
    expect(firstModalButton).toHaveFocus();

    rerender(
      <Modal isOpen={true}>
        <button data-testid='first'>First Modal Button</button>
        <Modal isOpen={true}>
          <button data-testid='second'>Second Modal Button</button>
        </Modal>
      </Modal>,
    );

    const secondModalButton = screen.getByTestId("second");
    await user.click(secondModalButton);
    expect(secondModalButton).toHaveFocus();

    rerender(
      <Modal isOpen={true}>
        <button data-testid='first'>First Modal Button</button>
        <Modal isOpen={false}>
          <button data-testid='second'>Second Modal Button</button>
        </Modal>
      </Modal>,
    );
    await waitFor(() => expect(firstModalButton).toHaveFocus());
  });

  it("should trap focus within the modal when open", async () => {
    const user = userEvent.setup();
    render(
      <Modal isOpen={true}>
        <button data-testid='first'>First Modal Button</button>
      </Modal>,
    );

    const firstModalButton = screen.getByTestId("first");
    const closeButton = screen.getByRole("button", { name: /close modal/i });
    await user.click(firstModalButton);
    expect(firstModalButton).toHaveFocus();

    await user.tab();
    expect(closeButton).toHaveFocus();
    await user.tab();
    expect(firstModalButton).toHaveFocus();

    const triggerButton = document.getElementById("trigger");
    await user.click(triggerButton!);
    await waitFor(() => expect(firstModalButton).toHaveFocus());
  });
});
