import type { JSX } from "react";
import type { IconName } from "../../../types/name";

export type IconProps = JSX.IntrinsicElements["svg"] & {
  name: IconName;
};

function Icon({ name, ...svgProps }: IconProps) {
  const href = `/icons/sprite.svg#${name}`;
  return (
    <svg {...svgProps}>
      <use href={href} />
    </svg>
  );
}

export default Icon;
