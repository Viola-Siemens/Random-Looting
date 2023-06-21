package com.hexagram2021.randomlooting.mixin;

import com.hexagram2021.randomlooting.command.RLCommands;
import com.hexagram2021.randomlooting.config.RLServerConfig;
import com.hexagram2021.randomlooting.util.RLLogger;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	private int tickCount;

	@Shadow
	private ProfilerFiller profiler;

	@Shadow @Final
	private Random random;

	@Shadow
	public abstract PlayerList getPlayerList();

	private int lastAutoRefreshRecipeTick = 0;
	
	private boolean nextReshuffle = true;

	@Inject(method = "tickServer", at = @At(value = "TAIL"))
	public void tryReshuffling(BooleanSupplier hasTime, CallbackInfo ci) {
		long second = RLServerConfig.AUTO_REFRESH_SECOND.get();
		if(second > 0 && this.tickCount - this.lastAutoRefreshRecipeTick >= second * 20) {
			if(RLServerConfig.DISABLE.get()) {
				this.nextReshuffle = false;
			} else if(this.nextReshuffle) {
				this.lastAutoRefreshRecipeTick = this.tickCount;
				RLLogger.debug("Auto refresh recipes!");
				this.profiler.push("randomlooting:refresh_recipes");
				RLServerConfig.SALT.set(this.random.nextLong());
				RLCommands.messup((MinecraftServer)(Object)this);
				if(RLServerConfig.AUTO_REFRESH_CALLBACK.get()) {
					this.getPlayerList().broadcastMessage(
							new TranslatableComponent("commands.randomlooting.reshuffle.success"), ChatType.SYSTEM, Util.NIL_UUID
					);
				}
				this.profiler.pop();
			} else {
				this.lastAutoRefreshRecipeTick = this.tickCount;
				this.nextReshuffle = true;
			}
		}
	}
}
