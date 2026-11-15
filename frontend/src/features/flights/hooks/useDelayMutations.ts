import {useMutation} from "@tanstack/react-query"
import type {FlightKey} from "@/types/FlightKey.ts"
import {changeDelay, registerDelay, removeDelay} from "../api/flightsApi.ts"
import type {DelayCorrection, NewDelay} from "../types/DelayInput.ts"
import {useFlightUpdate} from "./useFlightUpdate.ts"

export function useRegisterDelay(key: FlightKey) {
    const onFlightUpdated = useFlightUpdate(key)
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (delay: NewDelay) => registerDelay(key, delay),
        onSuccess: onFlightUpdated,
    })

    return {registerDelay: mutate, isPending, error, reset}
}

export function useChangeDelay(key: FlightKey) {
    const onFlightUpdated = useFlightUpdate(key)
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: ({delayId, correction}: { delayId: string, correction: DelayCorrection }) =>
            changeDelay(key, delayId, correction),
        onSuccess: onFlightUpdated,
    })

    return {changeDelay: mutate, isPending, error, reset}
}

export function useRemoveDelay(key: FlightKey) {
    const onFlightUpdated = useFlightUpdate(key)
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (delayId: string) => removeDelay(key, delayId),
        onSuccess: onFlightUpdated,
    })

    return {removeDelay: mutate, isPending, error, reset}
}
