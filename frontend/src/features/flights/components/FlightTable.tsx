import {Link, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow} from "@mui/material"
import {Link as RouterLink} from "react-router"
import {flightKeyPath} from "@/utils/flightKey.ts"
import {formatDate, formatTime} from "@/utils/time.ts"
import type {Flight} from "../types/Flight.ts"
import {FlightStatusChip} from "./FlightStatusChip.tsx"

type FlightTableProps = {
    flights: Flight[]
    onOpen: (flight: Flight) => void
}

function flightLink(flight: Flight): string {
    return `/flights/${flightKeyPath({
        flightNumber: flight.flightNumber,
        date: flight.scheduledDepartureDate,
        departureAirport: flight.departureAirport,
    })}`
}

function estimate(estimated: string, scheduled: string): string {
    return estimated === scheduled ? "" : formatTime(estimated)
}

export function FlightTable({flights, onOpen}: FlightTableProps) {
    return (
        <TableContainer component={Paper} variant="outlined">
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>Flight</TableCell>
                        <TableCell>Date</TableCell>
                        <TableCell>Route</TableCell>
                        <TableCell>STD</TableCell>
                        <TableCell>ETD</TableCell>
                        <TableCell>ATD</TableCell>
                        <TableCell>STA</TableCell>
                        <TableCell>ETA</TableCell>
                        <TableCell>ATA</TableCell>
                        <TableCell>State</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {flights.map(flight =>
                        <TableRow key={flightLink(flight)} hover sx={{cursor: "pointer"}} onClick={() => onOpen(flight)}>
                            <TableCell>
                                <Link component={RouterLink} to={flightLink(flight)} onClick={event => event.stopPropagation()}
                                      sx={{fontWeight: 700}}>
                                    {flight.flightNumber}
                                </Link>
                            </TableCell>
                            <TableCell>{formatDate(flight.scheduledDepartureDate)}</TableCell>
                            <TableCell>{flight.departureAirport} → {flight.arrivalAirport}</TableCell>
                            <TableCell>{formatTime(flight.std)}</TableCell>
                            <TableCell sx={{color: "secondary.dark", fontWeight: 700}}>{estimate(flight.etd, flight.std)}</TableCell>
                            <TableCell>{formatTime(flight.atd)}</TableCell>
                            <TableCell>{formatTime(flight.sta)}</TableCell>
                            <TableCell sx={{color: "secondary.dark", fontWeight: 700}}>{estimate(flight.eta, flight.sta)}</TableCell>
                            <TableCell>{formatTime(flight.ata)}</TableCell>
                            <TableCell><FlightStatusChip status={flight.status}/></TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
        </TableContainer>
    )
}
