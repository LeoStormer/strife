import React, {
  type DetailedHTMLProps,
  type FC,
  type HTMLAttributes,
} from "react";
import { createPortal } from "react-dom";

const Modal: FC<
  DetailedHTMLProps<HTMLAttributes<HTMLDivElement>, HTMLDivElement>
> = (props) => {
  const modalRoot = document.getElementById("modal-root");

  if (!modalRoot) {
    throw new Error("Modal root cant be found");
  }

  return createPortal(<div {...props} />, modalRoot);
};

export default Modal;
