import type { DetailedHTMLProps, HTMLAttributes } from "react";

function getInitials(serverName: string) {
  return serverName
    .split(" ")
    .map((word) => word.charAt(0))
    .join("");
}

export type ServerIconProps = DetailedHTMLProps<
  HTMLAttributes<HTMLElement>,
  HTMLElement
> & {
  serverIconImage?: string | undefined;
  serverName: string;
};

function ServerIcon({
  serverIconImage,
  serverName,
  ...props
}: ServerIconProps) {
  const icon = serverIconImage ? (
    <img
      src={serverIconImage}
      alt=''
      {...(props as DetailedHTMLProps<
        HTMLAttributes<HTMLImageElement>,
        HTMLImageElement
      >)}
    ></img>
  ) : (
    <p
      {...(props as DetailedHTMLProps<
        HTMLAttributes<HTMLParagraphElement>,
        HTMLParagraphElement
      >)}
    >
      {getInitials(serverName)}
    </p>
  );
  return <>{icon}</>;
}

export default ServerIcon;
