package com.hexagram2021.randomlooting.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hexagram2021.randomlooting.config.RLServerConfig;
import com.hexagram2021.randomlooting.util.IMessUpLootTables;
import com.hexagram2021.randomlooting.util.RLLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(LootDataManager.class)
public class LootTablesMixin implements IMessUpLootTables {
	@Shadow
	private Map<LootDataId<?>, ?> elements;
	
	@Nullable
	private Map<ResourceLocation, LootTable> backup_tables;
	
	@Inject(method = "apply", at = @At(value = "TAIL"))
	public void init_backups(Map<LootDataType<?>, Map<ResourceLocation, ?>> map, CallbackInfo ci) {
		ImmutableMap.Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();
		this.elements.forEach((id, o) -> {
			if(id.type().equals(LootDataType.TABLE)) {
				builder.put(id.location(), (LootTable)o);
			}
		});
		this.backup_tables = builder.build();
	}
	
	@Override
	public void revoke() {
		ImmutableMap.Builder<LootDataId<?>, Object> builder = ImmutableMap.builder();
		this.elements.forEach((id, o) -> {
			if(!id.type().equals(LootDataType.TABLE)) {
				builder.put(id, o);
			}
		});
		Objects.requireNonNull(this.backup_tables).forEach((id, lt) -> builder.put(new LootDataId<>(LootDataType.TABLE, id), lt));
		this.elements = builder.build();
	}
	
	@Override
	public void messup(Random random) {
		List<ResourceLocation> ids = Lists.newArrayList();
		List<LootTable> results = Lists.newArrayList();
		ImmutableMap.Builder<LootDataId<?>, Object> whitelist_builder = ImmutableMap.builder();
		
		assert this.backup_tables != null;
		if(RLServerConfig.TYPE_SEPARATED.get()) {
			Map<ResourceLocation, List<LootTable>> separatedTables = Maps.newHashMap();
			
			this.backup_tables.forEach((id, lootTable) -> {
				if(!RLServerConfig.WHITELIST_LOOTS.get().contains(id.toString())) {
					ResourceLocation type = LootContextParamSets.getKey(lootTable.getParamSet());
					if(type != null && !RLServerConfig.WHITELIST_LOOT_TYPES.get().contains(type.toString())) {
						separatedTables.computeIfAbsent(
								type,
								resourceLocation -> Lists.newArrayList()
						).add(lootTable);
					}
				}
			});
			
			separatedTables.forEach((type, lootTableList) -> {
				RLLogger.debug("Shuffling " + type);
				Collections.shuffle(lootTableList, random);
			});
			
			this.backup_tables.forEach((id, lootTable) -> {
				if(!RLServerConfig.WHITELIST_LOOTS.get().contains(id.toString())) {
					ResourceLocation type = LootContextParamSets.getKey(lootTable.getParamSet());
					if (type != null && !RLServerConfig.WHITELIST_LOOT_TYPES.get().contains(type.toString())) {
						List<LootTable> listWithType = separatedTables.get(type);
						ids.add(id);
						if(listWithType.size() <= 0) {
							RLLogger.error("Error! Type %s has no more loot table to shuffle!".formatted(type.toString()));
							results.add(LootTable.EMPTY);
						} else {
							LootTable toAdd = listWithType.remove(listWithType.size() - 1);
							results.add(toAdd);
						}
					} else {
						whitelist_builder.put(new LootDataId<>(LootDataType.TABLE, id), lootTable);
					}
				} else {
					whitelist_builder.put(new LootDataId<>(LootDataType.TABLE, id), lootTable);
				}
			});
		} else {
			this.backup_tables.forEach((id, lootTable) -> {
				if(!RLServerConfig.WHITELIST_LOOTS.get().contains(id.toString())) {
					ResourceLocation type = LootContextParamSets.getKey(lootTable.getParamSet());
					if (type != null && !RLServerConfig.WHITELIST_LOOT_TYPES.get().contains(type.toString())) {
						ids.add(id);
						results.add(lootTable);
					} else {
						whitelist_builder.put(new LootDataId<>(LootDataType.TABLE, id), lootTable);
					}
				} else {
					whitelist_builder.put(new LootDataId<>(LootDataType.TABLE, id), lootTable);
				}
			});
			Collections.shuffle(results, random);
		}
		
		if(ids.size() != results.size()) {
			RLLogger.error("Error! The size of ids is %d, but the size of results is %d!".formatted(ids.size(), results.size()));
			while(results.size() < ids.size()) {
				results.add(LootTable.EMPTY);
			}
		}
		
		ImmutableMap.Builder<LootDataId<?>, Object> builder = ImmutableMap.builder();
		this.elements.forEach((id, o) -> {
			if(!id.type().equals(LootDataType.TABLE)) {
				builder.put(id, o);
			}
		});
		for(int i = 0; i < ids.size(); ++i) {
			builder.put(new LootDataId<>(LootDataType.TABLE, ids.get(i)), results.get(i));
		}
		builder.putAll(whitelist_builder.build());
		
		this.elements = builder.build();
	}
}
