package clre20.customTicket.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public final class TextUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private TextUtil() {
    }

    /** 解析文字：含 & 色碼使用 legacy，含 <tag> 使用 MiniMessage。 */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        if (text.indexOf('&') < 0 && text.indexOf('<') >= 0 && text.indexOf('>') > 0) {
            try {
                return MINI.deserialize(text);
            } catch (Exception ignored) {
                // 不是合法的 MiniMessage，退回 legacy
            }
        }
        return LEGACY.deserialize(text);
    }

    /** 物品名稱 / 描述用：預設白字且不斜體。 */
    public static Component parseItem(String text) {
        return Component.empty()
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .append(parse(text));
    }

    public static List<Component> parseLore(List<String> lines) {
        List<Component> result = new ArrayList<>(lines.size());
        for (String line : lines) result.add(parseItem(line));
        return result;
    }

    public static String toPlainText(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /** 依序以 (key, value) 成對替換佔位符。 */
    public static String replace(String template, Object... pairs) {
        if (template == null) return "";
        String result = template;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            result = result.replace(String.valueOf(pairs[i]), String.valueOf(pairs[i + 1]));
        }
        return result;
    }
}
