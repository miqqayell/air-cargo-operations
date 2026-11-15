import {Button, Chip, LinearProgress, List, ListItem, ListItemText, Paper, Stack, Typography} from "@mui/material"
import {ErrorState} from "@/components/ui/ErrorState.tsx"
import {LoadingState} from "@/components/ui/LoadingState.tsx"
import {errorMessage} from "@/lib/api.ts"
import type {LoadReport} from "../types/LoadReport.ts"

type LoadChecksPanelProps = {
    report: LoadReport | undefined
    isPending: boolean
    isFetching: boolean
    error: unknown
    onRunChecks: () => void
}

export function LoadChecksPanel({report, isPending, isFetching, error, onRunChecks}: LoadChecksPanelProps) {
    if (isPending) {
        return <LoadingState message="Checking the load…"/>
    }
    if (!report) {
        return <ErrorState message="The load could not be checked" detail={errorMessage(error)} onRetry={onRunChecks}/>
    }

    return (
        <Paper variant="outlined">
            {isFetching ? <LinearProgress/> : <div style={{height: 4}}/>}
            <List dense>
                {report.results.map(result =>
                    <ListItem key={result.name}>
                        <ListItemText
                            primary={
                                <Stack direction="row" spacing={1} sx={{alignItems: "center"}}>
                                    <Chip size="small" label={result.passed ? "Passes" : "Fails"}
                                          color={result.passed ? "success" : result.kind === "HARD" ? "error" : "warning"}/>
                                    <Typography>{result.name}</Typography>
                                    <Chip size="small" variant="outlined" label={result.kind === "HARD" ? "hard" : "soft"}/>
                                </Stack>}
                            secondary={result.reason}/>
                    </ListItem>
                )}
            </List>
            <Stack direction="row" sx={{px: 2, pb: 2}}>
                <Button size="small" onClick={onRunChecks}>Check the load again</Button>
            </Stack>
        </Paper>
    )
}
