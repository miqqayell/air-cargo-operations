package be.kdg.prog6.flights.core;

import be.kdg.prog6.flights.domain.DelayCode;
import be.kdg.prog6.flights.domain.IataDelayCodes;
import be.kdg.prog6.flights.port.in.ListDelayCodesQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultListDelayCodesQuery implements ListDelayCodesQuery {

    @Override
    public List<DelayCode> listDelayCodes() {
        return IataDelayCodes.all();
    }
}
