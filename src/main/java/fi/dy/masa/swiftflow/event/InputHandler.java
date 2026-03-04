package fi.dy.masa.swiftflow.event;

import fi.dy.masa.swiftflow.SwiftFlow;
import fi.dy.masa.swiftflow.util.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

// ================================================================
// [SWIFTFLOW::INPUT] :: keyboard + mouse event handler
// key 268 = Home, toggles active state
// ================================================================
public class InputHandler {

    // STATE: LMB drag in progress
    private boolean dragging = false;

    // STATE: last hovered slot during drag sequence
    private Slot lastSlot = null;

    // KEY: toggle active on Home press (action=1)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (event.getKey() != 268 || event.getAction() != 1) return;
        SwiftFlow.active = !SwiftFlow.active;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // MSG: notify player of toggle state
        String msg = SwiftFlow.active ? "[swiftflow] on" : "[swiftflow] off";
        mc.player.displayClientMessage(new StringTextComponent(msg), true);
    }

    // CLICK: LMB in inventory - quick-move if shift held and slot valid
    @SubscribeEvent
    public void onClick(GuiScreenEvent.MouseClickedEvent.Pre event) {
        if (!SwiftFlow.active) return;
        if (!(event.getGui() instanceof ContainerScreen)) return;
        // CHK: only LMB with shift
        if (event.getButton() != 0 || !shiftDown()) return;
        Minecraft mc = Minecraft.getInstance();
        // CHK: no item on cursor
        if (!mc.player.inventory.getCarried().isEmpty()) return;
        dragging = true;
        ContainerScreen<?> gui = (ContainerScreen<?>) event.getGui();
        Slot slot = InventoryUtils.slotAt(gui);
        lastSlot = slot;
        if (slot != null && slot.hasItem()) {
            InventoryUtils.quickMove(gui, slot);
            event.setCanceled(true);
        }
    }

    // RELEASE: clear drag state on LMB release
    @SubscribeEvent
    public void onRelease(GuiScreenEvent.MouseReleasedEvent.Pre event) {
        if (event.getButton() == 0) {
            dragging  = false;
            lastSlot  = null;
        }
    }

    // DRAG: shift-drag quick-moves each newly hovered slot
    @SubscribeEvent
    public void onDrag(GuiScreenEvent.MouseDragEvent.Pre event) {
        if (!SwiftFlow.active) return;
        if (!dragging || !shiftDown()) return;
        if (!(event.getGui() instanceof ContainerScreen)) return;
        ContainerScreen<?> gui = (ContainerScreen<?>) event.getGui();
        Slot slot = InventoryUtils.slotAt(gui);
        // SKIP: null or same slot as last
        if (slot == null || slot == lastSlot) return;
        lastSlot = slot;
        if (slot.hasItem()) {
            InventoryUtils.quickMove(gui, slot);
            event.setCanceled(true);
        }
    }

    // SCROLL: up/down in inventory - swap hotbar or quick-move (shift)
    @SubscribeEvent
    public void onScroll(GuiScreenEvent.MouseScrollEvent.Pre event) {
        if (!SwiftFlow.active) return;
        if (!(event.getGui() instanceof ContainerScreen)) return;
        ContainerScreen<?> gui = (ContainerScreen<?>) event.getGui();
        Slot slot = InventoryUtils.slotAt(gui);
        if (slot == null) return;
        if (shiftDown()) {
            // SHIFT+SCROLL: quick-move hovered slot
            InventoryUtils.quickMove(gui, slot);
        } else {
            // SCROLL: swap hotbar slot matching item (up=true, down=false)
            boolean up = event.getScrollDelta() > 0;
            InventoryUtils.swapHotbar(gui, slot, up);
        }
        event.setCanceled(true);
    }

    // UTIL: check if either shift key is held
    private boolean shiftDown() {
        long win = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT)  == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }
}
