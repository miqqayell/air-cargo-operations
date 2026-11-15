import {useQuery} from "@tanstack/react-query"
import type {FlightKey} from "@/types/FlightKey.ts"
import {getLoad} from "../api/loadsApi.ts"

export function useLoad(key: FlightKey) {
    const {isPending, isError, error, refetch, dataUpdatedAt, data: load} = useQuery({
        queryKey: ["load", key],
        queryFn: () => getLoad(key),
        refetchInterval: 30_000,
    })

    return {isPending, isError, error, refetch, load, loadedAt: new Date(dataUpdatedAt)}
}
