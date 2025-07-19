package com.smanzana.nostrummagica.client.listener;

import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.block.dungeon.DungeonAirBlock;
import com.smanzana.nostrummagica.capabilities.IBonusJumpCapability;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.effects.ClientEffect;
import com.smanzana.nostrummagica.client.effects.ClientEffectAnimated;
import com.smanzana.nostrummagica.client.effects.ClientEffectFormBasic;
import com.smanzana.nostrummagica.client.effects.ClientEffectIcon;
import com.smanzana.nostrummagica.client.effects.ClientEffectRenderer;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierGrow;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierShrink;
import com.smanzana.nostrummagica.client.effects.modifiers.ClientEffectModifierTranslate;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.render.OutlineRenderer;
import com.smanzana.nostrummagica.client.render.OutlineRenderer.Outline;
import com.smanzana.nostrummagica.client.render.SelectionRenderer;
import com.smanzana.nostrummagica.client.render.SpellShapeRenderer;
import com.smanzana.nostrummagica.client.render.layer.AetherCloakLayer;
import com.smanzana.nostrummagica.client.render.layer.ArmorElytraLayer;
import com.smanzana.nostrummagica.client.render.layer.DragonFlightWingsLayer;
import com.smanzana.nostrummagica.client.render.layer.EntityEffectLayer;
import com.smanzana.nostrummagica.client.render.layer.KoidHelmLayer;
import com.smanzana.nostrummagica.client.render.layer.ManaArmorLayer;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.item.api.IRaytraceOverlay;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.item.equipment.SpellScroll;
import com.smanzana.nostrummagica.item.equipment.SpellTome;
import com.smanzana.nostrummagica.listener.ClientChargeManager;
import com.smanzana.nostrummagica.listener.ClientChargeManager.ClientSpellCharge;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.listener.PlayerJumpEvent;
import com.smanzana.nostrummagica.listener.PlayerListener;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.BladeCastMessage;
import com.smanzana.nostrummagica.network.message.ClientCastAdhocMessage;
import com.smanzana.nostrummagica.network.message.ClientCastMessage;
import com.smanzana.nostrummagica.network.message.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.MagicCapability;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Incantation;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.ChargeType;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.SpellCharge;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientPlayerListener extends PlayerListener {
	
	// Whether jump was or wasn't pressed last frame
	private boolean jumpPressedLastFrame;
	
	// Whether the last false->true jump state transition was cancelled
	private boolean jumpConsumedThisPress;
	
	// Whether this current frame has a jump that was unconsumed
	private boolean hasJump;
	
	private final KeyMapping bindingCastSlow;
	private final KeyMapping bindingCast1;
	private final KeyMapping bindingCast2;
	private final KeyMapping bindingCast3;
	private final KeyMapping bindingCast4;
	private final KeyMapping bindingCast5;
	private final KeyMapping bindingScroll;
	private final KeyMapping bindingInfo;
	private final KeyMapping bindingBladeCast;
	private final KeyMapping bindingHUD;
	private final KeyMapping bindingShapeHelp;
	private final OverlayRenderer overlayRenderer;
	private final ClientEffectRenderer effectRenderer;
	private final OutlineRenderer outlineRenderer;
	private final SpellShapeRenderer spellshapeRenderer;
	private final SelectionRenderer selectionRenderer;
	private final ClientChargeManager chargeManager;
	private final ClientTargetManager targetManager;
	private final NostrumTutorialClient tutorial;
	
	public ClientPlayerListener() {
		super();
		
		this.overlayRenderer = new OverlayRenderer();
		this.effectRenderer = ClientEffectRenderer.instance();
		this.outlineRenderer = new OutlineRenderer();
		this.spellshapeRenderer = new SpellShapeRenderer(this.outlineRenderer);
		this.selectionRenderer = new SelectionRenderer();
		this.chargeManager = new ClientChargeManager();
		this.targetManager = new ClientTargetManager();
		this.tutorial = new NostrumTutorialClient();
		
		bindingCast1 = new KeyMapping("key.cast1.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.nostrummagica.desc");
		bindingCast2 = new KeyMapping("key.cast2.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.nostrummagica.desc");
		bindingCast3 = new KeyMapping("key.cast3.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.nostrummagica.desc");
		bindingCast4 = new KeyMapping("key.cast4.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.nostrummagica.desc");
		bindingCast5 = new KeyMapping("key.cast5.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.nostrummagica.desc");
		bindingScroll = new KeyMapping("key.spellscroll.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.nostrummagica.desc");
		bindingInfo = new KeyMapping("key.infoscreen.desc", GLFW.GLFW_KEY_HOME, "key.nostrummagica.desc");
		bindingBladeCast = new KeyMapping("key.bladecast.desc", GLFW.GLFW_KEY_Y, "key.nostrummagica.desc");
		bindingHUD = new KeyMapping("key.hud.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, "key.nostrummagica.desc");
		bindingShapeHelp = new KeyMapping("key.shapehelp.desc", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.nostrummagica.desc");
		bindingCastSlow = new KeyMapping("key.castslow.desc", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.nostrummagica.desc");
	}
	
	public void initKeybinds() {
		ClientRegistry.registerKeyBinding(bindingCast1);
		ClientRegistry.registerKeyBinding(bindingCast2);
		ClientRegistry.registerKeyBinding(bindingCast3);
		ClientRegistry.registerKeyBinding(bindingCast4);
		ClientRegistry.registerKeyBinding(bindingCast5);
		ClientRegistry.registerKeyBinding(bindingScroll);
		ClientRegistry.registerKeyBinding(bindingInfo);
		ClientRegistry.registerKeyBinding(bindingBladeCast);
		ClientRegistry.registerKeyBinding(bindingHUD);
		ClientRegistry.registerKeyBinding(bindingShapeHelp);
		ClientRegistry.registerKeyBinding(bindingCastSlow);
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
	
	public KeyMapping getBindingInfo() {
		return bindingInfo;
	}

	public KeyMapping getBindingIncant() {
		return bindingCastSlow;
	}
	
	public SelectionRenderer getSelectionRenderer() {
		return this.selectionRenderer;
	}
	
	public OverlayRenderer getOverlayRenderer() {
		return this.overlayRenderer;
	}
	
	public ClientEffectRenderer getEffectRenderer() {
		return this.effectRenderer;
	}
	
	public ClientChargeManager getChargeManager() {
		return this.chargeManager;
	}
	
	public ClientTargetManager getTargetManager() {
		return targetManager;
	}
	
	public NostrumTutorialClient getTutorial() {
		return this.tutorial;
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
			
//			int unused;
//			Player player = mc.player;
//			final Vec3 pos1 = new Vec3(player.getX()-2, player.getY(), player.getZ() - 2);
//			final Vec3 pos2 = new Vec3(player.getX()+2, player.getY(), player.getZ() - 2);
//			NostrumParticles.LIGHTNING_CHAIN.spawn(mc.level, new SpawnParams(32, player.getX(), player.getY(), player.getZ() - 2, 0, 180, 0,
//					new TargetLocation(pos1)).setExtraTarget(new TargetLocation(pos2)).gravity(false).color(EMagicElement.LIGHTNING.getColor()));
			
//			NostrumParticles.COLOR_TRAIL.spawn(mc.level, new SpawnParams(50, player.getX(), player.getY() - 2, player.getZ(), 0, 180, 0,
//					new Vec3(0, .25, 0), new Vec3(.25, .05, .25)).gravity(true).color(1f, .8f, .3f, .4f));
			
//			NostrumParticles.COLOR_TRAIL.spawn(mc.level, new SpawnParams(1, player.getX(), player.getY() + 1, player.getZ() - 1, 0, 600, 0,
//					player.getId()).setTargetBehavior(TargetBehavior.ATTACH).color(1f, .8f, .3f, .4f));
			
//			NostrumParticles.GLOW_TRAIL.spawn(mc.level, new SpawnParams(1, player.getX(), player.getY() + 1, player.getZ() - 1, 0, 600, 0,
//					player.getId()).setTargetBehavior(TargetBehavior.ORBIT).setOrbitRadius(2f).color(1f, .8f, .3f, .4f));
			
//			NostrumParticles.RISING_GLOW.spawn(mc.level, new SpawnParams(3, player.getX(), player.getY(), player.getZ(), 0, 20, 0,
//					player.getId()).setTargetBehavior(TargetBehavior.ATTACH).color(RenderFuncs.ARGBFade(EMagicElement.ICE.getColor(), .6f)));
			
			/*
			 * int count, double spawnX, double spawnY, double spawnZ, double spawnJitterRadius, int lifetime, int lifetimeJitter, 
				Vec3 velocity, Vec3 velocityJitter
			 */
		} else if (bindingShapeHelp.consumeClick()) {
			this.spellshapeRenderer.toggle();
		} else if (bindingCastSlow.consumeClick()) {
			this.startIncantHold(mc.player);
		}
	}
	
	@SubscribeEvent
	public void onMouseClick(ClickInputEvent event) {
		if (this.chargeManager.getCurrentCharge() != null) {
			this.interruptSpellCharge();
		}
	}
	
	@SubscribeEvent
	public void onInputUpdate(MovementInputUpdateEvent event) {
		// Movement spell charging interrupt
		{
			final Input input = event.getInput();
			if (input.down || input.up || input.left || input.right || input.jumping) {
				if (this.chargeManager.getCurrentCharge() != null && !NostrumMagica.Proxy.getPlayer().isCrouching()) {
					this.interruptSpellCharge();
				}
			}
		}
		
		// Jumping
		{
			final boolean newPress = !jumpPressedLastFrame && event.getInput().jumping;
			jumpPressedLastFrame = event.getInput().jumping;
			// 
			
			if (newPress) {
				jumpConsumedThisPress = false;
				final PlayerJumpEvent.Pre jumpEvent = new PlayerJumpEvent.Pre(event.getPlayer());
				MinecraftForge.EVENT_BUS.post(jumpEvent);
				
				if (jumpEvent.isConsumed()) {
					jumpConsumedThisPress = true;
				} else {
					hasJump = true;
				}
			}
			
			if (jumpConsumedThisPress) {
				// Keep eating the jump so that it never appears to transition to on in the regular player loop
				event.getInput().jumping = false;
			}
		}
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		final @Nullable LocalPlayer player = mc.player;
		
		if (event.phase == Phase.START) {
			if (mc.player != null) {
				PortalBlock.clientTick();
				//TeleportRune.tick();
				spellChargeTick();
				targeterUpdateTick();
			}
		} else if (event.phase == Phase.END) {
			if (hasJump) {
				MinecraftForge.EVENT_BUS.post(new PlayerJumpEvent.Post(player));
			}
			hasJump = false;
		}
	}
	
	@SubscribeEvent
	public void onJumpInput(PlayerJumpEvent.Pre event) {
		final LocalPlayer player = (LocalPlayer) event.getPlayer();
		
		// make smoother client-side rooted
		if (player.hasEffect(NostrumEffects.rooted) && player.getEffect(NostrumEffects.rooted).getDuration() > 0) {
			event.consume();
			NostrumMagicaSounds.CAST_FAIL.play(player);
			player.sendMessage(new TranslatableComponent("info.rooted.no_jump"), Util.NIL_UUID);
			return;
		}
		
		// Evaluate double jump
		
		if (!event.isConsumed() && !player.isOnGround()) {
			final int extraJumps = (int) event.getPlayer().getAttributeValue(NostrumAttributes.bonusJump);
			if (extraJumps > 0) {
				final @Nullable IBonusJumpCapability jumps = NostrumMagica.getBonusJump(player);
				if (jumps != null) {
					if (jumps.getCount() < extraJumps) {
						jumps.incrCount();
						player.jumpFromGround();
						event.consume();
					}
				}
			}
		}
	}
	
	private void doBladeCast() {
		NetworkHandler.sendToServer(new BladeCastMessage());
	}

	public void doManaWiggle(int wiggleCount) {
		this.overlayRenderer.startManaWiggle(wiggleCount);
	}
	
	protected boolean hasSpellChargeUnlocked(Player player, INostrumMagic attr, boolean isIncant) {
		final MagicCapability cap = (isIncant ? MagicCapability.INCANT_OVERCHARGE : MagicCapability.SPELLCAST_OVERCHARGE);
		return cap.matches(attr);
	}
	
	private static final class ClientTomeCharge extends ClientSpellCharge {
		public final int castSlot;
		public ClientTomeCharge(SpellCharge charge, ItemStack mainhand, ItemStack offhand, float displayRate, int castSlot) {
			super(charge, mainhand, offhand, displayRate);
			this.castSlot = castSlot;
		}
	}
	
	private void doCast(int castSlot) {
		if (this.getChargeManager().getCurrentCharge() != null) {
			return;
		}
		
		final Player player = NostrumMagica.Proxy.getPlayer();
		RegisteredSpell[] spells = NostrumMagica.getCurrentSpellLoadout(player);
		if (castSlot < 0 || spells == null || spells.length == 0 || spells.length <= castSlot) {
			return;
		}
		
		final RegisteredSpell spell = spells[castSlot];
		if (spell == null) {
			// No spell in slot
			return;
		}
		
		// Find the tome this was cast from, if any
		ItemStack tome = NostrumMagica.getCurrentTome(player); 
		if (!tome.isEmpty()) {
			SpellCastResult result = SpellCasting.CheckToolCast(spell, player, tome);
			if (result.succeeded) {
				final ClientTomeCharge charge = new ClientTomeCharge(
						new SpellCharge(spell, result.summary.getFinalCastTicks(), ChargeType.TOME_CAST, 0),
						player.getMainHandItem(), player.getOffhandItem(),
						1f - result.summary.getCastSpeedRate(), // 1.5f -> -.5f; .8f -> .2f
						castSlot);
				if (result.summary.getFinalCastTicks() > 0) {
					this.chargeManager.startCharging(charge);
				} else {
					this.finishSpellCast(player, charge, false);
				}
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
	
	public void startIncantationCast(Incantation incant) {
		final Player player = NostrumMagica.Proxy.getPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr != null && attr.isUnlocked() && MagicCapability.INCANT_ENABLED.matches(attr)) {
			if (incant != null) {
				Spell spell = incant.makeSpell();
				
				SpellCastResult result = SpellCasting.CheckToolCast(spell, player, ItemStack.EMPTY);
				if (result.succeeded) {
					// We think we can cast it, so start charging
					this.chargeManager.startCharging(new ClientSpellCharge(
							new SpellCharge(spell, result.summary.getFinalCastTicks(), ChargeType.INCANTATION, 0),
							player.getMainHandItem(), player.getOffhandItem(),
							1f - result.summary.getCastSpeedRate()));
					
//					{
//						final UUID id = UUID.fromString("637ec07c-9931-45ca-bd8e-e47c7f9b50a6");
//						Multimap<Attribute, AttributeModifier> multimap = ImmutableListMultimap.of(Attributes.MOVEMENT_SPEED,
//								new AttributeModifier(id, "Incanting Speed Mod", -.4, AttributeModifier.Operation.MULTIPLY_TOTAL));
//						
//						player.getAttributes().addTransientAttributeModifiers(multimap);
//					}
					
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
	}
	
	private static final class ClientScrollCharge extends ClientSpellCharge {
		public final ItemStack scroll;
		public final boolean isMainhand;
		public ClientScrollCharge(SpellCharge charge, ItemStack mainhand, ItemStack offhand, float displayRate, ItemStack scroll, boolean isMainhand) {
			super(charge, mainhand, offhand, displayRate);
			this.scroll = scroll;
			this.isMainhand = isMainhand;
		}
	}
	
	public void startScrollCast(InteractionHand hand, ItemStack scroll, RegisteredSpell spell) {
		final Player player = NostrumMagica.Proxy.getPlayer();
		final ClientScrollCharge charge = new ClientScrollCharge(
				new SpellCharge(spell, 20, ChargeType.SCROLL, 0),
				player.getMainHandItem(), player.getOffhandItem(),
				0f,
				scroll, hand == InteractionHand.MAIN_HAND);
		this.chargeManager.startCharging(charge);
	}
	
	protected boolean hasIncantHand(Player player) {
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
			if (SpellCasting.ItemAllowsCasting(player.getItemBySlot(slot), slot)) {
				return true;
			}
		}
		
		// Check tome slot
		@Nullable IInventorySlotKey<LivingEntity> key = NostrumMagica.CuriosProxy.getTomeSlotKey(player);
		if (key != null) {
			if (SpellCasting.ItemAllowsCasting(key.getHeldStack(player), null)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean hasIncantSelectUnlocked(Player player, INostrumMagic attr) {
		// This is an unlock check, PLUS a check that we actually have things to display
		if (!MagicCapability.INCANT_COMPONENT_SELECT.matches(attr)) {
			return false;
		}
		
		// Can hackily just check elements, since that's how players have to unlock it.
		// But instead I will checkf or anything to make it expand easier in the future
		if (attr.getKnownElements().values().stream().filter((b) -> !!b).mapToInt(b -> b ? 1 : 0).sum() > 1) {
			return true;
		}
		
		if (attr.getShapes() != null && attr.getShapes().size() > 1) {
			return true;
		}
		
		if (attr.getAlterations() != null && attr.getAlterations().containsValue(Boolean.TRUE)) {
			return true;
		}
		
		return false;
	}
	
	protected boolean startIncantHold(Player player) {
		if (chargeManager.getCurrentCharge() == null && NostrumMagica.instance.getSpellCooldownTracker(player.level).getCooldowns(player).getGlobalCooldown().endTicks < player.tickCount) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr != null && attr.isUnlocked() && MagicCapability.INCANT_ENABLED.matches(attr)) {
				if (hasIncantHand(player)) {
					// Either enable selection, or just cast baby incantation if nothing else is unlocked
					if (!hasIncantSelectUnlocked(player, attr)) {
						startIncantationCast(new Incantation(
								attr.getShapes() == null || attr.getShapes().isEmpty() ? NostrumSpellShapes.Touch : attr.getShapes().get(0),
								EMagicElement.PHYSICAL, // hardcoding that physical is first element
								null
								) {
							@Override
							public int getWeight() {
								// Special default baby spell has no weight so it's free
								return 0;
							}
						});
						this.tutorial.onStarterIncantationCast();
					} else {
						this.overlayRenderer.enableIncantationSelection();
						this.tutorial.onIncantationFormStarted();					}
				} else {
					player.sendMessage(new TranslatableComponent("info.incant.nohands"), Util.NIL_UUID);
				}
			}
		}
		return false;
	}
	
	protected void finishSpellCast(Player player, ClientTomeCharge charge, boolean chargeHeld) {
		if (chargeHeld && charge.charge.overchargeCount() < 2) {
			// Make new charge and start charging that
			ClientTomeCharge newCharge = new ClientTomeCharge(
					charge.charge.withOvercharge(charge.charge.overchargeCount() + 1),
					charge.mainhandItem,
					charge.offhandItem,
					charge.chargeSpeed,
					charge.castSlot
					);
			this.chargeManager.startCharging(newCharge);
			onOvercharge();
		} else {
			RegisteredSpell[] spells = NostrumMagica.getCurrentSpellLoadout(player);
			if (charge.castSlot < 0 || spells == null || spells.length == 0 || spells.length <= charge.castSlot) {
				return;
			}
			
			final RegisteredSpell spell = spells[charge.castSlot];
			if (spell == null) {
				// No spell in slot
				return;
			}
			
			// Find the tome this was cast from, if any
			ItemStack tome = NostrumMagica.getCurrentTome(player); 
			SpellCastProperties props = new SpellCastProperties(1f, charge.charge.overchargeCount(), this.getTargetManager().getLastTarget(1f));
			
			NetworkHandler.sendToServer(
	    			new ClientCastMessage(spell, false, SpellTome.getTomeID(tome), props));
			player.swing(InteractionHand.MAIN_HAND);
		}
	}
	
	protected void finishIncantationCast(Player player, ClientSpellCharge charge, boolean doOvercharge) {
		if (doOvercharge && charge.charge.overchargeCount() < 2) {
			// Make a new charge with the appropriate charge count and start it charging again
			ClientSpellCharge newCharge = new ClientSpellCharge(
					charge.charge.withOvercharge(charge.charge.overchargeCount() + 1),
					charge.mainhandItem,
					charge.offhandItem,
					charge.chargeSpeed
					);
			this.chargeManager.startCharging(newCharge);
			onOvercharge();
		} else {
			//
			SpellCastProperties props = new SpellCastProperties(1f, charge.charge.overchargeCount(), this.getTargetManager().getLastTarget(1f));
			
			NetworkHandler.sendToServer(new ClientCastAdhocMessage(charge.charge.spell(), props));
			player.swing(InteractionHand.MAIN_HAND);
			
			this.tutorial.onIncantationCastFinished();
		}
	}
	
	protected void finishScrollCast(Player playerIn, ClientScrollCharge charge, boolean chargeHeld) {
		// Note: ignore attempt to overcharge
		
		HitResult mop = RayTrace.raytraceApprox(playerIn.getLevel(), playerIn, playerIn.getEyePosition(), playerIn.getXRot(), playerIn.getYRot(), 100, (e) -> e != playerIn && e instanceof LivingEntity, .5);
		final @Nullable LivingEntity hint = RayTrace.entFromRaytrace(mop) == null ? null : (LivingEntity) RayTrace.entFromRaytrace(mop);
		SpellCastProperties props = new SpellCastProperties(1f, charge.charge.overchargeCount(), hint);
		
		RegisteredSpell spell = SpellScroll.GetSpell(charge.scroll);
		NetworkHandler.sendToServer(new ClientCastMessage(spell, true, charge.isMainhand ? 0 : 1, props));
		playerIn.swing(InteractionHand.MAIN_HAND);
	}
	
	protected void finishChargeCast(ClientSpellCharge charge) {
		final Player player = NostrumMagica.Proxy.getPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		final boolean chargeHeld = this.getBindingIncant().isDown() && hasSpellChargeUnlocked(player, attr, charge.charge.type() == ChargeType.INCANTATION);
		if (attr != null && attr.isUnlocked()) {
			switch (charge.charge.type()) {
			case INCANTATION:
				finishIncantationCast(player, charge, chargeHeld);
				break;
			case TOME_CAST:
				finishSpellCast(player, (ClientTomeCharge) charge, chargeHeld);
				break;
			case SCROLL:
				finishScrollCast(player, (ClientScrollCharge) charge, chargeHeld);
				break;
			}
		}
	}
	
	protected void interruptSpellCharge() {
		this.chargeManager.cancelCharge(true);
		
//		final UUID id = UUID.fromString("637ec07c-9931-45ca-bd8e-e47c7f9b50a6");
//		NostrumMagica.instance.proxy.getPlayer().getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(id);
		
		this.tutorial.onChargeCancel();
	}
	
	protected void onOvercharge() {
		this.getTutorial().onOverchargeStage();
	}
	
	protected void spellChargeTick() {
		// TODO since this uses clocktime, single player can pause until it's done!
		final Minecraft mc = Minecraft.getInstance();
		
		if (!mc.isPaused() && this.chargeManager.isDoneCharging()) {
			ClientSpellCharge charge = this.chargeManager.getCurrentCharge();
			this.chargeManager.cancelCharge(false);
			finishChargeCast(charge);
//			final UUID id = UUID.fromString("637ec07c-9931-45ca-bd8e-e47c7f9b50a6");
//			NostrumMagica.instance.proxy.getPlayer().getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(id);
		}
		
		if (this.chargeManager.getCurrentCharge() != null && this.chargeManager.getCurrentCharge().charge.type() == ChargeType.SCROLL) {
			// make sure item hasn't changed!
			final Player player = NostrumMagica.Proxy.getPlayer();
			final ClientScrollCharge charge = (ClientScrollCharge) this.chargeManager.getCurrentCharge();
			ItemStack inHand = charge.isMainhand ? player.getMainHandItem() : player.getOffhandItem();
			if (!inHand.equals(charge.scroll, false)) {
				this.interruptSpellCharge();
			}
		}
	}
	
	protected HitResult defaultTrace() {
		final Minecraft mc = Minecraft.getInstance();
		final Player player = NostrumMagica.Proxy.getPlayer();
		final float partialTicks = mc.getFrameTime();
		
		double range = 0f;
		
		if (this.getChargeManager().getCurrentCharge() != null) {
			range = this.getChargeManager().getCurrentCharge().charge.spell().getTraceRange(player);
		}
		
		ItemStack stack = player.getMainHandItem();
		if (range <= 0f && !stack.isEmpty() && stack.getItem() instanceof IRaytraceOverlay traced && traced.shouldTrace(player.level, player, stack)) {
			range = traced.getTraceRange(player.level, player, stack);
		}
		
		stack = player.getOffhandItem();
		if (range <= 0f && !stack.isEmpty() && stack.getItem() instanceof IRaytraceOverlay traced && traced.shouldTrace(player.level, player, stack)) {
			range = traced.getTraceRange(player.level, player, stack);
		}
		
		if (range <= 0f) {
			// default
			range = 10f;
		}
		
		return RayTrace.raytrace(player.level, player, player.getEyePosition(partialTicks), player.getViewXRot(partialTicks), player.getViewYRot(partialTicks), (float) range, new RayTrace.OtherLiving(player));
	}
	
	protected boolean shouldDoTargetting() {
		final Player player = NostrumMagica.Proxy.getPlayer();
		if (this.getChargeManager().getCurrentCharge() != null
				&& this.getChargeManager().getCurrentCharge().charge.spell().shouldTrace(player)) {
			return true;
		}
		
		ItemStack stack = player.getMainHandItem();
		if (!stack.isEmpty() && stack.getItem() instanceof IRaytraceOverlay traced && traced.shouldTrace(player.level, player, stack)) {
			return true;
		}
		
		stack = player.getOffhandItem();
		if (!stack.isEmpty() && stack.getItem() instanceof IRaytraceOverlay traced && traced.shouldTrace(player.level, player, stack)) {
			return true;
		}
		
		return false;
	}
	
	protected void targeterUpdateTick() {
		if (shouldDoTargetting()) {
			@Nullable LivingEntity lastTarget = this.getTargetManager().getLastTarget(1f);
			@Nullable LivingEntity target = this.getTargetManager().traceOrLastTarget(this::defaultTrace, ClientTargetManager.STANDARD_VIEW);
			if (target != lastTarget) {
				if (lastTarget != null) {
					this.outlineRenderer.remove(lastTarget);
				}
				if (target != null) {
					this.outlineRenderer.add(target, new Outline(1f, 1f, 1f, 1f));
				}
			}
		} else {
			@Nullable LivingEntity lastTarget = this.getTargetManager().getLastTarget(1f);
			if (lastTarget != null) {
				this.outlineRenderer.remove(lastTarget);
			}
			this.getTargetManager().clearTarget();
		}
	}
	
	protected void renderRoots(PoseStack matrixStackIn, LivingEntity entity) {
		if (entity.tickCount % 4 == 0) {
			EffectData data = NostrumMagica.magicEffectProxy.getData(entity, SpecialEffect.ROOTED);
			if (data != null && data.getCount() != 0) {
				final ClientEffect effect = new ClientEffectAnimated(entity.position(), 1000L,
						new ClientEffect[] {
							new ClientEffect(Vec3.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_0, 0, 0, 0), 1500L),
							new ClientEffect(Vec3.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_1, 0, 0, 0), 1500L),
							new ClientEffect(Vec3.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_2, 0, 0, 0), 1500L),
							new ClientEffect(Vec3.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_3, 0, 0, 0), 1500L),
							new ClientEffect(Vec3.ZERO, new ClientEffectFormBasic(ClientEffectIcon.THORN_4, 0, 0, 0), 1500L),
						},
						new float[] {
							.1f,
							.2f,
							.3f,
							.4f,
							1f
						});
				effect
					.modify(new ClientEffectModifierTranslate(NostrumMagica.rand.nextFloat() - .5f, 0, NostrumMagica.rand.nextFloat() - .5f, 0, NostrumMagica.rand.nextFloat() * 360f))
					.modify(new ClientEffectModifierGrow(.05f, 1f, .1f, 1f, .2f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .0f, .8f));
				ClientEffectRenderer.instance().addEffect(effect);
			}
		}
	}
	
	private static final Map<LivingEntityRenderer<?, ?>, Boolean> LivingInjectedSet = new WeakHashMap<>();
	
	@SubscribeEvent
	public <T extends LivingEntity, M extends EntityModel<T>> void onEntityRender(RenderLivingEvent.Pre<T, M> event) {
		final LivingEntityRenderer<T, M> renderer = event.getRenderer();
		if (!LivingInjectedSet.containsKey(renderer)) {
			LivingInjectedSet.put(renderer, true);
			renderer.addLayer(new EntityEffectLayer<T, M>(renderer));
		}
	}
	
	private boolean renderRecurseMarker = false;
	
	@SubscribeEvent
	public void onEntityRender(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
		if (!renderRecurseMarker) {
			final LivingEntity entity = event.getEntity();
			final PoseStack matrixStackIn = event.getPoseStack();
			
			//final float partialTicks = event.getPartialRenderTick();
			renderRecurseMarker = true;
			{
				renderRoots(matrixStackIn, entity);
			}
			renderRecurseMarker = false;
		}
	}
	
	@SubscribeEvent
	public void onRenderLast(RenderLevelStageEvent event) {
		if (event.getStage() != Stage.AFTER_PARTICLES) {
			return;
		}
		
		// Copy of what vanilla uses to figure out if it should render the render entity:
		Minecraft mc = Minecraft.getInstance();
		final boolean shouldRenderMe = (
				mc.options.getCameraType() != CameraType.FIRST_PERSON
				|| (mc.getCameraEntity() instanceof LivingEntity && ((LivingEntity)mc.getCameraEntity()).isSleeping())
				);
		
		if (!shouldRenderMe) {
			final Player player = NostrumMagica.Proxy.getPlayer();
			
			// Normal render didn't happen. Do rooted render manually instead.
			renderRoots(event.getPoseStack(), player);
		}
	}
	
	//private RenderCastingPlayer castingRender = null;
	
	protected final boolean playerIsCasting(Player player) {
		return NostrumMagica.spellChargeTracker.isCharging(player);
	}
	
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Pre event) {
		final Player player = event.getPlayer();
		
		if (!Minecraft.getInstance().isPaused() && playerIsCasting(player)) {
			// maybe add particle
			for (HumanoidArm hand : HumanoidArm.values()) {
				if (NostrumMagica.rand.nextInt(4) == 0) {
					SpellCharge charge = NostrumMagica.spellChargeTracker.getCharge(player);
					final EMagicElement elem = charge.spell().getPrimaryElement();
					
					final boolean left = hand == HumanoidArm.LEFT;
					ModelPart part = left ? event.getRenderer().getModel().leftArm : event.getRenderer().getModel().rightArm;
					PoseStack base = new PoseStack();
					
					base.mulPose(Vector3f.YP.rotationDegrees(-player.yBodyRot + 180));
					
					base.scale(-1.0F, -1.0F, 1.0F);
					//this.scale(p_115308_, p_115311_, p_115310_);
					base.translate(0.0D, (double)-1.201F, 0.0D);
					
					part.translateAndRotate(base);
					
					base.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
					base.mulPose(Vector3f.YP.rotationDegrees(180.0F));
					
					Matrix4f transform = base.last().pose();
					Vector4f vector4f = new Vector4f(((float)(left ? -1 : 1) / 16.0F), 0.15f, -0.725f, 1.0F);
					vector4f.transform(transform);
					
					final double x = player.getX() + vector4f.x();
					final double y = player.getY() + vector4f.y();
					final double z = player.getZ() + vector4f.z();
					
					NostrumParticles.GLOW_ORB.spawn(player.getLevel(), new SpawnParams(1, x, y, z, .0125f, 40, 0, new Vec3(0, .05, 0), new Vec3(.001, .01, .001)).color(0x40FFFFFF & elem.getColor()));
					player.getLevel().addParticle(ParticleTypes.CRIT, x, y, z, 0, .1f, 0);
				}
			}
		}
		
		if (KoidHelmLayer.ShouldRender(player)) {
			event.getRenderer().getModel().head.visible = false;
			event.getRenderer().getModel().jacket.visible = false;
		}
	}
	
	private static final Map<PlayerRenderer, Boolean> InjectedSet = new WeakHashMap<>();
	
	@SubscribeEvent
	public void onPlayerRender(RenderPlayerEvent.Post event) {
		if (!InjectedSet.containsKey(event.getRenderer())) {
			InjectedSet.put(event.getRenderer(), true);
			
			// Instead, we just inject a layer for our custom elytras, and another for dragon-flight wings
			event.getRenderer().addLayer(new DragonFlightWingsLayer(event.getRenderer()));
			event.getRenderer().addLayer(new AetherCloakLayer(event.getRenderer()));
			event.getRenderer().addLayer(new ManaArmorLayer(event.getRenderer()));
			event.getRenderer().addLayer(new KoidHelmLayer(event.getRenderer()));
			event.getRenderer().addLayer(new ArmorElytraLayer<>(event.getRenderer(), Minecraft.getInstance().getEntityModels()));
		}

		Minecraft mc = Minecraft.getInstance();
		if (event.getPlayer() != mc.player) {
			// For other players, possibly do armor render ticks
			for (@Nonnull ItemStack equipStack : event.getPlayer().getArmorSlots()) {
				if (equipStack.isEmpty() || !(equipStack.getItem() instanceof ElementalArmor)) {
					continue;
				}
				
				((ElementalArmor) equipStack.getItem()).onArmorTick(equipStack, event.getPlayer().level, event.getPlayer());
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerFirstPersonArmRender(RenderArmEvent event) {
		if (playerIsCasting(event.getPlayer())) {
			final AbstractClientPlayer player = event.getPlayer();
			final boolean isLeft = event.getArm() == HumanoidArm.LEFT;
			final PoseStack base = event.getPoseStack();
			final Minecraft mc = Minecraft.getInstance();
			final float partialTicks = mc.getDeltaFrameTime();
			SpellCharge charge = NostrumMagica.spellChargeTracker.getCharge(player);
			final EMagicElement elem = charge.spell().getPrimaryElement();
			final float rotScale = .145f + (charge.overchargeCount() * .075f);
			final int particleRandNum = 4 - charge.overchargeCount();
			
			base.mulPose(Vector3f.ZP.rotationDegrees((isLeft ? -1 : 1) * 30f * this.getChargeManager().getChargePercent()));
			base.mulPose(Vector3f.XN.rotationDegrees(30f * this.getChargeManager().getChargePercent()));
			base.mulPose(Vector3f.YP.rotationDegrees((isLeft ? -1 : 1) * 20f + 10f * Mth.sin(player.tickCount * rotScale)));
				
			if (!mc.isPaused() && NostrumMagica.rand.nextInt(particleRandNum) == 0) {
				Vec3 offset = new Vec3((isLeft ? -1 : 1) * .4f, -.2f, -.6f);
				offset = offset.xRot(((player.getViewXRot(partialTicks) % 360f) / 180f) * Mth.PI);
				offset = offset.yRot((float)Math.PI + -((player.getViewYRot(partialTicks) % 360f) / 180f) * (float)Math.PI);
				offset = offset.add(0, 1.6f, 0);
				
				final double x = player.getX() + offset.x();
				final double y = player.getY() + offset.y();
				final double z = player.getZ() + offset.z();
				
				if (NostrumMagica.rand.nextBoolean()) {
					player.getLevel().addParticle(ParticleTypes.CRIT, x, y, z, 0, .1f, 0);
				} else {
					NostrumParticles.GLOW_ORB.spawn(player.getLevel(), new SpawnParams(1, x, y, z, .0125f, 40, 0, new Vec3(0, .015, 0), new Vec3(.001, .01, .001)).color(0x40FFFFFF & elem.getColor()));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerFirstPersonHandRender(RenderHandEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		final AbstractClientPlayer player = mc.player;
		
		// other arm will not render normally if empty, so force it
		if (event.getHand() == InteractionHand.OFF_HAND && event.getItemStack().isEmpty() && playerIsCasting(player)) {
			PoseStack base = event.getPoseStack();
			base.pushPose();
			
			final ItemInHandRenderer handRenderer = mc.getItemInHandRenderer();
			handRenderer.renderPlayerArm(base, event.getMultiBufferSource(), event.getPackedLight(), event.getEquipProgress(), 0f, player.getMainArm().getOpposite());
			base.popPose();
		}
	}
	
	@SubscribeEvent
	public void onBlockHighlight(DrawSelectionEvent.HighlightBlock event) {
		if (event.isCanceled() || event.getTarget().getType() != HitResult.Type.BLOCK) {
			return;
		}
		
		BlockState state = event.getCamera().getEntity().level.getBlockState(RayTrace.blockPosFromResult(event.getTarget()));
		if (state == null) {
			return;
		}
		
		// Dungeon Air wants no overlay
		if (state.getBlock() instanceof DungeonAirBlock) {
			if (!(event.getCamera().getEntity() instanceof Player)
					|| ((Player) event.getCamera().getEntity()).isCreative()) {
				event.setCanceled(true);
				return;
			}
		}
	}
}
