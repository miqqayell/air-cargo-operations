import {api} from "@/lib/api.ts"
import type {FlightKey} from "@/types/FlightKey.ts"
import {flightKeyPath} from "@/utils/flightKey.ts"
import type {Load} from "../types/Load.ts"
import type {LoadReport} from "../types/LoadReport.ts"

function loadPath(key: FlightKey): string {
    return `/loads/${flightKeyPath(key)}`
}

export async function getLoad(key: FlightKey): Promise<Load> {
    const {data: load} = await api.get<Load>(loadPath(key))
    return load
}

export async function getLoadChecks(key: FlightKey): Promise<LoadReport> {
    const {data: report} = await api.get<LoadReport>(`${loadPath(key)}/checks`)
    return report
}

export async function assignPosition(key: FlightKey, uldCode: string, position: string): Promise<Load> {
    const {data: load} = await api.post<Load>(`${loadPath(key)}/positions`, {uldCode, position})
    return load
}

export async function takeOffDeck(key: FlightKey, uldCode: string): Promise<Load> {
    const {data: load} = await api.delete<Load>(`${loadPath(key)}/positions/${uldCode}`)
    return load
}

export async function declareFinal(key: FlightKey, confirmSoftWarnings: boolean): Promise<Load> {
    const {data: load} = await api.post<Load>(`${loadPath(key)}/final`, null, {params: {confirmSoftWarnings}})
    return load
}
