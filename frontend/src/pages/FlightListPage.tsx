import {Alert, Button, LinearProgress, Stack} from "@mui/material"
import {useNavigate} from "react-router"
import {PageHeader} from "@/components/layout/PageHeader.tsx"
import {EmptyState} from "@/components/ui/EmptyState.tsx"
import {ErrorState} from "@/components/ui/ErrorState.tsx"
import {LoadingState} from "@/components/ui/LoadingState.tsx"
import {FlightSearchForm} from "@/features/flights/components/FlightSearchForm.tsx"
import {FlightTable} from "@/features/flights/components/FlightTable.tsx"
import {useFlights} from "@/features/flights/hooks/useFlights.ts"
import {useFlightSearchParams} from "@/features/flights/hooks/useFlightSearchParams.ts"
import type {Flight} from "@/features/flights/types/Flight.ts"
import type {FlightSearch} from "@/features/flights/types/FlightSearch.ts"
import {flightStatusLabel} from "@/features/flights/utils/flightStatusLabel.ts"
import {errorMessage} from "@/lib/api.ts"
import {flightKeyPath} from "@/utils/flightKey.ts"
import {formatDate} from "@/utils/time.ts"

function describe(search: FlightSearch): string {
    const parts = [
        search.flightNumber && `flight ${search.flightNumber}`,
        search.date && `on ${formatDate(search.date)}`,
        search.departureAirport && `from ${search.departureAirport}`,
        search.arrivalAirport && `to ${search.arrivalAirport}`,
        search.status && `in state ${flightStatusLabel(search.status).toLowerCase()}`,
    ].filter(Boolean)
    return `No flights match your selection: ${parts.join(", ")}.`
}

export function FlightListPage() {
    const {search, applySearch} = useFlightSearchParams()
    const {flights, isPending, isError, isFetching, error, refetch} = useFlights(search)
    const navigate = useNavigate()

    function openFlight(flight: Flight) {
        navigate(`/flights/${flightKeyPath({
            flightNumber: flight.flightNumber,
            date: flight.scheduledDepartureDate,
            departureAirport: flight.departureAirport,
        })}`)
    }

    return (
        <Stack spacing={3}>
            <PageHeader title="Flights"/>
            <FlightSearchForm key={JSON.stringify(search)} initial={search} onSearch={applySearch}/>
            {isPending && <LoadingState message="Loading flights…"/>}
            {isError && !flights &&
                <ErrorState message="The flights could not be loaded" detail={errorMessage(error)} onRetry={refetch}/>}
            {isError && flights &&
                <Alert severity="warning" action={<Button color="inherit" size="small" onClick={() => refetch()}>Try again</Button>}>
                    The flights could not be refreshed. You are looking at the last list that was loaded.
                </Alert>}
            {flights && flights.length === 0 && <EmptyState message={describe(search)}/>}
            {flights && flights.length > 0 &&
                <Stack spacing={0}>
                    {isFetching ? <LinearProgress/> : <div style={{height: 4}}/>}
                    <FlightTable flights={flights} onOpen={openFlight}/>
                </Stack>}
        </Stack>
    )
}
