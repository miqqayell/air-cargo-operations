import type {DelaySide} from "./Flight.ts"

export type NewDelay = {
    side: DelaySide
    minutes: number
    delayCode: string
    description: string
}

export type DelayCorrection = {
    minutes: number
    description: string
}
