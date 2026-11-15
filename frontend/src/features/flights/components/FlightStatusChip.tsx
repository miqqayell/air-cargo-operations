import {Chip} from "@mui/material"
import type {FlightStatus} from "../types/Flight.ts"
import {flightStatusLabel} from "../utils/flightStatusLabel.ts"

const COLORS: Record<FlightStatus, "default" | "secondary" | "success"> = {
    SCHEDULED: "default",
    DEPARTED: "secondary",
    LANDED: "success",
}

type FlightStatusChipProps = {
    status: FlightStatus
}

export function FlightStatusChip({status}: FlightStatusChipProps) {
    return <Chip size="small" label={flightStatusLabel(status)} color={COLORS[status]}/>
}
