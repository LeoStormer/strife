import {type SVGProps} from 'react'
import type { IconName } from '../../../types/name'

type IconProps = SVGProps<SVGSVGElement> & {
  name: IconName,
  className?: string,
}

function Icon({ name, className, ...svgProps }: IconProps) {
  const href = `/icons/sprite.svg#${name}` 
  return (
    <svg className={className} {...svgProps}>
      <use href={href}/>
    </svg>
  )
}

export default Icon