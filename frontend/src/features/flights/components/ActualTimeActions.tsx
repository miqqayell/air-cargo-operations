import {Stack} from "@mui/material"
import {useState} from "react"
import {ActionButton} from "@/components/ui/ActionButton.tsx"
import {useSecurity} from "@/hooks/useSecurity.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import type {Flight, Movement} from "../types/Flight.ts"
import {canRecordArrival, canRecordDeparture} from "../utils/flightActions.ts"
import {ActualTimeDialog} from "./ActualTimeDialog.tsx"

type OpenDialog = {
    movement: Movement
    openedAt: Date
}

type ActualTimeActionsProps = {
    flight: Flight
    flightKey: FlightKey
}

export function ActualTimeActions({flight, flightKey}: ActualTimeActionsProps) {
    const {loggedInUser} = useSecurity()
    const [dialog, setDialog] = useState<OpenDialog | null>(null)
    const departure = canRecordDeparture(flight, loggedInUser)
    const arrival = canRecordArrival(flight, loggedInUser)

    return (
        <Stack direction={{xs: "column", sm: "row"}} spacing={3}>
            <ActionButton variant="outlined" onClick={() => setDialog({movement: "departure", openedAt: new Date()})}
                          unavailableReason={departure.allowed ? undefined : departure.reason}>
                Record departure
            </ActionButton>
            <ActionButton variant="outlined" onClick={() => setDialog({movement: "arrival", openedAt: new Date()})}
                          unavailableReason={arrival.allowed ? undefined : arrival.reason}>
                Record arrival
            </ActionButton>
            {dialog &&
                <ActualTimeDialog flightKey={flightKey} movement={dialog.movement} openedAt={dialog.openedAt}
                                  onClose={() => setDialog(null)}/>}
        </Stack>
    )
}
