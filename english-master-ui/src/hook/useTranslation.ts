import { useMutation } from "@tanstack/react-query";
import {
  getTranslationChallenge,
  submitTranslation,
  type TranslationDirection,
} from "@/service/translation";

export const useGenerateTranslationChallenge = () =>
  useMutation({
    mutationFn: (direction: TranslationDirection) =>
      getTranslationChallenge(direction),
  });

export const useSubmitTranslation = () =>
  useMutation({
    mutationFn: submitTranslation,
  });
