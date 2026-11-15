package be.kdg.prog6;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.port.out.FlightLoadPort;
import be.kdg.prog6.loading.domain.Uld;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class IncomingFeedsTest {

    private static final AirportCode BRU = new AirportCode("BRU");

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private FlightLoadPort flightLoadPort;
    @Autowired
    private LoadLoadPort loadLoadPort;

    @Test
    void ssmCreatesOneFlightPerMatchingWeekday() {
        send("ssm", ssmJson(UUID.randomUUID(), "HJ601", "2026-11-02", "2026-11-08", "[\"MONDAY\",\"FRIDAY\"]"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> assertEquals(2, flightsWithNumber("HJ601")));
        assertTrue(flightLoadPort.loadFlight(flightId("HJ601", LocalDate.of(2026, 11, 2))).isPresent());
        assertTrue(flightLoadPort.loadFlight(flightId("HJ601", LocalDate.of(2026, 11, 6))).isPresent());
    }

    @Test
    void ssmDeliveredTwiceHasEffectOnce() {
        String ssm = ssmJson(UUID.randomUUID(), "HJ602", "2026-11-02", "2026-11-02", "[\"MONDAY\"]");
        send("ssm", ssm);
        send("ssm", ssm);
        send("ssm", ssmJson(UUID.randomUUID(), "HJ603", "2026-11-02", "2026-11-02", "[\"MONDAY\"]"));

        await().atMost(Duration.ofSeconds(10)).until(() -> flightsWithNumber("HJ603") == 1);
        assertEquals(1, flightsWithNumber("HJ602"));
    }

    @Test
    void uldMessageBooksTheUldOnTheNearestFlight() {
        LocalDate departureDate = LocalDate.now(ZoneOffset.UTC).plusDays(2);
        send("ssm", ssmJson(UUID.randomUUID(), "HJ604", departureDate.toString(), departureDate.toString(),
                "[\"" + departureDate.getDayOfWeek() + "\"]"));
        FlightId flightId = flightId("HJ604", departureDate);
        await().atMost(Duration.ofSeconds(10)).until(() -> loadLoadPort.loadLoad(flightId).isPresent());

        send("uld", """
                {"messageId":"%s","producedAt":"2026-10-17T10:00:00Z","formatVersion":1,
                 "flightNumber":"HJ604","departureDay":%d,"departureAirport":"BRU",
                 "uldCode":"AKE16689R7","weight":1000,"weightUnit":"KG","destination":"ATH"}
                """.formatted(UUID.randomUUID(), departureDate.getDayOfMonth()));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Uld uld = loadLoadPort.loadLoad(flightId).orElseThrow().getUlds().getFirst();
            assertEquals("AKE16689R7", uld.getCode().value());
        });
    }

    @Test
    void malformedSsmIsSetAsideInTheDeadLetterQueue() {
        send("ssm", "{\"flightNumber\": \"HJ605\", this is not json");

        assertArrivesInDeadLetterQueue("flights.ssm.dlq", "HJ605");
    }

    @Test
    void ssmAgainstTheRulesIsSetAsideInTheDeadLetterQueue() {
        send("ssm", ssmJson(UUID.randomUUID(), "HJ606", "2026-11-08", "2026-11-02", "[\"MONDAY\"]"));

        assertArrivesInDeadLetterQueue("flights.ssm.dlq", "HJ606");
        assertEquals(0, flightsWithNumber("HJ606"));
    }

    private void send(String routingKey, String json) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        rabbitTemplate.send("feeds", routingKey, new Message(json.getBytes(StandardCharsets.UTF_8), properties));
    }

    private void assertArrivesInDeadLetterQueue(String queue, String marker) {
        await().atMost(Duration.ofSeconds(30)).until(() -> {
            Message deadLetter = rabbitTemplate.receive(queue);
            return deadLetter != null && new String(deadLetter.getBody(), StandardCharsets.UTF_8).contains(marker);
        });
    }

    private static String ssmJson(UUID messageId, String flightNumber, String firstDate, String lastDate,
                                  String weekdays) {
        return """
                {"messageId":"%s","producedAt":"2026-10-17T10:00:00Z","formatVersion":1,
                 "flightNumber":"%s","departureAirport":"BRU","arrivalAirport":"ATH",
                 "aircraftRegistration":"SXBAA","firstDate":"%s","lastDate":"%s",
                 "weekdays":%s,"departureTime":"06:30","arrivalTime":"10:00"}
                """.formatted(messageId, flightNumber, firstDate, lastDate, weekdays);
    }

    private int flightsWithNumber(String flightNumber) {
        return jdbcTemplate.queryForObject("select count(*) from kdg_flights.flight where flight_number = ?",
                Integer.class, flightNumber);
    }

    private static FlightId flightId(String flightNumber, LocalDate date) {
        return new FlightId(new FlightNumber(flightNumber), date, BRU);
    }
}
