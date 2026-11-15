import {Box, Tooltip, Typography} from "@mui/material"
import type {DeckPosition, Load, Uld} from "../types/Load.ts"
import type {BlockedUld} from "../types/LoadReport.ts"

type DeckViewProps = {
    load: Load
    selectedUld: string | null
    blockedUlds: BlockedUld[]
    editable: boolean
    onSelectUld: (uldCode: string) => void
    onSelectPosition: (position: string) => void
}

function gridRow(position: DeckPosition): string {
    if (position.side === "RIGHT") {
        return "1"
    }
    if (position.side === "LEFT") {
        return "2"
    }
    return "1 / span 2"
}

export function DeckView({load, selectedUld, blockedUlds, editable, onSelectUld, onSelectPosition}: DeckViewProps) {
    const positions = load.floorPlan.positions
    const columns = Math.max(...positions.map(position => position.column)) + 1
    const uldAt = new Map(load.ulds.filter(uld => uld.position).map(uld => [uld.position!, uld]))
    const door = positions.find(position => position.code === load.floorPlan.door)
    const blockingUlds = new Set(blockedUlds.flatMap(blocked => blocked.blockedBy))

    return (
        <Box sx={{overflowX: "auto", pb: 1}}>
            <Box sx={{
                minWidth: columns * 104 + 72,
                p: 1.5,
                pl: 6,
                bgcolor: "background.paper",
                border: "1px solid",
                borderColor: "grey.300",
                borderRadius: "110px 24px 24px 110px",
                boxSizing: "border-box",
            }}>
            <Box sx={{
                display: "grid",
                gridTemplateColumns: `repeat(${columns}, minmax(100px, 1fr))`,
                gridTemplateRows: "88px 88px 22px",
                gap: 0.5,
            }}>
                {positions.map(position =>
                    <DeckCell key={position.code}
                              position={position}
                              uld={uldAt.get(position.code)}
                              flightDestination={load.destination}
                              selected={uldAt.get(position.code)?.code === selectedUld}
                              blockedBy={blockedUlds.find(blocked => blocked.uldCode === uldAt.get(position.code)?.code)?.blockedBy}
                              blocking={blockingUlds.has(uldAt.get(position.code)?.code ?? "")}
                              canDrop={editable && selectedUld !== null && !uldAt.has(position.code)}
                              onSelectUld={onSelectUld}
                              onSelectPosition={onSelectPosition}/>
                )}
                {door &&
                    <Typography variant="caption" color="text.secondary"
                                sx={{gridColumn: door.column + 1, gridRow: 3, textAlign: "center"}}>
                        ▲ cargo door
                    </Typography>}
            </Box>
            </Box>
        </Box>
    )
}

type DeckCellProps = {
    position: DeckPosition
    uld: Uld | undefined
    flightDestination: string
    selected: boolean
    blockedBy: string[] | undefined
    blocking: boolean
    canDrop: boolean
    onSelectUld: (uldCode: string) => void
    onSelectPosition: (position: string) => void
}

function DeckCell({position, uld, flightDestination, selected, blockedBy, blocking, canDrop, onSelectUld, onSelectPosition}: DeckCellProps) {
    const forThisDestination = uld?.destination === flightDestination
    const cell = (
        <Box component="button" type="button"
             aria-label={uld ? `${position.code}: ${uld.code}` : `${position.code}: empty`}
             onClick={() => uld ? onSelectUld(uld.code) : canDrop && onSelectPosition(position.code)}
             sx={{
                 gridColumn: position.column + 1,
                 gridRow: gridRow(position),
                 p: 0.5,
                 display: "flex",
                 flexDirection: "column",
                 justifyContent: "flex-start",
                 textAlign: "left",
                 font: "inherit",
                 borderRadius: 1,
                 cursor: uld || canDrop ? "pointer" : "default",
                 border: selected ? "3px solid" : blockedBy ? "2px solid" : blocking ? "2px dashed" : uld ? "1px solid" : "1px dashed",
                 borderColor: selected ? "primary.dark" : blockedBy || blocking ? "error.main" : uld ? "grey.500" : "grey.300",
                 bgcolor: uld ? (forThisDestination ? "tint.bordeaux" : "tint.gold") : canDrop ? "action.hover" : "background.paper",
                 "&:hover": canDrop ? {borderColor: "primary.main"} : undefined,
             }}>
            <Typography variant="caption" color="text.secondary" component="div">{position.code}</Typography>
            {uld &&
                <>
                    <Typography variant="caption" component="div" sx={{lineHeight: 1.2}}>
                        {uld.code}
                    </Typography>
                    <Typography variant="caption" component="div" sx={{lineHeight: 1.2}}>
                        {uld.destination} · {uld.weight} {uld.weightUnit}
                    </Typography>
                    {blockedBy &&
                        <Typography variant="caption" color="error" component="div" sx={{lineHeight: 1.2}}>
                            blocked
                        </Typography>}
                </>}
        </Box>
    )

    if (blockedBy) {
        return <Tooltip title={`Blocked by ${blockedBy.join(", ")}`}>{cell}</Tooltip>
    }
    return cell
}
