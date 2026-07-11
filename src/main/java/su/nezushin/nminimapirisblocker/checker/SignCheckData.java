package su.nezushin.nminimapirisblocker.checker;

import java.util.Map;

public record SignCheckData(SignCheckResult result, Map<String, String> resolved) {

}
