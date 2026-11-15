package be.kdg.prog6.loading.port.in;

import be.kdg.prog6.loading.domain.Load;

public interface TakeOffDeckUseCase {

    Load takeOffDeck(TakeOffDeckCommand command);
}
