import { useNavigate } from "react-router-dom";
import Icon from "../../../Icon";
import styles from "./AddFriendPanel.module.css";
import { SERVER_DISCOVERY_PATH } from "../../../../constants";
import { useState } from "react";

const AddFriendTab = () => {
  const navigate = useNavigate();
  const [emailInput, setEmailInput] = useState("");
  return (
    <div role='tabpanel' id='add-friend-panel'>
      <section className={styles.sectionBlock}>
        <h3>Add Friend</h3>
        <form onSubmit={(e) => e.preventDefault()}>
          <p>You can add friends with their email.</p>
          <div className={styles.inputWrapper}>
            <label htmlFor='email' className='sr-only'>
              Friend's Email
            </label>
            <input
              type='email'
              id='email'
              placeholder='Enter email address...'
              value={emailInput}
              required
              onChange={(e) => {
                setEmailInput(e.currentTarget.value.trim());
              }}
            />
            <button type='submit' disabled={emailInput.length === 0}>
              Send Friend Request
            </button>
          </div>
        </form>
      </section>
      <section className={styles.sectionBlock}>
        <h3>Other Places to Make Friends</h3>
        <p>Dont have an email on hand? Check our list of public servers.</p>
        <button
          className={styles.discoverButton}
          onClick={() => navigate(SERVER_DISCOVERY_PATH)}
        >
          <div className={styles.discoverIcon}>
            <Icon name='discover' />
          </div>
          <span>Explore Discoverable Servers</span>
          <Icon name='chevron-right' />
        </button>
      </section>
    </div>
  );
};

export default AddFriendTab;
