CREATE SCHEMA kdg_flights;

CREATE TABLE kdg_flights.flight
(
    flight_number            VARCHAR(6)  NOT NULL,
    scheduled_departure_date DATE        NOT NULL,
    departure_airport        VARCHAR(3)  NOT NULL,
    arrival_airport          VARCHAR(3)  NOT NULL,
    aircraft_registration    VARCHAR(10) NOT NULL,
    scheduled_departure      TIMESTAMP   NOT NULL,
    scheduled_arrival        TIMESTAMP   NOT NULL,
    actual_departure         TIMESTAMP,
    actual_arrival           TIMESTAMP,
    final_load_received      BOOLEAN     NOT NULL,
    PRIMARY KEY (flight_number, scheduled_departure_date, departure_airport)
);

CREATE TABLE kdg_flights.delay
(
    delay_id                 UUID         NOT NULL PRIMARY KEY,
    flight_number            VARCHAR(6)   NOT NULL,
    scheduled_departure_date DATE         NOT NULL,
    departure_airport        VARCHAR(3)   NOT NULL,
    side                     VARCHAR(10)  NOT NULL,
    minutes                  INTEGER      NOT NULL,
    delay_code               VARCHAR(10)  NOT NULL,
    description              VARCHAR(255) NOT NULL,
    FOREIGN KEY (flight_number, scheduled_departure_date, departure_airport)
        REFERENCES kdg_flights.flight ON DELETE CASCADE
);

CREATE TABLE kdg_flights.flight_policy
(
    id                            INTEGER NOT NULL PRIMARY KEY,
    actual_time_tolerance_minutes INTEGER NOT NULL,
    retention_days                INTEGER NOT NULL
);

CREATE SCHEMA kdg_loading;

CREATE TABLE kdg_loading.load
(
    flight_number            VARCHAR(6)  NOT NULL,
    scheduled_departure_date DATE        NOT NULL,
    departure_airport        VARCHAR(3)  NOT NULL,
    destination              VARCHAR(3)  NOT NULL,
    scheduled_departure      TIMESTAMP   NOT NULL,
    estimated_departure      TIMESTAMP   NOT NULL,
    status                   VARCHAR(10) NOT NULL,
    departed                 BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (flight_number, scheduled_departure_date, departure_airport)
);

CREATE TABLE kdg_loading.uld
(
    flight_number            VARCHAR(6)       NOT NULL,
    scheduled_departure_date DATE             NOT NULL,
    departure_airport        VARCHAR(3)       NOT NULL,
    uld_code                 VARCHAR(12)      NOT NULL,
    weight                   DOUBLE PRECISION NOT NULL,
    weight_unit              VARCHAR(2)       NOT NULL,
    destination              VARCHAR(3)       NOT NULL,
    position                 VARCHAR(3),
    PRIMARY KEY (flight_number, scheduled_departure_date, departure_airport, uld_code),
    UNIQUE (flight_number, scheduled_departure_date, departure_airport, position),
    FOREIGN KEY (flight_number, scheduled_departure_date, departure_airport)
        REFERENCES kdg_loading.load ON DELETE CASCADE
);

CREATE TABLE kdg_loading.loading_policy
(
    id                               INTEGER       NOT NULL PRIMARY KEY,
    window_opens_hours_before_std    INTEGER       NOT NULL,
    window_closes_minutes_before_etd INTEGER       NOT NULL,
    max_lateral_difference_percent   NUMERIC(5, 2) NOT NULL,
    offloading_check                 VARCHAR(4)    NOT NULL
);

CREATE SCHEMA kdg_events;

CREATE TABLE kdg_events.event_publication
(
    id                     UUID                     NOT NULL PRIMARY KEY,
    listener_id            TEXT                     NOT NULL,
    event_type             TEXT                     NOT NULL,
    serialized_event       TEXT                     NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMP WITH TIME ZONE
);

CREATE INDEX event_publication_serialized_event_hash_idx ON kdg_events.event_publication USING hash (serialized_event);
CREATE INDEX event_publication_by_completion_date_idx ON kdg_events.event_publication (completion_date);
