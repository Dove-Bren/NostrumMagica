package com.smanzana.nostrummagica.proxy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.ElementalMastery;
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
import com.smanzana.nostrummagica.client.gui.MirrorGui;
import com.smanzana.nostrummagica.client.gui.ObeliskScreen;
import com.smanzana.nostrummagica.client.gui.ScrollScreen;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.integration.jei.NostrumMagicaJEIPlugin;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.BladeCastMessage;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskSelectMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskTeleportationRequestMessage;
import com.smanzana.nostrummagica.network.messages.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.network.messages.StatRequestMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;
import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingCast;
	private KeyBinding bindingScroll;
	private KeyBinding bindingInfo;
	private KeyBinding bindingBladeCast;
	private OverlayRenderer overlayRenderer;
	private ClientEffectRenderer effectRenderer;
	
	public ClientProxy() {
		super();
		
		this.overlayRenderer = new OverlayRenderer();
		this.effectRenderer = ClientEffectRenderer.instance();
		
		MinecraftForge.EVENT_BUS.register(this); // For client join welcome message
	}
	
	public void initKeybinds() {
		bindingCast = new KeyBinding("key.cast.desc", GLFW.GLFW_KEY_LEFT_CONTROL, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast);
		bindingScroll = new KeyBinding("key.spellscroll.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		bindingInfo = new KeyBinding("key.infoscreen.desc", GLFW.GLFW_KEY_HOME, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingInfo);
		bindingBladeCast = new KeyBinding("key.bladecast.desc", GLFW.GLFW_KEY_R, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingBladeCast);
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
				if (bindingScroll.isKeyDown()) {
					wheel = (wheel > 0 ? -1 : 1);
					int index = SpellTome.incrementIndex(tome, wheel);
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
		if (bindingCast.isPressed())
			doCast();
		else if (bindingInfo.isPressed()) {
			PlayerEntity player = mc.player;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			Minecraft.getInstance().displayGuiScreen(new InfoScreen(attr, (String) null));
//			player.openGui(NostrumMagica.instance,
//					NostrumGui.infoscreenID, player.world, 0, 0, 0);
		} else if (mc.gameSettings.keyBindJump.isPressed()) {
			PlayerEntity player = mc.player;
			if (player.isPassenger() && player.getRidingEntity() instanceof EntityTameDragonRed) {
				((EntityDragon) player.getRidingEntity()).dragonJump();
			} else if (player.isPassenger() && player.getRidingEntity() instanceof EntityArcaneWolf) {
				((EntityArcaneWolf) player.getRidingEntity()).wolfJump();
			}
		} else if (bindingBladeCast.isPressed()) {
			PlayerEntity player = mc.player;
			if (player.getCooledAttackStrength(0.5F) > .95) {
				player.resetCooldown();
				//player.swingArm(Hand.MAIN_HAND);
				doBladeCast();
			}
			
		}
	}
	
	private void doBladeCast() {
		NetworkHandler.sendToServer(new BladeCastMessage());
	}
	
	private void doCast() {
		final Minecraft mc = Minecraft.getInstance();
		Spell spell = NostrumMagica.getCurrentSpell(mc.player);
		if (spell == null) {
			System.out.println("LOUD NULL SPELL"); // TODO remove
			return;
		}
		
		// Do mana check here (it's also done on server)
		// to stop redundant checks and get mana looking good
		// on client side immediately
		PlayerEntity player = mc.player;
		INostrumMagic att = NostrumMagica.getMagicWrapper(player);
		int mana = att.getMana();
		int cost = spell.getManaCost();
		SpellCastSummary summary = new SpellCastSummary(cost, 0);
		
		// Add the player's personal bonuses
		summary.addCostRate(-att.getManaCostModifier());
		
		// Find the tome this was cast from, if any
		ItemStack tome = NostrumMagica.getCurrentTome(player); 
		if (!tome.isEmpty() && tome.getItem() instanceof SpellTome) {
			// Casting from a tome.
			
			// Make sure it isn't too hard for the tome
			int cap = SpellTome.getMaxMana(tome);
			if (cap < cost) {
				player.sendMessage(new TranslationTextComponent(
						"info.spell.tome_weak", new Object[0]), Util.DUMMY_UUID);
				NostrumMagicaSounds.CAST_FAIL.play(player);
				System.out.println("LOUD tome weak"); // TODO remove
				return;
			}
			
			List<SpellTomeEnhancementWrapper> enhancements = SpellTome.getEnhancements(tome);
			if (enhancements != null && !enhancements.isEmpty())
			for (SpellTomeEnhancementWrapper enhance : enhancements) {
				enhance.getEnhancement().onCast(
						enhance.getLevel(), summary, player, att);
			}
		}
		
		// Cap enhancements at 80% LRC
		{
			float lrc = summary.getReagentCost();
			if (lrc < .2f)
				summary.addCostRate(.2f - lrc); // Add however much we need to get to 1
		}
		
		// Visit an equipped spell armor
		for (ItemStack equip : player.getEquipmentAndArmor()) {
			if (equip.isEmpty())
				continue;
			if (equip.getItem() instanceof ISpellArmor) {
				ISpellArmor armor = (ISpellArmor) equip.getItem();
				armor.apply(player, summary, equip);
			}
		}
		
		// Possible use baubles
		IInventory baubles = NostrumMagica.instance.curios.getCurios(player);
		if (baubles != null) {
			for (int i = 0; i < baubles.getSizeInventory(); i++) {
				ItemStack equip = baubles.getStackInSlot(i);
				if (equip.isEmpty()) {
					continue;
				}
				
				if (equip.getItem() instanceof ISpellArmor) {
					ISpellArmor armor = (ISpellArmor) equip.getItem();
					armor.apply(player, summary, equip);
				}
			}
		}
		
		cost = summary.getFinalCost();
		
		// Add dragon mana pool
		Collection<ITameDragon> dragons = NostrumMagica.getNearbyTamedDragons(player, 32, true);
		if (dragons != null && !dragons.isEmpty()) {
			for (ITameDragon dragon : dragons) {
				if (dragon.sharesMana(mc.player)) {
					mana += dragon.getMana();
				}
			}
		}
		
		if (!mc.player.isCreative()) {
			// Check mana
			if (mana < cost) {
				
				for (int i = 0; i < 15; i++) {
					double offsetx = Math.cos(i * (2 * Math.PI / 15)) * 1.0;
					double offsetz = Math.sin(i * (2 * Math.PI / 15)) * 1.0;
					player.world
						.addParticle(ParticleTypes.LARGE_SMOKE,
								player.getPosX() + offsetx, player.getPosY(), player.getPosZ() + offsetz,
								0, -.5, 0);
					
				}
				
				System.out.println("LOUD LOW MANA"); // TODO remove
				
				NostrumMagicaSounds.CAST_FAIL.play(player);
				overlayRenderer.startManaWiggle(2);
				return;
			}
			
			// Check attributes
			int maxComps = 2 * (att.getTech() + 1);
			int maxTriggers = 1 + (att.getFinesse());
			int maxElems = 1 + (3 * att.getControl());
			if (spell.getComponentCount() > maxComps) {
				player.sendMessage(new TranslationTextComponent(
						"info.spell.low_tech", new Object[0]), Util.DUMMY_UUID);
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			} else if (spell.getElementCount() > maxElems) {
				player.sendMessage(new TranslationTextComponent(
						"info.spell.low_control", new Object[0]), Util.DUMMY_UUID);
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			} else if (spell.getTriggerCount() > maxTriggers) {
				player.sendMessage(new TranslationTextComponent(
						"info.spell.low_finesse", new Object[0]), Util.DUMMY_UUID);
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			}
			
			// Check elemental mastery
			for (SpellPart part : spell.getSpellParts()) {
	    		if (part.isTrigger())
	    			continue;
	    		EMagicElement elem = part.getElement();
	    		if (elem == null)
	    			elem = EMagicElement.PHYSICAL;
	    		int level = part.getElementCount();
	    		
	    		final ElementalMastery neededMastery;
				switch (level) {
				case 0:
				case 1:
					neededMastery = ElementalMastery.NOVICE;
					break;
				case 2:
					neededMastery = ElementalMastery.ADEPT;
					break;
				case 3:
				default:
					neededMastery = ElementalMastery.MASTER;
					break;
				}
				
				final ElementalMastery currentMastery = att.getElementalMastery(elem);
				if (!currentMastery.isGreaterOrEqual(neededMastery)) {
					player.sendMessage(new TranslationTextComponent(
							"info.spell.low_mastery", neededMastery.name().toLowerCase(), elem.getName(), currentMastery.name().toLowerCase()), Util.DUMMY_UUID);
						NostrumMagicaSounds.CAST_FAIL.play(player);
				}
	    	}
			
			// Check reagents
			// Skip check if there's a server-side chance of it still working anyways
			if (summary.getReagentCost() >= 1f) {
				Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(player, row.getKey());
					if (count < row.getValue()) {
						player.sendMessage(new TranslationTextComponent("info.spell.bad_reagent", row.getKey().prettyName()), Util.DUMMY_UUID);
						return;
					}
				}
				
				// Don't actually deduct on client.
				// Response from server will result in deduct if it goes through
			}
			
			NostrumMagica.getMagicWrapper(mc.player)
				.addMana(-cost);
		}
		
		NetworkHandler.sendToServer(
    			new ClientCastMessage(spell, false, SpellTome.getTomeID(tome)));
	}
	
	@Override
	public void syncPlayer(ServerPlayerEntity player) {
		if (player.world.isRemote)
			return;
		
		super.syncPlayer(player);
	}
	
	@Override
	public PlayerEntity getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	private INostrumMagic overrides = null;
	@Override
	public void receiveStatOverrides(INostrumMagic override) {
		// If we can look up stats, apply them.
		// Otherwise, stash them for loading when we apply attributes
		final Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		INostrumMagic existing = NostrumMagica.getMagicWrapper(player);
		if (existing != null && player.isAlive()) {
			// apply them
			existing.copy(override);
			
			// If we're on a screen that cares, refresh it
			if (mc.currentScreen instanceof MirrorGui) {
				((MirrorGui) mc.currentScreen).refresh();
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
	public void openBook(PlayerEntity player, GuiBook book, Object userdata) {
		Minecraft.getInstance().displayGuiScreen(book.getScreen(userdata));
	}
	
	@Override
	public void openContainer(PlayerEntity player, IPackedContainerProvider provider) {
		if (!player.world.isRemote) {
			super.openContainer(player, provider);
		}
		; // On client, do nothing
	}
	
	@Override
	public void openSpellScreen(Spell spell) {
		Minecraft.getInstance().displayGuiScreen(new ScrollScreen(spell));
	}
	
	@Override
	public void openMirrorScreen() {
		final PlayerEntity player = getPlayer();
		if (player.world.isRemote()) {
			Minecraft.getInstance().displayGuiScreen((Screen) new MirrorGui(player));
		}
	}
	
	@Override
	public void openObeliskScreen(World world, BlockPos pos) {
		if (world.isRemote()) {
			NostrumObeliskEntity te = (NostrumObeliskEntity) world.getTileEntity(pos);
			Minecraft.getInstance().displayGuiScreen(new ObeliskScreen(te));
		}
	}
	
	@Override
	public void sendSpellDebug(PlayerEntity player, ITextComponent comp) {
		if (!player.world.isRemote) {
			super.sendSpellDebug(player, comp);
		}
		;
	}
	
	@Override
	public String getTranslation(String key) {
		return I18n.format(key, new Object[0]).trim();
	}
	
	@Override
	public void requestObeliskTransportation(BlockPos origin, BlockPos target) {
		// Send a request to the server
		NetworkHandler.sendToServer(
				new ObeliskTeleportationRequestMessage(origin, target)
				);
	}
	
	@Override
	public void setObeliskIndex(BlockPos obeliskPos, int index) {
		// Send a request to the server
		NetworkHandler.sendToServer(
				new ObeliskSelectMessage(obeliskPos, index)
				);
	}
	
	@Override
	public void requestStats(LivingEntity entity) {
		NetworkHandler.sendToServer(
				new StatRequestMessage()
				);
	}
	
	private static final ClientEffect doCorruptEffect(LivingEntity source,
			Vector3d sourcePos,
			LivingEntity target,
			Vector3d targetPos,
			SpellComponentWrapper flavor,
			boolean negative,
			float param) {
		ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
				new ClientEffectFormFlat(ClientEffectIcon.ARROWD, 0, 0, 0),
				3L * 500L, 6);
		
		if (flavor != null && flavor.isElement()) {
			effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
		}
		
		if (target != null) {
			effect.modify(new ClientEffectModifierFollow(target));
		}
		
		effect
		.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
		.modify(new ClientEffectModifierTranslate(0, 1.5f, -1.5f))
		.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, -2, 0), .3f, 1f))
		.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
		.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
		;
		return effect;
	}
	
	public void initDefaultEffects() {
		ClientEffectRenderer renderer = this.effectRenderer;
		
		renderer.registerEffect(new SpellComponentWrapper(AoEShape.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					// TODO get the shape params in here to modify scale
					// TODO get whether it's a good thing or not
					ClientEffect effect = new ClientEffectMajorSphere(target == null ? targetPos : new Vector3d(0, 0, 0),
							param + .5f,
							negative,
							1000L);
					
					if (target != null)
						effect.modify(new ClientEffectModifierFollow(target));
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					// negative will blow up and then shrink down in a cool way
					// positive will rise up and then fade out
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .4f, 0f));
					
					if (negative) {
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
		
		// elements 
		for (EMagicElement element : EMagicElement.values()) {
			renderer.registerEffect(new SpellComponentWrapper(element),
					(source, sourcePos, target, targetPos, flavor, negative, param) -> {
						ClientEffect effect = new ClientEffectMirrored(target == null ? targetPos : new Vector3d(0, 0, 0),
								new ClientEffectFormFlat(ClientEffectIcon.TING1, 0, 0, 0),
								500L, 5);
						
						if (target != null)
							effect.modify(new ClientEffectModifierFollow(target));
						
						effect
						.modify(new ClientEffectModifierColor(element.getColor(), element.getColor()))
						.modify(new ClientEffectModifierRotate(0f, .4f, 0f))
						.modify(new ClientEffectModifierTranslate(0, 0, -1))
						.modify(new ClientEffectModifierMove(new Vector3d(0, 1.5, 0), new Vector3d(0, .5, .7), .5f, 1f))
						.modify(new ClientEffectModifierGrow(.1f, .3f, .2f, .8f, .5f))
						;
						return effect;
					});
		}
		
		// triggers (that have them)
		renderer.registerEffect(new SpellComponentWrapper(BeamTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectBeam(sourcePos == null ? source.getPositionVec() : sourcePos,
							targetPos == null ? target.getPositionVec() : targetPos,
							500L);
					
					//if (target != null)
					//	effect.modify(new ClientEffectModifierFollow(target));
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
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
		
		renderer.registerEffect(new SpellComponentWrapper(OtherTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING3, 0, 0, 0),
							500L, 6);
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					effect
					.modify(new ClientEffectModifierTranslate(0, 1, -1))
					.modify(new ClientEffectModifierRotate(.4f, 0f, 1.2f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .3f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, .2f, .8f))
					;
					return effect;
				});
		
		renderer.registerEffect(new SpellComponentWrapper(HealthTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
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
		
		renderer.registerEffect(new SpellComponentWrapper(ManaTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
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
		
		renderer.registerEffect(new SpellComponentWrapper(FoodTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
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
		
		renderer.registerEffect(new SpellComponentWrapper(ProximityTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							2L * 1000L, 5);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					param = Math.max(1f, param);
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, .2f, (.5f * param)))
					.modify(new ClientEffectModifierGrow(.2f, .2f, .4f, .5f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .4f))
					;
					return effect;
				});
		
//		renderer.registerEffect(new SpellComponentWrapper(WallTrigger.instance()),
//				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
//					final boolean northsouth = (param >= 1000f);
//					final int radius = (int) param - (northsouth ? 1000 : 0);
//					
//					
//				}
//				);
		
		// Alterations
		renderer.registerEffect(new SpellComponentWrapper(EAlteration.INFLICT),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.ARROWD, 0, 0, 0),
							3L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, 1.5f, -1.5f))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, -2, 0), .3f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.RESIST),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.ARROWU, 0, 0, 0),
							3L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, .5f, 0f))
					.modify(new ClientEffectModifierTranslate(0, 0f, -1.5f))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, 1.5, 0), 0f, .7f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.GROWTH),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectEchoed(targetPos == null ? target.getPositionVec() : targetPos, 
							new ClientEffectMirrored(new Vector3d(0,0,0),
							new ClientEffectFormFlat(ClientEffectIcon.TING3, 0, 0, 0),
							2L * 1000L, 4), 2L * 1000L, 5, .2f);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
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

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.SUPPORT),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect;
					boolean isShield = false;
					if (flavor != null && flavor.isElement() && 
							(flavor.getElement() == EMagicElement.EARTH || flavor.getElement() == EMagicElement.ICE)) {
						// Special ones for shields!
						effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
								new ClientEffectFormBasic(ClientEffectIcon.SHIELD, 0, 0, 0),
								3L * 500L, 5);
						isShield = true;
					} else {
						effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
								new ClientEffectFormFlat(ClientEffectIcon.TING5, 0, 0, 0),
								3L * 500L, 10);
					}
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
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

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.ENCHANT),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVec() : targetPos).add(0, 1, 0),
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							3L * 500L, 6, new Vector3f(1, 0, 0));
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
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

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.CONJURE),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					// TODO physical breaks stuff. Lots of particles. Should we return null here?
					
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							1L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
										
					effect
					.modify(new ClientEffectModifierTranslate(0f, 1f, 0f))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.SUMMON),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVec() : targetPos,
							new ClientEffectFormFlat(ClientEffectIcon.TING1, 0, 0, 0),
							1L * 500L, 6);
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
					if (target != null) {
						effect.modify(new ClientEffectModifierFollow(target));
					}
					
					effect
					.modify(new ClientEffectModifierRotate(0f, -.5f, 0f))
					.modify(new ClientEffectModifierTranslate(0f, 1f, 0f))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vector3d(0, 0, 0), new Vector3d(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierRotate(1f, 0f, 0f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.RUIN),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVec() : targetPos).add(0, 1, 0),
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							2L * 500L, 6, new Vector3f(.5f, .5f, 0));
					
					if (flavor != null && flavor.isElement()) {
						effect.modify(new ClientEffectModifierColor(flavor.getElement().getColor(), flavor.getElement().getColor()));
					}
					
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
		
		renderer.registerEffect(new SpellComponentWrapper(EAlteration.CORRUPT), ClientProxy::doCorruptEffect);
	}
	
	@Override
	public void spawnEffect(World world, SpellComponentWrapper comp,
			LivingEntity caster, Vector3d casterPos,
			LivingEntity target, Vector3d targetPos,
			SpellComponentWrapper flavor, boolean isNegative, float compParam) {
		if (world == null && target != null) {
			world = target.world;
		}
		
		if (world != null) {
			if (!world.isRemote) {
				super.spawnEffect(world, comp, caster, casterPos, target, targetPos, flavor, isNegative, compParam);
				return;
			}
		}
		if (targetPos == null)
//			if (target != null)
//				targetPos = target.getPositionVec();
//			else
				targetPos = new Vector3d(0, 0, 0);
		
		this.effectRenderer.spawnEffect(comp, caster, casterPos, target, targetPos, flavor, isNegative, compParam);
	}
	
	@Override
	public void updateEntityEffect(ServerPlayerEntity player, LivingEntity entity, SpecialEffect effectType, EffectData data) {
		return;
	}
	
	private static boolean shownText = false;
	@SubscribeEvent
	public void onClientConnect(EntityJoinWorldEvent event) {
		if (ClientProxy.shownText == false && ModConfig.config.displayLoginText()
				&& event.getEntity() == Minecraft.getInstance().player) {
			final Minecraft mc = Minecraft.getInstance();
			final String translated = I18n.format(this.bindingInfo.getTranslationKey());
			mc.player.sendMessage(
					new TranslationTextComponent("info.nostrumwelcome.text", new Object[]{
							translated
					}), Util.DUMMY_UUID);
			ClientProxy.shownText = true;
		}
		
		if (event.getWorld() != null && event.getWorld().isRemote() && event.getEntity() instanceof PlayerEntity) {
			NostrumMagica.instance.proxy.requestStats((PlayerEntity) event.getEntity());
		}
	}
	
	@Override
	public void sendMana(PlayerEntity player) {
		if (player.world.isRemote) {
			return;
		}
		
		super.sendMana(player);
	}
	
	@Override
	public void sendManaArmorCapability(PlayerEntity player) {
		if (player.world.isRemote) {
			return;
		}
		
		super.sendManaArmorCapability(player);
	}
	
	@Override
	public void receiveManaArmorOverride(@Nonnull Entity ent, IManaArmor override) {
		@Nullable IManaArmor existing = NostrumMagica.getManaArmor(ent);
		if (existing != null) {
			existing.copy(override);
		}
	}
	
	@Override
	public void playRitualEffect(World world, BlockPos pos, EMagicElement element,
			ItemStack center, @Nullable NonNullList<ItemStack> extras, ReagentType[] types, ItemStack output) {
		if (world.isRemote) {
			return;
		}
		
		super.playRitualEffect(world, pos, element, center, extras, types, output);
	}
}
