import { ClientOnly } from "./client";

export function generateStaticParams() {
  const routes = [
    { slug: [""] },
  ];

  return routes;
}

export default function Page(props: any) {
  return <ClientOnly />;
}
