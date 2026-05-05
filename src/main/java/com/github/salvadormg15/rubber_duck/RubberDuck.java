package com.github.salvadormg15.rubber_duck;

import com.github.salvadormg15.rubber_duck.client.curio.RubberDuckCurioRenderer;
import com.github.salvadormg15.rubber_duck.common.Registries;
import com.github.salvadormg15.rubber_duck.common.config.CommonConfig;
import com.github.salvadormg15.rubber_duck.common.events.NeoForgeEventHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(RubberDuck.MOD_ID)
public class RubberDuck {
    public static final String MOD_ID = "rubber_duck";

    public RubberDuck(IEventBus bus, ModContainer mod) {
        bus.addListener(this::onConfigEvent);
    	bus.addListener(this::clientSetup);
        bus.addListener(this::addCreative);

        mod.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        Registries.register(bus);
        NeoForge.EVENT_BUS.register(NeoForgeEventHandler.class);
    }

    private void onConfigEvent(ModConfigEvent event) {
        if(event.getConfig().getSpec() != CommonConfig.SPEC)
            return;

        CommonConfig.init();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(Registries.RUBBER_DUCK_ITEM.get(), RubberDuckCurioRenderer::new);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(!event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS))
            return;

        event.accept(Registries.RUBBER_DUCK_ITEM.get());
    }
}
   