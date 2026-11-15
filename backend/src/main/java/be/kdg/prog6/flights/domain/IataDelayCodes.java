package be.kdg.prog6.flights.domain;

import java.util.List;

public final class IataDelayCodes {

    private static final List<DelayCode> CODES = List.of(
            new DelayCode("06", "No gate or stand available due to own airline activity"),
            new DelayCode("09", "Scheduled ground time less than declared minimum"),
            new DelayCode("11", "Late check-in, acceptance after deadline"),
            new DelayCode("12", "Late check-in, congestion in check-in area"),
            new DelayCode("13", "Check-in error"),
            new DelayCode("14", "Oversales, booking errors"),
            new DelayCode("15", "Boarding, discrepancies and paging"),
            new DelayCode("16", "Commercial publicity, passenger convenience"),
            new DelayCode("17", "Catering order, late or incorrect"),
            new DelayCode("18", "Baggage processing, sorting"),
            new DelayCode("19", "Boarding of passengers with reduced mobility"),
            new DelayCode("21", "Cargo documentation, errors"),
            new DelayCode("22", "Cargo late positioning"),
            new DelayCode("23", "Cargo late acceptance"),
            new DelayCode("24", "Cargo inadequate packing"),
            new DelayCode("25", "Cargo oversales, booking errors"),
            new DelayCode("26", "Cargo late preparation in warehouse"),
            new DelayCode("27", "Mail documentation, packing"),
            new DelayCode("28", "Mail late positioning"),
            new DelayCode("29", "Mail late acceptance"),
            new DelayCode("31", "Aircraft documentation late or inaccurate, weight and balance"),
            new DelayCode("32", "Loading and unloading, bulky or special load, lack of loading staff"),
            new DelayCode("33", "Loading equipment, lack of or breakdown"),
            new DelayCode("34", "Servicing equipment, lack of or breakdown"),
            new DelayCode("35", "Aircraft cleaning"),
            new DelayCode("36", "Fuelling and defuelling, fuel supplier"),
            new DelayCode("37", "Catering, late delivery or loading"),
            new DelayCode("38", "ULD, lack of or serviceability"),
            new DelayCode("39", "Technical equipment, lack of or breakdown"),
            new DelayCode("41", "Aircraft defects"),
            new DelayCode("42", "Scheduled maintenance, late release"),
            new DelayCode("43", "Non-scheduled maintenance, special checks"),
            new DelayCode("44", "Spares and maintenance equipment"),
            new DelayCode("45", "AOG spares, to be carried to another station"),
            new DelayCode("46", "Aircraft change for technical reasons"),
            new DelayCode("47", "Standby aircraft, lack of planned standby aircraft"),
            new DelayCode("48", "Scheduled cabin configuration or version adjustments"),
            new DelayCode("51", "Damage during flight operations"),
            new DelayCode("52", "Damage during ground operations"),
            new DelayCode("55", "Departure control system failure"),
            new DelayCode("56", "Cargo preparation and documentation system failure"),
            new DelayCode("57", "Flight plans system failure"),
            new DelayCode("58", "Other automated system failure"),
            new DelayCode("61", "Flight plan, late completion or change"),
            new DelayCode("62", "Operational requirements, fuel or load alteration"),
            new DelayCode("63", "Late crew boarding or departure procedures"),
            new DelayCode("64", "Flight deck crew shortage"),
            new DelayCode("65", "Flight deck crew special request"),
            new DelayCode("66", "Late cabin crew boarding"),
            new DelayCode("67", "Cabin crew shortage"),
            new DelayCode("68", "Cabin crew error or special request"),
            new DelayCode("69", "Captain request for security check"),
            new DelayCode("71", "Weather at departure station"),
            new DelayCode("72", "Weather at destination station"),
            new DelayCode("73", "Weather en route or at alternate"),
            new DelayCode("75", "De-icing of aircraft"),
            new DelayCode("76", "Removal of snow, ice, water or sand from airport"),
            new DelayCode("77", "Ground handling impaired by adverse weather"),
            new DelayCode("81", "ATFM due to ATC en-route demand and capacity"),
            new DelayCode("82", "ATFM due to ATC staff or equipment en route"),
            new DelayCode("83", "ATFM due to restriction at destination airport"),
            new DelayCode("84", "ATFM due to weather at destination"),
            new DelayCode("85", "Mandatory security"),
            new DelayCode("86", "Immigration, customs, health"),
            new DelayCode("87", "Airport facilities"),
            new DelayCode("88", "Restrictions at airport of destination"),
            new DelayCode("89", "Restrictions at airport of departure"),
            new DelayCode("91", "Load connection, awaiting load from another flight"),
            new DelayCode("92", "Through check-in error"),
            new DelayCode("93", "Aircraft rotation, late arrival from another flight"),
            new DelayCode("94", "Cabin crew rotation"),
            new DelayCode("95", "Crew rotation"),
            new DelayCode("96", "Operations control"),
            new DelayCode("97", "Industrial action within own airline"),
            new DelayCode("98", "Industrial action outside own airline"),
            new DelayCode("99", "Other reason"));

    private IataDelayCodes() {
    }

    public static List<DelayCode> all() {
        return CODES;
    }

    public static boolean isKnown(String code) {
        return CODES.stream().anyMatch(delayCode -> delayCode.code().equals(code));
    }
}
