import {Alert, Button, Stack, Typography} from "@mui/material"
import {useState} from "react"
import {ActionButton} from "@/components/ui/ActionButton.tsx"
import {ErrorState} from "@/components/ui/ErrorState.tsx"
import {LoadingState} from "@/components/ui/LoadingState.tsx"
import {useSecurity} from "@/hooks/useSecurity.ts"
import {errorMessage} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {useLoad} from "../hooks/useLoad.ts"
import {useLoadChecks} from "../hooks/useLoadChecks.ts"
import {useAssignPosition, useTakeOffDeck} from "../hooks/useLoadMutations.ts"
import {canChangeLoad, canDeclareFinal, canPlanLoads} from "../utils/loadActions.ts"
import {DeckView} from "./DeckView.tsx"
import {DeclareFinalDialog} from "./DeclareFinalDialog.tsx"
import {LoadChecksPanel} from "./LoadChecksPanel.tsx"
import {LoadingWindowStatus} from "./LoadingWindowStatus.tsx"
import {UnplacedUlds} from "./UnplacedUlds.tsx"

type LoadPlannerProps = {
    flightKey: FlightKey
}

export function LoadPlanner({flightKey}: LoadPlannerProps) {
    const {loggedInUser} = useSecurity()
    const {load, loadedAt, isPending, error, refetch} = useLoad(flightKey)
    const checks = useLoadChecks(flightKey)
    const placing = useAssignPosition(flightKey)
    const removing = useTakeOffDeck(flightKey)
    const [selectedUld, setSelectedUld] = useState<string | null>(null)
    const [confirmingFinal, setConfirmingFinal] = useState(false)

    if (isPending) {
        return <LoadingState message="Loading the deck…"/>
    }
    if (!load) {
        return <ErrorState message="The load could not be loaded" detail={errorMessage(error)} onRetry={refetch}/>
    }

    const planner = canPlanLoads(loggedInUser)
    const change = canChangeLoad(load, loadedAt, loggedInUser)
    const final = canDeclareFinal(load, checks.report, loadedAt, loggedInUser)
    const offloading = checks.report?.results.find(result => result.blockedUlds.length > 0)
    const selected = load.ulds.find(uld => uld.code === selectedUld)
    const actionError = placing.error ?? removing.error

    function selectUld(uldCode: string) {
        if (!change.allowed) {
            return
        }
        placing.reset()
        removing.reset()
        setSelectedUld(current => current === uldCode ? null : uldCode)
    }

    function placeSelectedUld(position: string) {
        if (selectedUld) {
            placing.assignPosition({uldCode: selectedUld, position}, {onSuccess: () => setSelectedUld(null)})
        }
    }

    function takeSelectedUldOffDeck() {
        if (selectedUld) {
            removing.takeOffDeck(selectedUld, {onSuccess: () => setSelectedUld(null)})
        }
    }

    return (
        <Stack spacing={3}>
            <LoadingWindowStatus load={load} loadedAt={loadedAt}/>
            {!planner.allowed && <Alert severity="info">{planner.reason} The deck is shown read-only.</Alert>}
            <Stack spacing={1}>
                <Typography variant="h6" component="h2">
                    Main deck · {load.floorPlan.aircraftType}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    Nose on the left, seen from above. Rose: cargo for {load.destination}. Gold: cargo for another
                    destination. A red border marks cargo that cannot leave the aircraft; a dashed red border marks what
                    blocks it.
                </Typography>
                {change.allowed &&
                    <Stack direction="row" spacing={2} sx={{alignItems: "center", minHeight: 40}}>
                        {selected
                            ? <>
                                <Typography>
                                    {selected.code} selected: {selected.position
                                    ? "choose an empty position to move it, or"
                                    : "choose an empty position on the deck."}
                                </Typography>
                                {selected.position &&
                                    <Button variant="outlined" color="error" loading={removing.isPending}
                                            onClick={takeSelectedUldOffDeck}>
                                        Take off the deck
                                    </Button>}
                                <Button onClick={() => setSelectedUld(null)}>Cancel</Button>
                            </>
                            : <Typography color="text.secondary">Choose a ULD, then an empty position.</Typography>}
                    </Stack>}
                {actionError && <Alert severity="error">{errorMessage(actionError)}</Alert>}
                <DeckView load={load} selectedUld={selectedUld} blockedUlds={offloading?.blockedUlds ?? []}
                          editable={change.allowed} onSelectUld={selectUld} onSelectPosition={placeSelectedUld}/>
            </Stack>
            <Stack spacing={1}>
                <Typography variant="h6" component="h2">Booked, not on the deck</Typography>
                <UnplacedUlds ulds={load.ulds.filter(uld => uld.position === null)} flightDestination={load.destination}
                              selectedUld={selectedUld} onSelectUld={selectUld}/>
            </Stack>
            <Stack spacing={1}>
                <Typography variant="h6" component="h2">Load checks</Typography>
                <LoadChecksPanel report={checks.report} isPending={checks.isPending} isFetching={checks.isFetching}
                                 error={checks.error} onRunChecks={() => checks.refetch()}/>
            </Stack>
            {load.status === "OPEN" &&
                <ActionButton variant="contained" size="large" onClick={() => setConfirmingFinal(true)}
                              unavailableReason={final.allowed ? undefined : final.reason}>
                    Declare the load final
                </ActionButton>}
            {confirmingFinal &&
                <DeclareFinalDialog flightKey={flightKey}
                                    softWarnings={checks.report?.results.filter(result => result.kind === "SOFT" && !result.passed) ?? []}
                                    onClose={() => setConfirmingFinal(false)}/>}
        </Stack>
    )
}
