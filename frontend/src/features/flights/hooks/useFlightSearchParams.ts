import {useSearchParams} from "react-router"
import {todayInBrussels} from "@/utils/time.ts"
import {FLIGHT_STATUSES, type FlightStatus} from "../types/Flight.ts"
import type {FlightSearch} from "../types/FlightSearch.ts"

function isFlightStatus(value: string | null): value is FlightStatus {
    return FLIGHT_STATUSES.some(status => status === value)
}

export function useFlightSearchParams() {
    const [params, setParams] = useSearchParams()

    const status = params.get("status")
    const selection: FlightSearch = {
        flightNumber: params.get("flightNumber") ?? undefined,
        date: params.get("date") ?? undefined,
        departureAirport: params.get("departureAirport") ?? undefined,
        arrivalAirport: params.get("arrivalAirport") ?? undefined,
        status: isFlightStatus(status) ? status : undefined,
    }
    const hasSelection = Object.values(selection).some(value => value)
    const search: FlightSearch = hasSelection ? selection : {date: todayInBrussels()}

    function applySearch(next: FlightSearch) {
        const entries = Object.entries(next).filter(([, value]) => value)
        setParams(Object.fromEntries(entries))
    }

    return {search, applySearch}
}
