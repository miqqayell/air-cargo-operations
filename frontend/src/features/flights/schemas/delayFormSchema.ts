import {z} from "zod"

export const MAX_DELAY_MINUTES = 12 * 60
export const MAX_DESCRIPTION_LENGTH = 255

export const delayFormSchema = z.object({
    side: z.enum(["DEPARTURE", "ARRIVAL"], {error: "Choose departure or arrival"}),
    minutes: z.number({error: "Enter the number of minutes"})
        .int("Whole minutes only")
        .min(1, "At least 1 minute")
        .max(MAX_DELAY_MINUTES, `At most 12 hours (${MAX_DELAY_MINUTES} minutes)`),
    delayCode: z.string().min(1, "Choose an IATA delay code"),
    description: z.string()
        .trim()
        .min(1, "Describe the delay")
        .max(MAX_DESCRIPTION_LENGTH, `At most ${MAX_DESCRIPTION_LENGTH} characters`),
})

export type DelayFormData = z.infer<typeof delayFormSchema>
