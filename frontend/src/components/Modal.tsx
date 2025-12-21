import {
  type FC,
  type JSX,
} from "react";
import { createPortal } from "react-dom";

const Modal: FC<JSX.IntrinsicElements["div"]> = (props) => {
  const modalRoot = document.getElementById("modal-root");

  if (!modalRoot) {
    throw new Error("Modal root cant be found");
  }

  return createPortal(<div {...props} />, modalRoot);
};

export default Modal;
