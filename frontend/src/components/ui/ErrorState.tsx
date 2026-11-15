import {Alert, Button} from "@mui/material"

type ErrorStateProps = {
    message: string
    detail?: string
    onRetry: () => void
}

export function ErrorState({message, detail, onRetry}: ErrorStateProps) {
    return (
        <Alert severity="error" action={<Button color="inherit" size="small" onClick={onRetry}>Try again</Button>}>
            {message}{detail ? `: ${detail}` : ""}
        </Alert>
    )
}
