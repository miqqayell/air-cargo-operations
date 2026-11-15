package be.kdg.prog6.flights.adapter.out;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MvtTelegramTest {

    private static final FlightId FLIGHT =
            new FlightId(new FlightNumber("HJ221"), LocalDate.of(2027, 1, 9), new AirportCode("BRU"));
    private static final AircraftRegistration SXBAA = new AircraftRegistration("SXBAA");

    @Test
    void estimatedDepartureTelegramHasThreeLines() {
        var telegram = new MvtTelegram(MvtType.ED, FLIGHT, SXBAA, new AirportCode("BRU"),
                LocalDateTime.of(2027, 1, 9, 6, 45));

        assertEquals("MVT\nHJ221/09.SXBAA.BRU\nED0645", telegram.toText());
    }

    @Test
    void arrivalTelegramNamesTheArrivalAirport() {
        var telegram = new MvtTelegram(MvtType.AA, FLIGHT, SXBAA, new AirportCode("ATH"),
                LocalDateTime.of(2027, 1, 9, 10, 5));

        assertEquals("MVT\nHJ221/09.SXBAA.ATH\nAA1005", telegram.toText());
    }

    @Test
    void dayIsTheScheduledDepartureDateEvenAfterMidnight() {
        var telegram = new MvtTelegram(MvtType.ED, FLIGHT, SXBAA, new AirportCode("BRU"),
                LocalDateTime.of(2027, 1, 10, 0, 15));

        assertEquals("MVT\nHJ221/09.SXBAA.BRU\nED0015", telegram.toText());
    }
}
