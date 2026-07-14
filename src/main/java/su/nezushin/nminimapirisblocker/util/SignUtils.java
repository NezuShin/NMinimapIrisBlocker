package su.nezushin.nminimapirisblocker.util;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public final class SignUtils {

    public static List<Component> padToFour(List<Component> lines) {
        List<Component> padded = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            padded.add(i < lines.size() ? lines.get(i) : Component.empty());
        }
        return padded;
    }
}
