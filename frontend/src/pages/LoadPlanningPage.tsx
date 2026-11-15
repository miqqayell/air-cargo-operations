import {Link, Stack} from "@mui/material"
import {Link as RouterLink, useParams} from "react-router"
import {PageHeader} from "@/components/layout/PageHeader.tsx"
import {useFlight} from "@/features/flights/hooks/useFlight.ts"
import {LoadPlanner} from "@/features/loads/components/LoadPlanner.tsx"
import type {FlightKey} from "@/types/FlightKey.ts"
import {flightKeyPath} from "@/utils/flightKey.ts"
import {formatDate, formatDateTime} from "@/utils/time.ts"

export function LoadPlanningPage() {
    const {flightNumber, date, departureAirport} = useParams()
    const key: FlightKey = {flightNumber: flightNumber!, date: date!, departureAirport: departureAirport!}
    const {flight} = useFlight(key)

    return (
        <Stack spacing={3}>
            <Link component={RouterLink} to={`/flights/${flightKeyPath(key)}`}>← Flight {key.flightNumber}</Link>
            <PageHeader
                title={`Load planning · ${key.flightNumber}${flight ? ` · ${flight.departureAirport} → ${flight.arrivalAirport}` : ""}`}
                subtitle={flight
                    ? `${formatDate(key.date)} · estimated departure ${formatDateTime(flight.etd)}`
                    : formatDate(key.date)}/>
            <LoadPlanner flightKey={key}/>
        </Stack>
    )
}
