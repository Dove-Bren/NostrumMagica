package com.smanzana.nostrummagica.listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.curios.inventory.CurioInventoryWrapper;
import com.smanzana.nostrummagica.integration.curios.inventory.CurioSlotReference;
import com.smanzana.nostrummagica.inventory.EquipmentSetRegistry;
import com.smanzana.nostrummagica.inventory.EquipmentSlotKey;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.item.set.EquipmentSet;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class ItemSetListener {
	
	private static final class SetState {
		public final Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> items;
		public Multimap<Attribute, AttributeModifier> attributes;
		
		public SetState() {
			this.items = new HashMap<>();
			this.attributes = HashMultimap.create();
		}
	}
	
	private static final class EquipmentState {
		public final Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> equipment;
		public final Map<EquipmentSet, SetState> sets;
		public final Multimap<Attribute, AttributeModifier> attributes;
		
		public EquipmentState() {
			equipment = new HashMap<>();
			sets = new HashMap<>();
			attributes = ArrayListMultimap.create();
		}
		
		protected void setFrom(LivingEntity entity) {
			equipment.clear();
			sets.clear();
			this.attributes.clear();
			
			// Scan inventory and record all equipment stacks
			ScanEntityEquipment(entity, (slot, stack) -> {
				if (!stack.isEmpty()) {
					equipment.put(slot, stack.copy());
					
					for (EquipmentSet set : EquipmentSetRegistry.GetAllSets()) {
						final Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> existing =
								sets.containsKey(set) ? sets.get(set).items : new HashMap<>();
						if (set.isSetItem(stack) && set.isSetItemValid(stack, slot, existing)) {
							sets.computeIfAbsent(set, r -> new SetState())
								.items.put(slot, stack.copy());
						}
					}
				}
				return true;
			});
			
			// Calculate set bonuses for all active sets
			for (Entry<EquipmentSet, SetState> entry : sets.entrySet()) {
				Multimap<Attribute, AttributeModifier> setAttribs = entry.getKey().getSetBonuses(entity, entry.getValue().items);
				this.attributes.putAll(setAttribs);
				entry.getValue().attributes.clear();
				entry.getValue().attributes.putAll(setAttribs);
			}
		}
	}
	
	protected static final boolean ScanEntityEquipment(LivingEntity entity, BiFunction<IInventorySlotKey<? extends LivingEntity>, ItemStack, Boolean> action) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			EquipmentSlotKey key = new EquipmentSlotKey(slot);
			if (!action.apply(key, entity.getItemBySlot(slot))) {
				return false;
			}
		}
		
		if (NostrumMagica.instance.curios.isEnabled() && entity instanceof Player) {
			CurioInventoryWrapper curios = NostrumMagica.instance.curios.getCurios((Player) entity);
			if (curios != null) {
				for (CurioSlotReference slot : curios.getKeySet()) {
					ItemStack stack = slot.getHeldStack(entity);
					if (!action.apply(slot, stack)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}

	/**
	 * State tracking for players and their equipment
	 */
	protected final Map<LivingEntity, EquipmentState> lastEquipState = new HashMap<>();
	
	public ItemSetListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	protected EquipmentState getLastTickState(LivingEntity entity) {
		return lastEquipState.computeIfAbsent(entity, e -> new EquipmentState());
	}
	
	protected boolean entityChangedEquipment(LivingEntity entity) {
		EquipmentState lastTickState = getLastTickState(entity);
		
		if (!ScanEntityEquipment(entity, (slot, stack) -> ItemStack.matches(lastTickState.equipment.getOrDefault(slot, ItemStack.EMPTY), stack))) {
			return true;
		}
		
		return false;
	}
	
	protected void onEntityEquipChange(LivingEntity entity) {
		EquipmentState state = getLastTickState(entity);
		// Remove old
		entity.getAttributes().removeAttributeModifiers(state.attributes);
		
		// Calculate new
		state.setFrom(entity);
		
		// Apply new
		entity.getAttributes().addTransientAttributeModifiers(state.attributes);
	}
	
	protected void onEntitySetTick(LivingEntity entity, EquipmentSet set, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setPieces) {
		set.setTick(entity, setPieces);
	}
	
	protected boolean canHaveSets(LivingEntity entity) {
		// For now, just restrict to players to avoid scanning/saving other entities
		return entity != null && entity instanceof Player;
	}
	
	protected void updateEntity(LivingEntity entity) {
		
		if (!canHaveSets(entity)) {
			return;
		}

		if (!entity.isAlive()) {
			lastEquipState.remove(entity);
			return;
		}

		// Check and change attributes
		if (entityChangedEquipment(entity)) {
			onEntityEquipChange(entity);
		}

		EquipmentState state = getLastTickState(entity);
		for (Entry<EquipmentSet, SetState> entry : state.sets.entrySet()) {
			this.onEntitySetTick(entity, entry.getKey(), entry.getValue().items);
		}
	}
	
	// Updates all entities' current set bonuses (or lack there-of) from sets
	@SubscribeEvent
	public void ServerWorldTick(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (ServerLevel world : ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getAllLevels()) {
				world.getEntities().get(EntityTypeTest.forClass(LivingEntity.class), (ent) -> {
					if (ent instanceof LivingEntity) {
						LivingEntity living = (LivingEntity) ent;
						updateEntity(living);
		
						if (living.isFallFlying() && living.isShiftKeyDown() && living instanceof ServerPlayer) {
							((ServerPlayer) living).stopFallFlying();
						}
					}
				});
			}
		}
	}
	
	@SubscribeEvent
	public void ClientWorldTick(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END
				&& !NostrumMagica.instance.proxy.hasIntegratedServer()
				&& NostrumMagica.instance.proxy.getPlayer() != null
				&& NostrumMagica.instance.proxy.getPlayer().level != null) {
			((ClientLevel) NostrumMagica.instance.proxy.getPlayer().level).entitiesForRendering().forEach((ent) -> {
				if (ent instanceof LivingEntity) {
					LivingEntity living = (LivingEntity) ent;
					updateEntity(living);
	
					if (living.isFallFlying() && living.isShiftKeyDown() && living instanceof ServerPlayer) {
						((ServerPlayer) living).stopFallFlying();
					}
				}
			});
		}
	}
	
	public Collection<EquipmentSet> getCurrentSets(LivingEntity entity) {
		EquipmentState state = getLastTickState(entity);
		return state.sets.keySet();
	}
	
	public int getActiveSetCount(LivingEntity entity, EquipmentSet set) {
		EquipmentState state = getLastTickState(entity);
		if (state.sets.containsKey(set)) {
			return state.sets.get(set).items.size();
		}
		return 0;
	}
	
	public Multimap<Attribute, AttributeModifier> getActiveSetBonus(LivingEntity entity, EquipmentSet set) {
		EquipmentState state = getLastTickState(entity);
		if (state.sets.containsKey(set)) {
			return state.sets.get(set).attributes;
		}
		return null;
	}
	
}
