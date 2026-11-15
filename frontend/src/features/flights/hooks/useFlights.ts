import {keepPreviousData, useQuery} from "@tanstack/react-query"
import {searchFlights} from "../api/flightsApi.ts"
import type {FlightSearch} from "../types/FlightSearch.ts"

export function useFlights(search: FlightSearch) {
    const {isPending, isError, isFetching, error, refetch, data: flights} = useQuery({
        queryKey: ["flights", search],
        queryFn: () => searchFlights(search),
        placeholderData: keepPreviousData,
        refetchInterval: 30_000,
    })

    return {isPending, isError, isFetching, error, refetch, flights}
}
