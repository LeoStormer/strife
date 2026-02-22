import type { JSX } from "react";

function getInitials(serverName: string) {
  return serverName
    .split(" ")
    .map((word) => word.charAt(0))
    .join("");
}

type ImageProps = {
  serverIconImage: string;
  serverName?: string | undefined;
} & Omit<JSX.IntrinsicElements["img"], "src">;

type ParagraphProps = {
  serverIconImage: "" | undefined;
  serverName: string;
} & JSX.IntrinsicElements["p"];

export type ServerIconProps = ImageProps | ParagraphProps;

function isImageProps(props: ServerIconProps): props is ImageProps {
  return props.serverIconImage != undefined && props.serverIconImage !== "";
}

function ServerIcon(props: ServerIconProps) {
  if (isImageProps(props)) {
    const { serverIconImage, serverName, ...imgProps } = props;
    return <img src={serverIconImage} {...imgProps} />;
  } else {
    const { serverIconImage, serverName, ...pProps } = props;
    return <p {...pProps}>{getInitials(serverName)}</p>;
  }
}

export default ServerIcon;
