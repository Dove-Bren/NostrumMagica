package com.smanzana.nostrummagica.proxy;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.client.effects.ClientEffect;
import com.smanzana.nostrummagica.client.effects.ClientEffectBeam;
import com.smanzana.nostrummagica.client.effects.ClientEffectEchoed;
import com.smanzana.nostrummagica.client.effects.ClientEffectFormBasic;
import com.smanzana.nostrummagica.client.effects.ClientEffectFormFlat;
import com.smanzana.nostrummagica.client.effects.ClientEffectIcon;
import com.smanzana.nostrummagica.client.effects.ClientEffectMajorSphere;
import com.smanzana.nostrummagica.client.effects.ClientEffectMirrored;
import com.smanzana.nostrummagica.client.effects.ClientEffectRenderer;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierColor;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierFollow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierGrow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierMove;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierRotate;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierShrink;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierTranslate;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.ObeliskScreen;
import com.smanzana.nostrummagica.client.gui.ScrollScreen;
import com.smanzana.nostrummagica.client.gui.TomeWorkshopScreen;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.client.gui.mirror.MirrorGui;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.client.render.OutlineRenderer;
import com.smanzana.nostrummagica.client.render.SelectionRenderer;
import com.smanzana.nostrummagica.client.render.SpellShapeRenderer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.integration.jei.NostrumMagicaJEIPlugin;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.BladeCastMessage;
import com.smanzana.nostrummagica.network.message.ClientCastMessage;
import com.smanzana.nostrummagica.network.message.ObeliskRemoveMessage;
import com.smanzana.nostrummagica.network.message.ObeliskSelectMessage;
import com.smanzana.nostrummagica.network.message.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.network.message.StatRequestMessage;
import com.smanzana.nostrummagica.network.message.WorldPortalTeleportRequestMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public class ClientProxy extends CommonProxy {
	
	private KeyMapping bindingCast1;
	private KeyMapping bindingCast2;
	private KeyMapping bindingCast3;
	private KeyMapping bindingCast4;
	private KeyMapping bindingCast5;
	private KeyMapping bindingScroll;
	private KeyMapping bindingInfo;
	private KeyMapping bindingBladeCast;
	private KeyMapping bindingHUD;
	private KeyMapping bindingShapeHelp;
	private OverlayRenderer overlayRenderer;
	private ClientEffectRenderer effectRenderer;
	private OutlineRenderer outlineRenderer;
	private SpellShapeRenderer spellshapeRenderer;
	private SelectionRenderer selectionRenderer;
	
	public ClientProxy() {
		super();
		
		this.overlayRenderer = new OverlayRenderer();
		this.effectRenderer = ClientEffectRenderer.instance();
		this.outlineRenderer = new OutlineRenderer();
		this.spellshapeRenderer = new SpellShapeRenderer(this.outlineRenderer);
		this.selectionRenderer = new SelectionRenderer();
		
		MinecraftForge.EVENT_BUS.register(this); // For client join welcome message
	}
	
	public void initKeybinds() {
		bindingCast1 = new KeyMapping("key.cast1.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast1);
		bindingCast2 = new KeyMapping("key.cast2.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast2);
		bindingCast3 = new KeyMapping("key.cast3.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast3);
		bindingCast4 = new KeyMapping("key.cast4.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast4);
		bindingCast5 = new KeyMapping("key.cast5.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast5);
		bindingScroll = new KeyMapping("key.spellscroll.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		bindingInfo = new KeyMapping("key.infoscreen.desc", GLFW.GLFW_KEY_HOME, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingInfo);
		bindingBladeCast = new KeyMapping("key.bladecast.desc", GLFW.GLFW_KEY_R, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingBladeCast);
		bindingHUD = new KeyMapping("key.hud.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingHUD);
		bindingShapeHelp = new KeyMapping("key.shapehelp.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingShapeHelp);
	}
	
	public KeyMapping getBindingCast1() {
		return bindingCast1;
	}

	public KeyMapping getBindingCast2() {
		return bindingCast2;
	}

	public KeyMapping getBindingCast3() {
		return bindingCast3;
	}

	public KeyMapping getBindingCast4() {
		return bindingCast4;
	}

	public KeyMapping getBindingCast5() {
		return bindingCast5;
	}
	
	public KeyMapping getHUDKey() {
		return bindingHUD;
	}

	@SubscribeEvent
	public void onMouse(MouseScrollEvent event) {
		int wheel = event.getScrollDelta() < 0 ? -1 : event.getScrollDelta() > 0 ? 1 : 0;
		if (wheel != 0) {
			final Minecraft mc = Minecraft.getInstance();
			if (!NostrumMagica.getMagicWrapper(mc.player)
					.isUnlocked())
				return;
			ItemStack tome = NostrumMagica.getCurrentTome(mc.player);
			if (!tome.isEmpty()) {
				if (bindingScroll.isDown()) {
					wheel = (wheel > 0 ? -1 : 1);
					int index = SpellTome.incrementPageIndex(tome, wheel);
					if (index != -1) {
						NetworkHandler.getSyncChannel()
							.sendToServer(new SpellTomeIncrementMessage(index));
					}
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		if (bindingCast1.consumeClick()) {
			doCast(0);
		} else if (bindingCast2.consumeClick()) {
			doCast(1);
		} else if (bindingCast3.consumeClick()) {
			doCast(2);
		} else if (bindingCast4.consumeClick()) {
			doCast(3);
		} else if (bindingCast5.consumeClick()) {
			doCast(4);
		} else if (bindingInfo.consumeClick()) {
			Player player = mc.player;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			Minecraft.getInstance().setScreen(new InfoScreen(attr, (String) null));
//			player.openGui(NostrumMagica.instance,
//					NostrumGui.infoscreenID, player.world, 0, 0, 0);
		} else if (mc.options.keyJump.consumeClick()) {
			Player player = mc.player;
			if (player.isPassenger() && player.getVehicle() instanceof TameRedDragonEntity) {
				((DragonEntity) player.getVehicle()).dragonJump();
			} else if (player.isPassenger() && player.getVehicle() instanceof ArcaneWolfEntity) {
				((ArcaneWolfEntity) player.getVehicle()).wolfJump();
			}
		} else if (bindingBladeCast.consumeClick()) {
			Player player = mc.player;
			if (player.getAttackStrengthScale(0.5F) > .95) {
				player.resetAttackStrengthTicker();
				//player.swingArm(Hand.MAIN_HAND);
				doBladeCast();
			}
			
		} else if (bindingHUD.consumeClick()) {
			this.overlayRenderer.toggleHUD();
		} else if (bindingShapeHelp.consumeClick()) {
			this.spellshapeRenderer.toggle();
		}
	}
	
	private void doBladeCast() {
		NetworkHandler.sendToServer(new BladeCastMessage());
	}
	
	private void doCast(int castSlot) {
		final Player player = getPlayer();
		Spell[] spells = NostrumMagica.getCurrentSpellLoadout(player);
		if (castSlot < 0 || spells == null || spells.length == 0 || spells.length <= castSlot) {
			return;
		}
		
		final Spell spell = spells[castSlot];
		if (spell == null) {
			// No spell in slot
			return;
		}
		
		// Find the tome this was cast from, if any
		ItemStack tome = NostrumMagica.getCurrentTome(player); 
		if (!tome.isEmpty()) {
			if (SpellCasting.CheckToolCast(spell, player, tome).succeeded) {
				NetworkHandler.sendToServer(
		    			new ClientCastMessage(spell, false, SpellTome.getTomeID(tome)));
				NostrumMagica.playerListener.overrideLastSpell(player, spell);
			} else {
				for (int i = 0; i < 15; i++) {
					double offsetx = Math.cos(i * (2 * Math.PI / 15)) * 1.0;
					double offsetz = Math.sin(i * (2 * Math.PI / 15)) * 1.0;
					player.level
						.addParticle(ParticleTypes.LARGE_SMOKE,
								player.getX() + offsetx, player.getY(), player.getZ() + offsetz,
								0, -.5, 0);
					
				}
				
				NostrumMagicaSounds.CAST_FAIL.play(player);
				doManaWiggle(2);
			}
		}
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
	
	private static final ClientEffect doCorruptEffect(LivingEntity source,
			Vec3 sourcePos,
			LivingEntity target,
			Vec3 targetPos,
			SpellEffectPart part) {
		ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
				new ClientEffectFormFlat(ClientEffectIcon.ARROWD, 0, 0, 0),
				3L * 500L, 6);
		
		effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
		
		if (target != null) {
			effect.modify(new ClientEffectModifierFollow(target));
		}
		
		effect
		.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
		.modify(new ClientEffectModifierTranslate(0, 1.5f, -1.5f))
		.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, -2, 0), .3f, 1f))
		.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
		.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
		;
		return effect;
	}
	
	public void initDefaultEffects() {
		ClientEffectRenderer renderer = this.effectRenderer;
		
		renderer.registerEffect(NostrumSpellShapes.Burst,
				(source, sourcePos, target, targetPos, properties, characteristics) -> {
					// TODO get the shape params in here to modify scale
					// TODO get whether it's a good thing or not
					ClientEffect effect = new ClientEffectMajorSphere(target == null ? targetPos : new Vec3(0, 0, 0),
							NostrumSpellShapes.Burst.getRadius(properties) + .5f,
							characteristics.isHarmful(),
							1000L);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect.modify(new ClientEffectModifierColor(characteristics.getElement().getColor(), characteristics.getElement().getColor()));
					
					// negative will blow up and then shrink down in a cool way
					// positive will rise up and then fade out
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .4f, 0f));
					
					if (characteristics.isHarmful()) {
						effect
						.modify(new ClientEffectModifierGrow(.75f, .2f, 1f, .5f, .2f))
						.modify(new ClientEffectModifierShrink(1, 1, 0f, .2f, .6f))
						;
					} else {
						effect
						.modify(new ClientEffectModifierGrow(.5f, .2f, 1f, .5f, .4f))
						.modify(new ClientEffectModifierShrink(1, 1, 1f, 0f, .8f))
						;
					}
					
					return effect;
				});
		renderer.registerEffect(NostrumSpellShapes.Ring,
				(source, sourcePos, targetIn, targetPosIn, properties, characteristics) -> {
					final float radius = NostrumSpellShapes.Ring.getOuterRadius(properties);
					
					ClientEffect effect = new ClientEffectMirrored(targetPosIn == null ? targetIn.position() : targetPosIn,
							new ClientEffectFormFlat(ClientEffectIcon.TING1, 0, 0, 0),
							1L * 500L, 6);
					
					effect.modify(new ClientEffectModifierColor(characteristics.getElement().getColor(), characteristics.getElement().getColor()));
					
					effect
					.modify(new ClientEffectModifierRotate(0, -.25f, 0))
					.modify(new ClientEffectModifierTranslate(0, 1f, radius))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});
		
		// triggers (that have them)
		renderer.registerEffect(NostrumSpellShapes.Beam,
				(source, sourcePos, target, targetPos, properties, characteristics) -> {
					ClientEffect effect = new ClientEffectBeam(sourcePos == null ? source.position() : sourcePos,
							targetPos == null ? target.position() : targetPos,
							500L);
					
					//if (target != null)
					//	effect.modify(new ClientEffectModifierFollow(target));
					
					effect.modify(new ClientEffectModifierColor(characteristics.getElement().getColor(), characteristics.getElement().getColor()));
					
					effect
					.modify(new ClientEffectModifierGrow(1f, .2f, 1f, 1f, .4f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .2f, .6f))
					;
					return effect;
				});
		
//		renderer.registerEffect(new SpellComponentWrapper(SelfTrigger.instance()),
//				(source, sourcePos, target, targetPos, flavor) -> {
//					ClientEffect effect = new ClientEffect(source == null ? sourcePos : source.getPositionVec(),
//							new ClientEffectFormBasic(ClientEffectIcon.TING2, 0, 0, 0),
//							10);
//					
//					if (target != null)
//						effect.modify(new ClientEffectModifierFollow(target));
//					
//					if (flavor != null && flavor.isElement()) {
//						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
//					}
//					
//					effect
//					.modify(new ClientEffectModifierGrow(1f, .2f, 1f, 1f, .4f))
//					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .2f, .6f))
//					;
//					return effect;
//				});
		// Can't think of a cool one for self. Oh well
		
//		renderer.registerEffect(new SpellComponentWrapper(OtherTrigger.instance()),
//				(source, sourcePos, target, targetPos, properties, characteristics) -> {
//					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
//							new ClientEffectFormFlat(ClientEffectIcon.TING3, 0, 0, 0),
//							500L, 6);
//					
//					if (target != null) {
//						effect.modify(new ClientEffectModifierFollow(target));
//					}
//					
//					if (flavor != null && flavor.isElement()) {
//						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
//					}
//					
//					effect
//					.modify(new ClientEffectModifierTranslate(0, 1, -1))
//					.modify(new ClientEffectModifierRotate(.4f, 0f, 1.2f))
//					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .3f))
//					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .2f, .8f))
//					;
//					return effect;
//				});
		
		renderer.registerEffect(NostrumSpellShapes.OnHealth,
				(source, sourcePos, target, targetPos, properties, characteristics) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING5, 0, 0, 0),
							1000L, 4);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect
					.modify(new ClientEffectModifierColor(0xFFA50500, 0xFFA50500))
					.modify(new ClientEffectModifierTranslate(0, 2, -1))
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(NostrumSpellShapes.OnMana,
				(source, sourcePos, target, targetPos, properties, characteristics) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING5, 0, 0, 0),
							1000L, 4);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect
					.modify(new ClientEffectModifierColor(0xFF0005A5, 0xFF0005A5))
					.modify(new ClientEffectModifierTranslate(0, 2, -1))
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(NostrumSpellShapes.OnFood,
				(source, sourcePos, target, targetPos, properties, characteristics) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING5, 0, 0, 0),
							1000L, 4);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect
					.modify(new ClientEffectModifierColor(0xFFC6CC30, 0xFFC6CC30))
					.modify(new ClientEffectModifierTranslate(0, 2, -1))
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(NostrumSpellShapes.Proximity,
				(source, sourcePos, target, targetPos, properties, characteristics) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							2L * 1000L, 5);
					
					effect.modify(new ClientEffectModifierColor(characteristics.getElement().getColor(), characteristics.getElement().getColor()));
					
					final float range = NostrumSpellShapes.Proximity.getRange(properties);
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, .2f, (.5f * range)))
					.modify(new ClientEffectModifierGrow(.2f, .2f, .4f, .5f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .4f))
					;
					return effect;
				});
		
//		renderer.registerEffect(new SpellComponentWrapper(WallTrigger.instance()),
//				(source, sourcePos, target, targetPos, properties, characteristics) -> {
//					final boolean northsouth = (param >= 1000f);
//					final int radius = (int) param - (northsouth ? 1000 : 0);
//					
//					
//				}
//				);
		
		// Alterations
		
		renderer.registerEffect((EAlteration) null,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect = new ClientEffectMirrored(target == null ? targetPos : new Vec3(0, 0, 0),
							new ClientEffectFormFlat(ClientEffectIcon.TING1, 0, 0, 0),
							500L, 5);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					effect
					.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()))
					.modify(new ClientEffectModifierRotate(0f, .4f, 0f))
					.modify(new ClientEffectModifierTranslate(0, 0, -1))
					.modify(new ClientEffectModifierMove(new Vec3(0, 1.5, 0), new Vec3(0, .5, .7), .5f, 1f))
					.modify(new ClientEffectModifierGrow(.1f, .3f, .2f, .8f, .5f))
					;
					return effect;
				});
		
		renderer.registerEffect(EAlteration.INFLICT,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.ARROWD, 0, 0, 0),
							3L * 500L, 6);
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, 1.5f, -1.5f))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, -2, 0), .3f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(EAlteration.RESIST,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.ARROWU, 0, 0, 0),
							3L * 500L, 6);
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, 0f, -1.5f))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, 1.5, 0), 0f, .7f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(EAlteration.GROWTH,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect = new ClientEffectEchoed(targetPos == null ? target.position() : targetPos, 
							new ClientEffectMirrored(new Vec3(0,0,0),
							new ClientEffectFormFlat(ClientEffectIcon.TING3, 0, 0, 0),
							2L * 1000L, 4), 2L * 1000L, 5, .2f);
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierTranslate(0, 1, 0))
					.modify(new ClientEffectModifierRotate(1f, 2f, 0f))
					.modify(new ClientEffectModifierTranslate(.5f, 0f, -1.2f))
					.modify(new ClientEffectModifierGrow(.2f, .2f, .4f, .6f, .5f))
					;
					return effect;
				});

		renderer.registerEffect(EAlteration.SUPPORT,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect;
					boolean isShield = false;
					if (part.getElement() == EMagicElement.EARTH || part.getElement() == EMagicElement.ICE) {
						// Special ones for shields!
						effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
								new ClientEffectFormBasic(ClientEffectIcon.SHIELD, 0, 0, 0),
								3L * 500L, 5);
						isShield = true;
					} else {
						effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
								new ClientEffectFormFlat(ClientEffectIcon.TING5, 0, 0, 0),
								3L * 500L, 10);
					}
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					if (!isShield)
						effect.modify(new ClientEffectModifierRotate(0f, -.5f, 0f));
					
					effect.modify(new ClientEffectModifierTranslate(0f, 1f, -1.2f));
					
					if (isShield) {
						effect.modify(new ClientEffectModifierRotate(0f, -.5f, 0f))
							.modify(new ClientEffectModifierGrow(1f, .2f, 1f, .8f, .5f));
					} else {
						effect
						.modify(new ClientEffectModifierGrow(.2f, .2f, .4f, .6f, .5f))
						.modify(new ClientEffectModifierShrink(1f, 1f, 0f, 0f, .5f))
					;
					}
					return effect;
				});

		renderer.registerEffect(EAlteration.ENCHANT,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.position() : targetPos).add(0, 1, 0),
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							3L * 500L, 6, new Vector3f(1, 0, 0));
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(1f, 0f, 1f))
					.modify(new ClientEffectModifierTranslate(-.5f, 0f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(EAlteration.CONJURE,
				(source, sourcePos, target, targetPos, part) -> {
					// TODO physical breaks stuff. Lots of particles. Should we return null here?
					
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							1L * 500L, 6);
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
										
					effect
					.modify(new ClientEffectModifierTranslate(0f, 1f, 0f))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(EAlteration.SUMMON,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.position() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING1, 0, 0, 0),
							1L * 500L, 6);
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, -.5f, 0f))
					.modify(new ClientEffectModifierTranslate(0f, 1f, 0f))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vec3(0, 0, 0), new Vec3(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierRotate(1f, 0f, 0f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(EAlteration.RUIN,
				(source, sourcePos, target, targetPos, part) -> {
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.position() : targetPos).add(0, 1, 0),
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							2L * 500L, 6, new Vector3f(.5f, .5f, 0));
					
					effect.modify(new ClientEffectModifierColor(part.getElement().getColor(), part.getElement().getColor()));
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierTranslate(0f, 0f, .5f))
					.modify(new ClientEffectModifierRotate(1f, 0f, 1f))
					.modify(new ClientEffectModifierTranslate(-.5f, 0f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .6f))
					;
					return effect;
				});
		
		renderer.registerEffect(EAlteration.CORRUPT, ClientProxy::doCorruptEffect);
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
		
		this.effectRenderer.spawnEffect(shape, caster, casterPos, target, targetPos, properties, characteristics);
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
		
		this.effectRenderer.spawnEffect(effect, caster, casterPos, target, targetPos);
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
			final String translated = I18n.get(this.bindingInfo.saveString());
			mc.player.sendMessage(
					new TranslatableComponent("info.nostrumwelcome.text", new Object[]{
							translated
					}), Util.NIL_UUID);
			ClientProxy.shownText = true;
		}
		
		if (event.getWorld() != null && event.getWorld().isClientSide() && event.getEntity() instanceof Player) {
			NostrumMagica.instance.proxy.requestStats((Player) event.getEntity());
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

	public void doManaWiggle(int wiggleCount) {
		this.overlayRenderer.startManaWiggle(wiggleCount);
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
	
	public SelectionRenderer getSelectionRenderer() {
		return this.selectionRenderer;
	}
	
	public OverlayRenderer getOverlayRenderer() {
		return this.overlayRenderer;
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
}
