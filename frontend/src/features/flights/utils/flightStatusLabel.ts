import type {FlightStatus} from "../types/Flight.ts"

const LABELS: Record<FlightStatus, string> = {
    SCHEDULED: "Scheduled",
    DEPARTED: "Departed",
    LANDED: "Landed",
}

export function flightStatusLabel(status: FlightStatus): string {
    return LABELS[status]
}
