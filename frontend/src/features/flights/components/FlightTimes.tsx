import {Paper, Table, TableBody, TableCell, TableHead, TableRow, Typography} from "@mui/material"
import {formatDateTime} from "@/utils/time.ts"
import type {Flight} from "../types/Flight.ts"

type FlightTimesProps = {
    flight: Flight
}

function estimate(estimated: string, scheduled: string): string {
    return estimated === scheduled ? "" : formatDateTime(estimated)
}

export function FlightTimes({flight}: FlightTimesProps) {
    return (
        <Paper variant="outlined">
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell/>
                        <TableCell>Scheduled</TableCell>
                        <TableCell>Estimated</TableCell>
                        <TableCell>Actual</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    <TableRow>
                        <TableCell><Typography variant="subtitle2">Departure {flight.departureAirport}</Typography></TableCell>
                        <TableCell>{formatDateTime(flight.std)}</TableCell>
                        <TableCell sx={{color: "secondary.dark", fontWeight: 700}}>{estimate(flight.etd, flight.std)}</TableCell>
                        <TableCell>{formatDateTime(flight.atd)}</TableCell>
                    </TableRow>
                    <TableRow>
                        <TableCell><Typography variant="subtitle2">Arrival {flight.arrivalAirport}</Typography></TableCell>
                        <TableCell>{formatDateTime(flight.sta)}</TableCell>
                        <TableCell sx={{color: "secondary.dark", fontWeight: 700}}>{estimate(flight.eta, flight.sta)}</TableCell>
                        <TableCell>{formatDateTime(flight.ata)}</TableCell>
                    </TableRow>
                </TableBody>
            </Table>
        </Paper>
    )
}
