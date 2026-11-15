import {useQueryClient} from "@tanstack/react-query"
import type {FlightKey} from "@/types/FlightKey.ts"
import type {Flight} from "../types/Flight.ts"

export function useFlightUpdate(key: FlightKey) {
    const queryClient = useQueryClient()

    return (flight: Flight) => {
        queryClient.setQueryData(["flight", key], flight)
        return queryClient.invalidateQueries({queryKey: ["flights"]})
    }
}
