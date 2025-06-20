package com.smanzana.nostrummagica.init;

import java.util.Map;
import java.util.Objects;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ElementalCrystalBlock;
import com.smanzana.nostrummagica.block.ModificationTableBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.MagicBreakableBlock;
import com.smanzana.nostrummagica.block.dungeon.MagicBreakableContainerBlock;
import com.smanzana.nostrummagica.block.dungeon.MimicBlock;
import com.smanzana.nostrummagica.block.dungeon.TogglePlatformBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.RainbowItemColor;
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
import com.smanzana.nostrummagica.client.gui.ISpellCraftPatternRenderer;
import com.smanzana.nostrummagica.client.gui.SpellCraftPatternAutoRenderer;
import com.smanzana.nostrummagica.client.gui.container.ActiveHopperGui;
import com.smanzana.nostrummagica.client.gui.container.BasicSpellCraftGui;
import com.smanzana.nostrummagica.client.gui.container.LauncherBlockGui;
import com.smanzana.nostrummagica.client.gui.container.LoreTableGui;
import com.smanzana.nostrummagica.client.gui.container.MasterSpellCreationGui;
import com.smanzana.nostrummagica.client.gui.container.ModificationTableGui;
import com.smanzana.nostrummagica.client.gui.container.MysticSpellCraftGui;
import com.smanzana.nostrummagica.client.gui.container.NostrumContainers;
import com.smanzana.nostrummagica.client.gui.container.PutterBlockGui;
import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.client.gui.container.RedwoodSpellCraftGui;
import com.smanzana.nostrummagica.client.gui.container.RuneBagGui;
import com.smanzana.nostrummagica.client.gui.container.RuneLibraryGui;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.client.gui.container.SilverMirrorGui;
import com.smanzana.nostrummagica.client.gui.tooltip.AbsoluteTooltipComponent;
import com.smanzana.nostrummagica.client.gui.tooltip.ConfigurableHintTooltipComponent;
import com.smanzana.nostrummagica.client.gui.tooltip.EnchantableHintTooltipComponent;
import com.smanzana.nostrummagica.client.gui.tooltip.ImbuementTooltipComponent;
import com.smanzana.nostrummagica.client.gui.tooltip.LoreHintTooltipComponent;
import com.smanzana.nostrummagica.client.gui.tooltip.TransmutableHintTooltipComponent;
import com.smanzana.nostrummagica.client.gui.widget.QuickMoveBagButton;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.client.model.DragonRedModel;
import com.smanzana.nostrummagica.client.model.MimicBlockBakedModel;
import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.render.IEffectRenderer;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.client.render.SpellShapeRenderer;
import com.smanzana.nostrummagica.client.render.effect.CursedFireEffectRenderer;
import com.smanzana.nostrummagica.client.render.effect.EffectBubbleRenderer;
import com.smanzana.nostrummagica.client.render.effect.EffectGemRenderer;
import com.smanzana.nostrummagica.client.render.entity.ArcaneWolfRenderer;
import com.smanzana.nostrummagica.client.render.entity.CursedGlassTriggerRenderer;
import com.smanzana.nostrummagica.client.render.entity.DragonEggRenderer;
import com.smanzana.nostrummagica.client.render.entity.DragonRedPartRenderer;
import com.smanzana.nostrummagica.client.render.entity.DragonRedRenderer;
import com.smanzana.nostrummagica.client.render.entity.EnderRodBallRenderer;
import com.smanzana.nostrummagica.client.render.entity.GolemRenderer;
import com.smanzana.nostrummagica.client.render.entity.HookShotRenderer;
import com.smanzana.nostrummagica.client.render.entity.KeySwitchTriggerRenderer;
import com.smanzana.nostrummagica.client.render.entity.KoidRenderer;
import com.smanzana.nostrummagica.client.render.entity.LuxRenderer;
import com.smanzana.nostrummagica.client.render.entity.MagicProjectileRenderer;
import com.smanzana.nostrummagica.client.render.entity.MagicSaucerRenderer;
import com.smanzana.nostrummagica.client.render.entity.PlantBossBodyRenderer;
import com.smanzana.nostrummagica.client.render.entity.PlantBossBrambleRenderer;
import com.smanzana.nostrummagica.client.render.entity.PlantBossLeafRenderer;
import com.smanzana.nostrummagica.client.render.entity.PlantBossRenderer;
import com.smanzana.nostrummagica.client.render.entity.PlayerStatueRenderer;
import com.smanzana.nostrummagica.client.render.entity.SeekerSaucerRenderer;
import com.smanzana.nostrummagica.client.render.entity.ShadowDragonRedRenderer;
import com.smanzana.nostrummagica.client.render.entity.ShrineTriggerRenderer;
import com.smanzana.nostrummagica.client.render.entity.SpellBoulderRenderer;
import com.smanzana.nostrummagica.client.render.entity.SpellBubbleRenderer;
import com.smanzana.nostrummagica.client.render.entity.SpellBulletRenderer;
import com.smanzana.nostrummagica.client.render.entity.SpellMortarRenderer;
import com.smanzana.nostrummagica.client.render.entity.SpellProjectileRenderer;
import com.smanzana.nostrummagica.client.render.entity.SpriteRenderer;
import com.smanzana.nostrummagica.client.render.entity.SwitchTriggerRenderer;
import com.smanzana.nostrummagica.client.render.entity.TameDragonRedRenderer;
import com.smanzana.nostrummagica.client.render.entity.WhirlwindRenderer;
import com.smanzana.nostrummagica.client.render.entity.WilloRenderer;
import com.smanzana.nostrummagica.client.render.entity.WispRenderer;
import com.smanzana.nostrummagica.client.render.item.NostrumItemSpecialRenderer;
import com.smanzana.nostrummagica.client.render.tile.AltarBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.BreakContainerBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.CandleBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.DungeonDoorBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.DungeonKeyChestBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.LaserBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.LockedChestBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.LockedDoorBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.ManaArmorerBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.ObeliskBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.PortalBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.ProgressionDoorBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.PushBlockBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.SummonGhostBlockEntityRenderer;
import com.smanzana.nostrummagica.client.render.tile.TrialBlockEntityRenderer;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ChakramSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.CyclerSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.boss.reddragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEarthGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEnderGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicFireGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicIceGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicLightningGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicPhysicalGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicWindGolemEntity;
import com.smanzana.nostrummagica.fluid.NostrumFluids;
import com.smanzana.nostrummagica.inventory.tooltip.ConfigurableHintTooltip;
import com.smanzana.nostrummagica.inventory.tooltip.EnchantableHintTooltip;
import com.smanzana.nostrummagica.inventory.tooltip.ImbuementTooltip;
import com.smanzana.nostrummagica.inventory.tooltip.LoreHintTooltip;
import com.smanzana.nostrummagica.inventory.tooltip.TransmutableHintTooltip;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.api.ICrystalEnchantableItem;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.item.equipment.AspectedEnderWeapon;
import com.smanzana.nostrummagica.item.equipment.AspectedFireWeapon;
import com.smanzana.nostrummagica.item.equipment.AspectedPhysicalWeapon;
import com.smanzana.nostrummagica.item.equipment.CasterWandItem;
import com.smanzana.nostrummagica.item.equipment.ChargingSwordItem;
import com.smanzana.nostrummagica.item.equipment.HookshotItem;
import com.smanzana.nostrummagica.item.equipment.MageBlade;
import com.smanzana.nostrummagica.item.equipment.MirrorShield;
import com.smanzana.nostrummagica.item.equipment.MirrorShieldImproved;
import com.smanzana.nostrummagica.item.equipment.SoulDagger;
import com.smanzana.nostrummagica.item.equipment.SpellTome.TomeStyle;
import com.smanzana.nostrummagica.item.equipment.ThanosStaff;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.ItemImbuement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.Transmutation;
import com.smanzana.nostrummagica.spell.component.shapes.BeamShape;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.spellcraft.pattern.NostrumSpellCraftPatterns;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.util.Curves;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		MenuScreens.register(NostrumContainers.ActiveHopper, ActiveHopperGui.ActiveHopperGuiContainer::new);
		MenuScreens.register(NostrumContainers.LoreTable, LoreTableGui.LoreTableGuiContainer::new);
		MenuScreens.register(NostrumContainers.ModificationTable, ModificationTableGui.ModificationGui::new);
		MenuScreens.register(NostrumContainers.Putter, PutterBlockGui.PutterBlockGuiContainer::new);
		MenuScreens.register(NostrumContainers.ReagentBag, ReagentBagGui.BagGui::new);
		MenuScreens.register(NostrumContainers.RuneBag, RuneBagGui.BagGui::new);
		MenuScreens.register(NostrumContainers.SpellCreationMaster, MasterSpellCreationGui.SpellGui::new);
		MenuScreens.register(NostrumContainers.SpellCreationBasic, BasicSpellCraftGui.BasicSpellCraftGuiContainer::new);
		MenuScreens.register(NostrumContainers.RuneShaper, RuneShaperGui.RuneShaperGuiContainer::new);
		MenuScreens.register(NostrumContainers.SpellCreationRedwood, RedwoodSpellCraftGui.Gui::new);
		MenuScreens.register(NostrumContainers.SpellCreationMystic, MysticSpellCraftGui.Gui::new);
		MenuScreens.register(NostrumContainers.RuneLibrary, RuneLibraryGui.Gui::new);
		MenuScreens.register(NostrumContainers.Launcher, LauncherBlockGui.LauncherBlockGuiContainer::new);
		MenuScreens.register(NostrumContainers.SilverMirror, SilverMirrorGui.MirrorGui::new);
		
		// Could probably make this be the default!
		ISpellCraftPatternRenderer.RegisterRenderer(NostrumSpellCraftPatterns.lightweight, SpellCraftPatternAutoRenderer.INSTANCE);
		
		// Register client command registering command.
		// Note that it's on the game event bus, so it has to be registered special
		MinecraftForge.EVENT_BUS.addListener(ClientInit::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(QuickMoveBagButton::OnContainerScreenShow);
		MinecraftForge.EVENT_BUS.addListener(ClientInit::InjectTooltips);
		MinecraftForge.EVENT_BUS.addListener(AbsoluteTooltipComponent::CaptureTooltipDimensions);
		
		registerBlockRenderLayer();
		//registerEntityRenderers();
		
		event.enqueueWork(ClientInit::registerItemModelProperties);
		
    	ElementalArmor.ClientInit();
    	
    	ClientPlayerListener clientListener = ((ClientPlayerListener) NostrumMagica.playerListener);
    	clientListener.initKeybinds();
    	initDefaultEffects();
    	registerSpellShapeRenderers();
    	registerEffectRenderers();
    	
    	clientListener.getOverlayRenderer().registerLayers();
    	
    	MinecraftForgeClient.registerTooltipComponentFactory(EnchantableHintTooltip.class, EnchantableHintTooltipComponent::new);
    	MinecraftForgeClient.registerTooltipComponentFactory(ConfigurableHintTooltip.class, ConfigurableHintTooltipComponent::new);
    	MinecraftForgeClient.registerTooltipComponentFactory(TransmutableHintTooltip.class, TransmutableHintTooltipComponent::new);
    	MinecraftForgeClient.registerTooltipComponentFactory(LoreHintTooltip.class, LoreHintTooltipComponent::new);
    	MinecraftForgeClient.registerTooltipComponentFactory(ImbuementTooltip.class, ImbuementTooltipComponent::new);
    	
    }
	
	// Subscribed to game bus in #clientSetup
	public static final void registerCommands(RegisterCommandsEvent event) {
		// Client-only commands
		// Note: registered in common mod one to show up on server and be valid commands
//		final CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
//		CommandInfoScreenGoto.register(dispatcher);
//		CommandDebugEffect.register(dispatcher);
	}
	
	@SubscribeEvent
	public static void registerAllModels(ModelRegistryEvent event) {
		
		// Register models that don't have an item/block associate with them
		
		// I want to do this, but
		// 1) it only uses the JSON loaders, and
		// 2) it puts things in the registry with the original ResourceLocation, even though
		// there is no accessor that takes it.
//		for (ClientEffectIcon icon: ClientEffectIcon.values()) {
//			ModelLoader.addSpecialModel(new ModelResourceLocation(new ResourceLocation(
//					NostrumMagica.MODID, "effect/" + icon.getModelKey()), "")
//					);
//    	}
		
		
		for (ClientEffectIcon icon : ClientEffectIcon.values()) {
			if (icon.usesModel()) {
				//"effect/orb_cloudy", "effect/orb_scaled", "effects/cyl", 
				final String modelLoc = "effect/" + icon.getKey(); // This should be hardcoded somewhere else...
				ForgeModelBakery.addSpecialModel(NostrumMagica.Loc(modelLoc));
			}
		}
		
		for (String key : new String[] {"block/orb_crystal", "entity/orb", "entity/sprite_core", "entity/sprite_arms", "entity/magic_saucer", "entity/koid"}) {
			ForgeModelBakery.addSpecialModel(NostrumMagica.Loc(key));
		}
		
		for (ResourceLocation loc : DragonRedModel.getModelParts()) {
			ForgeModelBakery.addSpecialModel(loc);
		}
		
		ForgeModelBakery.addSpecialModel(NostrumItemSpecialRenderer.BASE_MODEL);
	}
	
	@SubscribeEvent
	public static void registerColorHandlers(ColorHandlerEvent.Item ev) {
		ItemColor tinter = new ItemColor() {
			@Override
			public int getColor(ItemStack stack, int tintIndex) {
				EMagicElement element = EssenceItem.findType(stack);
				return element.getColor();
			}
			
		};
		ev.getItemColors().register(tinter, NostrumItems.essenceEarth, NostrumItems.essenceEnder, NostrumItems.essenceFire,
				NostrumItems.essenceIce, NostrumItems.essenceLightning, NostrumItems.essencePhysical, NostrumItems.essenceWind
				);
		
		ev.getItemColors().register(new CasterWandItem.CasterWandColor(), NostrumItems.casterWand);
		ev.getItemColors().register(new RainbowItemColor(0), NostrumItems.koidHelm);
	}
	
	@SubscribeEvent
	public static void registerColorHandlers(ColorHandlerEvent.Block event) {
		BlockColor tinter = new BlockColor() {
			@Override
			public int getColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
				BlockState mimickedState = MimicBlock.getMirrorState(state, world, pos).orElse(null);
				
				if (mimickedState != null) {
					return event.getBlockColors().getColor(mimickedState, world, pos, tintIndex);
				} else {
					return -1;
				}
			}
			
		};
		
		event.getBlockColors().register(tinter, NostrumBlocks.mimicDoor, NostrumBlocks.mimicDoorUnbreakable, NostrumBlocks.mimicFacade,
				NostrumBlocks.mimicFacadeUnbreakable);
		
		event.getBlockColors().register(TogglePlatformBlock::MakePlatformColor, NostrumBlocks.togglePlatform);
		event.getBlockColors().register(MagicBreakableBlock::MakeBlockColor, NostrumBlocks.breakBlock);
		event.getBlockColors().register(MagicBreakableContainerBlock::MakeBlockColor, NostrumBlocks.breakContainerBlock);
		event.getBlockColors().register(ElementalCrystalBlock::MakeBlockColor, NostrumBlocks.elementalCrystal);
	}
	
	@SubscribeEvent
	public static final void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(NostrumEntityTypes.spellProjectile, (manager) -> new SpellProjectileRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.spellBullet, (manager) -> new SpellBulletRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.golemEarth, (manager) -> new GolemRenderer<MagicEarthGolemEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.golemFire, (manager) -> new GolemRenderer<MagicFireGolemEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.golemIce, (manager) -> new GolemRenderer<MagicIceGolemEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.golemLightning, (manager) -> new GolemRenderer<MagicLightningGolemEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.golemEnder, (manager) -> new GolemRenderer<MagicEnderGolemEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.golemPhysical, (manager) -> new GolemRenderer<MagicPhysicalGolemEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.golemWind, (manager) -> new GolemRenderer<MagicWindGolemEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.koid, (manager) -> new KoidRenderer(manager, .3f));
		event.registerEntityRenderer(NostrumEntityTypes.dragonRed, (manager) ->  new DragonRedRenderer<RedDragonEntity>(manager, 5));
		event.registerEntityRenderer(NostrumEntityTypes.dragonRedBodyPart, (manager) -> new DragonRedPartRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.tameDragonRed, (manager) -> new TameDragonRedRenderer(manager, 2));
		event.registerEntityRenderer(NostrumEntityTypes.shadowDragonRed, (manager) -> new ShadowDragonRedRenderer(manager, 2));
		event.registerEntityRenderer(NostrumEntityTypes.sprite, (manager) -> new SpriteRenderer(manager, .7f));
		event.registerEntityRenderer(NostrumEntityTypes.dragonEgg, (manager) -> new DragonEggRenderer(manager, .45f));
		event.registerEntityRenderer(NostrumEntityTypes.chakramSpellSaucer, (manager) -> new MagicSaucerRenderer<ChakramSpellSaucerEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.cyclerSpellSaucer, (manager) -> new MagicSaucerRenderer<CyclerSpellSaucerEntity>(manager));
		event.registerEntityRenderer(NostrumEntityTypes.switchTrigger, (manager) -> new SwitchTriggerRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.keySwitchTrigger, (manager) -> new KeySwitchTriggerRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.tameLightning, (manager) -> new LightningBoltRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.hookShot, (manager) -> new HookShotRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.wisp, (manager) -> new WispRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.lux, (manager) -> new LuxRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.willo, (manager) -> new WilloRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.arcaneWolf, (manager) -> new ArcaneWolfRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.plantBoss, (manager) -> new PlantBossRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.plantBossLeaf, (manager) -> new PlantBossLeafRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.spellMortar, (manager) -> new SpellMortarRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.plantBossBramble, (manager) -> new PlantBossBrambleRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.plantBossBody, (manager) -> new PlantBossBodyRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.enderRodBall, (manager) -> new EnderRodBallRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.areaEffect, NoopRenderer::new);//new AreaEffectCloudRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.spellBubble, (manager) -> new SpellBubbleRenderer(manager, 1f));
		event.registerEntityRenderer(NostrumEntityTypes.elementShrine, (manager) -> new ShrineTriggerRenderer.Element(manager));
		event.registerEntityRenderer(NostrumEntityTypes.shapeShrine, (manager) -> new ShrineTriggerRenderer.Shape(manager));
		event.registerEntityRenderer(NostrumEntityTypes.alterationShrine, (manager) -> new ShrineTriggerRenderer.Alteration(manager));
		event.registerEntityRenderer(NostrumEntityTypes.tierShrine, (manager) -> new ShrineTriggerRenderer.Tier(manager));
		event.registerEntityRenderer(NostrumEntityTypes.magicDamageProjectile, (manager) -> new MagicProjectileRenderer(manager, .5f));
		event.registerEntityRenderer(NostrumEntityTypes.cursedGlassTrigger, (manager) -> new CursedGlassTriggerRenderer(manager));
		event.registerEntityRenderer(NostrumEntityTypes.seekerSpellSaucer, SeekerSaucerRenderer::new);
		event.registerEntityRenderer(NostrumEntityTypes.boulder, SpellBoulderRenderer::new);
		event.registerEntityRenderer(NostrumEntityTypes.playerStatue, PlayerStatueRenderer::new);
		event.registerEntityRenderer(NostrumEntityTypes.whirlwind, WhirlwindRenderer::new);
	}
	
	@SubscribeEvent
	public static final void registerTileEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		//event.registerBlockEntityRenderer(NostrumTileEntities.SymbolTileEntityType, TileEntitySymbolRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.TrialBlock, TrialBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.Candle, CandleBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.Altar, AltarBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.NostrumObelisk, ObeliskBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.TeleportationPortal, PortalBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.ObeliskPortal, PortalBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.SorceryPortal, PortalBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.TemporaryPortal, PortalBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.ProgressionDoor, ProgressionDoorBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.ManaArmorer, ManaArmorerBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.LockedChest, LockedChestBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.LockedDoor, LockedDoorBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.DungeonKeyChest, DungeonKeyChestBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.DungeonDoor, DungeonDoorBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.PushBlock, PushBlockBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.BreakContainer, BreakContainerBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.SummonGhostBlock, SummonGhostBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(NostrumBlockEntities.Laser, LaserBlockEntityRenderer::new);
	}
	
	@SubscribeEvent
	public static void registerClientParticleFactories(ParticleFactoryRegisterEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		ParticleEngine manager = mc.particleEngine;
		
		for (NostrumParticles particle : NostrumParticles.values()) {
			manager.register(particle.getType(), (sprites) -> new ParticleProvider<NostrumParticleData>() {
				@Override
				public Particle createParticle(NostrumParticleData typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
					return particle.getFactory().createParticle(worldIn, sprites, typeIn.getParams());
				}
			});
		}
	}
	
	private static final void registerBlockRenderLayer() {
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.activeHopper, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.candle, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.chalk, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.cursedIce, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.dungeonAir, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.itemDuct, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.lockedChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.magicWall, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.maniCrystalBlock, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.kaniCrystalBlock, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.vaniCrystalBlock, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mimicDoor, (t) -> true);//RenderType.getCutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mimicDoorUnbreakable, (t) -> true);//RenderType.getCutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mimicFacade, (t) -> true);//RenderType.getCutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mimicFacadeUnbreakable, (t) -> true);//RenderType.getCutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mineBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.obelisk, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.singleSpawner, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.matchSpawner, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.triggeredMatchSpawner, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.paradoxMirror, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.spellTable, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.switchBlock, RenderType.cutout());
		//RenderTypeLookup.setRenderLayer(NostrumBlocks.keySwitch, RenderType.getTranslucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.teleportRune, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.triggerRepeater, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.midnightIris, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.crystabloom, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mandrakeCrop, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.ginsengCrop, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.essenceCrop, RenderType.cutoutMipped());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.poisonWaterBlock, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.unbreakablePoisonWaterBlock, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumFluids.poisonWater, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumFluids.poisonWaterFlowing, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumFluids.unbreakablePoisonWater, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumFluids.unbreakablePoisonWaterFlowing, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.manaArmorerBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.dungeonBars, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.basicSpellTable, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.advancedSpellTable, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mysticSpellTable, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.runeLibrary, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.cursedFire, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumFluids.mysticWater, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mysticWaterBlock, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.toggleDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.togglePlatform, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.cursedGlass, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.smallDungeonKeyChest, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.largeDungeonKeyChest, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.smallDungeonDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.largeDungeonDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.pushBlock, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.breakBlock, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.breakContainerBlock, RenderType.translucent());
		//ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.conjureGhostBlock, NostrumRenderTypes.COLORED_GHOSTBLOCK); // doens't actually render and we need tile entity
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.laser, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.elementalCrystal, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.pureWater, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumFluids.pureWater, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumFluids.pureWaterFlowing, RenderType.translucent());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.mageLight, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.pushPassthroughBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(NostrumBlocks.rootingAir, RenderType.translucent());
	}
	
	private static final void registerItemModelProperties() {
		ItemProperties.register(NostrumItems.enderRod, ChargingSwordItem.PROPERTY_CHARGE, AspectedEnderWeapon::ModelCharge);
		ItemProperties.register(NostrumItems.enderRod, ChargingSwordItem.PROPERTY_CHARGING, AspectedEnderWeapon::ModelCharging);
		ItemProperties.register(NostrumItems.flameRod, ChargingSwordItem.PROPERTY_CHARGE, AspectedFireWeapon::ModelCharge);
		ItemProperties.register(NostrumItems.flameRod, ChargingSwordItem.PROPERTY_CHARGING, AspectedFireWeapon::ModelCharging);
		ItemProperties.register(NostrumItems.deepMetalAxe, NostrumMagica.Loc("blocking"), AspectedPhysicalWeapon::ModelBlocking);
		ItemProperties.register(NostrumItems.hookshotWeak, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemProperties.register(NostrumItems.hookshotMedium, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemProperties.register(NostrumItems.hookshotStrong, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemProperties.register(NostrumItems.hookshotClaw, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemProperties.register(NostrumItems.mageBlade, NostrumMagica.Loc("element"), MageBlade::ModelElement);
		ItemProperties.register(NostrumItems.mirrorShield, new ResourceLocation("blocking"), MirrorShield::ModelBlocking);
		ItemProperties.register(NostrumItems.mirrorShieldImproved, new ResourceLocation("blocking"), MirrorShield::ModelBlocking);
		ItemProperties.register(NostrumItems.mirrorShieldImproved, NostrumMagica.Loc("charged"), MirrorShieldImproved::ModelCharged);
		ItemProperties.register(NostrumItems.soulDagger, ChargingSwordItem.PROPERTY_CHARGE, SoulDagger::ModelCharge);
		ItemProperties.register(NostrumItems.soulDagger, ChargingSwordItem.PROPERTY_CHARGING, SoulDagger::ModelCharging);
		ItemProperties.register(NostrumItems.thanosStaff, NostrumMagica.Loc("activated"), ThanosStaff::ModelActivated);
		ItemProperties.register(NostrumItems.casterWand, ChargingSwordItem.PROPERTY_CHARGE, CasterWandItem::ModelCharge);
		ItemProperties.register(NostrumItems.casterWand, ChargingSwordItem.PROPERTY_CHARGING, CasterWandItem::ModelCharging);
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void stitchEventPre(TextureStitchEvent.Pre event) {
		if(!Objects.equals(event.getAtlas().location(), TextureAtlas.LOCATION_BLOCKS)) {
			return;
		}
		
//		// We have to request loading textures that aren't explicitly loaded by any of the normal registered models.
//		// That means entity OBJ models, or textures we load on the fly, etc.
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/koid"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/golem_ender"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/dragon_c"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/armor/dragon_scales"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/armor/dragon_scales_gold"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/armor/dragon_scales_diamond"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/sprite_core"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/sprite_arms"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/magic_blade"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "block/portal"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/blade"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/hilt"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/ruby"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/wood"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/white"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/crystal"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/crystal_blank"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/dragonflightwing"));
//		
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/block/chain_link"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/block/lock_plate"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "block/key_cage"));
//
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/mist_bad"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/mist_good"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/thornskin"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/ting1"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/ting2"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/ting3"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/ting4"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/ting5"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/shield"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/arrow_down"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/arrow_up"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/slate"));
//		event.addSprite(new ResourceLocation(
//				NostrumMagica.MODID, "effects/arrow_slash"));
		
		// Tome renderer materials
		for (TomeStyle style : TomeStyle.values()) {
			event.addSprite(NostrumMagica.Loc("entity/spelltome_render_" + style.name().toLowerCase()));
		}
    }
	
	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
    	// Mimic blocks special model
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicDoor);
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicDoorUnbreakable);
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicFacade);
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicFacadeUnbreakable);
	}
	
	private static void putMimicBlockModel(Map<ResourceLocation, BakedModel> registry, Block block) {
		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			ModelResourceLocation loc = BlockModelShaper.stateToModelLocation(state);
			registry.put(loc, new MimicBlockBakedModel(registry.get(loc))); // Put a new mimic model wrapped around the default one
		}
	}
	
	private static final void registerSpellShapeRenderers() {
		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.BLOCKPOS, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final SpellLocation location = comp.getLocation();
			final BlockPos selected = location.selectedBlockPos;
			final BlockPos hit = location.hitBlockPos;
			
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			
			
			if (!selected.equals(hit)) {
				// Actual selected as outline
				VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
				matrixStackIn.pushPose();
				matrixStackIn.translate(selected.getX() + .5, selected.getY() + .5, selected.getZ() + .5);
				matrixStackIn.scale(1.005f, 1.005f, 1.005f);
				RenderFuncs.drawUnitCubeOutline(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
				matrixStackIn.popPose();
				
				// Hit as small cube
				buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
				matrixStackIn.pushPose();
				matrixStackIn.translate(hit.getX() + .5, hit.getY() + .5, hit.getZ() + .5);
				matrixStackIn.scale(.5f, .5f, .5f);
				RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
				matrixStackIn.popPose();
			} else {
				final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
				matrixStackIn.pushPose();
				matrixStackIn.translate(selected.getX() + .5, selected.getY() + .5, selected.getZ() + .5);
				RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
				matrixStackIn.popPose();
			}
		});
		
		// ENT done in renderer itself

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.LINE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vec3 start = comp.getStart();
			final Vec3 end = comp.getEnd();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
			RenderFuncs.renderLine(matrixStackIn, buffer, start, end, 10, combinedOverlay, combinedLight, red, green, blue, alpha);
		});

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.AOE_LINE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vec3 start = comp.getStart();
			final Vec3 end = comp.getEnd();
			final float width = comp.getWidth();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES_THICK);
			RenderFuncs.renderLine(matrixStackIn, buffer, start, end, width, combinedOverlay, combinedLight, red, green, blue, alpha);
		});

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.CURVE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vec3 start = comp.getStart();
			final ICurve3d curve = comp.getCurve();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
