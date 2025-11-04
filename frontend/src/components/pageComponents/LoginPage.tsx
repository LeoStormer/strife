import { Navigate, useNavigate } from "react-router-dom";
import LoginForm from "../LoginForm";
import api from "../../api";
import { HttpStatusCode, isAxiosError } from "axios";
import { type FormEvent, useContext } from "react";
import { UserContext } from "../../contexts/UserContext";

function LoginPage() {
  const navigate = useNavigate();
  const { user, login } = useContext(UserContext);

  if (user !== null) {
    return <Navigate to='/servers/@me/friends' replace />;
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formdata = new FormData(e.currentTarget);
    const payload = Object.fromEntries(formdata.entries());
    try {
      const response = await api.post("/api/user/login", payload);
      login(response.data);
      navigate("/servers/@me/friends", { replace: true });
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
