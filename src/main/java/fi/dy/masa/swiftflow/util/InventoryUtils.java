package fi.dy.masa.swiftflow.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import java.lang.reflect.Field;

// ================================================================
// [SWIFTFLOW::UTIL] :: inventory slot + click helpers
// uses obf-reflection to read hovered slot + gui offsets
// ================================================================
public class InventoryUtils {

    // OBF: ContainerScreen field names (SRG)
    private static final String F_HOVERED = "field_147006_u"; // hoveredSlot
    private static final String F_LEFT    = "field_147003_i"; // guiLeft
    private static final String F_TOP     = "field_147009_r"; // guiTop

    // GET: hovered slot from ContainerScreen via reflection
    public static Slot slotAt(ContainerScreen<?> gui) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ContainerScreen.class, F_HOVERED);
            Object val = f.get(gui);
            if (val instanceof Slot) return (Slot) val;
        } catch (Exception ignored) {}
        // FALLBACK: manual hit-test by mouse position
        return slotAtFallback(gui);
    }

    // FALLBACK: find slot under cursor using scaled mouse coords
    private static Slot slotAtFallback(ContainerScreen<?> gui) {
        Minecraft mc  = Minecraft.getInstance();
        double scale  = mc.getWindow().getGuiScale();
        double mx     = mc.mouseHandler.xpos() / scale;
        double my     = mc.mouseHandler.ypos() / scale;
        int left = guiLeft(gui);
        int top  = guiTop(gui);
        for (Slot s : gui.getMenu().slots) {
            int sx = left + s.x;
            int sy = top  + s.y;
            // CHK: 16x16 slot hit box
            if (mx >= sx && mx < sx + 16 && my >= sy && my < sy + 16)
                return s;
        }
        return null;
    }

    // ACTION: shift-click slot (QUICK_MOVE)
    public static void quickMove(ContainerScreen<?> gui, Slot slot) {
        if (slot == null || !slot.hasItem()) return;
        Minecraft mc = Minecraft.getInstance();
        mc.gameMode.handleInventoryMouseClick(
            gui.getMenu().containerId,
            slot.index, 0,
            ClickType.QUICK_MOVE,
            mc.player
        );
    }

    // ACTION: swap slot with matching hotbar slot (SWAP)
    // up=true -> find hotbar slot with matching item; up=false -> use selected slot
    public static void swapHotbar(ContainerScreen<?> gui, Slot slot, boolean up) {
        if (slot == null || !slot.hasItem()) return;
        Minecraft mc = Minecraft.getInstance();
        int hotbarIdx;
        if (up) {
            hotbarIdx = matchingOrEmpty(mc, slot.getItem());
        } else {
            hotbarIdx = mc.player.inventory.selected;
        }
        if (hotbarIdx < 0) return;
        mc.gameMode.handleInventoryMouseClick(
            gui.getMenu().containerId,
            slot.index, hotbarIdx,
            ClickType.SWAP,
            mc.player
        );
    }

    // FIND: hotbar slot index with matching item, or first empty slot
    private static int matchingOrEmpty(Minecraft mc, ItemStack target) {
        // PASS 1: find slot with same item type
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getItem(i);
            if (!s.isEmpty() && s.getItem() == target.getItem()) return i;
        }
        // PASS 2: find first empty slot
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getItem(i).isEmpty()) return i;
        }
        return mc.player.inventory.selected;
    }

    // GET: guiLeft via reflection (fallback: screen.width/2 - 88)
    public static int guiLeft(ContainerScreen<?> gui) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ContainerScreen.class, F_LEFT);
            return (int) f.get(gui);
        } catch (Exception ignored) {}
        return gui.width / 2 - 88;
    }

    // GET: guiTop via reflection (fallback: (screen.height - 166) / 2)
    public static int guiTop(ContainerScreen<?> gui) {
        try {
            Field f = ObfuscationReflectionHelper.findField(ContainerScreen.class, F_TOP);
            return (int) f.get(gui);
        } catch (Exception ignored) {}
        return (gui.height - 166) / 2;
    }
}
