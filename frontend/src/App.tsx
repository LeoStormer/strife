import "./App.css";
import {
  createBrowserRouter,
  createRoutesFromElements,
  RouterProvider,
  Route,
} from "react-router-dom";
import RootLayout from "./layouts/RootLayout";
import LoginPage from "./components/pageComponents/LoginPage";
import RegristrationPage from "./components/pageComponents/RegistrationPage";
import FriendsPage from "./components/pageComponents/FriendsPage";
import NotFoundPage from "./components/pageComponents/NotFoundPage";
import AuthenticatedLayout from "./layouts/AuthenticatedLayout";
import ServerChannelPage from "./components/pageComponents/ServerChannelPage";
import ConversationPage from "./components/pageComponents/ConversationPage";
import UserLayout from "./layouts/UserLayout";
import HomePage from "./components/pageComponents/HomePage";
import ServerLayout from "./layouts/ServerLayout";
import DiscoveryLayout from "./layouts/DiscoveryLayout";
import ServerDiscoveryPage from "./components/pageComponents/ServerDiscoveryPage";

function App() {
  const router = createBrowserRouter(
    createRoutesFromElements(
      <Route path='/' element={<RootLayout />}>
        <Route index element={<HomePage />} />
        <Route path='login' element={<LoginPage />} />
        <Route path='register' element={<RegristrationPage />} />
        <Route path='*' element={<NotFoundPage />} />
        <Route path='servers' element={<AuthenticatedLayout />}>
          <Route path='' element={<ServerLayout />}>
            <Route
              path=':serverId/:channelId'
              element={<ServerChannelPage />}
            />
            <Route
              path=':serverId/:channelId/threads/:threadId'
              element={<ServerChannelPage />}
            />
            <Route
              path=':serverId/:channelId/:threadId'
              element={<ServerChannelPage />}
            />
          </Route>
          <Route path='@me' element={<UserLayout />}>
            <Route path='friends' element={<FriendsPage />} />
            <Route path=':channelId' element={<ConversationPage />} />
          </Route>
          <Route path='discover' element={<DiscoveryLayout />}>
            <Route path="servers" element={<ServerDiscoveryPage />} />
            <Route path="applications" element={<ServerDiscoveryPage />} />
          </Route>
        </Route>
      </Route>
    )
  );

  return <RouterProvider router={router} />;
}

export default App;
