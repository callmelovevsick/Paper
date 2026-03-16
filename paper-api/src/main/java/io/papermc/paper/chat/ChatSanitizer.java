package io.papermc.paper.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

/**
 * Sanitizes player-provided MiniMessage strings to prevent
 * click/hover event injection attacks.
 *
 * <p>This is a Paper core utility — plugin developers should use this
 * instead of calling MiniMessage directly on untrusted input.</p>
 */
public final class ChatSanitizer {

    // ── Chỉ cho phép color + decoration, KHÔNG có click/hover/insert ──
    private static final TagResolver SAFE_TAGS = TagResolver.builder()
        .resolver(StandardTags.color())
        .resolver(StandardTags.decorations())
        .resolver(StandardTags.gradient())
        .resolver(StandardTags.rainbow())
        .resolver(StandardTags.reset())
        .resolver(StandardTags.newline())
        // KHÔNG thêm: StandardTags.clickEvent(), StandardTags.hoverEvent(),
        //             StandardTags.insertion(), StandardTags.selector(),
        //             StandardTags.score(), StandardTags.nbt()
        .build();

    private static final MiniMessage SAFE_PARSER = MiniMessage.builder()
        .tags(SAFE_TAGS)
        .build();

    private static final MiniMessage STRIP_PARSER = MiniMessage.miniMessage();

    private ChatSanitizer() {}

    /**
     * Strip TẤT CẢ MiniMessage tags khỏi chuỗi.
     * Dùng khi không muốn người chơi format gì cả.
     *
     * @param input chuỗi từ người chơi
     * @return plain text không có tags
     */
    @NotNull
    public static String stripAll(@NotNull String input) {
        return STRIP_PARSER.stripTags(input);
    }

    /**
     * Strip chỉ các tags nguy hiểm (click, hover, insert, selector, nbt, score).
     * Vẫn giữ lại color/decoration cho đẹp.
     *
     * @param input chuỗi từ người chơi
     * @return chuỗi đã bỏ tags nguy hiểm, giữ tags an toàn
     */
    @NotNull
    public static String stripDangerous(@NotNull String input) {
        // Strip các tags không nằm trong whitelist bằng cách parse + serialize lại
        Component safe = SAFE_PARSER.deserialize(input);
        return SAFE_PARSER.serialize(safe);
    }

    /**
     * Parse chuỗi từ người chơi thành Component an toàn.
     * ClickEvent/HoverEvent sẽ bị loại bỏ hoàn toàn.
     *
     * @param input chuỗi từ người chơi
     * @return Component không chứa sự kiện nguy hiểm
     */
    @NotNull
    public static Component toSafeComponent(@NotNull String input) {
        return SAFE_PARSER.deserialize(input);
    }

    /**
     * Kiểm tra nhanh xem chuỗi có chứa tags nguy hiểm không.
     *
     * @param input chuỗi cần kiểm tra
     * @return true nếu phát hiện tag nguy hiểm
     */
    public static boolean containsDangerousTags(@NotNull String input) {
        String[] dangerousPatterns = {
            "<click:", "<hover:", "<insert:", "<selector:", "<sel:",
            "<score:", "<nbt:", "<data:", "<lang:", "<tr:", "<translate:"
        };
        String lower = input.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lower.contains(pattern)) return true;
        }
        return false;
    }
}
