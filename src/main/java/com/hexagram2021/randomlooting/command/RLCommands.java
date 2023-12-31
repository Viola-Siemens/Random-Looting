package com.hexagram2021.randomlooting.command;

import com.hexagram2021.randomlooting.config.RLServerConfig;
import com.hexagram2021.randomlooting.util.IMessUpLootTables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.Random;

public class RLCommands {
	public static void registerCommands(RegisterCommandsEvent event) {
		final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(RLCommands.register());
	}

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("rl").then(
				Commands.literal("reshuffle").requires(stack -> stack.hasPermission(RLServerConfig.PERMISSION_LEVEL_RESHUFFLE.get()))
						.executes(context -> reshuffle(context.getSource().getServer(), context.getSource().getPlayerOrException()))
						.then(
								Commands.argument("salt", LongArgumentType.longArg())
										.executes(context -> reshuffle(context.getSource().getServer(), LongArgumentType.getLong(context, "salt")))
						)
		).then(
				Commands.literal("revoke").requires(stack -> stack.hasPermission(RLServerConfig.PERMISSION_LEVEL_REVOKE.get()))
						.executes(context -> revoke(context.getSource().getServer()))
		);
	}

	private static int reshuffle(MinecraftServer server, ServerPlayer entity) {
		return reshuffle(server, entity.getRandom().nextLong());
	}

	private static int reshuffle(MinecraftServer server, long seed) {
		RLServerConfig.SALT.set(seed);
		if(RLServerConfig.DISABLE.get()) {
			RLServerConfig.DISABLE.set(false);
		}
		messup(server);
		server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomlooting.reshuffle.success"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int revoke(MinecraftServer server) {
		RLServerConfig.DISABLE.set(true);
		((IMessUpLootTables) server.getLootData()).revoke();
		server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomlooting.revoke.success"), false);
		return Command.SINGLE_SUCCESS;
	}

	public static void messup(MinecraftServer server) {
		long seed = server.getWorldData().worldGenOptions().seed() ^ RLServerConfig.SALT.get();
		Random random = new Random(seed);
		((IMessUpLootTables) server.getLootData()).messup(random);
	}
}
