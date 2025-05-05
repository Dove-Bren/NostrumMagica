package com.smanzana.nostrummagica.listener;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.capabilities.IBonusJumpCapability;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.effects.ClientEffectRenderer;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.client.render.OutlineRenderer;
import com.smanzana.nostrummagica.client.render.SelectionRenderer;
import com.smanzana.nostrummagica.client.render.SpellShapeRenderer;
import com.smanzana.nostrummagica.entity.ArcaneWolfEntity;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.BladeCastMessage;
import com.smanzana.nostrummagica.network.message.ClientCastMessage;
import com.smanzana.nostrummagica.network.message.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCasting;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
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
	
	public ClientPlayerListener() {
		super();
		
		this.overlayRenderer = new OverlayRenderer();
		this.effectRenderer = ClientEffectRenderer.instance();
		this.outlineRenderer = new OutlineRenderer();
		this.spellshapeRenderer = new SpellShapeRenderer(this.outlineRenderer);
		this.selectionRenderer = new SelectionRenderer();
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
	
	public KeyMapping getBindingInfo() {
		return bindingInfo;
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
	
	@SubscribeEvent
	public void onInputUpdate(MovementInputUpdateEvent event) {
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
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		final @Nullable LocalPlayer player = mc.player;
		
		if (event.phase == Phase.START) {
			if (mc.player != null) {
				PortalBlock.clientTick();
				//TeleportRune.tick();
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
}