//			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
//			RenderFuncs.renderCurve(matrixStackIn, buffer, start, curve, 50, combinedOverlay, combinedLight, red, green, blue, alpha);
			
			final float period =  500f;
			final float prog = (float) (System.currentTimeMillis() % (double) period) / period;
			
			final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_ORB_CHAIN);
			RenderFuncs.renderHorizontalRibbon(matrixStackIn, buffer, start, curve, 50, .2f, prog, combinedOverlay, combinedLight, red, green, blue, alpha);
		});

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.DISK, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vec3 start = comp.getStart();
			final float radius = comp.getRadius();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final float period =  500f;
			final float prog = (float) (System.currentTimeMillis() % (double) period) / period;
			
			final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_ORB_CHAIN);
			final ICurve3d curve = new Curves.FlatEllipse(radius, radius);
			RenderFuncs.renderVerticalRibbon(matrixStackIn, buffer, start, curve, 50, .2f, prog, combinedOverlay, combinedLight, red, green, blue, alpha);
		});
		
		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.BOX, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vec3 start = comp.getStart();
			final Vec3 end = comp.getEnd();
			final Vec3 halfdiff = end.subtract(start).add(1, 1, 1).scale(.5f);
			
			final int combinedLight = RenderFuncs.BrightPackedLight;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
			matrixStackIn.pushPose();
			matrixStackIn.translate(start.x() + halfdiff.x(), start.y() + halfdiff.y(), start.z() + halfdiff.z());
			matrixStackIn.scale((float) halfdiff.x() * 2, (float) halfdiff.y() * 2, (float) halfdiff.z() * 2);
			RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
			matrixStackIn.popPose();
		});
	}
	
	private static final void registerEffectRenderers() {
		IEffectRenderer.RegisterRenderer(NostrumEffects.cursedFire, new CursedFireEffectRenderer());
		registerEffectBubbleRenderer(NostrumEffects.mysticWater);
		registerEffectBubbleRenderer(NostrumEffects.mysticAir);
		registerEffectBubbleRenderer(NostrumEffects.lootLuck);
		registerEffectBubbleRenderer(NostrumEffects.rend);
		registerEffectBubbleRenderer(NostrumEffects.magicRend);
		registerEffectBubbleRenderer(NostrumEffects.magicShield);
		registerEffectBubbleRenderer(NostrumEffects.physicalShield);
		registerEffectBubbleRenderer(NostrumEffects.soulDrain);
		registerEffectBubbleRenderer(NostrumEffects.sublimation);
		registerEffectBubbleRenderer(NostrumEffects.disruption);
		registerEffectBubbleRenderer(NostrumEffects.healResist);

		registerEffectGemRenderer(NostrumEffects.spellBoostEarth);
		registerEffectGemRenderer(NostrumEffects.spellBoostEnder);
		registerEffectGemRenderer(NostrumEffects.spellBoostFire);
		registerEffectGemRenderer(NostrumEffects.spellBoostIce);
		registerEffectGemRenderer(NostrumEffects.spellBoostLightning);
		registerEffectGemRenderer(NostrumEffects.spellBoostPhysical);
		registerEffectGemRenderer(NostrumEffects.spellBoostWind);
		
	}
	
	private static final void registerEffectBubbleRenderer(MobEffect effect) {
		IEffectRenderer.RegisterRenderer(effect, new EffectBubbleRenderer(effect));
	}
	
	private static final void registerEffectGemRenderer(MobEffect effect) {
		IEffectRenderer.RegisterRenderer(effect, new EffectGemRenderer(effect));
	}
	
	public static final NostrumItemSpecialRenderer makeSpellPatternTomeRenderer() {
		return NostrumItemSpecialRenderer.INSTANCE;
	}
	
	// Subscribed in client init
	public static final void InjectTooltips(RenderTooltipEvent.GatherComponents event) {
		// All of these require magic to be unlocked
		Minecraft mc = Minecraft.getInstance();
		INostrumMagic attr = NostrumMagica.getMagicWrapper(mc.player);
		if (attr == null || !attr.isUnlocked()) {
			return;
		}
		
		final ItemStack stack = event.getItemStack();
		if (ICrystalEnchantableItem.isEnchantable(stack)) {
			event.getTooltipElements().add(Either.right(new EnchantableHintTooltip()));
		}
		if (ModificationTableBlock.IsModifiable(stack)) {
			event.getTooltipElements().add(Either.right(new ConfigurableHintTooltip()));
		}
		if (Transmutation.IsTransmutable(stack.getItem())) {
			event.getTooltipElements().add(Either.right(new TransmutableHintTooltip()));
		}
		
		// Lore icon
		final ILoreTagged tag;
		if (stack.getItem() instanceof BlockItem) {
			if (!(((BlockItem) stack.getItem()).getBlock() instanceof ILoreTagged)) {
				tag = null;
			} else {
				tag = (ILoreTagged) ((BlockItem) stack.getItem()).getBlock();
			}
		} else if (!(stack.getItem() instanceof ILoreTagged)) {
			tag = null;
		} else {
			tag = (ILoreTagged) stack.getItem();
		}
		
		if (tag != null) {
			final LoreHintTooltip.LoreLevel level;
			if (attr.hasFullLore(tag)) {
				level = LoreHintTooltip.LoreLevel.FULL;
			} else if (attr.hasLore(tag)) {
				level = LoreHintTooltip.LoreLevel.BASIC;
			} else {
				level = LoreHintTooltip.LoreLevel.NONE;
			}
			
			event.getTooltipElements().add(Either.right(new LoreHintTooltip(level)));
		}
		
		if (Screen.hasShiftDown() && stack.is(NostrumTags.Items.SpellChanneling)) {
			event.getTooltipElements().add(Either.left(new TranslatableComponent("info.item.spellchanneling")));
		}
		
		ItemImbuement imbue = ItemImbuement.FromItemStack(stack);
		if (imbue != null) {
			event.getTooltipElements().add(Either.right(new ImbuementTooltip(imbue)));
		}
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
	
	private static final ClientEffect doExtractEffect(LivingEntity source,
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
	
	public static void initDefaultEffects() {
		ClientEffectRenderer renderer = ((ClientPlayerListener) NostrumMagica.playerListener).getEffectRenderer();
		
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
							BeamShape.BEAM_DURATION_TICKS + 10);
					
					if (source != null)
						effect.modify(new ClientEffectModifierFollow(source));
					
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
		
		renderer.registerEffect(EAlteration.HARM,
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

		renderer.registerEffect((EAlteration) null,
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
		
		renderer.registerEffect(EAlteration.CORRUPT, ClientInit::doCorruptEffect);
		renderer.registerEffect(EAlteration.EXTRACT, ClientInit::doExtractEffect);
	}
}
