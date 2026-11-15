import {useMutation, useQueryClient} from "@tanstack/react-query"
import type {FlightKey} from "@/types/FlightKey.ts"
import {assignPosition, declareFinal, takeOffDeck} from "../api/loadsApi.ts"
import type {Load} from "../types/Load.ts"

function useLoadUpdate(key: FlightKey) {
    const queryClient = useQueryClient()

    return (load: Load) => {
        queryClient.setQueryData(["load", key], load)
        return queryClient.invalidateQueries({queryKey: ["loadChecks", key]})
    }
}

export function useAssignPosition(key: FlightKey) {
    const onLoadUpdated = useLoadUpdate(key)
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: ({uldCode, position}: { uldCode: string, position: string }) =>
            assignPosition(key, uldCode, position),
        onSuccess: onLoadUpdated,
    })

    return {assignPosition: mutate, isPending, error, reset}
}

export function useTakeOffDeck(key: FlightKey) {
    const onLoadUpdated = useLoadUpdate(key)
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (uldCode: string) => takeOffDeck(key, uldCode),
        onSuccess: onLoadUpdated,
    })

    return {takeOffDeck: mutate, isPending, error, reset}
}

export function useDeclareFinal(key: FlightKey) {
    const onLoadUpdated = useLoadUpdate(key)
    const {mutate, isPending, error} = useMutation({
        mutationFn: (confirmSoftWarnings: boolean) => declareFinal(key, confirmSoftWarnings),
        onSuccess: onLoadUpdated,
    })

    return {declareFinal: mutate, isPending, error}
}
