import axios, {isAxiosError} from "axios"

export const api = axios.create({baseURL: "/api"})

export function addAccessTokenToAuthHeader(token: string | undefined) {
    if (token) {
        api.defaults.headers.common["Authorization"] = `Bearer ${token}`
    } else {
        removeAccessTokenFromAuthHeader()
    }
}

export function removeAccessTokenFromAuthHeader() {
    delete api.defaults.headers.common["Authorization"]
}

type ProblemDetail = {
    detail?: string
}

export function errorMessage(error: unknown): string {
    if (isAxiosError<ProblemDetail>(error)) {
        return error.response?.data?.detail ?? messageForStatus(error.response?.status) ?? error.message
    }
    return error instanceof Error ? error.message : String(error)
}

function messageForStatus(status: number | undefined): string | undefined {
    if (status === 401) {
        return "You are no longer logged in, log in again"
    }
    if (status === 403) {
        return "Your role does not allow this action"
    }
    return undefined
}

export function isNotFound(error: unknown): boolean {
    return isAxiosError(error) && error.response?.status === 404
}

export function isClientError(error: unknown): boolean {
    const status = isAxiosError(error) ? error.response?.status : undefined
    return status !== undefined && status >= 400 && status < 500
}
