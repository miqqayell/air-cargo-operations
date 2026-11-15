import {Link, Stack, Typography} from "@mui/material"
import {Link as RouterLink} from "react-router"

export function NotFoundPage() {
    return (
        <Stack spacing={2}>
            <Typography variant="h4" component="h1">Page not found</Typography>
            <Link component={RouterLink} to="/flights">Go to the flights</Link>
        </Stack>
    )
}
