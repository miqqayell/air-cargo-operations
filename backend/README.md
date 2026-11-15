# Air Cargo Operations — backend

Spring Boot · Java 21 · Gradle · PostgreSQL · RabbitMQ · Keycloak

## Running locally

Start everything from `docker-compose.yaml`. On the first start PostgreSQL runs `init-scripts/sql/schema-creation.sql`, which creates the schemas and tables, and Keycloak imports the realm `air-freight` from `keycloak/import/air-freight-realm.json`.

```bash
docker compose up -d
./gradlew bootRun
```

| Service | Address |
|---|---|
| the backend | http://localhost:8080 |
| RabbitMQ management | http://localhost:15672 (user `user`, password `password`) |
| Keycloak admin console | http://localhost:8180 (user `admin`, password `admin`) |
| partner-service mock | http://localhost:8000 |

To fill an empty system with flights and bookings for today and tomorrow, while the backend and Keycloak run:

```bash
src/test/http/seed-demo-data.sh
```

### Keycloak

| What | Value |
|---|---|
| Realm | `air-freight` |
| Issuer | http://localhost:8180/realms/air-freight |
| Realm roles | `flight-planner`, `load-planner`, `admin` |
| Client of the web interface | `air-freight-frontend`: public, standard flow with PKCE S256, redirect `http://localhost:5173/*` |
| Client for the `.http` file | `air-freight-api`: public, direct access grants (password grant) only |

Both clients put `air-freight-api` in the audience of their access tokens, and refresh tokens are rotated (*Revoke Refresh Token*).

| User | Password | Realm role |
|---|---|---|
| `t_flight_planner` | `t_password` | `flight-planner` |
| `t_load_planner` | `t_password` | `load-planner` |
| `t_admin` | `t_password` | `admin` |

These are test users of the local development realm only. The realm is imported once: Keycloak keeps it in the MySQL database (`idp_mysql_data/`) and skips the import when the realm already exists. To import a changed realm file, delete the realm in the admin console, or stop the containers and delete `idp_mysql_data/`.

The login page uses the theme in `keycloak/themes/air-freight`, in the same colours as the web interface. A realm imported before this theme existed gets it with:

```bash
docker compose up -d idp_keycloak
docker exec kdg-idp_keycloak-1 /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
docker exec kdg-idp_keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/air-freight -s loginTheme=air-freight -s "displayName=Air Cargo Operations"
```

`src/test/http/air-freight.http` logs in with the password grant and stores the access token for the requests below it. The password grant is only for testing by hand; the web interface uses the authorization code flow with PKCE.

The tests start their own PostgreSQL and RabbitMQ with Testcontainers, so Docker must be running:

```bash
./gradlew test
```

To start again with an empty database, stop the container and delete the `postgresql/` folder.

## Week 1 — DDD, Spring Modulith and Hexagonal Architecture

### Strategic design

The assignment is divided into subdomains based on the business responsibilities described by the domain:

| Subdomain | Type |
|---|---|
| Flight operations | Core |
| Load planning | Core |
| Partner messaging | Supporting |
| Incoming feeds and simulation | Supporting |
| Policy settings | Supporting |
| Identity and access | Generic |

Week 1 starts with the two core bounded contexts needed for the domain model:

- `flights`
- `loading`

### Bounded contexts

`flights` owns the `Flight` aggregate and its delay and lifecycle rules.

`loading` owns the `Load` aggregate and its ULD and position rules.

The `common` module is a small shared kernel. It contains only concepts that both contexts need to agree on, such as:

- `FlightId`
- `FlightNumber`
- `AirportCode`
- shared domain event types used to collaborate between modules

The loading context refers to a flight through `FlightId`; it does not import the `Flight` aggregate.

### Tactical DDD model

`Flight` is an aggregate root. `Delay` is an entity inside the Flight aggregate.

`Load` is an aggregate root. `Uld` is an entity inside the Load aggregate.

