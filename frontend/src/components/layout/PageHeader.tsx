import {Chip, Stack, Typography} from "@mui/material"
import type {ReactNode} from "react"
import {TIME_ZONE_LABEL} from "@/utils/time.ts"

type PageHeaderProps = {
    title: ReactNode
    subtitle?: ReactNode
}

export function PageHeader({title, subtitle}: PageHeaderProps) {
    return (
        <Stack direction={{xs: "column", sm: "row"}} spacing={1}
               sx={{justifyContent: "space-between", alignItems: {xs: "flex-start", sm: "center"}}}>
            <Stack spacing={0.5}>
                <Typography variant="h4" component="h1">{title}</Typography>
                {subtitle && <Typography color="text.secondary">{subtitle}</Typography>}
            </Stack>
            <Chip size="small" variant="outlined" label={TIME_ZONE_LABEL}/>
        </Stack>
    )
}
