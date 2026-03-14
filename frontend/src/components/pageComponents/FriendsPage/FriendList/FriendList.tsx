import React from 'react'
import styles from "./FriendList.module.css"

type Props = {
  activeFilter: string
}

function FriendList({ activeFilter } : Props) {
  return (
    <div role='tabpanel' id={`${activeFilter} Friends List`}>FriendList</div>
  )
}

export default FriendList