
declare module "*.css";

declare module "*.svg" {
    const content: string;
    export default content;
}

declare module "*.svg" {
    import { type FC, type SVGProps } from "react";
    export const ReactComponent: FC<SVGProps<SVGSVGElement>>;

    const src: string;
    export default src;
}