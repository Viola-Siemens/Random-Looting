package com.hexagram2021.randomlooting.mixin;

import com.hexagram2021.randomlooting.command.RLCommands;
import com.hexagram2021.randomlooting.config.RLServerConfig;
import com.hexagram2021.randomlooting.util.RLLogger;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	private int tickCount;

	@Shadow
	private ProfilerFiller profiler;

	@Shadow @Final
	private RandomSource random;

	@Shadow
	public abstract PlayerList getPlayerList();

	@Unique
	private int rl$lastAutoRefreshLootTableTick = 0;
	
	@Unique
	private boolean rl$nextReshuffle = true;

	@Inject(method = "tickServer", at = @At(value = "TAIL"))
	public void tryReshuffling(BooleanSupplier hasTime, CallbackInfo ci) {
		long second = RLServerConfig.AUTO_REFRESH_SECOND.get();
		if(second > 0 && this.tickCount - this.rl$lastAutoRefreshLootTableTick >= second * 20) {
			if(RLServerConfig.DISABLE.get()) {
				this.rl$nextReshuffle = false;
			} else if(this.rl$nextReshuffle) {
				this.rl$lastAutoRefreshLootTableTick = this.tickCount;
				RLLogger.debug("Auto refresh loot tables!");
				this.profiler.push("randomlooting:refresh_loot_tables");
				RLServerConfig.SALT.set(this.random.nextLong());
				RLCommands.messup((MinecraftServer)(Object)this);
				if(RLServerConfig.AUTO_REFRESH_CALLBACK.get()) {
					this.getPlayerList().broadcastSystemMessage(
							Component.translatable("commands.randomlooting.reshuffle.success"), ChatType.SYSTEM
					);
				}
				this.profiler.pop();
			} else {
				this.rl$lastAutoRefreshLootTableTick = this.tickCount;
				this.rl$nextReshuffle = true;
			}
		}
	}
}
