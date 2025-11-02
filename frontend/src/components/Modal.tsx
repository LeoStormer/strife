import React, { type PropsWithChildren } from "react";
import { createPortal } from "react-dom";

function Modal({ children }: PropsWithChildren) {
  const modalRoot = document.getElementById("modal-root");

  if (!modalRoot) {
    throw new Error("Modal root cant be found");
  }

  return createPortal(
    <div className='modal-wrapper'>{children}</div>,
    modalRoot
  );
}

export default Modal;
