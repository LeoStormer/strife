import React from "react";

function getInitials(serverName: string) {
  return serverName
    .split(" ")
    .map((word) => word.charAt(0))
    .join("");
}

type ServerIconProps = {
  serverIconImage?: string;
  serverName: string;
};

function ServerIcon({ serverIconImage, serverName }: ServerIconProps) {
  const icon =
    serverIconImage == null ? (
      <img src={serverIconImage} alt=''></img>
    ) : (
      <label>{getInitials(serverName)}</label>
    );
  return <>{icon}</>;
}

export default ServerIcon;
