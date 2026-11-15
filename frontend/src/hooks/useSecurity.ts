import {useContext} from "react"
import {SecurityContext} from "@/providers/SecurityContext.ts"

export function useSecurity() {
    return useContext(SecurityContext)
}
