package com.hexagram2021.randomlooting;

import com.hexagram2021.randomlooting.command.RLCommands;
import com.hexagram2021.randomlooting.config.RLServerConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(RandomLooting.MODID)
public class RandomLooting {
    public static final String MODID = "randomlooting";

    public RandomLooting() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RLServerConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(RLCommands::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    private void onServerStarted(ServerStartedEvent event) {
        if(!RLServerConfig.DISABLE.get()) {
            RLCommands.messup(event.getServer());
        }
    }
}