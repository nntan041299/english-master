import { useMutation } from "@tanstack/react-query";
import {
  getListeningAudio,
  getListeningChallenge,
  submitListening,
} from "@/service/listening";

export const useGenerateListeningChallenge = () =>
  useMutation({
    mutationFn: getListeningChallenge,
  });

export const usePlayListeningAudio = () =>
  useMutation({
    mutationFn: getListeningAudio,
  });

export const useSubmitListening = () =>
  useMutation({
    mutationFn: submitListening,
  });
