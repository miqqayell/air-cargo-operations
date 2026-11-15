export type Availability =
    | { allowed: true }
    | { allowed: false, reason: string }

export const ALLOWED: Availability = {allowed: true}
