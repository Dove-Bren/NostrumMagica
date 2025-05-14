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
import com.smanzana.nostrummagica.client.render.SelectionRenderer;
import com.smanzana.nostrummagica.client.render.SpellShapeRenderer;
import com.smanzana.nostrummagica.client.render.layer.EntityEffectLayer;
import com.smanzana.nostrummagica.client.render.layer.LayerAetherCloak;
import com.smanzana.nostrummagica.client.render.layer.LayerArmorElytra;
import com.smanzana.nostrummagica.client.render.layer.LayerDragonFlightWings;
import com.smanzana.nostrummagica.client.render.layer.LayerKoidHelm;
import com.smanzana.nostrummagica.client.render.layer.LayerManaArmor;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.listener.ClientChargeManager;
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
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Incantation;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.spell.SpellCasting.SpellCastResult;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.SpellCharge;
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
import net.minecraft.world.entity.Entity;
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
	
	public ClientPlayerListener() {
		super();
		
		this.overlayRenderer = new OverlayRenderer();
		this.effectRenderer = ClientEffectRenderer.instance();
		this.outlineRenderer = new OutlineRenderer();
		this.spellshapeRenderer = new SpellShapeRenderer(this.outlineRenderer);
		this.selectionRenderer = new SelectionRenderer();
		this.chargeManager = new ClientChargeManager();
		
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
		} else if (bindingCastSlow.consumeClick()) {
			if (chargeManager.getCurrentCharge() == null && NostrumMagica.instance.getSpellCooldownTracker(mc.player.level).getCooldowns(mc.player).getGlobalCooldown().endTicks < mc.player.tickCount) {
				Player player = mc.player;
				INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
				if (attr != null && attr.isUnlocked()) {
					if (hasIncantHand(player)) {
						this.overlayRenderer.enableIncantationSelection();
					} else {
						player.sendMessage(new TranslatableComponent("info.incant.nohands"), Util.NIL_UUID);
					}
				}
			}
		}
	}
	
	protected boolean hasIncantHand(Player player) {
		for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
			if (SpellCasting.ItemAllowsCasting(player.getItemBySlot(slot), slot)) {
				return true;
			}
		}
		
		// Check tome slot
		@Nullable IInventorySlotKey<LivingEntity> key = NostrumMagica.instance.curios.getTomeSlotKey(player);
		if (key != null) {
			if (SpellCasting.ItemAllowsCasting(key.getHeldStack(player), null)) {
				return true;
			}
		}
		
		return false;
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
				if (this.chargeManager.getCurrentCharge() != null && !NostrumMagica.instance.proxy.getPlayer().isCrouching()) {
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
		// Evaluate double jump
		final LocalPlayer player = (LocalPlayer) event.getPlayer();
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
	
	private void doCast(int castSlot) {
		final Player player = NostrumMagica.instance.proxy.getPlayer();
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
				HitResult mop = RayTrace.raytraceApprox(player.getLevel(), player, player.getEyePosition(), player.getXRot(), player.getYRot(), 100, (e) -> e != player && e instanceof LivingEntity, .5);
				
				NetworkHandler.sendToServer(
		    			new ClientCastMessage(spell, false, SpellTome.getTomeID(tome), RayTrace.entFromRaytrace(mop)));
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
		final Player player = NostrumMagica.instance.proxy.getPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr != null && attr.isUnlocked()) {
			if (incant != null) {
				Spell spell = incant.makeSpell();
				
				SpellCastResult result = SpellCasting.CheckToolCast(spell, player, ItemStack.EMPTY);
				if (result.succeeded) {
					// We think we can cast it, so start charging
					this.chargeManager.startCharging(new SpellCharge(incant, result.summary.getFinalCastTicks()));
					
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
	
	protected void finishIncantationCast(SpellCharge charge) {
		final Player player = NostrumMagica.instance.proxy.getPlayer();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr != null && attr.isUnlocked()) {
			Incantation incant = charge.incant();
			if (incant != null) {
				Spell spell = incant.makeSpell();
				HitResult mop = RayTrace.raytraceApprox(player.getLevel(), player, player.getEyePosition(), player.getXRot(), player.getYRot(), 100, (e) -> e != player && e instanceof LivingEntity, .5);
				final @Nullable Entity targetHint = RayTrace.entFromRaytrace(mop);
				
				NetworkHandler.sendToServer(new ClientCastAdhocMessage(spell, targetHint));
				player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}
	
	protected void interruptSpellCharge() {
		this.chargeManager.cancelCharge(true);
		
//		final UUID id = UUID.fromString("637ec07c-9931-45ca-bd8e-e47c7f9b50a6");
//		NostrumMagica.instance.proxy.getPlayer().getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(id);
	}
	
	protected void spellChargeTick() {
		// TODO since this uses clocktime, single player can pause until it's done!
		final Minecraft mc = Minecraft.getInstance();
		
		if (!mc.isPaused() && this.chargeManager.isDoneCharging()) {
			SpellCharge charge = this.chargeManager.getCurrentCharge();
			finishIncantationCast(charge);
			this.chargeManager.cancelCharge(false);
//			final UUID id = UUID.fromString("637ec07c-9931-45ca-bd8e-e47c7f9b50a6");
//			NostrumMagica.instance.proxy.getPlayer().getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(id);
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
			final Player player = NostrumMagica.instance.proxy.getPlayer();
			
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
					final EMagicElement elem = charge.incant().getElement();
					
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
		
		if (LayerKoidHelm.ShouldRender(player)) {
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
			event.getRenderer().addLayer(new LayerDragonFlightWings(event.getRenderer()));
			event.getRenderer().addLayer(new LayerAetherCloak(event.getRenderer()));
			event.getRenderer().addLayer(new LayerManaArmor(event.getRenderer()));
			event.getRenderer().addLayer(new LayerKoidHelm(event.getRenderer()));
			event.getRenderer().addLayer(new LayerArmorElytra<>(event.getRenderer(), Minecraft.getInstance().getEntityModels()));
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
			final EMagicElement elem = charge.incant().getElement();
			
			base.mulPose(Vector3f.ZP.rotationDegrees((isLeft ? -1 : 1) * 30f * this.getChargeManager().getChargePercent()));
			base.mulPose(Vector3f.XN.rotationDegrees(30f * this.getChargeManager().getChargePercent()));
			base.mulPose(Vector3f.YP.rotationDegrees((isLeft ? -1 : 1) * 20f + 10f * Mth.sin(player.tickCount * .145f)));
				
			if (!mc.isPaused() && NostrumMagica.rand.nextInt(4) == 0) {
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