Value objects are used for concepts such as identifiers, airport codes, aircraft registrations, ULD codes, positions and weights.

Business rules stay in the domain model:

- ETD = STD + all departure delays
- ETA = STA + all departure delays + all arrival delays
- a landed flight cannot receive another delay
- after departure, another departure delay cannot be registered
- a flight cannot depart before the load is final
- a position can contain at most one ULD
- a final load can no longer be changed
- the flight lifecycle is Scheduled → Departed → Landed

### Spring Modulith

The bounded contexts are application modules in one Spring Boot application.

- `flights` is a closed module.
- `loading` is a closed module.
- `common` is an open shared kernel.

The public API of a closed module lives in its root package. Internal domain, application and adapter packages are kept behind the module boundary.

The architecture test uses `ApplicationModules.of(...).verify()` to keep the module boundaries checked.

Modules collaborate in two ways shown in the Week 1 material:

- through a public module API when a known module is asked to do something;
- through ordinary Spring application events when a fact that happened should be delivered to interested modules.

### Hexagonal Architecture

Each bounded context separates its application core from technical details.

```text
adapter/in -> input port -> use case -> domain model
                                      -> output port -> adapter/out
```

Input adapters translate external input into commands and call input ports.

Use cases implement input ports and coordinate the work. When state changes, they use output ports.

Output adapters implement output ports. Week 1 uses in-memory persistence and Spring application-event publishing as technology examples; database and broker adapters belong to later work.

The domain model itself has no Spring dependency.

### Week 1 scope

This repository intentionally stops at the material covered in Week 1:

- Domain Driven Design
- bounded contexts, aggregates, entities and value objects
- shared kernel and module APIs
- Spring Modulith module verification
- Hexagonal Architecture
- input/output ports and adapters
- ordinary in-process Spring application events

## Week 2 — Persistence

The in-memory adapters are replaced by PostgreSQL persistence adapters.

- Each module owns its own schema: `kdg_flights` and `kdg_loading`. There are no joins or foreign keys across schemas.
- One persistence adapter per aggregate: `FlightPersistenceAdapter` and `LoadPersistenceAdapter`. They implement the existing output ports and map between the aggregate and its JPA entity. The domain has no JPA annotations.
- `Delay` and `Uld` are stored with their aggregate root, as element collections.
- `Flight.restore(...)` and `Load.restore(...)` rebuild an existing aggregate without recording events again.
- The use case is the unit of work, so `@Transactional` is on the use cases.
- The persistence adapter runs before the event publisher (`@Order(1)` and `@Order(2)`): an aggregate is saved before its events are published.
- `spring.jpa.hibernate.ddl-auto=validate`: Hibernate only checks the entities against the tables of the init script.

## Week 2 — Events after commit

The modules react to each other's events with `@ApplicationModuleListener` instead of `@EventListener`.

- The listener only runs after the publishing transaction has committed: an event of a rolled-back change is never delivered.
- It runs on another thread, in a transaction of its own. A failing listener no longer rolls back the change that published the event.
- Spring Modulith's event publication registry is the outbox: the `kdg_events.event_publication` table stores every publication in the same transaction as the change, and marks it `COMPLETED` or `FAILED`. A failed publication is kept, not lost.
- `spring.task.execution.pool.core-size=1`: one listener thread handles the events one at a time, in commit order.
- The other module is now eventually consistent. Right after a load is declared final, recording the departure can be refused for a moment; once `LoadFinalisedEvent` is handled, it succeeds.

An existing local database does not get the new `kdg_events` schema automatically: delete the `postgresql/` folder and restart the container, or run the `kdg_events` part of `init-scripts/sql/schema-creation.sql` by hand.

## Week 2 — Incoming feeds through RabbitMQ

The simulator no longer calls the modules. It turns the HTTP JSON into SSM and ULD messages and sends them to RabbitMQ, like the real schedule and cargo feeds would. The modules process them under the same rules as messages from an external system.

