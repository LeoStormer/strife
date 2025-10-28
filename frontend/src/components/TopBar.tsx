import React, { useContext } from 'react'
import PageNameContext from '../contexts/PageNameContext';

function TopBar() {
  const pageName = useContext(PageNameContext)

  return (
    <div>{`Page Name = ${pageName}`}</div>
  )
}

export default TopBar