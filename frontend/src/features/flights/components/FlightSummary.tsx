import {Paper, Stack, Typography} from "@mui/material"
import type {Flight} from "../types/Flight.ts"
import {FlightStatusChip} from "./FlightStatusChip.tsx"

type FlightSummaryProps = {
    flight: Flight
}

export function FlightSummary({flight}: FlightSummaryProps) {
    return (
        <Paper variant="outlined" sx={{p: 2}}>
            <Stack direction={{xs: "column", sm: "row"}} spacing={4}>
                <Stack spacing={0.5}>
                    <Typography variant="caption" color="text.secondary">State</Typography>
                    <FlightStatusChip status={flight.status}/>
                </Stack>
                <Stack spacing={0.5}>
                    <Typography variant="caption" color="text.secondary">Aircraft</Typography>
                    <Typography>{flight.aircraftRegistration}</Typography>
                </Stack>
                <Stack spacing={0.5}>
                    <Typography variant="caption" color="text.secondary">Final load received</Typography>
                    <Typography>{flight.finalLoadReceived ? "Yes" : "No"}</Typography>
                </Stack>
            </Stack>
        </Paper>
    )
}
