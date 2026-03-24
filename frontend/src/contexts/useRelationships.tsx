import {
  keepPreviousData,
  useInfiniteQuery,
  useMutation,
  useQueryClient,
  type InfiniteData,
} from "@tanstack/react-query";
import api from "../api";
import type { User } from "./UserContext";

export type Relationship =
  | { type: "FRIEND"; user: User }
  | { type: "BLOCKED"; user: User }
  | { type: "PENDING"; user: User; requestId: string }
  | { type: "PENDING_OTHER"; user: User; requestId: string };

type RelationshipResponse = {
  total: number;
  page: number;
  size: number;
  relationships: Relationship[];
};

type UserId = User["id"];
type RequestId = string;

type Props = {
  filter: string;
  search: string;
};

const useRelationships = ({ filter, search }: Props) => {
  const queryClient = useQueryClient();

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isLoading,
    isFetching,
    isFetchingNextPage,
  } = useInfiniteQuery<
    RelationshipResponse,
    Error,
    InfiniteData<RelationshipResponse>,
    [string, string, string],
    number
  >({
    queryKey: ["relationships", filter, search],
    queryFn: async ({ pageParam = 0 }) => {
      const res = await api.get<RelationshipResponse>(`/user/relationships`, {
        params: { page: pageParam, size: 20, filter, search },
      }).then ();
      return res.data;
    },
    placeholderData: keepPreviousData,
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      const totalPages = Math.ceil(lastPage.total / lastPage.size);
      return lastPage.page + 1 < totalPages ? lastPage.page + 1 : undefined;
    },
  });

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ["relationships"] });

  const acceptMutation = useMutation<void, Error, RequestId>({
    mutationFn: (requestId) =>
      api.put(`/user/friends/friend-request?requestId=${requestId}`),
    onSettled: invalidate,
  });

  const rejectMutation = useMutation<void, Error, RequestId>({
    mutationFn: (requestId) =>
      api.delete(`/user/friends/friend-request?requestId=${requestId}`),
    onSettled: invalidate,
  });

  const unblockMutation = useMutation<void, Error, UserId>({
    mutationFn: (userId) =>
      api.delete(`/user/unblock-user?receiverId=${userId}`),
    onSettled: invalidate,
  });

  return {
    relationships: data?.pages.flatMap((page) => page.relationships) ?? [],
    total: data?.pages[0]?.total ?? 0,
    fetchNextPage,
    hasNextPage,
    isLoading,
    isFetching,
    isFetchingNextPage,
    acceptFriendRequest: acceptMutation.mutate,
    rejectFriendRequest: rejectMutation.mutate,
    unblockUser: unblockMutation.mutate,
  };
};

export type UseRelationshipsReturnType = ReturnType<typeof useRelationships>

export default useRelationships;
