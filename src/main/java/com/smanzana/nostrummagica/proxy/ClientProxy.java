package com.smanzana.nostrummagica.proxy;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.ObeliskScreen;
import com.smanzana.nostrummagica.client.gui.ScrollScreen;
import com.smanzana.nostrummagica.client.gui.TomeWorkshopScreen;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.client.gui.mirror.MirrorGui;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.integration.jei.NostrumMagicaJEIPlugin;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ObeliskRemoveMessage;
import com.smanzana.nostrummagica.network.message.ObeliskSelectMessage;
import com.smanzana.nostrummagica.network.message.StatRequestMessage;
import com.smanzana.nostrummagica.network.message.WorldPortalTeleportRequestMessage;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class ClientProxy extends CommonProxy {
	
	public ClientProxy() {
		super();
		
		MinecraftForge.EVENT_BUS.register(this); // For client join welcome message
	}
	
	protected final ClientPlayerListener getPlayerListener() {
		return (ClientPlayerListener) NostrumMagica.playerListener;
	}
	
	@Override
	public void syncPlayer(ServerPlayer player) {
		if (player.level.isClientSide)
			return;
		
		super.syncPlayer(player);
	}
	
	@Override
	public Player getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	private INostrumMagic overrides = null;
	@Override
	public void receiveStatOverrides(INostrumMagic override) {
		// If we can look up stats, apply them.
		// Otherwise, stash them for loading when we apply attributes
		final Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		INostrumMagic existing = NostrumMagica.getMagicWrapper(player);
		if (existing != null && player.isAlive()) {
			// apply them
			existing.copy(override);
			
			// If we're on a screen that cares, refresh it
			if (mc.screen instanceof MirrorGui) {
				//((MirrorGui) mc.currentScreen).refresh();
			}
			
			if (ModList.get().isLoaded("jei")) {
				NostrumMagicaJEIPlugin.RefreshTransmuteRecipes(player);
			}
		} else {
			// Stash them
			overrides = override;
		}
	}
	
	@Override
	public void applyOverride() {
		if (overrides == null)
			return;
		
		final Minecraft mc = Minecraft.getInstance();
		INostrumMagic existing = NostrumMagica.getMagicWrapper(mc.player);
		
		if (existing == null)
			return; // Mana got here before we attached
		
		existing.copy(overrides);
		
		overrides = null;
		
		if (ModList.get().isLoaded("jei")) {
			NostrumMagicaJEIPlugin.RefreshTransmuteRecipes(mc.player);
		}
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public void openBook(Player player, GuiBook book, Object userdata) {
		Minecraft.getInstance().setScreen(book.getScreen(userdata));
	}
	
	@Override
	public void openContainer(Player player, IPackedContainerProvider provider) {
		if (!player.level.isClientSide) {
			super.openContainer(player, provider);
		}
		; // On client, do nothing
	}
	
	@Override
	public void openSpellScreen(Spell spell) {
		Minecraft.getInstance().setScreen(new ScrollScreen(spell));
	}
	
	@Override
	public void openMirrorScreen() {
		final Player player = getPlayer();
		if (player.level.isClientSide()) {
			Minecraft.getInstance().setScreen((Screen) new MirrorGui(player));
		}
	}
	
	@Override
	public void openObeliskScreen(Level world, BlockPos pos) {
		if (world.isClientSide()) {
			ObeliskTileEntity te = (ObeliskTileEntity) world.getBlockEntity(pos);
			Minecraft.getInstance().setScreen(new ObeliskScreen(te));
		}
	}
	
	@Override
	public void openTomeWorkshopScreen() {
		final Player player = getPlayer();
		if (player.level.isClientSide()) {
			Minecraft.getInstance().setScreen(new TomeWorkshopScreen(player));
		}
	}
	
	public void openLoreLink(String tag) {
		final Minecraft mc = Minecraft.getInstance();
		final Player player = mc.player;
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			player.sendMessage(new TextComponent("Could not find magic wrapper for player"), Util.NIL_UUID);
		} else {
			mc.setScreen(new InfoScreen(attr, tag));
		}
	}
	
	@Override
	public void sendSpellDebug(Player player, Component comp) {
		if (!player.level.isClientSide) {
			super.sendSpellDebug(player, comp);
		}
		;
	}
	
	@Override
	public String getTranslation(String key) {
		return I18n.get(key, new Object[0]).trim();
	}
	
	@Override
	public void setObeliskIndex(BlockPos obeliskPos, int index) {
		// Send a request to the server
		NetworkHandler.sendToServer(
				new ObeliskSelectMessage(obeliskPos, index)
				);
	}
	
	@Override
	public void removeObeliskIndex(BlockPos obeliskPos, int index) {
		// Send a request to the server
		NetworkHandler.sendToServer(new ObeliskRemoveMessage(obeliskPos, index));
	}
	
	@Override
	public void requestStats(LivingEntity entity) {
		NetworkHandler.sendToServer(
				new StatRequestMessage()
				);
	}
	
	@Override
	public void spawnSpellShapeVfx(Level world, SpellShape shape, SpellShapeProperties properties,
			LivingEntity caster, Vec3 casterPos,
			LivingEntity target, Vec3 targetPos,
			SpellCharacteristics characteristics) {
		if (world == null && target != null) {
			world = target.level;
		}
		
		if (world != null) {
			if (!world.isClientSide) {
				super.spawnSpellShapeVfx(world, shape, properties, caster, casterPos, target, targetPos, characteristics);
				return;
			}
		}
		if (targetPos == null)
//			if (target != null)
//				targetPos = target.getPositionVec();
//			else
				targetPos = new Vec3(0, 0, 0);
		
		getPlayerListener().getEffectRenderer().spawnEffect(shape, caster, casterPos, target, targetPos, properties, characteristics);
	}
	
	@Override
	public void spawnSpellEffectVfx(Level world, SpellEffectPart effect,
			LivingEntity caster, Vec3 casterPos,
			LivingEntity target, Vec3 targetPos) {
		if (world == null && target != null) {
			world = target.level;
		}
		
		if (world != null) {
			if (!world.isClientSide) {
				super.spawnSpellEffectVfx(world, effect, caster, casterPos, target, targetPos);
				return;
			}
		}
		if (targetPos == null)
//			if (target != null)
//				targetPos = target.getPositionVec();
//			else
				targetPos = new Vec3(0, 0, 0);
		
		getPlayerListener().getEffectRenderer().spawnEffect(effect, caster, casterPos, target, targetPos);
	}
	
	@Override
	public void updateEntityEffect(ServerPlayer player, LivingEntity entity, SpecialEffect effectType, EffectData data) {
		return;
	}
	
	private static boolean shownText = false;
	@SubscribeEvent
	public void onClientConnect(EntityJoinWorldEvent event) {
		if (ClientProxy.shownText == false && ModConfig.config.displayLoginText()
				&& event.getEntity() == Minecraft.getInstance().player) {
			final Minecraft mc = Minecraft.getInstance();
			final String translated = I18n.get(getPlayerListener().getBindingInfo().saveString());
			mc.player.sendMessage(
					new TranslatableComponent("info.nostrumwelcome.text", new Object[]{
							translated
					}), Util.NIL_UUID);
			ClientProxy.shownText = true;
		}
		
		if (event.getWorld() != null && event.getWorld().isClientSide() && event.getEntity() instanceof Player) {
			NostrumMagica.Proxy.requestStats((Player) event.getEntity());
		}
	}
	
	@Override
	public void sendMana(Player player) {
		if (player.level.isClientSide) {
			return;
		}
		
		super.sendMana(player);
	}
	
	@Override
	public void sendPlayerStatSync(Player player) {
		if (player.level.isClientSide()) {
			return;
		}
		
		super.sendPlayerStatSync(player);
	}
	
	@Override
	public void sendManaArmorCapability(Player player) {
		if (player.level.isClientSide) {
			return;
		}
		
		super.sendManaArmorCapability(player);
	}
	
	@Override
	public void sendSpellCraftingCapability(Player player) {
		if (player.level.isClientSide()) {
			return;
		}
		
		super.sendSpellCraftingCapability(player);
	}
	
	@Override
	public void receiveManaArmorOverride(@Nonnull Entity ent, IManaArmor override) {
		@Nullable IManaArmor existing = NostrumMagica.getManaArmor(ent);
		if (existing != null) {
			existing.copy(override);
		}
	}
	
	@Override
	public void receiveSpellCraftingOverride(Entity ent, ISpellCrafting override) {
		@Nullable ISpellCrafting existing = NostrumMagica.getSpellCrafting(ent);
		if (existing != null) {
			existing.copy(override);
		}
	}
	
	@Override
	public void playRitualEffect(Level world, BlockPos pos, EMagicElement element,
			ItemStack center, @Nullable List<ItemStack> extras, List<ItemStack> reagents, ItemStack output) {
		if (world.isClientSide) {
			return;
		}
		
		super.playRitualEffect(world, pos, element, center, extras, reagents, output);
	}
	
	@Override
	public boolean attemptBlockTeleport(Entity entity, BlockPos pos) {
		// Check if this is a logical server op, since integrated still will call this version
		if (!entity.getCommandSenderWorld().isClientSide()) {
			return super.attemptBlockTeleport(entity, pos);
		}
		
		// Ask server to do the teleport for us
		if (entity == this.getPlayer()) {
			NetworkHandler.sendToServer(new WorldPortalTeleportRequestMessage(pos));
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean hasIntegratedServer() {
		return Minecraft.getInstance().isLocalServer();
	}
	
	@Override
	public boolean attemptPlayerInteract(Player player, Level world, BlockPos pos, InteractionHand hand, BlockHitResult hit) {
		if (!player.level.isClientSide()) {
			return super.attemptPlayerInteract(player, world, pos, hand, hit);
		}
		
		final Minecraft mc = Minecraft.getInstance();
		return mc.gameMode.useItemOn((LocalPlayer) player, (ClientLevel) world, hand, hit)
				!= InteractionResult.PASS;
	}

	@Override
	public void castScroll(InteractionHand hand, ItemStack itemStackIn, RegisteredSpell spell) {
		((ClientPlayerListener) NostrumMagica.playerListener).startScrollCast(hand, itemStackIn, spell);
	}
}
