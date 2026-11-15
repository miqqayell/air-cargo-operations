package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.loading.domain.DeckPosition;
import be.kdg.prog6.loading.domain.FloorPlan;
import be.kdg.prog6.loading.domain.Uld;
import be.kdg.prog6.loading.port.in.LoadOverview;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public record LoadDto(String flightNumber, LocalDate scheduledDepartureDate, String departureAirport,
                      String destination, String status, List<UldDto> ulds, FloorPlanDto floorPlan,
                      LoadingWindowDto loadingWindow) {

    public record LoadingWindowDto(Instant opensAt, Instant closesAt, boolean open) {
    }

    public record UldDto(String code, double weight, String weightUnit, String destination, String position) {

        static UldDto from(Uld uld) {
            return new UldDto(
                    uld.getCode().value(),
                    uld.getWeight().value(),
                    uld.getWeight().unit().name(),
                    uld.getDestination().code(),
                    uld.isPlaced() ? uld.getPosition().code() : null);
        }
    }

    public record FloorPlanDto(String aircraftType, String door, List<DeckPositionDto> positions) {

        static FloorPlanDto from(FloorPlan floorPlan) {
            return new FloorPlanDto(floorPlan.aircraftType(), floorPlan.door().code(),
                    floorPlan.positions().stream().map(DeckPositionDto::from).toList());
        }
    }

    public record DeckPositionDto(String code, String side, int column) {

        static DeckPositionDto from(DeckPosition deckPosition) {
            return new DeckPositionDto(deckPosition.position().code(), deckPosition.side().name(), deckPosition.column());
        }
    }

    public static LoadDto from(LoadOverview overview) {
        var load = overview.load();
        return new LoadDto(
                load.getFlightId().flightNumber().value(),
                load.getFlightId().scheduledDepartureDate(),
                load.getFlightId().departureAirport().code(),
                load.getDestination().code(),
                load.getStatus().name(),
                load.getUlds().stream().map(UldDto::from).toList(),
                FloorPlanDto.from(overview.floorPlan()),
                new LoadingWindowDto(overview.loadingWindow().opensAt().toInstant(ZoneOffset.UTC),
                        overview.loadingWindow().closesAt().toInstant(ZoneOffset.UTC), overview.loadingWindowOpen()));
    }
}
