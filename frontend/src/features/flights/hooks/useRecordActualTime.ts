import {useMutation} from "@tanstack/react-query"
import type {FlightKey} from "@/types/FlightKey.ts"
import {recordActualTime} from "../api/flightsApi.ts"
import type {Movement} from "../types/Flight.ts"
import {useFlightUpdate} from "./useFlightUpdate.ts"

export function useRecordActualTime(key: FlightKey, movement: Movement) {
    const onFlightUpdated = useFlightUpdate(key)
    const {mutate, isPending, error} = useMutation({
        mutationFn: (time: Date) => recordActualTime(key, movement, time),
        onSuccess: onFlightUpdated,
    })

    return {recordActualTime: mutate, isPending, error}
}
