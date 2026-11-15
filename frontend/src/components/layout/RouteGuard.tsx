import {type PropsWithChildren, useEffect} from "react"
import {LoadingState} from "@/components/ui/LoadingState.tsx"
import {useSecurity} from "@/hooks/useSecurity.ts"

export function RouteGuard({children}: PropsWithChildren) {
    const {isInitialised, isAuthenticated, login} = useSecurity()

    useEffect(() => {
        if (isInitialised && !isAuthenticated) {
            login()
        }
    }, [isInitialised, isAuthenticated, login])

    if (!isAuthenticated) {
        return <LoadingState message="Signing in…"/>
    }
    return children
}
