package pageqwq.guilib;

/** 跨版本资源标识（各版本实现内部映射为 ResourceLocation / Identifier） */
public record GuiResource(String namespace, String path) {
    public static GuiResource of(String namespace, String path) {
        return new GuiResource(namespace, path);
    }
}
