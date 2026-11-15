import {zodResolver} from "@hookform/resolvers/zod"
import {
    Alert,
    Autocomplete,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    MenuItem,
    Stack,
    TextField,
} from "@mui/material"
import {Controller, useForm} from "react-hook-form"
import {useSecurity} from "@/hooks/useSecurity.ts"
import {errorMessage} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {useDelayCodes} from "../hooks/useDelayCodes.ts"
import {useChangeDelay, useRegisterDelay} from "../hooks/useDelayMutations.ts"
import {type DelayFormData, delayFormSchema, MAX_DELAY_MINUTES} from "../schemas/delayFormSchema.ts"
import type {DelayCode} from "../types/DelayCode.ts"
import type {Delay, Flight} from "../types/Flight.ts"
import {delaySideLabel} from "../utils/delaySideLabel.ts"
import {canChangeDelays} from "../utils/flightActions.ts"

function delayCodeLabel(delayCode: DelayCode): string {
    return delayCode.description ? `${delayCode.code} · ${delayCode.description}` : delayCode.code
}

type DelayFormDialogProps = {
    flight: Flight
    flightKey: FlightKey
    delay?: Delay
    onClose: () => void
}

export function DelayFormDialog({flight, flightKey, delay, onClose}: DelayFormDialogProps) {
    const isCorrection = delay !== undefined
    const {loggedInUser} = useSecurity()
    const departure = canChangeDelays(flight, "DEPARTURE", loggedInUser)
    const {delayCodes, isPending: codesPending} = useDelayCodes()
    const {registerDelay, isPending: registering, error: registerError} = useRegisterDelay(flightKey)
    const {changeDelay, isPending: changing, error: changeError} = useChangeDelay(flightKey)
    const serverError = registerError ?? changeError

    const {register, handleSubmit, control, formState: {errors}} = useForm<DelayFormData>({
        resolver: zodResolver(delayFormSchema),
        mode: "onTouched",
        defaultValues: delay
            ? {side: delay.side, minutes: delay.minutes, delayCode: delay.delayCode, description: delay.description}
            : {side: departure.allowed ? "DEPARTURE" : "ARRIVAL", delayCode: "", description: ""},
    })

    function onSubmit(data: DelayFormData) {
        if (isCorrection) {
            changeDelay({delayId: delay.id, correction: {minutes: data.minutes, description: data.description}},
                {onSuccess: onClose})
        } else {
            registerDelay(data, {onSuccess: onClose})
        }
    }

    return (
        <Dialog open onClose={onClose} fullWidth maxWidth="sm">
            <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <DialogTitle>{isCorrection ? "Correct the delay" : "Register a delay"}</DialogTitle>
                <DialogContent>
                    <Stack spacing={2} sx={{pt: 1}}>
                        {serverError && <Alert severity="error">{errorMessage(serverError)}</Alert>}
                        <Controller name="side" control={control} render={({field, fieldState}) =>
                            <TextField {...field} select label="On" disabled={isCorrection}
                                       error={!!fieldState.error}
                                       helperText={fieldState.error?.message
                                           ?? (!isCorrection && !departure.allowed ? departure.reason : undefined)}>
                                <MenuItem value="DEPARTURE" disabled={!departure.allowed}>
                                    {delaySideLabel("DEPARTURE")}
                                </MenuItem>
                                <MenuItem value="ARRIVAL">{delaySideLabel("ARRIVAL")}</MenuItem>
                            </TextField>
                        }/>
                        <TextField label="Minutes" type="number" error={!!errors.minutes}
                                   helperText={errors.minutes?.message ?? `1 to ${MAX_DELAY_MINUTES} minutes`}
                                   slotProps={{htmlInput: {min: 1, max: MAX_DELAY_MINUTES}}}
                                   {...register("minutes", {valueAsNumber: true})}/>
                        <Controller name="delayCode" control={control} render={({field, fieldState}) =>
                            <Autocomplete
                                options={delayCodes ?? []}
                                value={delayCodes?.find(delayCode => delayCode.code === field.value)
                                    ?? (field.value ? {code: field.value, description: ""} : null)}
                                onChange={(_, delayCode) => field.onChange(delayCode?.code ?? "")}
                                onBlur={field.onBlur}
                                getOptionLabel={delayCodeLabel}
                                isOptionEqualToValue={(option, value) => option.code === value.code}
                                disabled={isCorrection || codesPending}
                                loading={codesPending}
                                autoHighlight
                                renderInput={params =>
                                    <TextField {...params} label="IATA delay code"
                                               placeholder="Type a code or a word, such as 93 or weather"
                                               error={!!fieldState.error}
                                               helperText={fieldState.error?.message
                                                   ?? (codesPending ? "Loading the codes…" : undefined)}/>}/>
                        }/>
                        <TextField label="Description" multiline minRows={2} error={!!errors.description}
                                   helperText={errors.description?.message} {...register("description")}/>
                    </Stack>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onClose}>Cancel</Button>
                    <Button type="submit" variant="contained" loading={registering || changing}>
                        {isCorrection ? "Save" : "Register"}
                    </Button>
                </DialogActions>
            </form>
        </Dialog>
    )
}
