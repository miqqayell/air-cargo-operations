import {alpha, createTheme} from "@mui/material"

declare module "@mui/material/styles" {
    interface Palette {
        tint: { bordeaux: string, gold: string }
    }

    interface PaletteOptions {
        tint: { bordeaux: string, gold: string }
    }
}

const BORDEAUX = "#6E1026"
const GOLD = "#96700A"
const HELVETICA = "Helvetica, \"Helvetica Neue\", Arial, sans-serif"

export const theme = createTheme({
    palette: {
        primary: {main: BORDEAUX, light: "#8E2A41", dark: "#4F0A1A", contrastText: "#FFF9F5"},
        secondary: {main: GOLD, light: "#B8922E", dark: "#735507", contrastText: "#FFFFFF"},
        error: {main: "#C0301C"},
        warning: {main: "#B25E09"},
        success: {main: "#2E6B4A"},
        info: {main: "#6F5B5D"},
        grey: {
            50: "#FAF6F5",
            100: "#F3EDEC",
            200: "#E7DEDD",
            300: "#D6CBCA",
            400: "#B5A8A7",
            500: "#928483",
            600: "#6F5F5F",
            700: "#574949",
            800: "#3B2F30",
            900: "#251C1D",
        },
        background: {default: "#F8F3F1", paper: "#FFFCFB"},
        text: {primary: "#251C1D", secondary: "#6B5A5B"},
        divider: "#E4D9D7",
        action: {hover: alpha(BORDEAUX, 0.05), selected: alpha(BORDEAUX, 0.1)},
        tint: {bordeaux: "#F6E7EA", gold: "#F7EED6"},
    },
    shape: {borderRadius: 8},
    typography: {
        fontFamily: HELVETICA,
        fontWeightLight: 700,
        fontWeightRegular: 700,
        fontWeightMedium: 700,
        fontWeightBold: 700,
        h4: {fontSize: "2rem", letterSpacing: "-0.01em"},
        h6: {fontSize: "1.25rem"},
        button: {textTransform: "none"},
    },
    components: {
        MuiButton: {
            defaultProps: {disableElevation: true},
        },
        MuiChip: {
            styleOverrides: {
                root: {borderRadius: 6},
            },
        },
        MuiAlert: {
            styleOverrides: {
                root: ({theme, ownerState}) => ({
                    border: `1px solid ${alpha(theme.palette[ownerState.severity ?? "success"].main, 0.25)}`,
                }),
            },
        },
        MuiLink: {
            defaultProps: {underline: "hover"},
        },
        MuiLinearProgress: {
            defaultProps: {color: "secondary"},
        },
        MuiTableCell: {
            styleOverrides: {
                root: {fontVariantNumeric: "tabular-nums"},
                body: {fontWeight: 400, "& .MuiTypography-body2": {fontWeight: 400}},
                head: ({theme}) => ({
                    backgroundColor: theme.palette.grey[100],
                    color: theme.palette.text.secondary,
                    fontSize: "0.75rem",
                    fontWeight: 700,
                    letterSpacing: "0.04em",
                }),
            },
        },
        MuiTableRow: {
            styleOverrides: {
                root: {"&:last-child td": {borderBottom: 0}},
            },
        },
    },
})
