import {createContext} from "react"
import type {User} from "@/types/User.ts"

export type Security = {
    isInitialised: boolean
    isAuthenticated: boolean
    loggedInUser: User | undefined
    login: () => void
    logout: () => void
}

export const SecurityContext = createContext<Security>({
    isInitialised: false,
    isAuthenticated: false,
    loggedInUser: undefined,
    login: () => {},
    logout: () => {},
})
