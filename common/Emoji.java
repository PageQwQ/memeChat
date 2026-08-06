package pageqwq.memechat.common;

/**
 * Meme metadata. `path` is the resource-pack-relative path (e.g. "memes/beluga.png"),
 * from which adapters build Identifier / ResourceLocation.
 * `group` is the memegroup (sub-folder of assets/memechat/memes/), empty for
 * memes placed directly in the memes folder.
 * `pack` is the source pack id (e.g. "file/memechat-test-pack").
 */
public record Emoji(int id, String name, String path, boolean isGif, String group, String pack) {

    /** The code point used for rendering (only interpreted inside the memechat:emoji font) */
    public int codePoint() {
        return id + 33;
    }
}
