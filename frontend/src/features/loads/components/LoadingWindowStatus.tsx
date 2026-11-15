import {Alert} from "@mui/material"
import {formatDateTime} from "@/utils/time.ts"
import type {Load} from "../types/Load.ts"

type LoadingWindowStatusProps = {
    load: Load
    loadedAt: Date
}

export function LoadingWindowStatus({load, loadedAt}: LoadingWindowStatusProps) {
    const {opensAt, closesAt, open} = load.loadingWindow

    if (load.status === "FINAL") {
        return <Alert severity="success">The load is final. It is shown read-only.</Alert>
    }
    if (open) {
        return <Alert severity="info">The loading window is open until {formatDateTime(closesAt)}.</Alert>
    }
    if (loadedAt < new Date(opensAt)) {
        return <Alert severity="warning">The loading window opens at {formatDateTime(opensAt)}.</Alert>
    }
    return <Alert severity="warning">The loading window closed at {formatDateTime(closesAt)}.</Alert>
}
