package com.smanzana.nostrummagica.proxy;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ClientEffectRenderMessage;
import com.smanzana.nostrummagica.network.message.MagicEffectUpdate;
import com.smanzana.nostrummagica.network.message.ManaArmorSyncMessage;
import com.smanzana.nostrummagica.network.message.ManaMessage;
import com.smanzana.nostrummagica.network.message.PlayerStatSyncMessage;
import com.smanzana.nostrummagica.network.message.SpawnNostrumRitualEffectMessage;
import com.smanzana.nostrummagica.network.message.SpawnPredefinedEffectMessage;
import com.smanzana.nostrummagica.network.message.SpellCraftingCapabilitySyncMessage;
import com.smanzana.nostrummagica.network.message.SpellDebugMessage;
import com.smanzana.nostrummagica.network.message.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.stat.PlayerStats;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;

public class CommonProxy {
	
	public CommonProxy() {
		
	}
	
    public void syncPlayer(ServerPlayerEntity player) {
    	INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
    	attr.refresh(player);
    	NetworkHandler.sendTo(
    			new StatSyncMessage(NostrumMagica.getMagicWrapper(player)),
    			player);
    	NetworkHandler.sendTo(
    			new SpellRequestReplyMessage(NostrumMagica.instance.getSpellRegistry().getAllSpells(), true),
    			player);
    	NetworkHandler.sendTo(
    			new ManaArmorSyncMessage(player, NostrumMagica.getManaArmor(player)),
    			player);
    	NetworkHandler.sendTo(
    			new SpellCraftingCapabilitySyncMessage(player, NostrumMagica.getSpellCrafting(player)),
    			player);
    }
    
    public void updateEntityEffect(ServerPlayerEntity player, LivingEntity entity, SpecialEffect effectType, EffectData data) {
    	NetworkHandler.sendTo(
    			new MagicEffectUpdate(entity, effectType, data),
    			player);
    }

	public PlayerEntity getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public void receiveStatOverrides(INostrumMagic override) {
		return; // Server side doesn't do anything
	}
	
	public void applyOverride() {
		; // do nothing
	}

	public boolean isServer() {
		return true;
	}
	
	public void openBook(PlayerEntity player, GuiBook book, Object userdata) {
		; // Server does nothing
	}
	
	public void openContainer(PlayerEntity player, IPackedContainerProvider provider) {
		if (!player.world.isRemote() && player instanceof ServerPlayerEntity) {
			NetworkHooks.openGui((ServerPlayerEntity) player, provider, provider.getData());
		}
	}
	
	public void openSpellScreen(Spell spell) {
		; // Nothing on server side
	}
	
	public void openMirrorScreen() {
		; // Nothing on server side
	}
	
	public void openObeliskScreen(World world, BlockPos pos) {
		; // Nothing on server side
	}
	
	public void openTomeWorkshopScreen() {
		; // Nothing on server side
	}

	public void sendSpellDebug(PlayerEntity player, ITextComponent comp) {
		NetworkHandler.sendTo(new SpellDebugMessage(comp), (ServerPlayerEntity) player);
	}
	
	public String getTranslation(String key) {
		return key; // This is the server, silly!
	}
	
	public void requestObeliskTransportation(BlockPos origin, BlockPos target) {
		; // server does nothing
	}
	
	public void setObeliskIndex(BlockPos obeliskPos, int index) {
		; // server does nothing
	}
	
	public void requestStats(LivingEntity entity) {
		;
	}
	
