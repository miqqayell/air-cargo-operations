import {
    Alert,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    List,
    ListItem,
    ListItemText,
} from "@mui/material"
import {errorMessage} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {useDeclareFinal} from "../hooks/useLoadMutations.ts"
import type {CheckResult} from "../types/LoadReport.ts"

type DeclareFinalDialogProps = {
    flightKey: FlightKey
    softWarnings: CheckResult[]
    onClose: () => void
}

export function DeclareFinalDialog({flightKey, softWarnings, onClose}: DeclareFinalDialogProps) {
    const {declareFinal, isPending, error} = useDeclareFinal(flightKey)
    const hasWarnings = softWarnings.length > 0

    return (
        <Dialog open onClose={onClose} fullWidth maxWidth="sm">
            <DialogTitle>Declare the load final?</DialogTitle>
            <DialogContent>
                {error && <Alert severity="error" sx={{mb: 2}}>{errorMessage(error)}</Alert>}
                {hasWarnings
                    ? <>
                        <DialogContentText>These soft warnings remain. Confirm that the load goes like this:</DialogContentText>
                        <List dense>
                            {softWarnings.map(warning =>
                                <ListItem key={warning.name}>
                                    <ListItemText primary={warning.name} secondary={warning.reason}/>
                                </ListItem>
                            )}
                        </List>
                    </>
                    : <DialogContentText>All checks pass.</DialogContentText>}
                <DialogContentText sx={{mt: 1}}>
                    After this the load can no longer change, and the CPM is sent to the partners.
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancel</Button>
                <Button variant="contained" loading={isPending}
                        onClick={() => declareFinal(hasWarnings, {onSuccess: onClose})}>
                    {hasWarnings ? "Confirm and declare final" : "Declare final"}
                </Button>
            </DialogActions>
        </Dialog>
    )
}
