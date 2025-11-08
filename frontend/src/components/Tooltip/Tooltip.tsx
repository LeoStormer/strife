import { useEffect, useRef, useState, type RefObject } from 'react'
import styles from "./ToolTip.module.css"
import Modal from '../Modal'
import StyleComposer from '../../utils/StyleComposer'

type TooltipProps = {
  text: string;
  targetRef?: RefObject<HTMLElement>;
  tailStyle?: TailStyle;
}

type TailStyle = "up" | "down" | "left" | "right" |"none"

const TAIL_MAP: Record<TailStyle, string | undefined> = {
  up: styles.up,
  down: styles.down,
  left: styles.left,
  right: styles.right,
  none: undefined,
}

function Tooltip({ text, targetRef, tailStyle = 'left' }: TooltipProps) {
  const [position, setPosition] = useState({top: 0, left: 0})

  useEffect(() =>{
    if (!(targetRef && targetRef.current)) {
      return
    }

    const targetRect = targetRef.current.getBoundingClientRect();
    const top = targetRect.top
    const left = targetRect.left + targetRect.width + 4
    setPosition({top: window.screenY + top, left: window.screenX + left})
  }, [ text])

  const bubbleClass = StyleComposer(styles.bubble, {
    [TAIL_MAP[tailStyle] as string]: true,
  })

  return (
    <Modal className={styles.container} style={{position:'absolute', top: position.top, left:position.left}}>
      <div className={bubbleClass}>
        <p className={styles.content}>{text}</p>
      </div>
    </Modal>
  )
}

export default Tooltip