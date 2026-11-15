export const TIME_ZONE = "Europe/Brussels"
export const TIME_ZONE_LABEL = "Times in Brussels local time"

const timeFormat = new Intl.DateTimeFormat("en-GB", {
    timeZone: TIME_ZONE,
    hour: "2-digit",
    minute: "2-digit",
})

const dateTimeFormat = new Intl.DateTimeFormat("en-GB", {
    timeZone: TIME_ZONE,
    weekday: "short",
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
})

const dateFormat = new Intl.DateTimeFormat("en-GB", {
    weekday: "short",
    day: "2-digit",
    month: "short",
    year: "numeric",
})

const isoDateFormat = new Intl.DateTimeFormat("en-CA", {timeZone: TIME_ZONE})

export function formatTime(instant: string | null): string {
    return instant ? timeFormat.format(new Date(instant)) : ""
}

export function formatDateTime(instant: string | null): string {
    return instant ? dateTimeFormat.format(new Date(instant)) : ""
}

export function formatDate(isoDate: string): string {
    const [year, month, day] = isoDate.split("-").map(Number)
    return dateFormat.format(new Date(year, month - 1, day))
}

export function todayInBrussels(): string {
    return isoDateFormat.format(new Date())
}

const partsFormat = new Intl.DateTimeFormat("en-CA", {
    timeZone: TIME_ZONE,
    hourCycle: "h23",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
})

function brusselsParts(date: Date) {
    const parts = Object.fromEntries(partsFormat.formatToParts(date).map(part => [part.type, Number(part.value)]))
    return {
        year: parts.year, month: parts.month, day: parts.day,
        hour: parts.hour, minute: parts.minute, second: parts.second,
    }
}

function brusselsOffsetMillis(date: Date): number {
    const parts = brusselsParts(date)
    const sameWallClockInUtc = Date.UTC(parts.year, parts.month - 1, parts.day, parts.hour, parts.minute, parts.second)
    return sameWallClockInUtc - Math.floor(date.getTime() / 1000) * 1000
}

function pad(value: number): string {
    return String(value).padStart(2, "0")
}

export function toBrusselsInput(date: Date): string {
    const parts = brusselsParts(date)
    return `${parts.year}-${pad(parts.month)}-${pad(parts.day)}T${pad(parts.hour)}:${pad(parts.minute)}`
}

export function fromBrusselsInput(value: string): Date {
    const [datePart, timePart] = value.split("T")
    const [year, month, day] = datePart.split("-").map(Number)
    const [hour, minute] = timePart.split(":").map(Number)
    const wallClockAsUtc = Date.UTC(year, month - 1, day, hour, minute)
    const firstGuess = wallClockAsUtc - brusselsOffsetMillis(new Date(wallClockAsUtc))
    return new Date(wallClockAsUtc - brusselsOffsetMillis(new Date(firstGuess)))
}
