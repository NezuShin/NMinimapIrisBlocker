package su.nezushin.nminimapirisblocker.checker.records;

import su.nezushin.nminimapirisblocker.checker.SignCheckResult;

import java.util.Map;

public record SignCheckData(SignCheckResult result, Map<String, String> resolved) {

}
