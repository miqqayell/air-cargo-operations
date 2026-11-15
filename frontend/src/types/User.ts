export const ROLES = ["flight-planner", "load-planner", "admin"] as const

export type Role = typeof ROLES[number]

export type User = {
    username: string
    name: string
    roles: Role[]
}
