import {Button, Paper, Stack, Table, TableBody, TableCell, TableHead, TableRow} from "@mui/material"
import {EmptyState} from "@/components/ui/EmptyState.tsx"
import type {Availability} from "@/types/Availability.ts"
import type {Delay, DelaySide} from "../types/Flight.ts"
import {delaySideLabel} from "../utils/delaySideLabel.ts"

type DelayListProps = {
    delays: Delay[]
    canChange: (side: DelaySide) => Availability
    onCorrect: (delay: Delay) => void
    onRemove: (delay: Delay) => void
}

export function DelayList({delays, canChange, onCorrect, onRemove}: DelayListProps) {
    if (delays.length === 0) {
        return <EmptyState message="No delays are registered on this flight."/>
    }

    return (
        <Paper variant="outlined">
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>On</TableCell>
                        <TableCell>Minutes</TableCell>
                        <TableCell>Code</TableCell>
                        <TableCell>Description</TableCell>
                        <TableCell/>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {delays.map(delay =>
                        <TableRow key={delay.id}>
                            <TableCell>{delaySideLabel(delay.side)}</TableCell>
                            <TableCell>{delay.minutes}</TableCell>
                            <TableCell>{delay.delayCode}</TableCell>
                            <TableCell>{delay.description}</TableCell>
                            <TableCell align="right">
                                {canChange(delay.side).allowed &&
                                    <Stack direction="row" spacing={1} sx={{justifyContent: "flex-end"}}>
                                        <Button size="small" onClick={() => onCorrect(delay)}>Correct</Button>
                                        <Button size="small" color="error" onClick={() => onRemove(delay)}>Remove</Button>
                                    </Stack>}
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
        </Paper>
    )
}
