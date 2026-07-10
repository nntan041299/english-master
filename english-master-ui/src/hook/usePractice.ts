import { useQuery, useMutation } from "@tanstack/react-query";
import { getPractices, answerPractice } from "@/service/practice";

export const PRACTICE_QUERY_KEY = ["practices"];

export const usePractices = () =>
  useQuery({
    queryKey: PRACTICE_QUERY_KEY,
    queryFn: getPractices,
  });

export const useAnswerPractice = () =>
  useMutation({
    mutationFn: answerPractice,
  });
