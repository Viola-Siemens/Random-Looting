package com.hexagram2021.randomlooting;

import com.hexagram2021.randomlooting.command.RLCommands;
import com.hexagram2021.randomlooting.config.RLServerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class RandomLooting implements ModInitializer {
	public static final String MODID = "randomlooting";

	public RandomLooting() {
		ModLoadingContext.registerConfig(MODID, ModConfig.Type.SERVER, RLServerConfig.SPEC);
	}

	@Override
	public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if(!RLServerConfig.DISABLE.get()) {
                RLCommands.messup(server);
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(RLCommands.register()));
	}
}