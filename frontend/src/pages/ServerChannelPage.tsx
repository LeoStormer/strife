import React, { useContext, useEffect } from 'react'
import { PageNameDispatchcontext } from '../contexts/PageNameContext'
import { ServerSelectionContext } from '../contexts/ServerSelectionContext'

function ServerChannelPage() {
  const setPageName = useContext(PageNameDispatchcontext)
  const {getServer, selectedId} = useContext(ServerSelectionContext)

  useEffect(() => {
    const serverName = selectedId ? getServer(selectedId)?.name : ''
    setPageName(serverName ?? 'Server Page')
  }, [selectedId, getServer, setPageName])

  return (
    <div>ServerChannelPage</div>
  )
}

export default ServerChannelPage