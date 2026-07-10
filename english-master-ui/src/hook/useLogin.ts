import { useMutation } from "@tanstack/react-query";
import { login } from "@/service/auth";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthProvider";

export const useLogin = () => {
  const navigate = useNavigate();
  const { addToken } = useAuth();

  return useMutation({
    mutationFn: login,
    onSuccess: (response) => {
      const { accessToken, refreshToken } = response.data.data as {
        accessToken: string;
        refreshToken: string;
        tokenType: string;
      };
      addToken(accessToken, refreshToken);
      navigate("/");
    },
    onError: (error) => {
      console.log("Login error:" + error);
    },
  });
};
