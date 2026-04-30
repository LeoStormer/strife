import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import api from "../api";

export type Conversation = {
  id: string;
  userIds: string[];
  locked: boolean;
};

export const useConversations = () => {
  const queryClient = useQueryClient();
  const { data, isLoading } = useQuery<Conversation[]>({
    queryKey: ["conversations"],
    queryFn: async () => {
      const res = await api.get("/conversation");
      return res.data;
    },
  });

  const startConversationMutation = useMutation({
    mutationFn: async (userIds: string[]) => {
      const res = await api.post("/conversation", userIds);
      return res.data as Conversation;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["conversations"] });
    },
  });

  const leaveConversationMutation = useMutation({
    mutationFn: async (conversationId: string) => {
      await api.delete(`/conversation?conversationId=${conversationId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["conversations"] });
    },
  });

  return {
    conversations: data ?? [],
    isLoading,
    startConversation: startConversationMutation.mutateAsync,
    leaveConversation: leaveConversationMutation.mutateAsync,
  };
};
