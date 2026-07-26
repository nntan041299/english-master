import { useMutation, useQuery } from "@tanstack/react-query";
import {
  getWritingChallenge,
  getWritingHistory,
  submitWriting,
} from "@/service/writing";

export const useGenerateChallenge = () =>
  useMutation({
    mutationFn: getWritingChallenge,
  });

export const useSubmitWriting = () =>
  useMutation({
    mutationFn: submitWriting,
  });

export const useWritingHistory = (page: number, size = 10) =>
  useQuery({
    queryKey: ["writing-history", page, size],
    queryFn: () => getWritingHistory({ page, size }),
  });
