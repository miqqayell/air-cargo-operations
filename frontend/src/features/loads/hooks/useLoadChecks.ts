import {keepPreviousData, useQuery} from "@tanstack/react-query"
import type {FlightKey} from "@/types/FlightKey.ts"
import {getLoadChecks} from "../api/loadsApi.ts"

export function useLoadChecks(key: FlightKey) {
    const {isPending, isError, isFetching, error, refetch, data: report} = useQuery({
        queryKey: ["loadChecks", key],
        queryFn: () => getLoadChecks(key),
        placeholderData: keepPreviousData,
    })

    return {isPending, isError, isFetching, error, refetch, report}
}
