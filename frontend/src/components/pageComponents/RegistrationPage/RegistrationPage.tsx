import { Navigate, useNavigate } from "react-router-dom";
import RegistrationForm from "./RegistrationForm";
import { HttpStatusCode, isAxiosError } from "axios";
import { type FormEvent } from "react";
import {
  REGISTRATION_SUCCESS_BUT_LOGIN_FAILED_ERROR,
  useUserContext,
  type RegistrationRequest,
} from "../../../contexts/UserContext";
import { FRIENDS_PAGE_PATH, LOGIN_PAGE_PATH } from "../../../constants";
import LoadingPage from "../LoadingPage";

function RegristrationPage() {
  const navigate = useNavigate();
  const { user, register, isLoading } = useUserContext();

  if (isLoading) {
    return <LoadingPage />;
  }

  if (user !== null) {
    return <Navigate to={FRIENDS_PAGE_PATH} replace />;
  }

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formdata = new FormData(e.currentTarget);
    const payload = Object.fromEntries(
      formdata.entries(),
    ) as RegistrationRequest;
    try {
      await register(payload);
      navigate(FRIENDS_PAGE_PATH, { replace: true });
    } catch (error) {
      if (
        error instanceof Error &&
        error.message === REGISTRATION_SUCCESS_BUT_LOGIN_FAILED_ERROR
      ) {
        alert(
          "Account successfully created but an error occured while logging in. " +
            "Please try logging in later.",
        );
        navigate(LOGIN_PAGE_PATH);
        return;
      }

      if (isAxiosError(error) && error.status === HttpStatusCode.Conflict) {
        alert("Please use a different email");
        return;
      }

      alert("The server is experiencing issues please try again later");
    }
  };

  return <RegistrationForm handleSubmit={handleSubmit} />;
}

export default RegristrationPage;
