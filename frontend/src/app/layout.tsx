import type { Metadata } from "next";
import "../index.css"
import "../themes.css"

export const metaData: Metadata = {
  title: 'React App',
  description: 'Web site created using create-react-app',
}

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
