package be.kdg.prog6.loading.domain;

import java.util.List;

public record BlockedUld(UldCode uld, List<UldCode> blockedBy) {
}
