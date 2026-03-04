package fi.dy.masa.swiftflow.recipes;

import fi.dy.masa.swiftflow.SwiftFlow;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ================================================================
// [SWIFTFLOW::CRAFTING] :: crafting grid slot registry
// maps output slot -> ingredient slot range per GUI class
// ================================================================
public class CraftingHandler {

    // MAP: (gui class, slot class, slot index) -> ingredient range
    private static final Map<OutSlot, SlotRange> GRIDS   = new HashMap<>();

    // SET: registered crafting GUI classes
    private static final Set<Class<? extends ContainerScreen>> GUI_SET = new HashSet<>();

    // CLEAR: reset all registered crafting definitions
    public static void clearDefinitions() {
        GRIDS.clear();
        GUI_SET.clear();
    }

    // REG: bind output slot to ingredient range for a given GUI
    public static boolean reg(String guiClass, String slotClass, int outIdx, SlotRange range) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends ContainerScreen> gui =
                (Class<? extends ContainerScreen>) Class.forName(guiClass);
            Class<? extends Slot> slot = Class.forName(slotClass).asSubclass(Slot.class);
            GRIDS.put(new OutSlot(gui, slot, outIdx), range);
            GUI_SET.add(gui);
            return true;
        } catch (Exception e) {
            SwiftFlow.logger.warn("[swiftflow] reg failed: gui={} slot={}", guiClass, slotClass);
            return false;
        }
    }

    // GET: ingredient range for hovered slot in given GUI (null if not a crafting slot)
    public static SlotRange getRange(ContainerScreen<?> gui, Slot slot) {
        return GRIDS.get(OutSlot.from(gui, slot));
    }

    // GET: first output slot in crafting GUI (null if GUI not registered)
    public static Slot firstOutputSlot(ContainerScreen<?> gui) {
        if (!GUI_SET.contains(gui.getClass())) return null;
        for (Slot s : gui.getMenu().slots) {
            if (getRange(gui, s) != null) return s;
        }
        return null;
    }

    // ---- OUTPUT SLOT KEY ----
    // identifies output slot by: GUI class + slot class + slot index
    static class OutSlot {
        final Class<? extends ContainerScreen> gui;
        final Class<? extends Slot>            slot;
        final int                              idx;

        OutSlot(Class<? extends ContainerScreen> gui, Class<? extends Slot> slot, int idx) {
            this.gui  = gui;
            this.slot = slot;
            this.idx  = idx;
        }

        // BUILD: key from live GUI + slot instances
        static OutSlot from(ContainerScreen<?> gui, Slot slot) {
            return new OutSlot(gui.getClass(), slot.getClass(), slot.index);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof OutSlot)) return false;
            OutSlot other = (OutSlot) o;
            return idx == other.idx && gui == other.gui && slot == other.slot;
        }

        @Override
        public int hashCode() {
            return 31 * (31 * gui.hashCode() + slot.hashCode()) + idx;
        }
    }

    // ---- SLOT RANGE ----
    // inclusive range [first, last] of ingredient slot indices
    public static class SlotRange {
        public final int first;
        public final int last;

        // CTOR: first=start, last=start+count-1
        public SlotRange(int first, int count) {
            this.first = first;
            this.last  = first + count - 1;
        }

        // CHK: slot index within range
        public boolean contains(int idx) {
            return idx >= first && idx <= last;
        }
    }
}
