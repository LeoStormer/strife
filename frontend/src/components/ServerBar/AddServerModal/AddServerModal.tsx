import { useState, type MouseEventHandler } from "react";
import styles from "./AddServerModal.module.css";
import Modal from "../../Modal";

const templates = [
  "Gaming",
  "Friends",
  "Study Group",
  "School Club",
  "Local Community",
  "Artists & Creators",
] as const;

type ServerCreationOptions = {
  serverName: string;
  chosenTemplate: (typeof templates)[number] | "None";
  expectedSize: "small" | "large";
};

function Header({ title, subheader }: { title: string; subheader: string }) {
  return (
    <div className={styles.header}>
      <h2 className={styles.headerTitle}>{title}</h2>
      <p className={styles.subheader}>{subheader} </p>
    </div>
  );
}

function LandingContent() {
  return (
    <>
      <Header
        title='Create Your Server'
        subheader={
          "Your server is where you and your friends hang out." +
          " Make yours and start talking."
        }
      />
      <div className={styles.scrollable}>
        <button className={styles.button}>Create My Own</button>
        <h4 className={styles.subheader}>Start from a template</h4>
        {templates.map((template: string) => (
          <button className={styles.button}>{template}</button>
        ))}
      </div>
      <h3>Have an invite already?</h3>
      <button className={styles.button}>Join A Server</button>
    </>
  );
}

function JoinServerContent() {
  return (
    <>
      <Header
        title='Join a Server'
        subheader='Enter an invite below to join an existing server'
      />
      <label>Invite link</label>
      <input type='text' required />
      <p>Invites should look like</p>
      <span>example</span>
      <span>invite link example</span>
      <span>custom invite link example</span>
      <button>
        <h3>Dont have an invite?</h3>
        <p>Check out discoverable communities in server discovery</p>
      </button>
      <button>Back</button> <button>Join Server</button>
    </>
  );
}

function AddServerModal({
  deselectButton,
}: {
  deselectButton: MouseEventHandler;
}) {
  // TODO: Look up how to make multi-stage forms
  return (
    <Modal className={styles.background} onClick={deselectButton}>
      <div className={styles.container} onClick={(e) => e.stopPropagation()}>
        <LandingContent />
      </div>
    </Modal>
  );
}

export default AddServerModal;
