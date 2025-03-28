package com.smanzana.nostrummagica.proxy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.IPortalBlock;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ClientEffectVfxRenderMessage;
import com.smanzana.nostrummagica.network.message.ClientShapeVfxRenderMessage;
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
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.stat.PlayerStats;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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
    	sendPlayerStatSync(player);
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
	
	public void openLoreLink(String tag) {
		; // Nothing on server side
	}

	public void sendSpellDebug(PlayerEntity player, ITextComponent comp) {
		NetworkHandler.sendTo(new SpellDebugMessage(comp), (ServerPlayerEntity) player);
	}
	
	public String getTranslation(String key) {
		return key; // This is the server, silly!
	}
	
	public void setObeliskIndex(BlockPos obeliskPos, int index) {
		; // server does nothing
	}
	
	public void removeObeliskIndex(BlockPos obeliskPos, int index) {
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
	public void spawnSpellShapeVfx(World world, SpellShape shape, SpellShapeProperties properties,
			LivingEntity caster, Vector3d casterPos,
			LivingEntity target, Vector3d targetPos,
			SpellCharacteristics characteristics) {
		if (world == null) {
			if (caster == null)
				world = target.world; // If you NPE here you suck. Supply a world!
			else
				world = caster.world;
		}
		
		final double MAX_RANGE = 50.0;
		
		ClientShapeVfxRenderMessage message = new ClientShapeVfxRenderMessage(
				caster, casterPos,
				target, targetPos,
				shape, properties, characteristics);
		if (target != null) {
			NetworkHandler.sendToAllTracking(message, target);
		} else {
			NetworkHandler.sendToAllAround(message, new TargetPoint(targetPos.x, targetPos.y, targetPos.z, MAX_RANGE, world.getDimensionKey()));
		}
	}
	
	public void spawnSpellEffectVfx(World world, SpellEffectPart effect,
			LivingEntity caster, Vector3d casterPos,
			LivingEntity target, Vector3d targetPos) {
		if (world == null) {
			if (caster == null)
				world = target.world; // If you NPE here you suck. Supply a world!
			else
				world = caster.world;
		}
		
		final double MAX_RANGE = 50.0;
		
		ClientEffectVfxRenderMessage message = new ClientEffectVfxRenderMessage(
				caster, casterPos,
				target, targetPos,
				effect);
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
			ItemStack center, List<ItemStack> extras, List<ItemStack> reagents, ItemStack output) {
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
					pos, element, reagents, center, extras, output
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
	
	public boolean attemptBlockTeleport(Entity entity, BlockPos portalPos) {
		final World world = entity.getEntityWorld();
		BlockState worldBlock = world.getBlockState(portalPos);
		if (!(worldBlock.getBlock() instanceof IPortalBlock)) {
			NostrumMagica.logger.warn("Entity requested teleport from non-portal block: " + entity + " at " + portalPos);
		} else {
			IPortalBlock block = (IPortalBlock) worldBlock.getBlock();
			block.attemptTeleport(world, portalPos, worldBlock, entity);
		}
		
		return true;
	}

	public boolean hasIntegratedServer() {
		return false;
	}
	
	public boolean attemptPlayerInteract(PlayerEntity player, World world, BlockPos pos, Hand hand, BlockRayTraceResult hit) {
		ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
		return serverPlayer.interactionManager.func_219441_a(serverPlayer, world, ItemStack.EMPTY, hand, hit)
				!= ActionResultType.PASS;
	}
}
