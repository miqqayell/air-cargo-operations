export const FLIGHT_STATUSES = ["SCHEDULED", "DEPARTED", "LANDED"] as const

export type FlightStatus = typeof FLIGHT_STATUSES[number]

export type DelaySide = "DEPARTURE" | "ARRIVAL"

export type Movement = "departure" | "arrival"

export type Delay = {
    id: string
    side: DelaySide
    minutes: number
    delayCode: string
    description: string
}

export type Flight = {
    flightNumber: string
    scheduledDepartureDate: string
    departureAirport: string
    arrivalAirport: string
    aircraftRegistration: string
    status: FlightStatus
    std: string
    sta: string
    etd: string
    eta: string
    atd: string | null
    ata: string | null
    finalLoadReceived: boolean
    delays: Delay[]
}