	/**
	 * Spawns an client-rendered effect.
	 * @param world Only needed if caster and target are null
	 * @param comp
	 * @param caster
	 * @param casterPos
	 * @param target
	 * @param targetPos
	 * @param flavor Optional component used to flavor the effect.
	 * @param negative whether the effect should be considered 'negative'/harmful
	 * @param param optional extra float param for display
	 */
	public void spawnEffect(World world, SpellComponentWrapper comp,
			LivingEntity caster, Vector3d casterPos,
			LivingEntity target, Vector3d targetPos,
			SpellComponentWrapper flavor, boolean isNegative, float compParam) {
		if (world == null) {
			if (caster == null)
				world = target.world; // If you NPE here you suck. Supply a world!
			else
				world = caster.world;
		}
		
		final double MAX_RANGE = 50.0;
		
		ClientEffectRenderMessage message = new ClientEffectRenderMessage(
				caster, casterPos,
				target, targetPos,
				comp, flavor,
				isNegative, compParam);
		if (target != null) {
			NetworkHandler.sendToAllTracking(message, target);
		} else {
			NetworkHandler.sendToAllAround(message, new TargetPoint(targetPos.x, targetPos.y, targetPos.z, MAX_RANGE, world.getDimensionKey()));
		}
	}
	
	public void sendMana(PlayerEntity player) {
		INostrumMagic stats = NostrumMagica.getMagicWrapper(player);
		final int mana;
		if (stats == null) {
			mana = 0;
		} else {
			mana = stats.getMana();
		}
		
		NetworkHandler.sendToAllTracking(new ManaMessage(player, mana), player);
	}
	
	public void sendPlayerStatSync(PlayerEntity player) {
		PlayerStats stats = NostrumMagica.instance.getPlayerStats().get(player);
		NetworkHandler.sendTo(new PlayerStatSyncMessage(player.getUniqueID(), stats), (ServerPlayerEntity) player);
	}
	
	public void sendManaArmorCapability(PlayerEntity player) {
		IManaArmor stats = NostrumMagica.getManaArmor(player);
		NetworkHandler.sendToAllTracking(new ManaArmorSyncMessage(player, stats), player);
	}
	
	public void sendSpellCraftingCapability(PlayerEntity player) {
		ISpellCrafting stats = NostrumMagica.getSpellCrafting(player);
		NetworkHandler.sendToAllTracking(new SpellCraftingCapabilitySyncMessage(player, stats), player);
	}
	
	public void receiveManaArmorOverride(@Nonnull Entity ent, IManaArmor override) {
		; // Nothing to do on server
	}

	public void receiveSpellCraftingOverride(Entity ent, ISpellCrafting stats) {
		; // Nothing to do on server
	}
	
	public void playRitualEffect(World world, BlockPos pos, EMagicElement element,
			ItemStack center, @Nullable NonNullList<ItemStack> extras, ReagentType[] types, ItemStack output) {
		Set<PlayerEntity> players = new HashSet<>();
		final double MAX_RANGE_SQR = 2500.0;
		if (pos != null) {
			for (PlayerEntity player : ((ServerWorld) world).getPlayers()) {
				if (player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= MAX_RANGE_SQR)
					players.add(player);
			}
		}
		
		if (!players.isEmpty()) {
			SpawnNostrumRitualEffectMessage message = new SpawnNostrumRitualEffectMessage(
					//int dimension, BlockPos pos, ReagentType[] reagents, ItemStack center, @Nullable NonNullList<ItemStack> extras, ItemStack output
					world.getDimensionKey(),
					pos, element, types, center, extras, output
					);
			for (PlayerEntity player : players) {
				NetworkHandler.sendTo(message, (ServerPlayerEntity) player);
			}
		}
	}
	
	public void playPredefinedEffect(PredefinedEffect type, int duration, World world, Vector3d position) {
		playPredefinedEffect(new SpawnPredefinedEffectMessage(type, duration, world.getDimensionKey(), position), world, position);
	}
	
	public void playPredefinedEffect(PredefinedEffect type, int duration, World world, Entity entity) {
		playPredefinedEffect(new SpawnPredefinedEffectMessage(type, duration, world.getDimensionKey(), entity.getEntityId()), world, entity.getPositionVec());
	}
	
	private void playPredefinedEffect(SpawnPredefinedEffectMessage message, World world, Vector3d center) {
		final double MAX_RANGE = 50.0;
		NetworkHandler.sendToAllAround(message, new TargetPoint(center.x, center.y, center.z, MAX_RANGE, world.getDimensionKey()));
	}
}
