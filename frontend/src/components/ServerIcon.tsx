function getInitials(serverName: string) {
  return serverName
    .split(" ")
    .map((word) => word.charAt(0))
    .join("");
}

type ServerIconProps = {
  serverIconImage: string | undefined;
  serverName: string;
};

function ServerIcon({ serverIconImage, serverName }: ServerIconProps) {
  const icon = serverIconImage ? (
    <img src={serverIconImage} alt=''></img>
  ) : (
    <label>{getInitials(serverName)}</label>
  );
  return <>{icon}</>;
}

export default ServerIcon;
