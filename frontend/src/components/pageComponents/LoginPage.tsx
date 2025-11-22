import { Navigate, useNavigate } from "react-router-dom";
import LoginForm from "../LoginForm";
import api from "../../api";
import { HttpStatusCode, isAxiosError } from "axios";
import { type FormEvent } from "react";
import { useUserContext } from "../../contexts/UserContext";
import { FRIENDS_PAGE_PATH } from "../../constants";

function LoginPage() {
  const navigate = useNavigate();
  const { user, login } = useUserContext();

  if (user !== null) {
    return <Navigate to={FRIENDS_PAGE_PATH} replace />;
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formdata = new FormData(e.currentTarget);
    const payload = Object.fromEntries(formdata.entries());
    try {
      const response = await api.post("/api/user/login", payload);
      login(response.data);
      navigate(FRIENDS_PAGE_PATH, { replace: true });
    } catch (error: unknown) {
      if (isAxiosError(error) && error.status === HttpStatusCode.BadRequest) {
        alert("Invalid email or password");
      } else {
        alert("Server experiencing issues please try again later");
      }
    }
  };

  // TODO: switch to an alternate styling when under 512 pixel width
  return <LoginForm handleSubmit={handleSubmit} />;
}

export default LoginPage;
