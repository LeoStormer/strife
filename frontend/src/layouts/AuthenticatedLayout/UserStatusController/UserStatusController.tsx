import styles from "./UserStatusController.module.css";
import Icon from "../../../components/Icon";
import type { IconName } from "../../../../types/name";
import ProfilePicture from "../../../components/ProfilePicture";
import { useUserContext, type User } from "../../../contexts/UserContext";
import TooltipTrigger from "../../../components/TooltipTrigger";

type Options = {
  tooltipText: string;
};

type SettingsButtonProps = {
  options: Options | null;
  icon: IconName;
  tooltipText: string;
};

const SettingsButton = ({
  icon,
  options,
  tooltipText,
}: SettingsButtonProps) => {
  const buttonTriggerProps = TooltipTrigger<HTMLButtonElement>({
    tooltipText,
    tailStyle: "down",
  });
  const optionsTriggerProps = options
    ? TooltipTrigger<HTMLButtonElement>({
        tooltipText: options.tooltipText,
        tailStyle: "down",
      })
    : null;

  return (
    <div className={styles.controlWrapper}>
      <button
        {...buttonTriggerProps.getAllProps()}
        className={`${styles.button} ${styles.toggle}`}
      >
        <Icon name={icon} />
      </button>
      {options ? (
        <button
          {...optionsTriggerProps?.getAllProps()}
          className={`${styles.button} ${styles.options}`}
        >
          <Icon name='chevron-down' />
        </button>
      ) : null}
    </div>
  );
};

function UserStatusController() {
  const { user } = useUserContext();
  const { profilePic, username } = user as User;

  return (
    <div className={styles.container}>
      <button className={`${styles.button} ${styles.statusContainer}`}>
        <ProfilePicture
          profilePic={profilePic}
          className={styles.profilePicture}
        />
        <div className={styles.labelContainer}>
          <strong className={styles.username}>{username}</strong>
          <span className={styles.status}>idle</span>
        </div>
      </button>
      <div className={styles.controlsFlexContainer}>
        <SettingsButton
          tooltipText='Mute'
          icon='mic-fill'
          options={{ tooltipText: "Input Options" }}
        />
        <SettingsButton
          tooltipText='Deafen'
          icon='headphones'
          options={{ tooltipText: "Output Options" }}
        />
        <SettingsButton
          tooltipText='Settings'
          icon='gear-fill'
          options={null}
        />
      </div>
    </div>
  );
}

export default UserStatusController;