```text
POST /api/simulator/schedules -> SSM -> exchange "feeds", key "ssm" -> queue flights.ssm  -> SsmMessageListener
POST /api/simulator/bookings  -> ULD -> exchange "feeds", key "uld" -> queue loading.uld -> UldMessageListener
```

- SSM and ULD messages are commands: each has exactly one handler, so they go through a direct exchange to one queue.
- Each module declares its own queue, binding and dead letter queue (`FlightsRabbitTopology`, `LoadingRabbitTopology`). The exchange name and routing keys are plain strings: the JSON message is the contract, not a shared Java class. The simulator has its own copy of the message records.
- The listeners are input adapters: they map a message to a command for an existing use case. The core does not know RabbitMQ.
- The simulator is not available with the `production` profile.

### Message format

Every message carries `messageId`, `producedAt` and `formatVersion`.

```json
{"messageId": "…", "producedAt": "2026-10-17T10:00:00Z", "formatVersion": 1,
 "flightNumber": "HJ221", "departureAirport": "BRU", "arrivalAirport": "ATH", "aircraftRegistration": "SXBAA",
 "firstDate": "2026-10-19", "lastDate": "2026-11-01", "weekdays": ["MONDAY", "WEDNESDAY"],
 "departureTime": "06:30", "arrivalTime": "10:00"}
```

```json
{"messageId": "…", "producedAt": "2026-10-17T10:00:00Z", "formatVersion": 1,
 "flightNumber": "HJ221", "departureDay": 19, "departureAirport": "BRU",
 "uldCode": "AKE16689R7", "weight": 1000, "weightUnit": "KG", "destination": "ATH"}
```

As the assignment describes, a ULD message names its flight by the day of the month only. `DayOfMonth` derives the date as in an IATA telegram: the nearest date to the moment the message is processed, the future one when two dates are equally near, and invalid when that day does not exist in that month.

### Duplicates and invalid messages

- A message delivered twice has effect only once. Both handlers are idempotent by nature: a flight that already exists is not created again, and a ULD that is already booked on the flight is ignored. Both cases are logged.
- A listener that fails is retried three times, after 1, 2 and 4 seconds. A booking that arrives just before its load was opened succeeds on a retry.
- A message that still fails, or cannot even be read, is moved to the module's dead letter queue (`flights.ssm.dlq`, `loading.uld.dlq`) and logged with its `messageId`. It is set aside, never lost silently.

## Week 2 — Messages to partners

| Message | Format | Sent when | Triggered by |
|---|---|---|---|
| FLM | JSON | a flight is created | `FlightScheduledEvent` |
| MVT-ED | telegram | the ETD changes (departure delay) | `EstimatedDepartureChangedEvent` |
| MVT-EA | telegram | the ETA changes (any delay) | `EstimatedArrivalChangedEvent` |
| MVT-AD | telegram | the departure is recorded | `DepartureRecordedEvent` |
| MVT-AA | telegram | the arrival is recorded | `ArrivalRecordedEvent` |
| CPM | JSON | the load is declared final | `LoadFinalisedEvent` |

- The aggregates record a specific event for every change that partners must hear about. `Flight` records `EstimatedDepartureChangedEvent` only for a departure delay, and `EstimatedArrivalChangedEvent` for every delay, because a departure delay shifts the arrival too.
- `FlightPartnerMessagePublisher` and `LoadPartnerMessagePublisher` are output adapters. They listen with `@ApplicationModuleListener`, so they only send after the change has been committed, and every message is first stored in the event publication registry.
- Both modules publish to the topic exchange `partner-exchange` (`partners.exchange` in `application.properties`), with the keys `flm`, `mvt.ed`, `mvt.ea`, `mvt.ad`, `mvt.aa` and `cpm`. The partner-service mock of the course, in `docker-compose.yaml`, binds its durable queue `partner-inbox` to it and shows every message it receives on http://localhost:8000, with remarks when a message misses its identification, its moment of production or its format version.
- Every message carries the id of the event that caused it as its identification, the moment it was produced and its format version. JSON messages carry them in the body; telegrams carry them as message properties (`message_id`, `timestamp`, header `formatVersion`).
- A telegram has the three lines of the IATA format, for example `MVT` / `HJ221/09.SXBAA.BRU` / `ED0645`. The day is that of the scheduled departure date, also when the estimate falls after midnight.
- The CPM lists only the ULDs that have a position.

