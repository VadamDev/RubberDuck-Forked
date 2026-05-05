package com.github.salvadormg15.rubber_duck;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = RubberDuck.MOD_ID, dist = Dist.CLIENT)
public class RubberDuckClient {
    public RubberDuckClient(ModContainer mod) {
        mod.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
