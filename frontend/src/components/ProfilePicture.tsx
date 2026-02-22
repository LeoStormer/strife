import { type JSX } from "react";
import Icon, { type IconProps } from "./Icon";

type ImageProps = { profilePic: string } & JSX.IntrinsicElements["img"];
type Props =
  | ImageProps
  | ({ profilePic: "" | undefined } & Omit<IconProps, "name">);

function isImageProps(props: Props): props is ImageProps {
  return props.profilePic != undefined && props.profilePic !== "";
}

function ProfilePicture(props: Props) {
  if (isImageProps(props)) {
    const { profilePic, ...imageProps } = props;
    return <img {...imageProps} src={profilePic} />;
  } else {
    const { profilePic, ...iconProps } = props;
    return <Icon {...iconProps} name='person-circle' />;
  }
}

export default ProfilePicture;
