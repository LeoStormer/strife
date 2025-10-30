import { Navigate, useNavigate } from "react-router-dom";
import api from "../api";
import RegistrationForm from "../components/RegistrationForm";
import { HttpStatusCode, isAxiosError } from "axios";
import { type FormEvent, useContext } from "react";
import { UserContext } from "../contexts/UserContext";

function RegristrationPage() {
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
      const response = await api.post("/api/user/register", payload);
      if (response.status === HttpStatusCode.Created) {
        alert(
          "Account successfully created but an error occured while logging in. " +
            "Please try again later."
        );
        navigate("/login");
        return;
      }

      login(response.data);
      navigate("/servers/@me/friends", { replace: true });
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
