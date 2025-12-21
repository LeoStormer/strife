import type { Metadata } from "next";
import "../index.css";
import "../themes.css";

export const metadata: Metadata = {
  title: {
    template: "Strife | %s",
    default: "Strife",
  },
  description: "A Discord-like real-time chat application made for practice",
};

export default function RootLayout({ children }: React.PropsWithChildren) {
  return (
    <html lang='en'>
      <body>
        <div id='root'>{children}</div>
        <div id='modal-root'></div>
      </body>
    </html>
  );
}
