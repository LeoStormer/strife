import { useEffect, useRef, useState, type KeyboardEvent } from "react";
import styles from "./ChannelViewer.module.css";
import Icon from "../Icon";
import type { ThreePanelContentGridProps } from "../ThreePanelContentGrid";
import ThreePanelContentGrid from "../ThreePanelContentGrid";

type MessageListProps = {
  messages: string[];
};

const MessageList = ({ messages }: MessageListProps) => {
  return <div className={styles.messageList}>Messages</div>;
};

type MessageInputProps = {
  sendMessage: (message: string) => Promise<void>;
  channelName: string;
  disabled?: boolean;
  disabledPlaceholder?: string | undefined;
};

const MessageInput = ({
  sendMessage,
  channelName,
  disabled = false,
  disabledPlaceholder = "You do not have permission to send messages in this channel.",
}: MessageInputProps) => {
  const [text, setText] = useState("");
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (!textareaRef.current) {
      return;
    }

    textareaRef.current.style.height = "0px";
    const scrollHeight = textareaRef.current.scrollHeight;
    textareaRef.current.style.height = `${scrollHeight}px`;
  }, [text]);

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (disabled || e.key !== "Enter" || e.shiftKey) {
      return;
    }

    e.preventDefault();
    const trimmedText = text.trim();
    if (trimmedText) {
      sendMessage(trimmedText);
      setText("");
    }
  };

  return (
    <div className={styles.inputContainer}>
      {disabled ? null : (
        <button className={styles.iconButton}>
          <Icon name='plus-lg' />
        </button>
      )}
      <textarea
        ref={textareaRef}
        className={styles.textarea}
        rows={1}
        disabled={disabled}
        value={disabled ? "" : text}
        placeholder={disabled ? disabledPlaceholder : `Message ${channelName}`}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={handleKeyDown}
      />
      <div>s</div>
    </div>
  );
};

type Props = MessageListProps &
  MessageInputProps &
  Omit<ThreePanelContentGridProps, "children">;

function ChannelViewer({
  messages,
  sendMessage,
  channelName,
  disabled = false,
  disabledPlaceholder,
  isSidePanelOpen = true,
  ...contentGridProps
}: Props) {
  return (
    <ThreePanelContentGrid
      {...contentGridProps}
      isSidePanelOpen={isSidePanelOpen}
    >
      <MessageList messages={messages} />
      <MessageInput
        disabled={disabled}
        channelName={channelName}
        sendMessage={sendMessage}
        disabledPlaceholder={disabledPlaceholder}
      />
    </ThreePanelContentGrid>
  );
}

export default ChannelViewer;
