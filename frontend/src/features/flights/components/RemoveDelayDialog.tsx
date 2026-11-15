import {Alert, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material"
import {errorMessage} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {useRemoveDelay} from "../hooks/useDelayMutations.ts"
import type {Delay} from "../types/Flight.ts"
import {delaySideLabel} from "../utils/delaySideLabel.ts"

type RemoveDelayDialogProps = {
    flightKey: FlightKey
    delay: Delay
    onClose: () => void
}

export function RemoveDelayDialog({flightKey, delay, onClose}: RemoveDelayDialogProps) {
    const {removeDelay, isPending, error} = useRemoveDelay(flightKey)

    return (
        <Dialog open onClose={onClose} fullWidth maxWidth="xs">
            <DialogTitle>Remove the delay?</DialogTitle>
            <DialogContent>
                {error && <Alert severity="error" sx={{mb: 2}}>{errorMessage(error)}</Alert>}
                <DialogContentText>
                    {delaySideLabel(delay.side)} delay of {delay.minutes} minutes, code {delay.delayCode}:
                    "{delay.description}". The estimated times are recalculated and the partners are informed.
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>Cancel</Button>
                <Button color="error" variant="contained" loading={isPending}
                        onClick={() => removeDelay(delay.id, {onSuccess: onClose})}>
                    Remove
                </Button>
            </DialogActions>
        </Dialog>
    )
}
