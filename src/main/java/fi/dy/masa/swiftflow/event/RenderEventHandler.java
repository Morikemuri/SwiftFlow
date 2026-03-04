package fi.dy.masa.swiftflow.event;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// ================================================================
// [SWIFTFLOW::RENDER] :: overlay render hook (reserved for HUD)
// ================================================================
public class RenderEventHandler {

    // OVERLAY: placeholder - no HUD rendering needed
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        // reserved
    }
}
