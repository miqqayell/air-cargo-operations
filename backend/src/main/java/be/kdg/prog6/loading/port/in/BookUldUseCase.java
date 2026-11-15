package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.loading.domain.Load;

public interface BookUldUseCase {

    Load bookUld(BookUldCommand command);
}
