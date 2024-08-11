package com.smanzana.nostrummagica.init;

import java.util.Map;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.MimicBlock;
import com.smanzana.nostrummagica.block.dungeon.TogglePlatformBlock;
import com.smanzana.nostrummagica.client.RainbowItemColor;
import com.smanzana.nostrummagica.client.effects.ClientEffectIcon;
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
import com.smanzana.nostrummagica.client.gui.widget.QuickMoveBagButton;
import com.smanzana.nostrummagica.client.model.MimicBlockBakedModel;
import com.smanzana.nostrummagica.client.model.ModelDragonRed;
import com.smanzana.nostrummagica.client.model.ModelGolem;
import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.render.IEffectRenderer;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.client.render.SpellShapeRenderer;
import com.smanzana.nostrummagica.client.render.effect.CursedFireEffectRenderer;
import com.smanzana.nostrummagica.client.render.effect.EffectBubbleRenderer;
import com.smanzana.nostrummagica.client.render.effect.EffectGemRenderer;
import com.smanzana.nostrummagica.client.render.entity.RenderArcaneWolf;
import com.smanzana.nostrummagica.client.render.entity.RenderCursedGlassTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonEgg;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonRedPart;
import com.smanzana.nostrummagica.client.render.entity.RenderEnderRodBall;
import com.smanzana.nostrummagica.client.render.entity.RenderGolem;
import com.smanzana.nostrummagica.client.render.entity.RenderHookShot;
import com.smanzana.nostrummagica.client.render.entity.RenderKeySwitchTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderKoid;
import com.smanzana.nostrummagica.client.render.entity.RenderLux;
import com.smanzana.nostrummagica.client.render.entity.RenderMagicProjectile;
import com.smanzana.nostrummagica.client.render.entity.RenderMagicSaucer;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBoss;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBossBody;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBossBramble;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBossLeaf;
import com.smanzana.nostrummagica.client.render.entity.RenderShadowDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderShrineTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderSpellBubble;
import com.smanzana.nostrummagica.client.render.entity.RenderSpellBullet;
import com.smanzana.nostrummagica.client.render.entity.RenderSpellMortar;
import com.smanzana.nostrummagica.client.render.entity.RenderSpellProjectile;
import com.smanzana.nostrummagica.client.render.entity.RenderSprite;
import com.smanzana.nostrummagica.client.render.entity.RenderSwitchTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderTameDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderWillo;
import com.smanzana.nostrummagica.client.render.entity.RenderWisp;
import com.smanzana.nostrummagica.client.render.item.SpellPatternTomeRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityAltarRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityCandleRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityDungeonDoorRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityDungeonKeyChestRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityLockedChestRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityLockedDoorRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityManaArmorerRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityObeliskRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityPortalRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityProgressionDoorRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityTrialRenderer;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ChakramSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.CyclerSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEarthGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEnderGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicFireGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicIceGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicLightningGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicPhysicalGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicWindGolemEntity;
import com.smanzana.nostrummagica.fluid.NostrumFluids;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.NostrumItems;
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
import com.smanzana.nostrummagica.item.equipment.ThanosStaff;
import com.smanzana.nostrummagica.proxy.ClientProxy;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.spellcraft.pattern.NostrumSpellCraftPatterns;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.util.Curves;
import com.smanzana.nostrummagica.util.Curves.ICurve3d;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.AreaEffectCloudRenderer;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
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
		//ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.SymbolTileEntityType, TileEntitySymbolRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.TrialBlockEntityType, TileEntityTrialRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.CandleTileEntityType, TileEntityCandleRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.AltarTileEntityType, TileEntityAltarRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.NostrumObeliskEntityType, TileEntityObeliskRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.TeleportationPortalTileEntityType, TileEntityPortalRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.ObeliskPortalTileEntityType, TileEntityPortalRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.SorceryPortalTileEntityType, TileEntityPortalRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.TemporaryPortalTileEntityType, TileEntityPortalRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.ProgressionDoorTileEntityType, TileEntityProgressionDoorRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.ManaArmorerTileEntityType, TileEntityManaArmorerRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.LockedChestEntityType, TileEntityLockedChestRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.LockedDoorType, TileEntityLockedDoorRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.DungeonKeyChestTileEntityType, TileEntityDungeonKeyChestRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.DungeonDoorTileEntityType, TileEntityDungeonDoorRenderer::new);
		
		ScreenManager.registerFactory(NostrumContainers.ActiveHopper, ActiveHopperGui.ActiveHopperGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.LoreTable, LoreTableGui.LoreTableGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.ModificationTable, ModificationTableGui.ModificationGui::new);
		ScreenManager.registerFactory(NostrumContainers.Putter, PutterBlockGui.PutterBlockGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.ReagentBag, ReagentBagGui.BagGui::new);
		ScreenManager.registerFactory(NostrumContainers.RuneBag, RuneBagGui.BagGui::new);
		ScreenManager.registerFactory(NostrumContainers.SpellCreationMaster, MasterSpellCreationGui.SpellGui::new);
		ScreenManager.registerFactory(NostrumContainers.SpellCreationBasic, BasicSpellCraftGui.BasicSpellCraftGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.RuneShaper, RuneShaperGui.RuneShaperGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.SpellCreationRedwood, RedwoodSpellCraftGui.Gui::new);
		ScreenManager.registerFactory(NostrumContainers.SpellCreationMystic, MysticSpellCraftGui.Gui::new);
		ScreenManager.registerFactory(NostrumContainers.RuneLibrary, RuneLibraryGui.Gui::new);
		ScreenManager.registerFactory(NostrumContainers.Launcher, LauncherBlockGui.LauncherBlockGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.SilverMirror, SilverMirrorGui.MirrorGui::new);
		
		// Could probably make this be the default!
		ISpellCraftPatternRenderer.RegisterRenderer(NostrumSpellCraftPatterns.lightweight, SpellCraftPatternAutoRenderer.INSTANCE);
		
		// Register client command registering command.
		// Note that it's on the game event bus, so it has to be registered special
		MinecraftForge.EVENT_BUS.addListener(ClientInit::registerCommands);
		
		MinecraftForge.EVENT_BUS.addListener(QuickMoveBagButton::OnContainerScreenShow);
		
		registerBlockRenderLayer();
		registerEntityRenderers();
		
		event.enqueueWork(ClientInit::registerItemModelProperties);
		
    	ElementalArmor.ClientInit();
    	
    	ClientProxy proxy = (ClientProxy) NostrumMagica.instance.proxy;
		proxy.initKeybinds();
    	proxy.initDefaultEffects();
    	registerSpellShapeRenderers();
    	registerEffectRenderers();
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
				ModelLoader.addSpecialModel(NostrumMagica.Loc(modelLoc));
			}
		}
		
		for (String key : new String[] {"block/orb_crystal", "entity/orb", "entity/sprite_core", "entity/sprite_arms", "entity/magic_saucer", "entity/koid"}) {
			ModelLoader.addSpecialModel(NostrumMagica.Loc(key));
		}
		
		for (ResourceLocation loc : ModelDragonRed.getModelParts()) {
			ModelLoader.addSpecialModel(loc);
		}
		
		ModelLoader.addSpecialModel(SpellPatternTomeRenderer.BASE_MODEL);
	}
	
	@SubscribeEvent
	public static void registerColorHandlers(ColorHandlerEvent.Item ev) {
		IItemColor tinter = new IItemColor() {
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
		IBlockColor tinter = new IBlockColor() {
			@Override
			public int getColor(BlockState state, IBlockDisplayReader world, BlockPos pos, int tintIndex) {
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
	}
	
	private static final void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.spellProjectile, (manager) -> new RenderSpellProjectile(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.spellBullet, (manager) -> new RenderSpellBullet(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemEarth, (manager) -> new RenderGolem<MagicEarthGolemEntity>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemFire, (manager) -> new RenderGolem<MagicFireGolemEntity>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemIce, (manager) -> new RenderGolem<MagicIceGolemEntity>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemLightning, (manager) -> new RenderGolem<MagicLightningGolemEntity>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemEnder, (manager) -> new RenderGolem<MagicEnderGolemEntity>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemPhysical, (manager) -> new RenderGolem<MagicPhysicalGolemEntity>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemWind, (manager) -> new RenderGolem<MagicWindGolemEntity>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.koid, (manager) -> new RenderKoid(manager, .3f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.dragonRed, (manager) ->  new RenderDragonRed<RedDragonEntity>(manager, 5));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.dragonRedBodyPart, (manager) -> new RenderDragonRedPart(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.tameDragonRed, (manager) -> new RenderTameDragonRed(manager, 2));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.shadowDragonRed, (manager) -> new RenderShadowDragonRed(manager, 2));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.sprite, (manager) -> new RenderSprite(manager, .7f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.dragonEgg, (manager) -> new RenderDragonEgg(manager, .45f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.chakramSpellSaucer, (manager) -> new RenderMagicSaucer<ChakramSpellSaucerEntity>(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.cyclerSpellSaucer, (manager) -> new RenderMagicSaucer<CyclerSpellSaucerEntity>(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.switchTrigger, (manager) -> new RenderSwitchTrigger(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.keySwitchTrigger, (manager) -> new RenderKeySwitchTrigger(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.tameLightning, (manager) -> new LightningBoltRenderer(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.hookShot, (manager) -> new RenderHookShot(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.wisp, (manager) -> new RenderWisp(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.lux, (manager) -> new RenderLux(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.willo, (manager) -> new RenderWillo(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.arcaneWolf, (manager) -> new RenderArcaneWolf(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.plantBoss, (manager) -> new RenderPlantBoss(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.plantBossLeaf, (manager) -> new RenderPlantBossLeaf(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.spellMortar, (manager) -> new RenderSpellMortar(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.plantBossBramble, (manager) -> new RenderPlantBossBramble(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.plantBossBody, (manager) -> new RenderPlantBossBody(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.enderRodBall, (manager) -> new RenderEnderRodBall(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.areaEffect, (manager) -> new AreaEffectCloudRenderer(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.spellBubble, (manager) -> new RenderSpellBubble(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.elementShrine, (manager) -> new RenderShrineTrigger.Element(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.shapeShrine, (manager) -> new RenderShrineTrigger.Shape(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.alterationShrine, (manager) -> new RenderShrineTrigger.Alteration(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.tierShrine, (manager) -> new RenderShrineTrigger.Tier(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.magicDamageProjectile, (manager) -> new RenderMagicProjectile(manager, .5f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.cursedGlassTrigger, (manager) -> new RenderCursedGlassTrigger(manager));
	}
	
	@SubscribeEvent
	public static void registerClientParticleFactories(ParticleFactoryRegisterEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		ParticleManager manager = mc.particles;
		
		for (NostrumParticles particle : NostrumParticles.values()) {
			manager.registerFactory(particle.getType(), new IParticleFactory<NostrumParticleData>() {
				@Override
				public Particle makeParticle(NostrumParticleData typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
					return particle.getFactory().createParticle(worldIn, typeIn.getParams());
				}
			});
		}
	}
	
	private static final void registerBlockRenderLayer() {
		RenderTypeLookup.setRenderLayer(NostrumBlocks.activeHopper, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.candle, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.chalk, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.cursedIce, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.dungeonAir, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.itemDuct, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.lockedChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.magicWall, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.maniCrystalBlock, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.kaniCrystalBlock, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.vaniCrystalBlock, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mimicDoor, (t) -> true);//RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mimicDoorUnbreakable, (t) -> true);//RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mimicFacade, (t) -> true);//RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mimicFacadeUnbreakable, (t) -> true);//RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mineBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.obelisk, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.singleSpawner, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.matchSpawner, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.triggeredMatchSpawner, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.paradoxMirror, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.spellTable, RenderType.getCutout());
		//RenderTypeLookup.setRenderLayer(NostrumBlocks.switchBlock, RenderType.getTranslucent());
		//RenderTypeLookup.setRenderLayer(NostrumBlocks.keySwitch, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.teleportRune, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.triggerRepeater, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.midnightIris, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.crystabloom, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mandrakeCrop, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.ginsengCrop, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.essenceCrop, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.poisonWaterBlock, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.unbreakablePoisonWaterBlock, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumFluids.poisonWater, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumFluids.poisonWaterFlowing, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumFluids.unbreakablePoisonWater, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumFluids.unbreakablePoisonWaterFlowing, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.manaArmorerBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.dungeonBars, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.basicSpellTable, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.advancedSpellTable, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mysticSpellTable, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.runeLibrary, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.cursedFire, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumFluids.mysticWater, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.mysticWaterBlock, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.toggleDoor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.togglePlatform, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.cursedGlass, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.smallDungeonKeyChest, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.largeDungeonKeyChest, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.smallDungeonDoor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(NostrumBlocks.largeDungeonDoor, RenderType.getCutout());
	}
	
	private static final void registerItemModelProperties() {
		ItemModelsProperties.registerProperty(NostrumItems.enderRod, ChargingSwordItem.PROPERTY_CHARGE, AspectedEnderWeapon::ModelCharge);
		ItemModelsProperties.registerProperty(NostrumItems.enderRod, ChargingSwordItem.PROPERTY_CHARGING, AspectedEnderWeapon::ModelCharging);
		ItemModelsProperties.registerProperty(NostrumItems.flameRod, ChargingSwordItem.PROPERTY_CHARGE, AspectedFireWeapon::ModelCharge);
		ItemModelsProperties.registerProperty(NostrumItems.flameRod, ChargingSwordItem.PROPERTY_CHARGING, AspectedFireWeapon::ModelCharging);
		ItemModelsProperties.registerProperty(NostrumItems.deepMetalAxe, NostrumMagica.Loc("blocking"), AspectedPhysicalWeapon::ModelBlocking);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotWeak, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotMedium, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotStrong, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotClaw, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.mageBlade, NostrumMagica.Loc("element"), MageBlade::ModelElement);
		ItemModelsProperties.registerProperty(NostrumItems.mirrorShield, new ResourceLocation("blocking"), MirrorShield::ModelBlocking);
		ItemModelsProperties.registerProperty(NostrumItems.mirrorShieldImproved, new ResourceLocation("blocking"), MirrorShield::ModelBlocking);
		ItemModelsProperties.registerProperty(NostrumItems.mirrorShieldImproved, NostrumMagica.Loc("charged"), MirrorShieldImproved::ModelCharged);
		ItemModelsProperties.registerProperty(NostrumItems.soulDagger, ChargingSwordItem.PROPERTY_CHARGE, SoulDagger::ModelCharge);
		ItemModelsProperties.registerProperty(NostrumItems.soulDagger, ChargingSwordItem.PROPERTY_CHARGING, SoulDagger::ModelCharging);
		ItemModelsProperties.registerProperty(NostrumItems.thanosStaff, NostrumMagica.Loc("activated"), ThanosStaff::ModelActivated);
		ItemModelsProperties.registerProperty(NostrumItems.casterWand, ChargingSwordItem.PROPERTY_CHARGE, CasterWandItem::ModelCharge);
		ItemModelsProperties.registerProperty(NostrumItems.casterWand, ChargingSwordItem.PROPERTY_CHARGING, CasterWandItem::ModelCharging);
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public void stitchEventPre(TextureStitchEvent.Pre event) {
		if(event.getMap().getTextureLocation() != AtlasTexture.LOCATION_BLOCKS_TEXTURE) {
			return;
		}
		
		// We have to request loading textures that aren't explicitly loaded by any of the normal registered models.
		// That means entity OBJ models, or textures we load on the fly, etc.
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/koid"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/golem_ender"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/dragon_c"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/dragon_scales"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/dragon_scales_gold"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/dragon_scales_diamond"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/sprite_core"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/sprite_arms"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/magic_blade"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "block/portal"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/blade"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/hilt"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/ruby"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/wood"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/white"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/crystal"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/crystal_blank"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/dragonflightwing"));
		
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/block/chain_link"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/block/lock_plate"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "block/key_cage"));

		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/mist_bad"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/mist_good"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/thornskin"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting1"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting2"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting3"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting4"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting5"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/shield"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/arrow_down"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/arrow_up"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/slate"));
		event.addSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/arrow_slash"));
    }
	
	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
    	// Mimic blocks special model
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicDoor);
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicDoorUnbreakable);
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicFacade);
    	putMimicBlockModel(event.getModelRegistry(), NostrumBlocks.mimicFacadeUnbreakable);
	}
	
	private static void putMimicBlockModel(Map<ResourceLocation, IBakedModel> registry, Block block) {
		for (BlockState state : block.getStateContainer().getValidStates()) {
			ModelResourceLocation loc = BlockModelShapes.getModelLocation(state);
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
				IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
				matrixStackIn.push();
				matrixStackIn.translate(selected.getX() + .5, selected.getY() + .5, selected.getZ() + .5);
				matrixStackIn.scale(1.005f, 1.005f, 1.005f);
				RenderFuncs.drawUnitCubeOutline(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
				matrixStackIn.pop();
				
				// Hit as small cube
				buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
				matrixStackIn.push();
				matrixStackIn.translate(hit.getX() + .5, hit.getY() + .5, hit.getZ() + .5);
				matrixStackIn.scale(.5f, .5f, .5f);
				RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
				matrixStackIn.pop();
			} else {
				final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
				matrixStackIn.push();
				matrixStackIn.translate(selected.getX() + .5, selected.getY() + .5, selected.getZ() + .5);
				RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
				matrixStackIn.pop();
			}
		});
		
		// ENT done in renderer itself

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.LINE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vector3d start = comp.getStart();
			final Vector3d end = comp.getEnd();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
			RenderFuncs.renderLine(matrixStackIn, buffer, start, end, 10, combinedOverlay, combinedLight, red, green, blue, alpha);
		});

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.AOE_LINE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vector3d start = comp.getStart();
			final Vector3d end = comp.getEnd();
			final float width = comp.getWidth();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES_THICK);
			RenderFuncs.renderLine(matrixStackIn, buffer, start, end, width, combinedOverlay, combinedLight, red, green, blue, alpha);
		});

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.CURVE, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vector3d start = comp.getStart();
			final ICurve3d curve = comp.getCurve();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
//			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_LINES);
//			RenderFuncs.renderCurve(matrixStackIn, buffer, start, curve, 50, combinedOverlay, combinedLight, red, green, blue, alpha);
			
			final float period =  500f;
			final float prog = (float) (System.currentTimeMillis() % (double) period) / period;
			
			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_ORB_CHAIN);
			RenderFuncs.renderHorizontalRibbon(matrixStackIn, buffer, start, curve, 50, .2f, prog, combinedOverlay, combinedLight, red, green, blue, alpha);
		});

		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.DISK, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vector3d start = comp.getStart();
			final float radius = comp.getRadius();
			final int combinedLight = 15728880;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final float period =  500f;
			final float prog = (float) (System.currentTimeMillis() % (double) period) / period;
			
			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_ORB_CHAIN);
			final ICurve3d curve = new Curves.FlatEllipse(radius, radius);
			RenderFuncs.renderVerticalRibbon(matrixStackIn, buffer, start, curve, 50, .2f, prog, combinedOverlay, combinedLight, red, green, blue, alpha);
		});
		
		SpellShapeRenderer.RegisterRenderer(SpellShapePreviewComponent.BOX, (matrixStackIn, bufferIn, partialTicks, comp, red, green, blue, alpha) -> {
			final Vector3d start = comp.getStart();
			final Vector3d end = comp.getEnd();
			final Vector3d halfdiff = end.subtract(start).add(1, 1, 1).scale(.5f);
			
			final int combinedLight = RenderFuncs.BrightPackedLight;
			final int combinedOverlay = OverlayTexture.NO_OVERLAY;
			
			final IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.SPELLSHAPE_QUADS);
			matrixStackIn.push();
			matrixStackIn.translate(start.getX() + halfdiff.getX(), start.getY() + halfdiff.getY(), start.getZ() + halfdiff.getZ());
			matrixStackIn.scale((float) halfdiff.getX() * 2, (float) halfdiff.getY() * 2, (float) halfdiff.getZ() * 2);
			RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLight, combinedOverlay, red, green, blue, alpha);
			matrixStackIn.pop();
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
	
	private static final void registerEffectBubbleRenderer(Effect effect) {
		IEffectRenderer.RegisterRenderer(effect, new EffectBubbleRenderer(effect));
	}
	
	private static final void registerEffectGemRenderer(Effect effect) {
		IEffectRenderer.RegisterRenderer(effect, new EffectGemRenderer(effect));
	}
	
	public static final SpellPatternTomeRenderer makeSpellPatternTomeRenderer() {
		return SpellPatternTomeRenderer.INSTANCE;
	}
}
