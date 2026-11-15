import Keycloak from "keycloak-js"
import {type PropsWithChildren, useCallback, useEffect, useMemo, useState} from "react"
import {addAccessTokenToAuthHeader, removeAccessTokenFromAuthHeader} from "@/lib/api.ts"
import {ROLES, type Role, type User} from "@/types/User.ts"
import {SecurityContext} from "./SecurityContext.ts"

const keycloak = new Keycloak({
    url: import.meta.env.VITE_KC_URL,
    realm: import.meta.env.VITE_KC_REALM,
    clientId: import.meta.env.VITE_KC_CLIENT_ID,
})
let isInitCalled = false

function userFromTokens(): User {
    const realmRoles: string[] = keycloak.tokenParsed?.realm_access?.roles ?? []
    return {
        username: keycloak.idTokenParsed?.preferred_username ?? "",
        name: keycloak.idTokenParsed?.given_name ?? keycloak.idTokenParsed?.preferred_username ?? "",
        roles: realmRoles.filter((role): role is Role => (ROLES as readonly string[]).includes(role)),
    }
}

export function SecurityContextProvider({children}: PropsWithChildren) {
    const [loggedInUser, setLoggedInUser] = useState<User>()
    const [isInitialised, setIsInitialised] = useState(false)

    useEffect(() => {
        function updateUserFromToken() {
            addAccessTokenToAuthHeader(keycloak.token)
            setLoggedInUser(userFromTokens())
        }

        function clearUser() {
            removeAccessTokenFromAuthHeader()
            setLoggedInUser(undefined)
        }

        keycloak.onAuthSuccess = updateUserFromToken
        keycloak.onAuthRefreshSuccess = updateUserFromToken
        keycloak.onAuthLogout = clearUser
        keycloak.onAuthError = clearUser
        keycloak.onTokenExpired = () => keycloak.updateToken().catch(clearUser)
        if (isInitCalled) {
            return
        }
        isInitCalled = true
        keycloak.init({onLoad: "check-sso", pkceMethod: "S256"})
            .catch(clearUser)
            .finally(() => setIsInitialised(true))
    }, [])

    const login = useCallback(() => keycloak.login(), [])
    const logout = useCallback(() => keycloak.logout({redirectUri: window.location.origin}), [])

    const security = useMemo(() => ({
        isInitialised,
        isAuthenticated: loggedInUser !== undefined,
        loggedInUser,
        login,
        logout,
    }), [isInitialised, loggedInUser, login, logout])

    return <SecurityContext value={security}>{children}</SecurityContext>
}
