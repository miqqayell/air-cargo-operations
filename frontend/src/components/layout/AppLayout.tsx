import {AppBar, Box, Button, Container, Link, Stack, Toolbar, Typography} from "@mui/material"
import {Link as RouterLink, Outlet} from "react-router"
import {useSecurity} from "@/hooks/useSecurity.ts"
import {roleLabel} from "@/utils/roles.ts"

export function AppLayout() {
    const {loggedInUser, logout} = useSecurity()

    return (
        <Box sx={{minHeight: "100vh"}}>
            <AppBar position="static" elevation={0} sx={{borderBottom: "3px solid", borderColor: "secondary.light"}}>
                <Toolbar>
                    <Box component="img" src="/favicon.svg" alt="" sx={{width: 28, height: 28, mr: 1.5}}/>
                    <Typography variant="h6" component="div" sx={{flexGrow: 1}}>
                        <Link component={RouterLink} to="/flights" color="inherit" underline="none">
                            Air Cargo Operations
                        </Link>
                    </Typography>
                    {loggedInUser &&
                        <Stack direction="row" spacing={2} sx={{alignItems: "center"}}>
                            <Box sx={{textAlign: "right"}}>
                                <Typography variant="body2">{loggedInUser.name}</Typography>
                                <Typography variant="caption" component="div" sx={{color: "#E9CF8E"}}>
                                    {loggedInUser.roles.map(roleLabel).join(", ") || "no role"}
                                </Typography>
                            </Box>
                            <Button color="inherit" variant="outlined" size="small" onClick={logout}>
                                Log out
                            </Button>
                        </Stack>}
                </Toolbar>
            </AppBar>
            <Container maxWidth="lg" sx={{py: 4}}>
                <Outlet/>
            </Container>
        </Box>
    )
}