### When the connection to the partners is down

The change is still saved, and its message stays `FAILED` in `kdg_events.event_publication`. `FailedEventResubmission` resubmits failed publications every 30 seconds (`events.resubmission.interval`), so the messages follow once RabbitMQ is back. A message that is sent again carries the same message id, so a partner can recognise it.

## Week 3 — Read endpoints for the frontend

| Endpoint | Returns |
|---|---|
| `GET /api/flights?flightNumber=&date=&departureAirport=&arrivalAirport=&status=` | the matching flights, sorted by scheduled departure; without any criteria, today's flights |
| `GET /api/flights/{flightNumber}/{date}/{departureAirport}` | one flight with its delays |
| `GET /api/loads/{flightNumber}/{date}/{departureAirport}` | the load of a flight with its ULDs and positions |

- The queries are input ports of their own (`ListFlightsQuery`, `GetFlightQuery`, `GetLoadQuery`), separate from the use cases that change state, following the course convention that a use case that only reads ends with `Query`.
- `FlightSearchPort` is a separate output port; the persistence adapter implements it with a Spring Data `Specification`, one predicate per given criterion.
- All times in the responses are UTC instants (`2026-10-20T06:30:00Z`), so the frontend can show them in Brussels time.

## Week 4 — The flight planner

| Endpoint | Does |
|---|---|
| `POST /api/flights/{…}/delays` | registers a delay |
| `PUT /api/flights/{…}/delays/{delayId}` | corrects the minutes and the description of a delay |
| `DELETE /api/flights/{…}/delays/{delayId}` | removes a delay |
| `POST /api/flights/{…}/departure`, `…/arrival` | records the actual time, sent as a UTC instant (`"2026-10-27T06:35:00Z"`) |
| `GET /api/delay-codes` | the IATA delay codes the planner chooses from |

- `Flight` checks every delay change the same way: after departure, departure delays can no longer change; after landing, no delay can. A delay is 1 minute to 12 hours, has a description and an IATA code from `IataDelayCodes`.
- After registering, correcting or removing a delay, `Flight` compares the estimates before and after: a changed ETD records `EstimatedDepartureChangedEvent` (MVT-ED), a changed ETA records `EstimatedArrivalChangedEvent` (MVT-EA). Removing the last delay brings the estimates back to the schedule and is announced the same way.
- An actual time cannot lie in the future and may be at most the configured tolerance later than the estimate; a difference exactly equal to the tolerance is allowed, earlier is always allowed. A later time is refused with "register a delay first".
- The tolerance comes through `FlightPolicyPort`. For now `FlightPolicyPropertiesAdapter` reads it from `flights.policy.actual-time-tolerance` (5 minutes); the admin's policy settings will replace that adapter without changing the rules. The use case passes the current time from the `Clock`, so the domain stays testable.

## Week 4 — The load planner

| Endpoint | Does |
|---|---|
| `GET /api/loads/{…}` | the load, its floor plan and its loading window |
| `POST /api/loads/{…}/positions` | gives a ULD a position, or moves it |
| `DELETE /api/loads/{…}/positions/{uldCode}` | takes a ULD off the deck |
| `GET /api/loads/{…}/checks` | runs all load checks and reports each result |
| `POST /api/loads/{…}/final?confirmSoftWarnings=true` | declares the load final |

### Floor plan

