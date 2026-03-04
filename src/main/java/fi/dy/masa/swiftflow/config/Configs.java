package fi.dy.masa.swiftflow.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

// ================================================================
// [SWIFTFLOW::CFG] :: JSON config loader
// file: .minecraft/config/swiftflow.json
// ================================================================
public class Configs {

    private static final String FILE = "config/swiftflow.json";

    // BLACKLIST: GUI class names to skip scroll/drag handling
    public static final Set<String> GUI_BLACKLIST  = new HashSet<>();

    // BLACKLIST: slot class names to skip quick-move
    public static final Set<String> SLOT_BLACKLIST = new HashSet<>();

    // LOAD: parse JSON config if present; silently skip on missing/bad file
    public static void load() {
        File f = cfgFile();
        if (!f.exists()) return;
        try (FileReader fr = new FileReader(f)) {
            JsonElement el = new JsonParser().parse(fr);
            if (el == null || !el.isJsonObject()) return;
            JsonObject root = el.getAsJsonObject();
            readT(root);
            readG(root);
            readSet(root, GUI_BLACKLIST,  "guiBlacklist");
            readSet(root, SLOT_BLACKLIST, "slotBlacklist");
        } catch (Exception ignored) {}
    }

    // READ: toggle flags from "toggles" object
    private static void readT(JsonObject root) {
        if (!root.has("toggles")) return;
        JsonObject t = root.getAsJsonObject("toggles");
        if (t.has("scrollSingle"))        T.SCROLL_SINGLE        = t.get("scrollSingle").getAsBoolean();
        if (t.has("scrollStacks"))        T.SCROLL_STACKS        = t.get("scrollStacks").getAsBoolean();
        if (t.has("shiftDrop"))           T.SHIFT_DROP           = t.get("shiftDrop").getAsBoolean();
        if (t.has("shiftPlace"))          T.SHIFT_PLACE          = t.get("shiftPlace").getAsBoolean();
        if (t.has("crafting"))            T.CRAFTING             = t.get("crafting").getAsBoolean();
        if (t.has("craftStackRightClick"))T.CRAFT_STACK_RIGHTCLICK = t.get("craftStackRightClick").getAsBoolean();
    }

    // READ: generic flags from "generic" object
    private static void readG(JsonObject root) {
        if (!root.has("generic")) return;
        JsonObject g = root.getAsJsonObject("generic");
        if (g.has("reverseSingle")) G.REVERSE_SINGLE = g.get("reverseSingle").getAsBoolean();
        if (g.has("reverseStacks")) G.REVERSE_STACKS = g.get("reverseStacks").getAsBoolean();
    }

    // READ: string array from JSON key into set
    private static void readSet(JsonObject root, Set<String> out, String key) {
        if (!root.has(key)) return;
        for (JsonElement e : root.getAsJsonArray(key))
            out.add(e.getAsString());
    }

    private static File cfgFile() {
        return new File(FILE);
    }

    // ---- toggle flags (all on by default) ----
    public static class T {
        public static boolean SCROLL_SINGLE         = true;
        public static boolean SCROLL_STACKS         = true;
        public static boolean SHIFT_DROP            = true;
        public static boolean SHIFT_PLACE           = true;
        public static boolean CRAFTING              = true;
        public static boolean CRAFT_STACK_RIGHTCLICK = true;
    }

    // ---- generic flags (all off by default) ----
    public static class G {
        public static boolean REVERSE_SINGLE = false;
        public static boolean REVERSE_STACKS = false;
    }
}
