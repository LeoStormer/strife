import {type SVGProps} from 'react'
import type { IconName } from '../../../types/name'

export type IconProps = SVGProps<SVGSVGElement> & {
  name: IconName,
}

function Icon({ name, ...svgProps }: IconProps) {
  const href = `/icons/sprite.svg#${name}` 
  return (
    <svg {...svgProps}>
      <use href={href}/>
    </svg>
  )
}

export default Icon