import {z} from "zod"
import {fromBrusselsInput} from "@/utils/time.ts"

export const actualTimeFormSchema = z.object({
    time: z.string()
        .min(1, "Enter the time")
        .refine(value => value === "" || fromBrusselsInput(value).getTime() <= Date.now(),
            "An actual time cannot lie in the future"),
})

export type ActualTimeFormData = z.infer<typeof actualTimeFormSchema>
