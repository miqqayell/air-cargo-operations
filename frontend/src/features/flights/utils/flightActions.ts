import {ALLOWED, type Availability} from "@/types/Availability.ts"
import type {User} from "@/types/User.ts"
import {requireRole} from "@/utils/roles.ts"
import type {DelaySide, Flight} from "../types/Flight.ts"

export function canChangeDelays(flight: Flight, side: DelaySide, user: User | undefined): Availability {
    const role = requireRole(user, "flight-planner", "register, change and remove delays")
    if (!role.allowed) {
        return role
    }
    if (flight.status === "LANDED") {
        return {allowed: false, reason: "The flight has landed: its delays can no longer change."}
    }
    if (flight.status === "DEPARTED" && side === "DEPARTURE") {
        return {allowed: false, reason: "The flight has departed: its departure delays can no longer change."}
    }
    return ALLOWED
}

export function canRecordDeparture(flight: Flight, user: User | undefined): Availability {
    const role = requireRole(user, "flight-planner", "record the departure")
    if (!role.allowed) {
        return role
    }
    if (flight.status !== "SCHEDULED") {
        return {allowed: false, reason: "The departure has already been recorded."}
    }
    if (!flight.finalLoadReceived) {
        return {allowed: false, reason: "The load is not final yet: the flight cannot depart."}
    }
    return ALLOWED
}

export function canRecordArrival(flight: Flight, user: User | undefined): Availability {
    const role = requireRole(user, "flight-planner", "record the arrival")
    if (!role.allowed) {
        return role
    }
    if (flight.status === "SCHEDULED") {
        return {allowed: false, reason: "The flight has not departed yet."}
    }
    if (flight.status === "LANDED") {
        return {allowed: false, reason: "The arrival has already been recorded."}
    }
    return ALLOWED
}
