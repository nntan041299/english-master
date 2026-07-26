import { useMutation, useQuery } from "@tanstack/react-query";
import {
  getTranslationChallenge,
  getTranslationHistory,
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

export const useTranslationHistory = (page: number, size = 10) =>
  useQuery({
    queryKey: ["translation-history", page, size],
    queryFn: () => getTranslationHistory({ page, size }),
  });
