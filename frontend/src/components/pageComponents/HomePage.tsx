import React from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../Navbar'
import NavFooter from '../NavFooter'

function HomePage() {
  return (
    <div>
      <Navbar />
      HomePage
      <NavFooter />
    </div>
  )
}

export default HomePage