`FloorPlan` describes an aircraft type: its positions with their side (left, right or centre), the cargo door, and the routes from a position to the door. `Boeing747Freighter` is the one implementation: A1, A2 and B1 in the nose, rows C to U with a left and a right position, X in the tail, the door next to RL. A ULD on a left or right position follows its own side to row R, crossing from RR to RL on the right; A1, A2, B1 and X first need CL and CR, or UL and UR, clear and then choose a side. `FloorPlanPort` gives the floor plan of a flight; another aircraft type is another `FloorPlan` implementation. A load planner can only choose positions that are on the floor plan.

### Loading window

The window opens 12 hours before the STD and closes 20 minutes before the ETD. The loading context keeps its own copy of both times: `FlightScheduledEvent` brings the STD, and `EstimatedDepartureChangedEvent`, now in `common`, brings every new ETD. Positions can only change, and the load can only be declared final, while the window is open.

### Load checks

Each check is a `LoadCheck` (strategy pattern): `LateralBalanceCheck` (hard), `OffloadingCheck` (hard or soft) and `AllCargoLoadedCheck` (soft). `LoadingPolicy` decides which checks run and with which settings; another check is another implementation added to that list.

- Lateral balance converts every weight to kilograms with `BigDecimal` and compares `heavier × 100 ≤ lighter × (100 + limit)`, so a difference exactly at the limit passes and nothing is rounded.
- Offloading asks the floor plan for every route from a ULD for the flight's destination to the door. The ULD is blocked when each route holds cargo for another destination; the result names the blocked ULD and what blocks it.
- `Load.declareFinal` runs the checks itself with the current policy: a failing hard check refuses the final load, and soft warnings must be confirmed.

The policy values come from `application.properties` through `LoadingPolicyPort` until the admin settings exist.

### Existing local databases

The `load` table has two new columns. A database created before this change gets them with:

```sql
ALTER TABLE kdg_loading.load ADD COLUMN scheduled_departure TIMESTAMP;
ALTER TABLE kdg_loading.load ADD COLUMN estimated_departure TIMESTAMP;
UPDATE kdg_loading.load l SET scheduled_departure = f.scheduled_departure, estimated_departure = f.scheduled_departure
FROM kdg_flights.flight f
WHERE (f.flight_number, f.scheduled_departure_date, f.departure_airport)
    = (l.flight_number, l.scheduled_departure_date, l.departure_airport);
ALTER TABLE kdg_loading.load ALTER COLUMN scheduled_departure SET NOT NULL;
ALTER TABLE kdg_loading.load ALTER COLUMN estimated_departure SET NOT NULL;
```

Or delete the `postgresql/` folder and start the container again.

## Week 5 — Security

