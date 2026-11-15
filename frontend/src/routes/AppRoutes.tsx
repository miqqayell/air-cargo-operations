import {Navigate, Route, Routes} from "react-router"
import {AppLayout} from "@/components/layout/AppLayout.tsx"
import {RouteGuard} from "@/components/layout/RouteGuard.tsx"
import {FlightDetailPage} from "@/pages/FlightDetailPage.tsx"
import {FlightListPage} from "@/pages/FlightListPage.tsx"
import {LoadPlanningPage} from "@/pages/LoadPlanningPage.tsx"
import {NotFoundPage} from "@/pages/NotFoundPage.tsx"

export function AppRoutes() {
    return (
        <Routes>
            <Route element={<RouteGuard><AppLayout/></RouteGuard>}>
                <Route path="/" element={<Navigate to="/flights" replace/>}/>
                <Route path="/flights" element={<FlightListPage/>}/>
                <Route path="/flights/:flightNumber/:date/:departureAirport" element={<FlightDetailPage/>}/>
                <Route path="/flights/:flightNumber/:date/:departureAirport/load" element={<LoadPlanningPage/>}/>
                <Route path="*" element={<NotFoundPage/>}/>
            </Route>
        </Routes>
    )
}
