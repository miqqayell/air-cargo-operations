export type Uld = {
    code: string
    weight: number
    weightUnit: "KG" | "LB"
    destination: string
    position: string | null
}

export type DeckSide = "LEFT" | "RIGHT" | "CENTRE"

export type DeckPosition = {
    code: string
    side: DeckSide
    column: number
}

export type FloorPlan = {
    aircraftType: string
    door: string
    positions: DeckPosition[]
}

export type LoadingWindow = {
    opensAt: string
    closesAt: string
    open: boolean
}

export type Load = {
    flightNumber: string
    scheduledDepartureDate: string
    departureAirport: string
    destination: string
    status: "OPEN" | "FINAL"
    ulds: Uld[]
    floorPlan: FloorPlan
    loadingWindow: LoadingWindow
}
