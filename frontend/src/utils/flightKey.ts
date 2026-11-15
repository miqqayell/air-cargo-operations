import type {FlightKey} from "@/types/FlightKey.ts"

export function flightKeyPath({flightNumber, date, departureAirport}: FlightKey): string {
    return `${flightNumber}/${date}/${departureAirport}`
}
