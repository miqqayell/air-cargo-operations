import {api} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {flightKeyPath} from "@/utils/flightKey.ts"
import type {DelayCode} from "../types/DelayCode.ts"
import type {DelayCorrection, NewDelay} from "../types/DelayInput.ts"
import type {Flight, Movement} from "../types/Flight.ts"
import type {FlightSearch} from "../types/FlightSearch.ts"

export async function searchFlights(search: FlightSearch): Promise<Flight[]> {
    const {data: flights} = await api.get<Flight[]>("/flights", {params: search})
    return flights
}

export async function getFlight(key: FlightKey): Promise<Flight> {
    const {data: flight} = await api.get<Flight>(`/flights/${flightKeyPath(key)}`)
    return flight
}

export async function getDelayCodes(): Promise<DelayCode[]> {
    const {data: delayCodes} = await api.get<DelayCode[]>("/delay-codes")
    return delayCodes
}

export async function registerDelay(key: FlightKey, delay: NewDelay): Promise<Flight> {
    const {data: flight} = await api.post<Flight>(`/flights/${flightKeyPath(key)}/delays`, delay)
    return flight
}

export async function changeDelay(key: FlightKey, delayId: string, correction: DelayCorrection): Promise<Flight> {
    const {data: flight} = await api.put<Flight>(`/flights/${flightKeyPath(key)}/delays/${delayId}`, correction)
    return flight
}

export async function removeDelay(key: FlightKey, delayId: string): Promise<Flight> {
    const {data: flight} = await api.delete<Flight>(`/flights/${flightKeyPath(key)}/delays/${delayId}`)
    return flight
}

export async function recordActualTime(key: FlightKey, movement: Movement, time: Date): Promise<Flight> {
    const {data: flight} = await api.post<Flight>(`/flights/${flightKeyPath(key)}/${movement}`,
        {time: time.toISOString()})
    return flight
}
