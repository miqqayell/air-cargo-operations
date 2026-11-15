export type BlockedUld = {
    uldCode: string
    blockedBy: string[]
}

export type CheckResult = {
    name: string
    kind: "HARD" | "SOFT"
    passed: boolean
    reason: string | null
    blockedUlds: BlockedUld[]
}

export type LoadReport = {
    hardChecksPass: boolean
    results: CheckResult[]
}
