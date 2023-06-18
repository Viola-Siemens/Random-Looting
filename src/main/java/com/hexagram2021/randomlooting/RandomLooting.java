package com.hexagram2021.randomlooting;

import com.hexagram2021.randomlooting.command.RLCommands;
import com.hexagram2021.randomlooting.config.RLServerConfig;
import com.hexagram2021.randomlooting.util.RLLogger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;

@Mod(RandomLooting.MODID)
public class RandomLooting {
    public static final String MODID = "randomlooting";

    public RandomLooting() {
        RLLogger.logger = LogManager.getLogger(MODID);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RLServerConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(RLCommands::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onServerStarted(ServerStartedEvent event) {
        if(!RLServerConfig.DISABLE.get()) {
            RLCommands.messup(event.getServer());
        }
    }
}