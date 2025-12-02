import React from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../Navbar'
import NavFooter from '../NavFooter'

function HomePage() {
  const navigate = useNavigate()

  return (
    <div>
      <Navbar />
      HomePage
    </div>
  )
}

export default HomePage