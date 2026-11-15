import {zodResolver} from "@hookform/resolvers/zod"
import {Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField} from "@mui/material"
import {useForm} from "react-hook-form"
import {errorMessage} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {fromBrusselsInput, TIME_ZONE_LABEL, toBrusselsInput} from "@/utils/time.ts"
import {useRecordActualTime} from "../hooks/useRecordActualTime.ts"
import {type ActualTimeFormData, actualTimeFormSchema} from "../schemas/actualTimeFormSchema.ts"
import type {Movement} from "../types/Flight.ts"

type ActualTimeDialogProps = {
    flightKey: FlightKey
    movement: Movement
    openedAt: Date
    onClose: () => void
}

export function ActualTimeDialog({flightKey, movement, openedAt, onClose}: ActualTimeDialogProps) {
    const {recordActualTime, isPending, error} = useRecordActualTime(flightKey, movement)
    const {register, handleSubmit, formState: {errors}} = useForm<ActualTimeFormData>({
        resolver: zodResolver(actualTimeFormSchema),
        mode: "onTouched",
        defaultValues: {time: toBrusselsInput(openedAt)},
    })

    function onSubmit(data: ActualTimeFormData) {
        recordActualTime(fromBrusselsInput(data.time), {onSuccess: onClose})
    }

    return (
        <Dialog open onClose={onClose} fullWidth maxWidth="xs">
            <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <DialogTitle>Record the actual {movement}</DialogTitle>
                <DialogContent>
                    <Stack spacing={2} sx={{pt: 1}}>
                        {error && <Alert severity="error">{errorMessage(error)}</Alert>}
                        <TextField label={movement === "departure" ? "Actual departure" : "Actual arrival"}
                                   type="datetime-local" error={!!errors.time}
                                   helperText={errors.time?.message ?? TIME_ZONE_LABEL}
                                   slotProps={{inputLabel: {shrink: true}}}
                                   {...register("time")}/>
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onClose}>Cancel</Button>
                    <Button type="submit" variant="contained" loading={isPending}>Record</Button>
                </DialogActions>
            </form>
        </Dialog>
    )
}
