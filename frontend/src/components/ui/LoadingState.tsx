import {CircularProgress, Stack, Typography} from "@mui/material"

type LoadingStateProps = {
    message: string
}

export function LoadingState({message}: LoadingStateProps) {
    return (
        <Stack direction="row" spacing={2} sx={{alignItems: "center", py: 4}} role="status">
            <CircularProgress size={24}/>
            <Typography color="text.secondary">{message}</Typography>
        </Stack>
    )
}
