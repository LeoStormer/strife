import { Navigate, useNavigate } from "react-router-dom";
import api from "../../api";
import RegistrationForm from "../RegistrationForm";
import { HttpStatusCode, isAxiosError } from "axios";
import { type FormEvent } from "react";
import { useUserContext } from "../../contexts/UserContext";
import { FRIENDS_PAGE_PATH, LOGIN_PAGE_PATH } from "../../constants";

function RegristrationPage() {
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
      const response = await api.post("/api/user/register", payload);
      if (response.status === HttpStatusCode.Created) {
        alert(
          "Account successfully created but an error occured while logging in. " +
            "Please try logging in later."
        );
        navigate(LOGIN_PAGE_PATH);
        return;
      }

      login(response.data);
      navigate(FRIENDS_PAGE_PATH, { replace: true });
    } catch (error) {
      if (isAxiosError(error) && error.status === HttpStatusCode.Conflict) {
        alert("Please use a different email");
      } else {
        alert("The server is experiencing issues please try again later");
      }
    }
  };

  return <RegistrationForm handleSubmit={handleSubmit} />;
}

export default RegristrationPage;
