import {ALLOWED, type Availability} from "@/types/Availability.ts"
import type {User} from "@/types/User.ts"
import {requireRole} from "@/utils/roles.ts"
import {formatDateTime} from "@/utils/time.ts"
import type {Load} from "../types/Load.ts"
import type {LoadReport} from "../types/LoadReport.ts"

export function canPlanLoads(user: User | undefined): Availability {
    return requireRole(user, "load-planner", "position ULDs and declare the load final")
}

export function canChangeLoad(load: Load, now: Date, user: User | undefined): Availability {
    const role = canPlanLoads(user)
    if (!role.allowed) {
        return role
    }
    if (load.status === "FINAL") {
        return {allowed: false, reason: "The load is final: it can no longer change."}
    }
    if (now < new Date(load.loadingWindow.opensAt)) {
        return {allowed: false, reason: `The loading window opens at ${formatDateTime(load.loadingWindow.opensAt)}.`}
    }
    if (!load.loadingWindow.open || now >= new Date(load.loadingWindow.closesAt)) {
        return {allowed: false, reason: `The loading window closed at ${formatDateTime(load.loadingWindow.closesAt)}.`}
    }
    return ALLOWED
}

export function canDeclareFinal(load: Load, report: LoadReport | undefined, now: Date,
                                user: User | undefined): Availability {
    const change = canChangeLoad(load, now, user)
    if (!change.allowed) {
        return change
    }
    if (!report) {
        return {allowed: false, reason: "The load checks have not run yet."}
    }
    const failedHardChecks = report.results.filter(result => result.kind === "HARD" && !result.passed)
    if (failedHardChecks.length > 0) {
        return {
            allowed: false,
            reason: `A hard check fails: ${failedHardChecks.map(result => result.name).join(", ")}.`,
        }
    }
    return ALLOWED
}
