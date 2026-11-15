import {Chip, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography} from "@mui/material"
import {EmptyState} from "@/components/ui/EmptyState.tsx"
import type {Load} from "../types/Load.ts"

type LoadOverviewProps = {
    load: Load
}

export function LoadOverview({load}: LoadOverviewProps) {
    const placed = load.ulds.filter(uld => uld.position !== null).length

    return (
        <Stack spacing={2}>
            <Stack direction="row" spacing={2} sx={{alignItems: "center"}}>
                <Chip size="small" label={load.status === "FINAL" ? "Final" : "Open"}
                      color={load.status === "FINAL" ? "success" : "default"}/>
                <Typography color="text.secondary">
                    {placed} of {load.ulds.length} booked ULDs have a position
                </Typography>
            </Stack>
            {load.ulds.length === 0
                ? <EmptyState message="No ULDs are booked on this flight yet."/>
                : <Paper variant="outlined">
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>ULD</TableCell>
                                <TableCell>Weight</TableCell>
                                <TableCell>Destination</TableCell>
                                <TableCell>Position</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {load.ulds.map(uld =>
                                <TableRow key={uld.code}>
                                    <TableCell>{uld.code}</TableCell>
                                    <TableCell>{uld.weight} {uld.weightUnit}</TableCell>
                                    <TableCell>{uld.destination}</TableCell>
                                    <TableCell>{uld.position ?? <Typography variant="body2" color="text.secondary">Not on the deck</Typography>}</TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </Paper>}
        </Stack>
    )
}
