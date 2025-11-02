/* eslint-disable react-hooks/exhaustive-deps */
import { useEffect } from "react";
import api from "../api";

type VoidFunc = () => void;
type UseAuthStatusProps = { onRequestFinished?: VoidFunc; logout: VoidFunc };

/**
 * Chechs the authentication status of the user in the backend by making an API call.
 * @param onRequestFinished - Callback function to be called when the auth status check is complete success or fail
 * @param logout - function to log the user out locally if they are not authenticated
 */
export const useAuthStatus = ({
  onRequestFinished = () => {},
  logout,
}: UseAuthStatusProps) => {
  useEffect(() => {
    api
      .get("/api/user/auth-status")
      .then((response) => {
        if (response.data === false) {
          console.log("User not authenticated, logging out");
          logout();
        }
      })
      .catch(() => {
        logout();
      })
      .finally(() => {
        onRequestFinished();
      });
  }, []);
};
