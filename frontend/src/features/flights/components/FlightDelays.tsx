import {Alert, Stack} from "@mui/material"
import {useState} from "react"
import {ActionButton} from "@/components/ui/ActionButton.tsx"
import {useSecurity} from "@/hooks/useSecurity.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import type {Delay, Flight} from "../types/Flight.ts"
import {canChangeDelays} from "../utils/flightActions.ts"
import {DelayFormDialog} from "./DelayFormDialog.tsx"
import {DelayList} from "./DelayList.tsx"
import {RemoveDelayDialog} from "./RemoveDelayDialog.tsx"

type OpenDialog =
    | { kind: "register" }
    | { kind: "correct", delay: Delay }
    | { kind: "remove", delay: Delay }

type FlightDelaysProps = {
    flight: Flight
    flightKey: FlightKey
}

export function FlightDelays({flight, flightKey}: FlightDelaysProps) {
    const {loggedInUser} = useSecurity()
    const [dialog, setDialog] = useState<OpenDialog | null>(null)
    const arrival = canChangeDelays(flight, "ARRIVAL", loggedInUser)
    const departure = canChangeDelays(flight, "DEPARTURE", loggedInUser)

    return (
        <Stack spacing={2}>
            <ActionButton variant="outlined" sx={{alignSelf: "flex-start"}}
                          unavailableReason={arrival.allowed ? undefined : arrival.reason}
                          onClick={() => setDialog({kind: "register"})}>
                Register a delay
            </ActionButton>
            {arrival.allowed && !departure.allowed && <Alert severity="info">{departure.reason}</Alert>}
            <DelayList delays={flight.delays}
                       canChange={side => canChangeDelays(flight, side, loggedInUser)}
                       onCorrect={delay => setDialog({kind: "correct", delay})}
                       onRemove={delay => setDialog({kind: "remove", delay})}/>
            {dialog?.kind === "register" &&
                <DelayFormDialog flight={flight} flightKey={flightKey} onClose={() => setDialog(null)}/>}
            {dialog?.kind === "correct" &&
                <DelayFormDialog flight={flight} flightKey={flightKey} delay={dialog.delay}
                                 onClose={() => setDialog(null)}/>}
            {dialog?.kind === "remove" &&
                <RemoveDelayDialog flightKey={flightKey} delay={dialog.delay} onClose={() => setDialog(null)}/>}
        </Stack>
    )
}
