import type {DelaySide} from "../types/Flight.ts"

const LABELS: Record<DelaySide, string> = {
    DEPARTURE: "Departure",
    ARRIVAL: "Arrival",
}

export function delaySideLabel(side: DelaySide): string {
    return LABELS[side]
}
