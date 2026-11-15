package be.kdg.prog6;

import be.kdg.prog6.common.domain.AirportCode;
import be.kdg.prog6.common.domain.FlightId;
import be.kdg.prog6.common.domain.FlightNumber;
import be.kdg.prog6.flights.domain.AircraftRegistration;
import be.kdg.prog6.flights.domain.DelaySide;
import be.kdg.prog6.flights.domain.FlightSchedule;
import be.kdg.prog6.flights.port.in.RegisterDelayCommand;
import be.kdg.prog6.flights.port.in.RegisterDelayUseCase;
import be.kdg.prog6.flights.port.in.ScheduleFlightsCommand;
import be.kdg.prog6.flights.port.in.ScheduleFlightsUseCase;
import be.kdg.prog6.loading.domain.Position;
import be.kdg.prog6.loading.domain.UldCode;
import be.kdg.prog6.loading.domain.Weight;
import be.kdg.prog6.loading.domain.WeightUnit;
import be.kdg.prog6.loading.port.in.AssignPositionCommand;
import be.kdg.prog6.loading.port.in.AssignPositionUseCase;
import be.kdg.prog6.loading.port.in.BookUldCommand;
import be.kdg.prog6.loading.port.in.BookUldUseCase;
import be.kdg.prog6.loading.port.in.DeclareLoadFinalCommand;
import be.kdg.prog6.loading.port.in.DeclareLoadFinalUseCase;
import be.kdg.prog6.loading.port.out.LoadLoadPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PartnerMessagesTest {

    private static final String PARTNER_INBOX = "test.partner-inbox";
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);
    private static final AirportCode BRU = new AirportCode("BRU");
    private static final AirportCode ATH = new AirportCode("ATH");

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private TopicExchange partnerExchange;
    @Autowired
    private ScheduleFlightsUseCase scheduleFlightsUseCase;
    @Autowired
    private RegisterDelayUseCase registerDelayUseCase;
    @Autowired
    private BookUldUseCase bookUldUseCase;
    @Autowired
    private AssignPositionUseCase assignPositionUseCase;
    @Autowired
    private DeclareLoadFinalUseCase declareLoadFinalUseCase;
    @Autowired
    private LoadLoadPort loadLoadPort;

    @BeforeEach
    void listenToThePartnerExchange() {
        Queue inbox = new Queue(PARTNER_INBOX);
        amqpAdmin.declareQueue(inbox);
        amqpAdmin.declareBinding(BindingBuilder.bind(inbox).to(partnerExchange).with("#"));
    }

    @Test
    void ssmLeadsToAnFlmForTheCreatedFlight() {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        String ssm = """
                {"messageId":"%s","producedAt":"2026-10-17T10:00:00Z","formatVersion":1,
                 "flightNumber":"HJ501","departureAirport":"BRU","arrivalAirport":"ATH",
                 "aircraftRegistration":"SXBAA","firstDate":"2026-11-02","lastDate":"2026-11-02",
                 "weekdays":["MONDAY"],"departureTime":"06:30","arrivalTime":"10:00"}
                """.formatted(UUID.randomUUID());
        rabbitTemplate.send("feeds", "ssm", new Message(ssm.getBytes(StandardCharsets.UTF_8), properties));

        Message flm = receive("flm", "\"flightNumber\":\"HJ501\"");

        assertEquals(MessageProperties.CONTENT_TYPE_JSON, flm.getMessageProperties().getContentType());
        assertNotNull(flm.getMessageProperties().getMessageId());
        String body = body(flm);
        assertTrue(body.contains("\"scheduledDepartureDate\":\"2026-11-02\""));
        assertTrue(body.contains("\"std\":\"2026-11-02T06:30:00\""));
        assertTrue(body.contains("\"sta\":\"2026-11-02T10:00:00\""));
        assertTrue(body.contains("\"arrivalAirport\":\"ATH\""));
        assertTrue(body.contains("\"formatVersion\":1"));
    }

    @Test
    void departureDelaySendsEstimatedDepartureAndArrivalTelegrams() {
        FlightId flightId = schedule("HJ502");

        registerDelayUseCase.registerDelay(new RegisterDelayCommand(flightId, DelaySide.DEPARTURE, 30, "93",
                "Late inbound aircraft"));

        Message departure = receive("mvt.ed", "HJ502/02.SXBAA.BRU");
        Message arrival = receive("mvt.ea", "HJ502/02.SXBAA.ATH");
        assertEquals("MVT\nHJ502/02.SXBAA.BRU\nED0700", body(departure));
        assertEquals("MVT\nHJ502/02.SXBAA.ATH\nEA1030", body(arrival));
        assertEquals(MessageProperties.CONTENT_TYPE_TEXT_PLAIN, departure.getMessageProperties().getContentType());
        assertNotNull(departure.getMessageProperties().getMessageId());
        assertEquals(1, (Integer) departure.getMessageProperties().getHeader("formatVersion"));
    }

    @Test
    void finalLoadSendsCpmWithThePlacedUlds() {
        FlightId flightId = scheduleDepartingSoon("HJ503");
        await().atMost(Duration.ofSeconds(10)).until(() -> loadLoadPort.loadLoad(flightId).isPresent());
        bookUldUseCase.bookUld(new BookUldCommand(flightId, new UldCode("AKE16689R7"),
                new Weight(1000, WeightUnit.KG), ATH));
        bookUldUseCase.bookUld(new BookUldCommand(flightId, new UldCode("PMC12345R7"),
                new Weight(800, WeightUnit.KG), ATH));
        assignPositionUseCase.assignPosition(new AssignPositionCommand(flightId, new UldCode("AKE16689R7"),
                new Position("A1")));

        declareLoadFinalUseCase.declareFinal(new DeclareLoadFinalCommand(flightId, true));

        String cpm = body(receive("cpm", "\"flightNumber\":\"HJ503\""));
        assertTrue(cpm.contains("\"uldCode\":\"AKE16689R7\",\"position\":\"A1\""));
        assertFalse(cpm.contains("PMC12345R7"));
    }

    private FlightId scheduleDepartingSoon(String flightNumber) {
        LocalDateTime std = LocalDateTime.now(ZoneOffset.UTC).plusHours(1).truncatedTo(ChronoUnit.MINUTES);
        scheduleFlightsUseCase.scheduleFlights(new ScheduleFlightsCommand(new FlightSchedule(
                new FlightNumber(flightNumber), BRU, ATH, new AircraftRegistration("SXBAA"),
                std.toLocalDate(), std.toLocalDate(), Set.of(std.getDayOfWeek()),
                std.toLocalTime(), std.plusHours(3).toLocalTime())));
        return new FlightId(new FlightNumber(flightNumber), std.toLocalDate(), BRU);
    }

    private FlightId schedule(String flightNumber) {
        scheduleFlightsUseCase.scheduleFlights(new ScheduleFlightsCommand(new FlightSchedule(
                new FlightNumber(flightNumber), BRU, ATH, new AircraftRegistration("SXBAA"),
                MONDAY, MONDAY, Set.of(DayOfWeek.MONDAY), LocalTime.of(6, 30), LocalTime.of(10, 0))));
        return new FlightId(new FlightNumber(flightNumber), MONDAY, BRU);
    }

    private Message receive(String routingKey, String marker) {
        AtomicReference<Message> found = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofMillis(20)).until(() -> {
            Message message = rabbitTemplate.receive(PARTNER_INBOX);
            if (message != null && routingKey.equals(message.getMessageProperties().getReceivedRoutingKey())
                    && body(message).contains(marker)) {
                found.set(message);
                return true;
            }
            return false;
        });
        return found.get();
    }

    private static String body(Message message) {
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }
}
