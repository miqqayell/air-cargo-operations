import {Chip, Stack, Typography} from "@mui/material"
import type {Uld} from "../types/Load.ts"

type UnplacedUldsProps = {
    ulds: Uld[]
    flightDestination: string
    selectedUld: string | null
    onSelectUld: (uldCode: string) => void
}

export function UnplacedUlds({ulds, flightDestination, selectedUld, onSelectUld}: UnplacedUldsProps) {
    if (ulds.length === 0) {
        return <Typography color="text.secondary">Every booked ULD has a position.</Typography>
    }

    return (
        <Stack direction="row" sx={{flexWrap: "wrap", gap: 1}}>
            {ulds.map(uld =>
                <Chip key={uld.code}
                      label={`${uld.code} · ${uld.destination} · ${uld.weight} ${uld.weightUnit}`}
                      variant={uld.code === selectedUld ? "filled" : "outlined"}
                      color={uld.code === selectedUld || uld.destination === flightDestination ? "primary" : "secondary"}
                      onClick={() => onSelectUld(uld.code)}/>
            )}
        </Stack>
    )
}
