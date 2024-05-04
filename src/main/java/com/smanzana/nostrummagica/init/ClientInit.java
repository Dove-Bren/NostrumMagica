package com.smanzana.nostrummagica.init;

import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.client.effects.ClientEffectIcon;
import com.smanzana.nostrummagica.client.gui.container.ActiveHopperGui;
import com.smanzana.nostrummagica.client.gui.container.LoreTableGui;
import com.smanzana.nostrummagica.client.gui.container.ModificationTableGui;
import com.smanzana.nostrummagica.client.gui.container.NostrumContainers;
import com.smanzana.nostrummagica.client.gui.container.PutterBlockGui;
import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.client.gui.container.RuneBagGui;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetGUIContainer;
import com.smanzana.nostrummagica.client.model.MimicBlockBakedModel;
import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.render.entity.ModelDragonRed;
import com.smanzana.nostrummagica.client.render.entity.ModelGolem;
import com.smanzana.nostrummagica.client.render.entity.RenderArcaneWolf;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonEgg;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonRedPart;
import com.smanzana.nostrummagica.client.render.entity.RenderEnderRodBall;
import com.smanzana.nostrummagica.client.render.entity.RenderGolem;
import com.smanzana.nostrummagica.client.render.entity.RenderHookShot;
import com.smanzana.nostrummagica.client.render.entity.RenderKeySwitchTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderKoid;
import com.smanzana.nostrummagica.client.render.entity.RenderLux;
import com.smanzana.nostrummagica.client.render.entity.RenderMagicSaucer;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBoss;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBossBody;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBossBramble;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBossLeaf;
import com.smanzana.nostrummagica.client.render.entity.RenderShadowDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderSpellBullet;
import com.smanzana.nostrummagica.client.render.entity.RenderSpellMortar;
import com.smanzana.nostrummagica.client.render.entity.RenderSpellProjectile;
import com.smanzana.nostrummagica.client.render.entity.RenderSprite;
import com.smanzana.nostrummagica.client.render.entity.RenderSwitchTrigger;
import com.smanzana.nostrummagica.client.render.entity.RenderTameDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderWillo;
import com.smanzana.nostrummagica.client.render.entity.RenderWisp;
import com.smanzana.nostrummagica.client.render.tile.TileEntityAltarRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityCandleRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityLockedChestRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityManaArmorerRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityObeliskRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityPortalRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityProgressionDoorRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntitySymbolRenderer;
import com.smanzana.nostrummagica.command.CommandDebugEffect;
import com.smanzana.nostrummagica.command.CommandInfoScreenGoto;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.items.AspectedEnderWeapon;
import com.smanzana.nostrummagica.items.AspectedFireWeapon;
import com.smanzana.nostrummagica.items.AspectedPhysicalWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.MageBlade;
import com.smanzana.nostrummagica.items.MagicArmor;
import com.smanzana.nostrummagica.items.MirrorShield;
import com.smanzana.nostrummagica.items.MirrorShieldImproved;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.SoulDagger;
import com.smanzana.nostrummagica.items.ThanosStaff;
import com.smanzana.nostrummagica.proxy.ClientProxy;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.tiles.NostrumTileEntities;

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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
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
 * Common (client and server) handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientInit {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.SymbolTileEntityType, TileEntitySymbolRenderer::new);
		ClientRegistry.bindTileEntityRenderer(NostrumTileEntities.TrialBlockEntityType, TileEntitySymbolRenderer::new);
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
		
		ScreenManager.registerFactory(NostrumContainers.ActiveHopper, ActiveHopperGui.ActiveHopperGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.LoreTable, LoreTableGui.LoreTableGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.ModificationTable, ModificationTableGui.ModificationGui::new);
		ScreenManager.registerFactory(NostrumContainers.Putter, PutterBlockGui.PutterBlockGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.ReagentBag, ReagentBagGui.BagGui::new);
		ScreenManager.registerFactory(NostrumContainers.RuneBag, RuneBagGui.BagGui::new);
		ScreenManager.registerFactory(NostrumContainers.SpellCreation, SpellCreationGui.SpellGui::new);
		ScreenManager.registerFactory(NostrumContainers.PetGui, new PetGUIFactory());
		ScreenManager.registerFactory(NostrumContainers.RuneShaper, RuneShaperGui.RuneShaperGuiContainer::new);
		
		// Register client command registering command.
		// Note that it's on the game event bus, so it has to be registered special
		MinecraftForge.EVENT_BUS.addListener(ClientInit::registerCommands);
		
		registerBlockRenderLayer();
		registerEntityRenderers();
		
		event.enqueueWork(ClientInit::registerItemModelProperties);
		
    	MagicArmor.ClientInit();
    	
    	ClientProxy proxy = (ClientProxy) NostrumMagica.instance.proxy;
		proxy.initKeybinds();
    	proxy.initDefaultEffects();
	}
	
	// To get around bounds matching. D:
	protected static class PetGUIFactory<T extends IEntityPet> implements ScreenManager.IScreenFactory<PetGUI.PetContainer<T>, PetGUI.PetGUIContainer<T>> {

			@Override
			public PetGUIContainer<T> create(PetContainer<T> c, PlayerInventory p,
					ITextComponent n) {
				return new PetGUI.PetGUIContainer<T>(c, p, n);
			}
	}
	
	// Subscribed to game bus in #clientSetup
	public static final void registerCommands(RegisterCommandsEvent event) {
		// Client-only commands
		final CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		CommandInfoScreenGoto.register(dispatcher);
		CommandDebugEffect.register(dispatcher);
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
			if (!icon.getModelKey().endsWith(".obj")) {
				// json
//				final String modelLoc = "effect/" + icon.getModelKey();
//				IUnbakedModel model = event.getModelLoader().getUnbakedModel(NostrumMagica.Loc(modelLoc));
//				//IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(NostrumMagica.Loc(modelLoc), "Failed to get json model for " + modelLoc);
//				
//				HashSet<String> missingTextureErrors = new HashSet<>();
//				
//				if (model != null && model != ModelLoaderRegistry.getMissingModel()) {
//					model.getTextures(event.getModelLoader()::getUnbakedModel, missingTextureErrors);
//					IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
//					event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(NostrumMagica.Loc(modelLoc)), bakedModel);
//				} else {
//					model.getClass();
//				}
				//int unused; // why dont I need this?
				// Because we don't render them as models, we only load the texture and draw as billboard.
				// And loading random textures works for any texture even if it's not stitched into the texture atlas
			} else {
				//"effect/orb_cloudy", "effect/orb_scaled", "effects/cyl", 
				final String modelLoc = "effect/" + icon.getKey();
				ModelLoader.addSpecialModel(NostrumMagica.Loc(modelLoc));
			}
		}
		
		for (String key : new String[] {"block/orb_crystal", "entity/orb", "entity/sprite_core", "entity/sprite_arms", "entity/magic_saucer", "entity/koid"}) {
			ModelLoader.addSpecialModel(NostrumMagica.Loc(key));
		}
		
		for (ResourceLocation loc : ModelDragonRed.getModelParts()) {
			ModelLoader.addSpecialModel(loc);
		}
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
	}
	
	private static final void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.spellProjectile, (manager) -> new RenderSpellProjectile(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.spellBullet, (manager) -> new RenderSpellBullet(manager, 1f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemEarth, (manager) -> new RenderGolem<EntityGolemEarth>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemFire, (manager) -> new RenderGolem<EntityGolemFire>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemIce, (manager) -> new RenderGolem<EntityGolemIce>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemLightning, (manager) -> new RenderGolem<EntityGolemLightning>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemEnder, (manager) -> new RenderGolem<EntityGolemEnder>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemPhysical, (manager) -> new RenderGolem<EntityGolemPhysical>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.golemWind, (manager) -> new RenderGolem<EntityGolemWind>(manager, new ModelGolem<>(), .8f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.koid, (manager) -> new RenderKoid(manager, .3f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.dragonRed, (manager) ->  new RenderDragonRed<EntityDragonRed>(manager, 5));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.dragonRedBodyPart, (manager) -> new RenderDragonRedPart(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.tameDragonRed, (manager) -> new RenderTameDragonRed(manager, 2));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.shadowDragonRed, (manager) -> new RenderShadowDragonRed(manager, 2));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.sprite, (manager) -> new RenderSprite(manager, .7f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.dragonEgg, (manager) -> new RenderDragonEgg(manager, .45f));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.chakramSpellSaucer, (manager) -> new RenderMagicSaucer<EntityChakramSpellSaucer>(manager));
		RenderingRegistry.registerEntityRenderingHandler(NostrumEntityTypes.cyclerSpellSaucer, (manager) -> new RenderMagicSaucer<EntityCyclerSpellSaucer>(manager));
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
	}
	
	private static final void registerItemModelProperties() {
		ItemModelsProperties.registerProperty(NostrumItems.enderRod, NostrumMagica.Loc("charge"), AspectedEnderWeapon::ModelCharge);
		ItemModelsProperties.registerProperty(NostrumItems.enderRod, NostrumMagica.Loc("charging"), AspectedEnderWeapon::ModelCharging);
		ItemModelsProperties.registerProperty(NostrumItems.flameRod, NostrumMagica.Loc("charge"), AspectedFireWeapon::ModelCharge);
		ItemModelsProperties.registerProperty(NostrumItems.flameRod, NostrumMagica.Loc("charging"), AspectedFireWeapon::ModelCharging);
		ItemModelsProperties.registerProperty(NostrumItems.deepMetalAxe, NostrumMagica.Loc("blocking"), AspectedPhysicalWeapon::ModelBlocking);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotWeak, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotMedium, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotStrong, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.hookshotClaw, NostrumMagica.Loc("extended"), HookshotItem::ModelExtended);
		ItemModelsProperties.registerProperty(NostrumItems.mageBlade, NostrumMagica.Loc("element"), MageBlade::ModelElement);
		ItemModelsProperties.registerProperty(NostrumItems.mirrorShield, new ResourceLocation("blocking"), MirrorShield::ModelBlocking);
		ItemModelsProperties.registerProperty(NostrumItems.mirrorShieldImproved, new ResourceLocation("blocking"), MirrorShield::ModelBlocking);
		ItemModelsProperties.registerProperty(NostrumItems.mirrorShieldImproved, NostrumMagica.Loc("charged"), MirrorShieldImproved::ModelCharged);
		ItemModelsProperties.registerProperty(NostrumItems.soulDagger, NostrumMagica.Loc("charge"), SoulDagger::ModelCharge);
		ItemModelsProperties.registerProperty(NostrumItems.soulDagger, NostrumMagica.Loc("charging"), SoulDagger::ModelCharging);
		ItemModelsProperties.registerProperty(NostrumItems.thanosStaff, NostrumMagica.Loc("activated"), ThanosStaff::ModelActivated);
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
	public void onModelBake(ModelBakeEvent event) {
		// Going to try 'special model'  loading for these
//		for (ClientEffectIcon icon : ClientEffectIcon.values()) {
//			if (!icon.getModelKey().endsWith(".obj")) {
//				// json
////				final String modelLoc = "effect/" + icon.getModelKey();
////				IUnbakedModel model = event.getModelLoader().getUnbakedModel(NostrumMagica.Loc(modelLoc));
////				//IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(NostrumMagica.Loc(modelLoc), "Failed to get json model for " + modelLoc);
////				
////				HashSet<String> missingTextureErrors = new HashSet<>();
////				
////				if (model != null && model != ModelLoaderRegistry.getMissingModel()) {
////					model.getTextures(event.getModelLoader()::getUnbakedModel, missingTextureErrors);
////					IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
////					event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(NostrumMagica.Loc(modelLoc)), bakedModel);
////				} else {
////					model.getClass();
////				}
//			} else {
//				//"effect/orb_cloudy", "effect/orb_scaled", "effects/cyl", 
//				final String modelLoc = "effect/" + icon.getKey();
//				IUnbakedModel model = ModelLoaderRegistry.getModel(NostrumMagica.Loc(modelLoc), "Failed to get obj model for " + modelLoc);
//				
//				if (model != null && model instanceof OBJModel) {
//					IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
//					// Note: putting as ModelResourceLocation to match RenderObj. Note creating like the various RenderObj users do.
//					event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(NostrumMagica.Loc(modelLoc)), bakedModel);
//				}
//			}
//		}
//		
//		for (String key : new String[] {"block/orb_crystal",
//				"entity/orb", "entity/sprite_core", "entity/sprite_arms", "entity/magic_saucer", "entity/koid"}) {
//			IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(NostrumMagica.Loc(key + ".obj"), "Failed to get obj model for " + key);
//			
//			if (model != null && model instanceof OBJModel) {
//				IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
//				// Note: putting as ModelResourceLocation to match RenderObj. Note creating like the various RenderObj users do.
//				event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(NostrumMagica.Loc(key)), bakedModel);
//			}
//		}
//		
//		for (ResourceLocation loc : ModelDragonRed.getModelParts()) {
//			ResourceLocation fullLoc = new ResourceLocation(loc.getNamespace(), loc.getPath() + ".obj");
//			IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(fullLoc, "Failed to get obj model for " + fullLoc);
//			
//			if (model != null && model instanceof OBJModel) {
//				IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
//				// Note: putting as ModelResourceLocation to match RenderObj. Note creating like the various RenderObj users do.
//				event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(loc), bakedModel);
//			}
//		}
		
//    	// Warlock blade; put obj in place for item model
//    	{
//    		IUnbakedModel model = ModelLoaderRegistry.getModelOrMissing(NostrumMagica.Loc("item/warlock_sword.obj"));
//			if (model != null && model instanceof OBJModel) {
//				IBakedModel bakedInvModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(),
//                        new BasicState(model.getDefaultState(), true), DefaultVertexFormats.ITEM);
//				
//				final ImmutableMap<TransformType, TRSRTransformation> SWORD_TRANSFORMS = ImmutableMap.<TransformType, TRSRTransformation>builder()
//			            .put(TransformType.GUI, Transforms.convert(-3.2f, -4f, 0.f, 0, 0, -45f, .125f))
//			            .put(TransformType.GROUND, Transforms.convert(0, 0, 0, 0, 0, 0, .125f))
//			            .put(TransformType.FIXED, Transforms.convert(0, 0, 0, 0, 0, 0, .125f))
//			            .put(TransformType.THIRD_PERSON_RIGHT_HAND, Transforms.convert(0.4f, 0, 0.4f, 0, 90, 0, 1.6f * .125f))
//			            .put(TransformType.THIRD_PERSON_LEFT_HAND, Transforms.leftify(Transforms.convert(0.05f, 0, 0.05f, 0, 90, 0, 1.6f * .125f)))
//			            .put(TransformType.FIRST_PERSON_RIGHT_HAND, Transforms.convert(0, 0, 0, 0, 90, 15, .125f))
//			            .put(TransformType.FIRST_PERSON_LEFT_HAND, Transforms.leftify(Transforms.convert(0, 0, 0, 0, 90, 15, .125f)))
//			            .build();
//				
//                bakedInvModel = new PerspectiveMapWrapper(bakedInvModel, SWORD_TRANSFORMS);
//				
//				event.getModelRegistry().put(new ModelResourceLocation(NostrumMagica.MODID + ":warlock_sword", "inventory"), bakedInvModel);
//				
////				"transform": {
////		            "scale": 0.125,
////		            "thirdperson": {
////		                "scale": 1.6,
////		                "translation": [0.05, 0, 0.05],
////		                "rotation": [ { "x": 0 }, { "y": 90 }, { "z": 0} ]
////		            },
////		            "firstperson": {
////		                "rotation": [ { "x": 0 }, { "y": 90 }, { "z": 15} ]
////		            },
////		            "gui": {
////		                "rotation": [ { "x": 0 }, { "y": 0 }, { "z": -45} ],
////		                "translation": [-0.2, -0.25, 0]
////		            }
////		        }
//				
//				//event.getModelRegistry().put(new ModelResourceLocation("minecraft:item/stick", "inventory"), bakedModel);
//			}
//    	}
    	
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
}
