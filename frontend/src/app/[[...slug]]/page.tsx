import { ClientOnly } from "./client";

export function generateStaticParams() {
  const routes = [
    { slug: [""] },
    { slug: ["login"] },
    { slug: ["register"] },
    { slug: ["servers", "@me"] },
    { slug: ["servers", "@me", "friends"] },
    { slug: ["servers", "discover"] },
    { slug: ["servers", "discover", "servers"] },
    { slug: ["servers", "discover", "applications"] },
  ];

  return routes;
}

export default function Page(props: any) {
  return <ClientOnly />;
}
