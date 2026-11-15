import {Button, Link, Stack, Typography} from "@mui/material"
import type {ReactNode} from "react"
import {Link as RouterLink, useParams} from "react-router"
import {PageHeader} from "@/components/layout/PageHeader.tsx"
import {EmptyState} from "@/components/ui/EmptyState.tsx"
import {ErrorState} from "@/components/ui/ErrorState.tsx"
import {LoadingState} from "@/components/ui/LoadingState.tsx"
import {ActualTimeActions} from "@/features/flights/components/ActualTimeActions.tsx"
import {FlightDelays} from "@/features/flights/components/FlightDelays.tsx"
import {FlightSummary} from "@/features/flights/components/FlightSummary.tsx"
import {FlightTimes} from "@/features/flights/components/FlightTimes.tsx"
import {useFlight} from "@/features/flights/hooks/useFlight.ts"
import {LoadOverview} from "@/features/loads/components/LoadOverview.tsx"
import {useLoad} from "@/features/loads/hooks/useLoad.ts"
import {errorMessage, isNotFound} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {flightKeyPath} from "@/utils/flightKey.ts"
import {formatDate} from "@/utils/time.ts"

export function FlightDetailPage() {
    const {flightNumber, date, departureAirport} = useParams()
    const key: FlightKey = {flightNumber: flightNumber!, date: date!, departureAirport: departureAirport!}
    const {flight, isPending, isError, error, refetch} = useFlight(key)

    return (
        <Stack spacing={3}>
            <Link component={RouterLink} to="/flights">← All flights</Link>
            {isPending && <LoadingState message="Loading the flight…"/>}
            {isError && isNotFound(error) &&
                <EmptyState message={`There is no flight ${key.flightNumber} from ${key.departureAirport} on ${formatDate(key.date)}.`}/>}
            {isError && !isNotFound(error) &&
                <ErrorState message="The flight could not be loaded" detail={errorMessage(error)} onRetry={refetch}/>}
            {flight &&
                <>
                    <PageHeader
                        title={`${flight.flightNumber} · ${flight.departureAirport} → ${flight.arrivalAirport}`}
                        subtitle={formatDate(flight.scheduledDepartureDate)}/>
                    <FlightSummary flight={flight}/>
                    <Section title="Times">
                        <FlightTimes flight={flight}/>
                        <ActualTimeActions flight={flight} flightKey={key}/>
                    </Section>
                    <Section title="Delays"><FlightDelays flight={flight} flightKey={key}/></Section>
                    <Section title="Load"><FlightLoad flightKey={key}/></Section>
                </>}
        </Stack>
    )
}

function Section({title, children}: { title: string, children: ReactNode }) {
    return (
        <Stack spacing={1}>
            <Typography variant="h6" component="h2">{title}</Typography>
            {children}
        </Stack>
    )
}

function FlightLoad({flightKey}: { flightKey: FlightKey }) {
    const {load, isPending, isError, error, refetch} = useLoad(flightKey)

    if (isPending) {
        return <LoadingState message="Loading the load…"/>
    }
    if (isError && isNotFound(error)) {
        return <EmptyState message="No load has been opened for this flight yet."/>
    }
    if (isError || !load) {
        return <ErrorState message="The load could not be loaded" detail={errorMessage(error)} onRetry={refetch}/>
    }
    return (
        <Stack spacing={2}>
            <Button component={RouterLink} to={`/flights/${flightKeyPath(flightKey)}/load`} variant="outlined"
                    sx={{alignSelf: "flex-start"}}>
                Open the deck
            </Button>
            <LoadOverview load={load}/>
        </Stack>
    )
}
