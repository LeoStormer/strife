import { ClientOnly } from "./client";

export function generateStaticParams() {
  const routes = [
    { slug: [""] },
    { slug: ["login"] },
    { slug: ["register"] },
    { slug: ["servers", "@me", "friends"] },
    { slug: ["servers", "discover"] },
  ];

  return routes;
}

export default function Page(props: any) {
  console.log(props)
  return <ClientOnly />;
}
