import type {FlightStatus} from "./Flight.ts"

export type FlightSearch = {
    flightNumber?: string
    date?: string
    departureAirport?: string
    arrivalAirport?: string
    status?: FlightStatus
}
