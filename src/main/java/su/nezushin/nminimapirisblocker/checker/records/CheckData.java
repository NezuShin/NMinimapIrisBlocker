package su.nezushin.nminimapirisblocker.checker.records;

import su.nezushin.nminimapirisblocker.checker.CheckResult;

import java.util.Map;

public record CheckData(CheckResult result, Map<String, String> resolved) {

}
