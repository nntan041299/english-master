import { useMutation } from "@tanstack/react-query";
import { getWritingChallenge, submitWriting } from "@/service/writing";

export const useGenerateChallenge = () =>
  useMutation({
    mutationFn: getWritingChallenge,
  });

export const useSubmitWriting = () =>
  useMutation({
    mutationFn: submitWriting,
  });
