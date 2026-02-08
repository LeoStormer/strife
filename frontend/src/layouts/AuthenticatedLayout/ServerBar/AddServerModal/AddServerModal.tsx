import { useState, type JSX, type MouseEventHandler } from "react";
import { useForm, type SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import styles from "./AddServerModal.module.css";
import Modal from "../../../../components/Modal";
import api from "../../../../api";
import type { ModalProps } from "../../../../components/Modal/Modal";
import { Link } from "react-router-dom";
import { SERVER_DISCOVERY_PATH } from "../../../../constants";
import Icon from "../../../../components/Icon";

function Header({ title, subheader }: { title: string; subheader: string }) {
  return (
    <header className={styles.header}>
      <h2 className={styles.headerTitle}>{title}</h2>
      <p className={styles.subheader}>{subheader} </p>
    </header>
  );
}

const serverCreationSchema = z.object({
  serverName: z.string().max(100).nonempty("This field is required"),
});

type ServerCreationForm = z.infer<typeof serverCreationSchema>;

type SwappableFormProps = Pick<JSX.IntrinsicElements["form"], "inert"> & {
  swapForm: VoidFunction;
  closeModal?: VoidFunction;
};

function ServerCreationForm({ swapForm, closeModal, ...formProps }: SwappableFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ServerCreationForm>({
    resolver: zodResolver(serverCreationSchema),
  });

  const onSubmit: SubmitHandler<ServerCreationForm> = (data) => {
    console.log(data);

    api
    .post(`/api/server?serverName=${data.serverName}`)
    .then((server) => {
      console.log("Server created successfully", server);
      closeModal?.();
    })
    .catch((err) => {
      console.log(err);
    });
  };

  return (
    <form
      className={styles.form}
      onSubmit={handleSubmit(onSubmit)}
      {...formProps}
    >
      <Header
        title='Create Your Server'
        subheader={
          "Your server is where you and your friends hang out." +
          " Make yours and start talking."
        }
      />
      <label htmlFor='serverName'>Server Name</label>
      <input type='text' {...register("serverName")} />
      {errors.serverName ? errors.serverName.message : null}
      <button type='submit' className={styles.button}>
        {isSubmitting ? "Creating Server..." : "Create Server"}
      </button>
      <h3>Have an invite already?</h3>
      <button type='button' className={styles.button} onClick={swapForm}>
        Join A Server
      </button>
    </form>
  );
}

const inviteRegex =
  /^(?:https:\/\/strife\.com\/)?([a-f0-9]{24}|[a-z0-9-]{1,25})$/;
const joinServerSchema = z.object({
  invite: z
    .string()
    .nonempty("This field is required")
    .refine(
      (s) => {
        return inviteRegex.test(s);
      },
      { error: "Please enter a valid invite" },
    ),
});

type JoinServerForm = z.infer<typeof joinServerSchema>;

function JoinServerForm({
  swapForm,
  closeModal,
  ...formProps
}: SwappableFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<JoinServerForm>({
    resolver: zodResolver(joinServerSchema),
  });

  const onSubmit: SubmitHandler<JoinServerForm> = (data) => {
    const inviteId = data.invite.match(inviteRegex)?.[1];
    console.log(inviteId);

    api
      .post(`/api/server/join-by-invite?inviteId=${inviteId}`)
      .then(() => {
        console.log("Server joined successfully");
        closeModal?.();
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <form
      className={styles.form}
      onSubmit={handleSubmit(onSubmit)}
      {...formProps}
    >
      <Header
        title='Join a Server'
        subheader='Enter an invite below to join an existing server'
      />
      <label htmlFor='invite'>Invite link</label>
      <input type='text' {...register("invite")} />
      {errors.invite ? errors.invite.message : null}
      <Link
        className={styles.link}
        to={SERVER_DISCOVERY_PATH}
        onClick={closeModal}
        draggable={false}
      >
        <div className={styles.discoverIcon}>
          <Icon name='discover' />
        </div>
        <h3>Dont have an invite?</h3>
        <p>Check out discoverable communities in Server Discovery</p>
        <Icon name='chevron-right' className={styles.chevronIcon} />
      </Link>
      <div className={styles.spaceBetween}>
        <button type='button' className={styles.backButton} onClick={swapForm}>
          Back
        </button>
        <button type='submit' className={styles.joinButton}>
          {isSubmitting ? "Joining..." : "Join Server"}
        </button>
      </div>
    </form>
  );
}

function AddServerModal({
  isOpen,
  onClose,
}: Pick<ModalProps, "isOpen" | "onClose">) {
  const [activeForm, setActiveForm] = useState<"create" | "join">("create");

  const closeModal = () => {
    onClose?.();
    setTimeout(() => setActiveForm("create"), 200);
  };

  return (
    <Modal
      id={styles.addServerModalContainer}
      data-selected-form={activeForm}
      isOpen={isOpen}
      onClose={closeModal}
    >
      <ServerCreationForm
        swapForm={() => setActiveForm("join")}
        closeModal={closeModal}
        inert={activeForm === "join"}
      />
      <JoinServerForm
        swapForm={() => setActiveForm("create")}
        closeModal={closeModal}
        inert={activeForm === "create"}
      />
    </Modal>
  );
}

export default AddServerModal;
