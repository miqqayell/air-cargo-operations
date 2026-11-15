import {CssBaseline, ThemeProvider} from "@mui/material"
import {QueryClient, QueryClientProvider} from "@tanstack/react-query"
import {ReactQueryDevtools} from "@tanstack/react-query-devtools"
import {BrowserRouter} from "react-router"
import {isClientError} from "@/lib/api.ts"
import {SecurityContextProvider} from "@/providers/SecurityContextProvider.tsx"
import {AppRoutes} from "@/routes/AppRoutes.tsx"
import {theme} from "@/theme.ts"

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: (failureCount, error) => !isClientError(error) && failureCount < 2,
        },
    },
})

export function App() {
    return (
        <SecurityContextProvider>
            <QueryClientProvider client={queryClient}>
                <ThemeProvider theme={theme}>
                    <CssBaseline/>
                    <BrowserRouter>
                        <AppRoutes/>
                    </BrowserRouter>
                </ThemeProvider>
                <ReactQueryDevtools/>
            </QueryClientProvider>
        </SecurityContextProvider>
    )
}
