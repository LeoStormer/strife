import "./App.css";
import {
  createBrowserRouter,
  createRoutesFromElements,
  RouterProvider,
  Route,
} from "react-router-dom";
import RootLayout from "./layouts/RootLayout";
import LoginPage from "./pages/LoginPage";
import RegristrationPage from "./pages/RegistrationPage";
import FriendsPage from "./pages/FriendsPage";
import NotFoundPage from "./pages/NotFoundPage";
import AuthenticatedLayout from "./layouts/AuthenticatedLayout";
import ServerChannelPage from "./pages/ServerChannelPage";
import ConversationPage from "./pages/ConversationPage";
import UserLayout from "./layouts/UserLayout";
import HomePage from "./pages/HomePage";
import ServerLayout from "./layouts/ServerLayout";

function App() {
  const router = createBrowserRouter(
    createRoutesFromElements(
      <Route path="/" element={<RootLayout />}>
        <Route index element={<HomePage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegristrationPage />} />
        <Route path="*" element={<NotFoundPage />} />
        <Route path="servers" element={<AuthenticatedLayout />}>
          <Route path="" element={<ServerLayout />}>
            <Route
              path=":serverId/:channelId"
              element={<ServerChannelPage />}
            />
            <Route
              path=":serverId/:channelId/threads/:threadId"
              element={<ServerChannelPage />}
            />
            <Route
              path=":serverId/:channelId/:threadId"
              element={<ServerChannelPage />}
            />
          </Route>
          <Route path="@me" element={<UserLayout />}>
            <Route path="friends" element={<FriendsPage />} />
            <Route path=":channelId" element={<ConversationPage />} />
          </Route>
        </Route>
      </Route>
    )
  );

  return <RouterProvider router={router} />;
}

export default App;
