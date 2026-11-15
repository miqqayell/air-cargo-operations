package be.kdg.prog6.flights.port.in;

import be.kdg.prog6.flights.domain.DelayCode;

import java.util.List;

public interface ListDelayCodesQuery {

    List<DelayCode> listDelayCodes();
}
