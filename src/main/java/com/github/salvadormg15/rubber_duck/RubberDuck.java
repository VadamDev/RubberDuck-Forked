package com.github.salvadormg15.rubber_duck;

import com.github.salvadormg15.rubber_duck.client.curio.RubberDuckCurioRenderer;
import com.github.salvadormg15.rubber_duck.common.Registries;
import com.github.salvadormg15.rubber_duck.common.config.CommonConfig;
import com.github.salvadormg15.rubber_duck.common.events.ForgeEventHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod("rubber_duck")
public class RubberDuck {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "rubber_duck";

    public RubberDuck() {
        final var bus = FMLJavaModLoadingContext.get().getModEventBus();
    	bus.addListener(this::setup);
    	bus.addListener(this::clientSetup);
        bus.addListener(this::addCreative);
        bus.addListener(this::enqueue);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, MOD_ID +"-common.toml");

        Registries.register(bus);

        MinecraftForge.EVENT_BUS.register(ForgeEventHandler.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CommonConfig.init();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(Registries.RUBBER_DUCK_ITEM.get(), RubberDuckCurioRenderer::new);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(!event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS))
            return;

        event.accept(Registries.RUBBER_DUCK_ITEM);
    }

    private void enqueue(InterModEnqueueEvent event) {
        if (!ModList.get().isLoaded("curios")) {
            LOGGER.error("Cannot find Curios in modloading");
            return;
        }

        //TODO: use datapack approach as this is deprecated for 1.21
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().cosmetic().build());
    }
}

   