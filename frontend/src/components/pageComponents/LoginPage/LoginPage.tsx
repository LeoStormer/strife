import { Navigate, useNavigate } from "react-router-dom";
import LoginForm from "./LoginForm";
import { HttpStatusCode, isAxiosError } from "axios";
import { type FormEvent } from "react";
import {
  useUserContext,
  type LoginRequest,
} from "../../../contexts/UserContext";
import { FRIENDS_PAGE_PATH } from "../../../constants";
import LoadingPage from "../LoadingPage";

function LoginPage() {
  const navigate = useNavigate();
  const { user, login, isLoading } = useUserContext();

  if (isLoading) {
    return <LoadingPage />;
  }

  if (user !== null) {
    return <Navigate to={FRIENDS_PAGE_PATH} replace />;
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formdata = new FormData(e.currentTarget);
    const loginRequest = Object.fromEntries(formdata.entries()) as LoginRequest;
    try {
      await login(loginRequest);
      navigate(FRIENDS_PAGE_PATH, { replace: true });
    } catch (error) {
      if (
        isAxiosError(error) &&
        error.response?.status === HttpStatusCode.BadRequest
      ) {
        alert("Invalid email or password");
      } else {
        alert("Server experiencing issues please try again later");
      }
    }
  };

  return <LoginForm handleSubmit={handleSubmit} />;
}

export default LoginPage;
