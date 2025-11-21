import React, { useContext, useEffect } from 'react'
import { PageNameDispatchContext } from '../../../contexts/PageNameContext'
import styles from "./FriendsPage.module.css"
import FriendList from './FriendList'
import ActivityFeed from './ActivityFeed'
import FriendFilterBar from './FriendFilterBar'

function FriendsPage() {
  const setPageName = useContext(PageNameDispatchContext)

  useEffect(() => {
    setPageName("Friends")
  }, [setPageName])
  
  return (
    <div className={styles.container}>
      <FriendFilterBar />
      <FriendList />
      <ActivityFeed />
    </div>
  )
}

export default FriendsPage