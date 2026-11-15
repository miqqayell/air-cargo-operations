import {Button, MenuItem, Paper, Stack, TextField} from "@mui/material"
import {type FormEvent, useState} from "react"
import {todayInBrussels} from "@/utils/time.ts"
import {FLIGHT_STATUSES, type FlightStatus} from "../types/Flight.ts"
import type {FlightSearch} from "../types/FlightSearch.ts"
import {flightStatusLabel} from "../utils/flightStatusLabel.ts"

type FlightSearchFormProps = {
    initial: FlightSearch
    onSearch: (search: FlightSearch) => void
}

export function FlightSearchForm({initial, onSearch}: FlightSearchFormProps) {
    const [flightNumber, setFlightNumber] = useState(initial.flightNumber ?? "")
    const [date, setDate] = useState(initial.date ?? "")
    const [departureAirport, setDepartureAirport] = useState(initial.departureAirport ?? "")
    const [arrivalAirport, setArrivalAirport] = useState(initial.arrivalAirport ?? "")
    const [status, setStatus] = useState<FlightStatus | "">(initial.status ?? "")

    function handleSubmit(event: FormEvent) {
        event.preventDefault()
        onSearch({
            flightNumber: flightNumber.trim().toUpperCase(),
            date,
            departureAirport: departureAirport.trim().toUpperCase(),
            arrivalAirport: arrivalAirport.trim().toUpperCase(),
            status: status || undefined,
        })
    }

    return (
        <Paper component="form" onSubmit={handleSubmit} variant="outlined" sx={{p: 2}}>
            <Stack direction={{xs: "column", md: "row"}} spacing={2}>
                <TextField label="Flight number" size="small" value={flightNumber}
                           onChange={event => setFlightNumber(event.target.value)}/>
                <TextField label="Date" type="date" size="small" value={date} sx={{minWidth: 170}}
                           onChange={event => setDate(event.target.value)}
                           slotProps={{inputLabel: {shrink: true}}}/>
                <TextField label="From" size="small" value={departureAirport} placeholder="BRU"
                           onChange={event => setDepartureAirport(event.target.value)}
                           slotProps={{htmlInput: {maxLength: 3}}}/>
                <TextField label="To" size="small" value={arrivalAirport} placeholder="ATH"
                           onChange={event => setArrivalAirport(event.target.value)}
                           slotProps={{htmlInput: {maxLength: 3}}}/>
                <TextField label="State" size="small" select value={status} sx={{minWidth: 140}}
                           onChange={event => setStatus(event.target.value as FlightStatus | "")}>
                    <MenuItem value="">Any</MenuItem>
                    {FLIGHT_STATUSES.map(value =>
                        <MenuItem key={value} value={value}>{flightStatusLabel(value)}</MenuItem>
                    )}
                </TextField>
                <Button type="submit" variant="contained">Search</Button>
                <Button onClick={() => onSearch({date: todayInBrussels()})}>Today</Button>
            </Stack>
        </Paper>
    )
}
