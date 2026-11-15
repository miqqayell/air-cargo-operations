import {ALLOWED, type Availability} from "@/types/Availability.ts"
import type {Role, User} from "@/types/User.ts"

const ROLE_LABELS: Record<Role, string> = {
    "flight-planner": "flight planner",
    "load-planner": "load planner",
    "admin": "admin",
}

export function roleLabel(role: Role): string {
    return ROLE_LABELS[role]
}

export function requireRole(user: User | undefined, role: Role, action: string): Availability {
    if (user?.roles.includes(role)) {
        return ALLOWED
    }
    return {allowed: false, reason: `Only a ${roleLabel(role)} can ${action}.`}
}
