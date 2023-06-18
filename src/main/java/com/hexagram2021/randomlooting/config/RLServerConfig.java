package com.hexagram2021.randomlooting.config;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class RLServerConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.LongValue SALT;
	public static final ForgeConfigSpec.BooleanValue DISABLE;
	public static final ForgeConfigSpec.IntValue PERMISSION_LEVEL_RESHUFFLE;
	public static final ForgeConfigSpec.IntValue PERMISSION_LEVEL_REVOKE;
	public static final ForgeConfigSpec.BooleanValue TYPE_SEPARATED;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELIST_LOOT_TYPES;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELIST_LOOTS;

	public static final ForgeConfigSpec.IntValue AUTO_REFRESH_SECOND;
	public static final ForgeConfigSpec.BooleanValue AUTO_REFRESH_CALLBACK;

	static {
		BUILDER.push("randomlooting-server-config");
			SALT = BUILDER.comment("The salt for random messing up the loot tables. You can change it dynamically by using command `/rl reshuffle`.")
					.defineInRange("SALT", 0L, -9223372036854775808L, 9223372036854775807L);
			DISABLE = BUILDER.comment("Disable this mod in your world.")
					.define("DISABLE", false);
			PERMISSION_LEVEL_RESHUFFLE = BUILDER.comment("The permission level for command `/rl reshuffle`.")
					.defineInRange("PERMISSION_LEVEL_RESHUFFLE", 2, 0, 4);
			PERMISSION_LEVEL_REVOKE = BUILDER.comment("The permission level for command `/rl revoke`.")
					.defineInRange("PERMISSION_LEVEL_REVOKE", 2, 0, 4);

			TYPE_SEPARATED = BUILDER.comment("Set true if you don't want all loot table types mess up with each other. For example, players can only get minecraft:cobblestone after breaking a stone or cobblestone block - if you set this to false, you may get it after killing a mob.")
					.define("TYPE_SEPARATED", true);
			WHITELIST_LOOT_TYPES = BUILDER.comment("The whitelist of loot table types that will not be messed up by this mod. For example, \"minecraft:gift\" to keep the loot tables of cats' and villagers' gift away from messing up.")
					.defineList("WHITELIST_LOOT_TYPES", ImmutableList.of(
							new ResourceLocation("minecraft", "barter").toString()
					), o -> ResourceLocation.isValidResourceLocation((String)o));
			WHITELIST_LOOTS = BUILDER.comment("The whitelist of loot tables that will not be messed up by this mod. For example, \"minecraft:blocks/acacia_planks\" to make sure you can always get acacia planks after breaking the corresponding block.")
					.defineList("WHITELIST_LOOTS", ImmutableList.of(
							new ResourceLocation("entities/ender_dragon").toString()
					), o -> ResourceLocation.isValidResourceLocation((String)o));

			AUTO_REFRESH_SECOND = BUILDER.comment("Set to x (x > 0), server will automatically reshuffle loot tables every x seconds. Set to 0 to disable.")
					.defineInRange("AUTO_REFRESH_SECOND", 0, 0, 2147483647);
			AUTO_REFRESH_CALLBACK = BUILDER.comment("Send message after automatically reshuffling loot tables to every players online or not.")
					.define("AUTO_REFRESH_CALLBACK", false);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
