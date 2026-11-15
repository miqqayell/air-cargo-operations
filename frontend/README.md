# Air Cargo Operations — frontend

React · TypeScript · Vite · pnpm · TanStack Query · React Router · MUI

## Running locally

Start the backend and Keycloak first (see the backend README); the backend listens on port 8080, Keycloak on 8180. The Vite dev server forwards every request to `/api` to it, so the browser only talks to one origin.

```bash
pnpm install
pnpm dev
```

Open http://localhost:5173.

```bash
pnpm build   # type-check and build
pnpm lint    # oxlint, including the import rules below
```

## Structure

The code is grouped by feature, not by file type.

```text
src/
├── components/   shared components: layout, loading, empty and error states
├── features/
│   ├── flights/  api, hooks, components and types of flights
│   └── loads/    api, hooks, components and types of loads
├── lib/          the axios instance and error helpers
├── hooks/        shared hooks (useSecurity)
├── pages/        one component per screen; puts the features together
├── providers/    the security context: keycloak-js and the logged-in user
├── routes/       the React Router configuration
├── theme.ts      the MUI theme: colours, fonts and component defaults
├── types/        shared types (FlightKey)
└── utils/        shared pure functions (time formatting)
```

`.oxlintrc.json` enforces the boundaries: shared code never imports a feature or a page, and a feature never imports another feature. A page, such as `FlightDetailPage`, is where flights and loads meet.

## Week 3 — Flights list and flight details

| Route | Screen |
|---|---|
| `/flights` | search and list flights |
| `/flights/:flightNumber/:date/:departureAirport` | one flight: times, delays and its load |

- **Server state** lives in TanStack Query. Every request has a data function in the feature's `api` folder and a custom hook (`useFlights`, `useFlight`, `useLoad`). The views poll every 30 seconds, because flights change while someone is looking at them.
- **The selection is part of the address.** `useFlightSearchParams` reads the search from the URL and writes it back, so a view can be shared and returned to. Without a selection, the list shows today's flights, sorted by scheduled departure time.
- **Loading, empty and error states** are shown on every screen. A list that is being refreshed stays visible (`placeholderData: keepPreviousData`). An error says what failed, shows the server's message and offers to try again. A 4xx response is not retried; a network or server error is retried twice.
- **Time.** The backend sends UTC instants. The screens show Brussels local time and say so. An estimated time is only shown when it differs from the schedule.

## Week 4 — The flight planner

On the flight screen a flight planner registers, corrects and removes delays, and records the actual departure and arrival.

- **Forms** use React Hook Form with a Zod schema (`delayFormSchema`, `actualTimeFormSchema`): the schema is the single description of a valid form, and its type is inferred with `z.infer`. Errors are shown under the field once it has been touched.
- **The delay code** is chosen from the IATA list that `GET /api/delay-codes` returns; it is never typed. When a delay is corrected, only its minutes and description can change.
- **Writes** go through one mutation hook per action (`useRegisterDelay`, `useChangeDelay`, `useRemoveDelay`, `useRecordActualTime`). The backend answers with the updated flight; `useFlightUpdate` puts it in the query cache, so the screen shows the new estimates at once, and marks the flight lists stale.
- **What is not allowed is not offered, and the screen says why.** `flightActions.ts` decides, from the state of the flight, whether delays can change and whether the departure or the arrival can be recorded, and gives the reason when not. `ActionButton` shows that reason under the disabled button. The backend still checks every rule: when it refuses, for example a departure more than five minutes after the ETD, the dialog shows its message and keeps what the planner entered.
## Week 4 — The load planner

`/flights/:flightNumber/:date/:departureAirport/load` shows the deck of the aircraft and lets a load planner place cargo on it.

- **The deck is drawn from data.** The backend sends the floor plan with the load: every position with its side and column, and the door. `DeckView` lays it out as a CSS grid, right side on top and left side below, nose and tail across both rows. Another aircraft type needs no frontend change.
- **Placing cargo.** The planner selects a ULD, on the deck or in the list of booked ULDs without a position, and then an empty position. A selected ULD on the deck can be moved the same way or taken off the deck.
- **Checks run after every change.** The mutation hooks put the returned load in the query cache and mark `loadChecks` stale, so TanStack Query fetches the check report again. The planner can also run the checks on demand. A ULD that cannot be unloaded gets a red border and the word "blocked"; the ULDs that block it get a dashed red border.
- **Declaring the load final** is offered only when the hard checks pass; otherwise the button says which check fails. A confirmation dialog names the soft warnings that remain, and confirming sends `confirmSoftWarnings=true`. Afterwards the screen is read-only.
- **The loading window** is shown with its closing time. Outside the window, or once the load is final, nothing can be changed and the screen says why. `canChangeLoad` compares with the moment the load was fetched (`dataUpdatedAt`), so rendering stays pure.

- **Local time in, UTC out.** The time fields are in Brussels local time. `fromBrusselsInput` converts them to an instant using the Brussels offset at that moment, so summer time and winter time are both handled, and the request sends UTC.

## Week 5 — Security

The web interface logs in through Keycloak with the **authorization code flow with PKCE**. The test users are listed in the backend README (`t_flight_planner`, `t_load_planner`, `t_admin`).

| What | Value (`.env.development`) |
|---|---|
| Keycloak | `VITE_KC_URL=http://localhost:8180` |
| Realm | `VITE_KC_REALM=air-freight` |
| Client | `VITE_KC_CLIENT_ID=air-freight-frontend`: public, no secret, PKCE S256 required |

