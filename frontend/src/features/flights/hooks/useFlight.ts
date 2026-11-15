import {useQuery} from "@tanstack/react-query"
import type {FlightKey} from "@/types/FlightKey.ts"
import {getFlight} from "../api/flightsApi.ts"

export function useFlight(key: FlightKey) {
    const {isPending, isError, error, refetch, data: flight} = useQuery({
        queryKey: ["flight", key],
        queryFn: () => getFlight(key),
        refetchInterval: 30_000,
    })

    return {isPending, isError, error, refetch, flight}
}
