package fi.dy.masa.swiftflow;

import fi.dy.masa.swiftflow.config.Configs;
import fi.dy.masa.swiftflow.event.InputHandler;
import fi.dy.masa.swiftflow.event.RenderEventHandler;
import fi.dy.masa.swiftflow.recipes.CraftingHandler;
import net.minecraft.client.gui.screen.inventory.CraftingScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// ================================================================
// [SWIFTFLOW] :: main mod entry point
// Home key toggles inventory scroll/drag mode
// ================================================================
@Mod("swiftflow")
public class SwiftFlow {

    public static final String MOD_ID = "swiftflow";
    public static final Logger logger = LogManager.getLogger(MOD_ID);

    // TOGGLE: active state, flipped by Home key press
    public static boolean active = true;

    public SwiftFlow() {
        // INIT: register client setup on mod event bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::boot);
    }

    // BOOT: called on FMLClientSetupEvent
    private void boot(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // LOAD: config + crafting grid defs
            Configs.load();
            CraftingHandler.clearDefinitions();
            // REG: crafting screen -> output slot 0, grid slots 1..9
            CraftingHandler.reg(
                CraftingScreen.class.getName(),
                CraftingResultSlot.class.getName(),
                0, new CraftingHandler.SlotRange(1, 9)
            );
            // REG: player inventory -> output slot 0, grid slots 1..4
            CraftingHandler.reg(
                InventoryScreen.class.getName(),
                CraftingResultSlot.class.getName(),
                0, new CraftingHandler.SlotRange(1, 4)
            );
        });

        // REG: input + render handlers on forge event bus
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
        logger.info("[swiftflow] loaded. inventory is mine.");
    }
}
