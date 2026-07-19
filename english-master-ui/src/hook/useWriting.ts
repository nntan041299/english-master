import { useMutation } from "@tanstack/react-query";
import {
  getWritingChallenge,
  submitWriting,
  type WritingLevel,
} from "@/service/writing";

export const useGenerateChallenge = () =>
  useMutation({
    mutationFn: (level: WritingLevel) => getWritingChallenge(level),
  });

export const useSubmitWriting = () =>
  useMutation({
    mutationFn: submitWriting,
  });