The backend is an OAuth2 resource server: every request to `/api` must carry an access token of the realm `air-freight` in the `Authorization: Bearer …` header.

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8180/realms/air-freight
spring.security.oauth2.resourceserver.jwt.audiences=air-freight-api
```

- **Is the token real?** Spring fetches the public keys of the realm through the issuer (`jwks_uri` in its OpenID configuration) and checks the signature, the issuer (`iss`), the audience (`aud`) and the expiry (`exp`) of every token. Keycloak is only asked for the keys, not for every request.
- **Which role?** `KeycloakRealmRoleConverter` turns the realm roles in the claim `realm_access.roles` into authorities `ROLE_<role>`, so `hasRole("flight-planner")` matches the realm role `flight-planner`. The user's name comes from `preferred_username`.
- **Stateless.** No session: every request brings its own token, so there is no cookie to abuse and CSRF protection is off.
- `SecurityConfiguration` sits in `config`, next to `RabbitConfiguration`: it is cross-cutting and only names URLs, so no module depends on it.

| Request | flight planner | load planner | admin |
|---|---|---|---|
| `GET /api/**`: flights, loads, load checks, delay codes, policy settings | yes | yes | yes |
| delays: `POST`, `PUT`, `DELETE …/delays/**` | yes | no | no |
| actual times: `POST …/departure`, `POST …/arrival` | yes | no | no |
| `POST …/positions`, `DELETE …/positions/{uldCode}`, `POST …/final` | no | yes | no |
| `PUT /api/policy-settings/**` | no | no | yes |
| `POST /api/simulator/**` | no | no | yes |
| anything else | no | no | no |

Everyone in operations may look at everything; each role only makes its own changes. No token, or an invalid one, gives `401 Unauthorized`; a valid token without the role gives `403 Forbidden`. A request that matches no rule is refused (`denyAll`), so a new endpoint is closed until a rule opens it.

The rules are on the URLs and not with `@PreAuthorize` on the use cases: the RabbitMQ listeners and the `@ApplicationModuleListener`s call use cases too, and they run without a logged-in user.

`SecurityConfigurationTest` and `SimulatorSecurityTest` are web slices (`@WebMvcTest`) with the use cases as mocks and a token from `jwt()` of `spring-security-test`, without Keycloak. The mocks answer "not found", so a request that passes the security rules gets `404`, one with the wrong role `403`, and one without a token `401`.

## Week 5 — Invalid requests

A request body is checked before it becomes a command. The request records carry Bean Validation annotations (`@NotNull`, `@NotBlank`) and the controllers ask for `@Valid`. `RequestErrorHandler` in `config` turns a failed check into `400 Bad Request` with a problem detail that names the missing fields, for example `delayCode must not be blank; minutes must not be null`, and a body that is no JSON, or has a value of the wrong type, into a `400` as well.

The request only checks that a value is there; whether the value makes sense is a rule of the domain. A delay of 900 minutes is complete, but `Delay` refuses it, and the controller turns that `IllegalArgumentException` into a `400` with the domain's message.

## Week 5 — Policy settings

An admin reads and changes the shared policy settings through the API, without a new release. There is no screen for them.

| Endpoint | Body | Who |
|---|---|---|
| `GET /api/policy-settings/flights` | | everyone logged in |
| `PUT /api/policy-settings/flights` | `{"actualTimeToleranceMinutes": 5, "retentionDays": 7}` | admin |
| `GET /api/policy-settings/loading` | | everyone logged in |
| `PUT /api/policy-settings/loading` | `{"windowOpensHoursBeforeStd": 12, "windowClosesMinutesBeforeEtd": 20, "maxLateralDifferencePercent": 10, "offloadingCheck": "HARD"}` | admin |

- **The module that enforces a rule owns its setting.** Flights owns the actual-time tolerance and the retention, in `FlightPolicy`. Loading owns the loading window, the maximum lateral weight difference and whether the offloading check is hard or soft, in `LoadingPolicy`. Neither module reads the other's settings.
- **The domain decides what is valid.**
  - `FlightPolicy`: a tolerance from 0 to 60 minutes, a retention from 1 to 365 days.
  - `LoadingPolicy`: the window opens 1 to 168 hours before the STD and closes at the latest at the ETD, and it must open earlier than it closes; the lateral difference is 0 to 100% with at most two decimals; the offloading check is `HARD` or `SOFT`.

  The request only checks that every value is there. A refused value gives `400` with the reason; a planner gets `403` and nothing is changed.
- **A `PUT` replaces all settings of the module at once**, so a policy is never half old and half new.
- **Stored in the database**, in the one-row tables `kdg_flights.flight_policy` and `kdg_loading.loading_policy`, so a change survives a restart. `FlightPolicyPersistenceAdapter` and `LoadingPolicyPersistenceAdapter` replace the former properties adapters behind the same ports (`FlightPolicyPort`, `LoadingPolicyPort`): the use cases that record actual times, place ULDs, check the load and declare it final did not change. As long as no admin has saved anything, the adapters return the defaults of `application.properties` (`flights.policy.*`, `loading.policy.*`): the initial values, not fixed rules.
- **Used for every later decision**, also for flights and loads that already exist, because the use cases ask the port each time. `PolicySettingsTest` shows it: a load that passes the lateral check at 10% fails it once the admin sets 5%. A final load is not reopened and recorded actual times stay as they are: the settings are only read when something is about to change.
- The update use cases log every change with the old and the new values.
- `src/test/http/air-freight.http` has the requests: log in as the admin first.

### Existing local databases

```sql
CREATE TABLE kdg_flights.flight_policy
(
    id                            INTEGER NOT NULL PRIMARY KEY,
    actual_time_tolerance_minutes INTEGER NOT NULL,
    retention_days                INTEGER NOT NULL
);

CREATE TABLE kdg_loading.loading_policy
(
    id                               INTEGER       NOT NULL PRIMARY KEY,
    window_opens_hours_before_std    INTEGER       NOT NULL,
    window_closes_minutes_before_etd INTEGER       NOT NULL,
    max_lateral_difference_percent   NUMERIC(5, 2) NOT NULL,
    offloading_check                 VARCHAR(4)    NOT NULL
);
```

## Week 5 — Flight retention

A flight and its data are kept for the retention period of the flight policy (`retentionDays`, 7 by default), counted from its scheduled departure date. On 30 October, with 7 days, the flights of 23 October and later stay; the flights of 22 October and earlier are removed.

```text
FlightRetentionScheduler (every hour, flights.retention.cron)
  -> RemoveExpiredFlightsUseCase: FlightPolicy.oldestRetainedDate(today)
  -> FlightLoadPort.loadFlightsScheduledBefore(that date)
  -> Flight.remove() records FlightRemovedEvent
  -> FlightRemovePort: FlightPersistenceAdapter deletes the flight and its delays,
                       FlightEventPublisherAdapter publishes the event
  -> loading: FlightRemovedEventListener -> RemoveLoadUseCase -> the load and its ULDs are deleted
```

- **Flights decides, loading follows.** The retention is a setting of the flights module, so the flights module finds the expired flights. The loading module does not look at dates: it removes a load when it hears `FlightRemovedEvent`, the same way it opens one on `FlightScheduledEvent`. The modules still never call each other.
- **Removing is the same pattern as updating.** `FlightRemovePort` has two implementations, in order: the database first (`@Order(1)`), the event second (`@Order(2)`), just like `FlightUpdatePort`.
- **Reliable.** The event goes through the event publication registry: if the application stops between deleting the flight and deleting the load, the load is removed once the publication is resubmitted.
- **The current setting counts.** The use case reads the policy at every run, so after an admin shortens the retention, the next run removes more.
- The scheduler is an input adapter, like a listener: it only triggers the use case. It runs on the hour, in UTC (`flights.retention.cron=0 0 * * * *`), and logs how many flights it removed and why.
- `RetentionTest` schedules a flight of 8 days ago and one of 7 days ago: after a run, the first is gone with its load and the second stays.

## Week 6 — No bookings on a departed flight

Cargo can be booked as long as the flight is scheduled, also after the load is final; the ULD then stays behind. Once the flight has departed, a booking is refused. Loading does not read the state of a flight, so it learns about the departure through an event:

```text
Flight.recordDeparture -> DepartureRecordedEvent (common)
  -> loading: DepartureRecordedEventListener -> MarkLoadDepartedUseCase -> Load.markDeparted()
```

A ULD message for a departed flight is logged and ends up on the dead-letter queue, like any other message that contradicts the rules.

A database created before this change gets the new column with:

```sql
ALTER TABLE kdg_loading.load ADD COLUMN departed BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE kdg_loading.load l SET departed = TRUE
FROM kdg_flights.flight f
WHERE (f.flight_number, f.scheduled_departure_date, f.departure_airport)
    = (l.flight_number, l.scheduled_departure_date, l.departure_airport)
  AND f.actual_departure IS NOT NULL;
```

## Week 6 — Errors in the logs

| What happens | Log level | Answer to the request |
|---|---|---|
| A change or lookup for a flight that does not exist | `warn` | 404 |
| The database cannot be reached | `error` | 503 |
| An unexpected database error | `error` | 500 |
| The message broker cannot be reached during a request | `error` | 503 |
| A partner message cannot be sent | `error` | none: the change is already stored and the message is sent again every 30 seconds |
| An invalid or duplicate SSM or ULD message | `warn` | none: invalid messages go to the dead-letter queue |

# Air Cargo Operations — exam hand-in

## Challenges & Accomplishments

**Challenges**
- **Messages that must never get lost.** A recorded change has to reach the partners even when RabbitMQ is down, and each message only once. We used Spring Modulith's event publication registry as an outbox, with resubmission every 30 seconds. We checked it live: with RabbitMQ stopped, a delay was saved, and once RabbitMQ was back the MVT-ED and MVT-EA arrived exactly once.
- **Dates that are only a day of the month.** ULD messages and telegrams carry a day, not a date, and a time without a date. `DayOfMonth` and `TelegramTime` take the nearest date or moment, including around midnight and at the end of a month.
- **Two bounded contexts that stay independent.** Flights and loading share only `common` (the flight id and the events). Loading learns about new flights, changed estimates, departures and removed flights through events, never by calling the flights module.
- **The offloading rule.** Every ULD for the destination needs a free route to the door at row R along its own side, and the nose and tail positions need their manoeuvring space.

**What we are most proud of**
- **Load checks as separate strategies** (`LoadCheck`), chosen by `LoadingPolicy`: a new check is a new class, and the aggregate does not change.
- **Hexagonal structure that paid off.** The policy settings moved from properties to the database by replacing one output adapter; the use cases did not change.
- **163 automated tests**, including integration tests with Testcontainers for the feeds, partner messages, retention and policy settings.

---

## ✅ Finished Features

- [x] Simulator endpoints (admin only, not in the `production` profile) that send SSM and ULD messages through RabbitMQ
- [x] SSM processing: one flight per matching weekday, next-day arrival, existing flights skipped and logged, an FLM per created flight
- [x] ULD bookings from the cargo feed: nearest date for the day of the month, a duplicate ULD ignored and logged, no bookings after departure
- [x] Invalid messages logged and moved to a dead-letter queue; a message delivered twice has effect once
- [x] Delays: register, correct and remove, 1 minute to 12 hours, IATA delay code list, the rules per flight state, ETD and ETA derived from the delays, MVT-ED and MVT-EA
- [x] Actual departure and arrival: not in the future, within the tolerance, arrival after departure, departure only after the final load; MVT-AD and MVT-AA
- [x] Loading window from the policy: opens before the STD, closes before the ETD
- [x] Boeing 747-400 freighter floor plan; give a ULD a position, move it, take it off the deck
- [x] Load checks: lateral balance (units converted, the exact limit allowed), offloading (hard or soft) and not all cargo loaded, each with a reason and a suggestion to fix it
- [x] Declaring the load final with confirmation of soft warnings; CPM to the partners
- [x] Partner messages with an id, a timestamp and a format version, sent through an outbox
- [x] Policy settings: read by anyone logged in, changed only by the admin, validated and stored in the database
- [x] Retention: every hour, flights older than the retention period are removed together with their load
- [x] Read endpoints for the frontend: flight search, flight, load, load checks, delay codes
- [x] Keycloak (OIDC) resource server: every request needs a token, every change needs the right role
- [x] Validation errors as `400` ProblemDetails; logging of refused requests, database errors and broker errors
- [x] The required tests: times and delays, SSM processing and FLMs, dates in IATA messages, load checks

---

## ❌ Unfinished / Planned Features

- [ ] Concurrent changes: when two planners save the same flight or load at once, the last save wins. A version column (optimistic locking) would refuse the second save.
- [ ] Reading incoming MVT telegrams from partners: `TelegramTime` and `DayOfMonth` derive the dates and are tested, but no adapter receives telegrams yet.
- [ ] Later phases from the assignment: autoload, geographic consistency, more load checks (longitudinal balance, dangerous goods, floor limits), more aircraft types, other movement types.