Every `VITE_` variable ends up in the bundle: fine for these addresses, never for a secret. The React app has no secret at all; the verifier that PKCE makes for every login is its secret for that login.

- **`SecurityContextProvider`** creates the `Keycloak` object once and starts it with `check-sso`: after a reload Keycloak recognises its own session cookie and the user is back without a login page. `isInitCalled` makes sure `init` runs once, also under `<StrictMode>`, which runs every effect twice in development.
- **The password is typed on Keycloak's page**, never in this app. `RouteGuard` sends a user who is not logged in there, and shows "Signing in…" until the tokens are back.
- **The access token goes only to our own backend.** `addAccessTokenToAuthHeader` puts it on the `api` axios instance, not on `axios.defaults`, so it never leaves with a request to another site. When the token expires (after five minutes), `onTokenExpired` asks Keycloak for a new one with the refresh token, and the header is updated in `onAuthRefreshSuccess`. Refresh tokens are rotated by Keycloak.
- **Tokens stay in memory**, never in `localStorage`, where a script injected through XSS could read them.
- **The header** shows the name from the ID token and the role, and a "Log out" button that ends the Keycloak session.
- **Roles decide what is offered.** `requireRole` in `utils/roles.ts` is the first check of every action rule (`canChangeDelays`, `canRecordDeparture`, `canRecordArrival`, `canChangeLoad`, `canDeclareFinal`). A load planner sees the delay buttons disabled with "Only a flight planner can register, change and remove delays."; a flight planner sees the deck read-only and why. Everyone may look at everything.
- **This is UX, not security.** Hiding or disabling a button protects nothing: the backend checks the token and the role of every request again, and answers `403` to a request from the wrong role.

## Usability tests

Four people who had not seen the app each did one key task, logged in with the test user of the matching role. They were told to think aloud; we did not help them. The demo flights came from `src/test/http/seed-demo-data.sh` in the backend.

| Task | Tested by | Feedback | What we changed |
|---|---|---|---|
| Find a flight: when does HJ810 tomorrow arrive? (flight planner) | Kacper | Picked 10 November in the date filter, searched and found it without any problem. | Nothing: the filter worked as intended. |
| Register a delay of 25 minutes on HJ621 (flight planner) | Anna | Struggled to find the right IATA delay code: scrolling through the whole list of reasons takes too long. | The delay code is now a search field (`Autocomplete` in `DelayFormDialog`): typing a code or a word, such as `93` or `weather`, narrows the list to the matching codes. |
| Give ULD AKE16689R7 a position on HJ621, then move it (load planner) | Michael | Finished without problems: after a couple of uses you really get used to the loading screen. | Nothing. |
| Place the cargo so the load may fly, and declare it final (load planner) | Erik | Neither easy nor hard. The load checks help, but they could recommend what to do to distribute the weight correctly. | A failing check now says how to fix it. Lateral balance names the weight to move from the heavier to the lighter side (or to add on the lighter side); offloading says to move the blocking ULDs off the way to the door, or the cargo for the destination closer to it. The suggestion is part of the check's reason in the backend, so the frontend shows it without changes. |

# Air Cargo Operations — exam hand-in

## Challenges & Accomplishments

**Challenges**
- **The deck.** Drawing the floor plan from the backend's data, a select-then-place interaction for placing and moving ULDs, and showing on the deck which ULD is blocked and which ULD blocks it.
- **Time zones.** Everything in the backend is UTC; planners see and enter Brussels local time, in summer and in winter time.
- **Keeping server state fresh.** After every change the right queries are invalidated, the load checks run again after each ULD change, and lists poll every 30 seconds.
- **Login with Keycloak.** Authorization code flow with PKCE, a session that survives a reload, token refresh, and `init` running once under `StrictMode`.

**What we are most proud of**
- **Unavailable actions explain themselves.** Instead of hiding a button, it is disabled with the reason: the role, the state of the flight or the loading window.
- **Changes driven by real users.** The usability tests led to a searchable delay code and to load checks that say how to fix a failing load.
- **A consistent look**, from the Keycloak login page to the deck.

---

## ✅ Finished Features

- [x] Login and logout through Keycloak (PKCE), a header with the user's name and role, a route guard
- [x] Flight list: filter on flight number, date, departure and arrival airport and state; the selection is in the URL; opens on today's flights sorted by STD
- [x] Loading, empty and error states on every screen, with "Try again"; a failed refresh keeps the last data on screen
- [x] Flight details: schedule, delays, scheduled, estimated (only when different) and actual times, booked ULDs and the load
- [x] Register, correct and remove delays (React Hook Form and Zod), with a searchable IATA delay code
- [x] Record the actual departure and arrival, entered in Brussels local time
- [x] Load planning: the deck with its door, the loading window, place, move and take off ULDs, checks after every change and on demand, blocked ULDs marked on the deck, declare final with confirmation of the soft warnings, read-only afterwards
- [x] Actions offered per role, with the reason when something is not available
- [x] Usability tests with four users, reported above

---

## ❌ Unfinished / Planned Features

- [ ] Optimistic updates: after saving, the screen waits for the backend's answer before it changes
- [ ] Live updates in full: lists poll every 30 seconds and a failed refresh keeps the last data with a warning, but the screen does not say since when the data may be out of date
- [ ] Later phases from the assignment: autoload, and a day chart of all flights on a timeline
