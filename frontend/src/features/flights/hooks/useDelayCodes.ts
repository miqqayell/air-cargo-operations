import {useQuery} from "@tanstack/react-query"
import {getDelayCodes} from "../api/flightsApi.ts"

export function useDelayCodes() {
    const {isPending, isError, data: delayCodes} = useQuery({
        queryKey: ["delayCodes"],
        queryFn: getDelayCodes,
        staleTime: Infinity,
    })

    return {isPending, isError, delayCodes}
}
