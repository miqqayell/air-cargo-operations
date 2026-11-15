import {Button, type ButtonProps, Stack, Typography} from "@mui/material"

type ActionButtonProps = ButtonProps & {
    unavailableReason?: string
}

export function ActionButton({unavailableReason, disabled, ...buttonProps}: ActionButtonProps) {
    return (
        <Stack spacing={0.5} sx={{alignItems: "flex-start"}}>
            <Button {...buttonProps} disabled={disabled || unavailableReason !== undefined}/>
            {unavailableReason &&
                <Typography variant="caption" color="text.secondary">{unavailableReason}</Typography>}
        </Stack>
    )
}
