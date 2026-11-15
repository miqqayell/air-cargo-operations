package be.kdg.prog6.loading.adapter.in;

import be.kdg.prog6.loading.domain.BlockedUld;
import be.kdg.prog6.loading.domain.CheckResult;
import be.kdg.prog6.loading.domain.LoadReport;
import be.kdg.prog6.loading.domain.UldCode;

import java.util.List;

public record LoadReportDto(boolean hardChecksPass, List<CheckResultDto> results) {

    public record CheckResultDto(String name, String kind, boolean passed, String reason,
                                 List<BlockedUldDto> blockedUlds) {

        static CheckResultDto from(CheckResult result) {
            return new CheckResultDto(result.name(), result.kind().name(), result.passed(), result.reason(),
                    result.blockedUlds().stream().map(BlockedUldDto::from).toList());
        }
    }

    public record BlockedUldDto(String uldCode, List<String> blockedBy) {

        static BlockedUldDto from(BlockedUld blockedUld) {
            return new BlockedUldDto(blockedUld.uld().value(),
                    blockedUld.blockedBy().stream().map(UldCode::value).toList());
        }
    }

    public static LoadReportDto from(LoadReport report) {
        return new LoadReportDto(report.hardChecksPass(), report.results().stream().map(CheckResultDto::from).toList());
    }
}
