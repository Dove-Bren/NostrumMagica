package com.smanzana.nostrummagica.proxy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumPortal.NostrumPortalTileEntityBase;
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
import com.smanzana.nostrummagica.client.gui.container.ActiveHopperGui;
import com.smanzana.nostrummagica.client.gui.container.LoreTableGui;
import com.smanzana.nostrummagica.client.gui.container.ModificationTableGui;
import com.smanzana.nostrummagica.client.gui.container.NostrumContainers;
import com.smanzana.nostrummagica.client.gui.container.PutterBlockGui;
import com.smanzana.nostrummagica.client.gui.container.ReagentBagGui;
import com.smanzana.nostrummagica.client.gui.container.RuneBagGui;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetGUIContainer;
import com.smanzana.nostrummagica.client.model.MimicBlockBakedModel;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.render.entity.ModelDragonRed;
import com.smanzana.nostrummagica.client.render.entity.ModelGolem;
import com.smanzana.nostrummagica.client.render.entity.RenderArcaneWolf;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonEgg;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonRedPart;
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
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.entity.EntityKeySwitchTrigger;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityLux;
import com.smanzana.nostrummagica.entity.EntitySpellBullet;
import com.smanzana.nostrummagica.entity.EntitySpellMortar;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.entity.EntitySprite;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.entity.EntityWillo;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;
import com.smanzana.nostrummagica.integration.jei.NostrumMagicaJEIPlugin;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.BladeCastMessage;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskSelectMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskTeleportationRequestMessage;
import com.smanzana.nostrummagica.network.messages.PetCommandMessage;
import com.smanzana.nostrummagica.network.messages.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.network.messages.StatRequestMessage;
import com.smanzana.nostrummagica.pet.PetPlacementMode;
import com.smanzana.nostrummagica.pet.PetTargetMode;
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
import com.smanzana.nostrummagica.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.tiles.CandleTileEntity;
import com.smanzana.nostrummagica.tiles.LockedChestEntity;
import com.smanzana.nostrummagica.tiles.ManaArmorerTileEntity;
import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.tiles.ProgressionDoorTileEntity;
import com.smanzana.nostrummagica.tiles.SymbolTileEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.RayTrace;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.ForgeBlockStateV1.Transforms;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@SuppressWarnings("deprecation")
public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingCast;
	private KeyBinding bindingScroll;
	private KeyBinding bindingInfo;
	private KeyBinding bindingBladeCast;
	private KeyBinding bindingPetPlacementModeCycle;
	private KeyBinding bindingPetTargetModeCycle;
	private KeyBinding bindingPetAttackAll;
	private KeyBinding bindingPetAttack;
	private KeyBinding bindingPetAllStop;
	private OverlayRenderer overlayRenderer;
	private ClientEffectRenderer effectRenderer;
	
	private @Nullable LivingEntity selectedPet; // Used for directing pets to do actions on key releases

	public ClientProxy() {
		super();
	}
	
	@Override
	public void preinit() {
		super.preinit();
		
		bindingCast = new KeyBinding("key.cast.desc", GLFW.GLFW_KEY_LEFT_CONTROL, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingCast);
		bindingScroll = new KeyBinding("key.spellscroll.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		bindingInfo = new KeyBinding("key.infoscreen.desc", GLFW.GLFW_KEY_HOME, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingInfo);
		bindingBladeCast = new KeyBinding("key.bladecast.desc", GLFW.GLFW_KEY_R, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingBladeCast);
		bindingPetPlacementModeCycle = new KeyBinding("key.pet.placementmode.desc", GLFW.GLFW_KEY_G, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingPetPlacementModeCycle);
		bindingPetTargetModeCycle = new KeyBinding("key.pet.targetmode.desc", GLFW.GLFW_KEY_H, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingPetTargetModeCycle);
		bindingPetAttackAll = new KeyBinding("key.pet.attackall.desc", GLFW.GLFW_KEY_X, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttackAll);
		bindingPetAttack = new KeyBinding("key.pet.attack.desc", GLFW.GLFW_KEY_C, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingPetAttack);
		bindingPetAllStop = new KeyBinding("key.pet.stopall.desc", GLFW.GLFW_KEY_L, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingPetAllStop);
		bindingPetAllStop = new KeyBinding("key.pet.stopall.desc", GLFW.GLFW_KEY_L, "key.nostrummagica.desc");
		ClientRegistry.registerKeyBinding(bindingPetAllStop);
		
		
    	
    	EnchantedArmor.ClientInit();
    	
    	MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public void postinit() {
		this.overlayRenderer = new OverlayRenderer();
		this.effectRenderer = ClientEffectRenderer.instance();
		
		initDefaultEffects(this.effectRenderer);
		
		
		super.postinit();
	}
	
	@Override
	public void startup(FMLServerStartingEvent event) {
		super.startup(event);
		
		// Client-only commands
		final CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
		CommandInfoScreenGoto.register(dispatcher);
		CommandDebugEffect.register(dispatcher);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public void clientSetup(FMLClientSetupEvent event) {
		OBJLoader.INSTANCE.addDomain(NostrumMagica.MODID);
		
		ClientRegistry.bindTileEntitySpecialRenderer(SymbolTileEntity.class, new TileEntitySymbolRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CandleTileEntity.class, new TileEntityCandleRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(AltarTileEntity.class, new TileEntityAltarRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(NostrumObeliskEntity.class, new TileEntityObeliskRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(NostrumPortalTileEntityBase.class, new TileEntityPortalRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ProgressionDoorTileEntity.class, new TileEntityProgressionDoorRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ManaArmorerTileEntity.class, new TileEntityManaArmorerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(LockedChestEntity.class, new TileEntityLockedChestRenderer());
		
		ScreenManager.registerFactory(NostrumContainers.ActiveHopper, ActiveHopperGui.ActiveHopperGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.LoreTable, LoreTableGui.LoreTableGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.ModificationTable, ModificationTableGui.ModificationGui::new);
		ScreenManager.registerFactory(NostrumContainers.Putter, PutterBlockGui.PutterBlockGuiContainer::new);
		ScreenManager.registerFactory(NostrumContainers.ReagentBag, ReagentBagGui.BagGui::new);
		ScreenManager.registerFactory(NostrumContainers.RuneBag, RuneBagGui.BagGui::new);
		ScreenManager.registerFactory(NostrumContainers.SpellCreation, SpellCreationGui.SpellGui::new);
		ScreenManager.registerFactory(NostrumContainers.PetGui, new PetGUIFactory());
		ScreenManager.registerFactory(NostrumContainers.RuneShaper, RuneShaperGui.RuneShaperGuiContainer::new);
	}
	
	// To get around bounds matching. D:
	protected static class PetGUIFactory<T extends IEntityPet> implements ScreenManager.IScreenFactory<PetGUI.PetContainer<T>, PetGUI.PetGUIContainer<T>> {

			@Override
			public PetGUIContainer<T> create(PetContainer<T> c, PlayerInventory p,
					ITextComponent n) {
				return new PetGUI.PetGUIContainer<T>(c, p, n);
			}
	}
	

	
	@SubscribeEvent
	public void registerAllModels(ModelRegistryEvent event) {
		registerEntityRenderers();
		
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
    	
	}
	
	@SubscribeEvent
	public void registerColorHandlers(ColorHandlerEvent.Item ev) {
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
	public void registerColorHandlers(ColorHandlerEvent.Block event) {
		IBlockColor tinter = new IBlockColor() {
			@Override
			public int getColor(BlockState state, IEnviromentBlockReader world, BlockPos pos, int tintIndex) {
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
	
	private void registerEntityRenderers() {
		
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellProjectile.class, new IRenderFactory<EntitySpellProjectile>() {
			@Override
			public EntityRenderer<? super EntitySpellProjectile> createRenderFor(EntityRendererManager manager) {
				return new RenderSpellProjectile(manager, 1f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellBullet.class, new IRenderFactory<EntitySpellBullet>() {
			@Override
			public EntityRenderer<? super EntitySpellBullet> createRenderFor(EntityRendererManager manager) {
				return new RenderSpellBullet(manager, 1f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolemEarth.class, new IRenderFactory<EntityGolemEarth>() {
			@Override
			public EntityRenderer<? super EntityGolemEarth> createRenderFor(EntityRendererManager manager) {
				return new RenderGolem<EntityGolemEarth>(manager, new ModelGolem<>(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolemFire.class, new IRenderFactory<EntityGolemFire>() {
			@Override
			public EntityRenderer<? super EntityGolemFire> createRenderFor(EntityRendererManager manager) {
				return new RenderGolem<EntityGolemFire>(manager, new ModelGolem<>(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolemIce.class, new IRenderFactory<EntityGolemIce>() {
			@Override
			public EntityRenderer<? super EntityGolemIce> createRenderFor(EntityRendererManager manager) {
				return new RenderGolem<EntityGolemIce>(manager, new ModelGolem<>(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolemLightning.class, new IRenderFactory<EntityGolemLightning>() {
			@Override
			public EntityRenderer<? super EntityGolemLightning> createRenderFor(EntityRendererManager manager) {
				return new RenderGolem<EntityGolemLightning>(manager, new ModelGolem<>(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolemEnder.class, new IRenderFactory<EntityGolemEnder>() {
			@Override
			public EntityRenderer<? super EntityGolemEnder> createRenderFor(EntityRendererManager manager) {
				return new RenderGolem<EntityGolemEnder>(manager, new ModelGolem<>(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolemPhysical.class, new IRenderFactory<EntityGolemPhysical>() {
			@Override
			public EntityRenderer<? super EntityGolemPhysical> createRenderFor(EntityRendererManager manager) {
				return new RenderGolem<EntityGolemPhysical>(manager, new ModelGolem<>(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolemWind.class, new IRenderFactory<EntityGolemWind>() {
			@Override
			public EntityRenderer<? super EntityGolemWind> createRenderFor(EntityRendererManager manager) {
				return new RenderGolem<EntityGolemWind>(manager, new ModelGolem<>(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityKoid.class, new IRenderFactory<EntityKoid>() {
			@Override
			public EntityRenderer<? super EntityKoid> createRenderFor(EntityRendererManager manager) {
				return new RenderKoid(manager, .3f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonRed.class, new IRenderFactory<EntityDragonRed>() {
			@Override
			public EntityRenderer<? super EntityDragonRed> createRenderFor(EntityRendererManager manager) {
				return new RenderDragonRed<EntityDragonRed>(manager, 5);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonRed.DragonBodyPart.class, new IRenderFactory<EntityDragonRed.DragonBodyPart>() {
			@Override
			public EntityRenderer<? super EntityDragonRed.DragonBodyPart> createRenderFor(EntityRendererManager manager) {
				return new RenderDragonRedPart(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityTameDragonRed.class, new IRenderFactory<EntityTameDragonRed>() {
			@Override
			public EntityRenderer<? super EntityTameDragonRed> createRenderFor(EntityRendererManager manager) {
				return new RenderTameDragonRed(manager, 2);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityShadowDragonRed.class, new IRenderFactory<EntityShadowDragonRed>() {
			@Override
			public EntityRenderer<? super EntityShadowDragonRed> createRenderFor(EntityRendererManager manager) {
				return new RenderShadowDragonRed(manager, 2);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySprite.class, new IRenderFactory<EntitySprite>() {
			@Override
			public EntityRenderer<? super EntitySprite> createRenderFor(EntityRendererManager manager) {
				return  new RenderSprite(manager, .7f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonEgg.class, new IRenderFactory<EntityDragonEgg>() {
			@Override
			public EntityRenderer<? super EntityDragonEgg> createRenderFor(EntityRendererManager manager) {
				return new RenderDragonEgg(manager, .45f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityChakramSpellSaucer.class, new IRenderFactory<EntityChakramSpellSaucer>() {
			@Override
			public EntityRenderer<? super EntityChakramSpellSaucer> createRenderFor(EntityRendererManager manager) {
				return new RenderMagicSaucer<EntityChakramSpellSaucer>(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityCyclerSpellSaucer.class, new IRenderFactory<EntityCyclerSpellSaucer>() {
			@Override
			public EntityRenderer<? super EntityCyclerSpellSaucer> createRenderFor(EntityRendererManager manager) {
				return new RenderMagicSaucer<EntityCyclerSpellSaucer>(manager);
			}
		});				
		RenderingRegistry.registerEntityRenderingHandler(EntitySwitchTrigger.class, new IRenderFactory<EntitySwitchTrigger>() {
			@Override
			public EntityRenderer<? super EntitySwitchTrigger> createRenderFor(EntityRendererManager manager) {
				return new RenderSwitchTrigger(manager);
			}
		});			
		RenderingRegistry.registerEntityRenderingHandler(EntityKeySwitchTrigger.class, new IRenderFactory<EntityKeySwitchTrigger>() {
			@Override
			public EntityRenderer<? super EntityKeySwitchTrigger> createRenderFor(EntityRendererManager manager) {
				return new RenderKeySwitchTrigger(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(NostrumTameLightning.class, new IRenderFactory<NostrumTameLightning>() {
			@Override
			public EntityRenderer<? super NostrumTameLightning> createRenderFor(EntityRendererManager manager) {
				return new LightningBoltRenderer(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityHookShot.class, new IRenderFactory<EntityHookShot>() {
			@Override
			public EntityRenderer<? super EntityHookShot> createRenderFor(EntityRendererManager manager) {
				return new RenderHookShot(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityWisp.class, new IRenderFactory<EntityWisp>() {
			@Override
			public EntityRenderer<? super EntityWisp> createRenderFor(EntityRendererManager manager) {
				return new RenderWisp(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityLux.class, new IRenderFactory<EntityLux>() {
			@Override
			public EntityRenderer<? super EntityLux> createRenderFor(EntityRendererManager manager) {
				return new RenderLux(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityWillo.class, new IRenderFactory<EntityWillo>() {
			@Override
			public EntityRenderer<? super EntityWillo> createRenderFor(EntityRendererManager manager) {
				return new RenderWillo(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityArcaneWolf.class, new IRenderFactory<EntityArcaneWolf>() {
			@Override
			public EntityRenderer<? super EntityArcaneWolf> createRenderFor(EntityRendererManager manager) {
				return new RenderArcaneWolf(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlantBoss.class, new IRenderFactory<EntityPlantBoss>() {
			@Override
			public EntityRenderer<? super EntityPlantBoss> createRenderFor(EntityRendererManager manager) {
				return new RenderPlantBoss(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlantBoss.PlantBossLeafLimb.class, new IRenderFactory<EntityPlantBoss.PlantBossLeafLimb>() {
			@Override
			public EntityRenderer<? super EntityPlantBoss.PlantBossLeafLimb> createRenderFor(EntityRendererManager manager) {
				return new RenderPlantBossLeaf(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellMortar.class, new IRenderFactory<EntitySpellMortar>() {
			@Override
			public EntityRenderer<? super EntitySpellMortar> createRenderFor(EntityRendererManager manager) {
				return new RenderSpellMortar(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlantBossBramble.class, new IRenderFactory<EntityPlantBossBramble>() {
			@Override
			public EntityRenderer<? super EntityPlantBossBramble> createRenderFor(EntityRendererManager manager) {
				return new RenderPlantBossBramble(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlantBoss.PlantBossBody.class, new IRenderFactory<EntityPlantBoss.PlantBossBody>() {
			@Override
			public EntityRenderer<? super EntityPlantBoss.PlantBossBody> createRenderFor(EntityRendererManager manager) {
				return new RenderPlantBossBody(manager);
			}
		});
	}
	
	@SubscribeEvent
	public static void registerClientParticleFactories(ParticleFactoryRegisterEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		ParticleManager manager = mc.particles;
		
		for (NostrumParticles particle : NostrumParticles.values()) {
			manager.registerFactory(particle.getType(), new IParticleFactory<NostrumParticleData>() {
				@Override
				public Particle makeParticle(NostrumParticleData typeIn, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
					return particle.getFactory().createParticle(worldIn, typeIn.getParams());
				}
			});
		}
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
			
		} else if (bindingPetPlacementModeCycle.isPressed()) {
			// Cycle placement mode
			final PetPlacementMode current = NostrumMagica.instance.getPetCommandManager().getPlacementMode(this.getPlayer());
			final PetPlacementMode next = PetPlacementMode.values()[(current.ordinal() + 1) % PetPlacementMode.values().length];
			
			// Set up client to have this locally
			NostrumMagica.instance.getPetCommandManager().setPlacementMode(getPlayer(), next);
			
			// Update client icon
			this.overlayRenderer.changePetPlacementIcon();
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllPlacementMode(next));
		} else if (bindingPetTargetModeCycle.isPressed()) {
			// Cycle target mode
			final PetTargetMode current = NostrumMagica.instance.getPetCommandManager().getTargetMode(this.getPlayer());
			final PetTargetMode next = PetTargetMode.values()[(current.ordinal() + 1) % PetTargetMode.values().length];
			
			// Update client icon
			this.overlayRenderer.changePetTargetIcon();
			
			// Set up client to have this locally
			NostrumMagica.instance.getPetCommandManager().setTargetMode(getPlayer(), next);
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllTargetMode(next));
		} else if (bindingPetAttackAll.isPressed()) {
			// Raytrace, find tar get, and set all to attack
			final PlayerEntity player = getPlayer();
			if (player != null && player.world != null) {
				final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
				final List<LivingEntity> tames = NostrumMagica.getTamedEntities(player);
				RayTraceResult result = RayTrace.raytraceApprox(
						player.world, player,
						player.getEyePosition(partialTicks),
						player.getLook(partialTicks),
						100, (e) -> { return e != player && e instanceof LivingEntity && !player.isOnSameTeam(e) && !tames.contains(e);},
						1);
				if (result != null && result.getType() == RayTraceResult.Type.ENTITY) {
					NetworkHandler.sendToServer(PetCommandMessage.AllAttack(RayTrace.livingFromRaytrace(result)));
				}
			}
		} else if (bindingPetAttack.isPressed()) {
			// Raytrace, find target, and then make single one attack
			// Probably could be same button but if raytrace is our pet,
			// have them hold it down and release on an enemy? Or 'select' them
			// and have them press again to select enemy?
			final PlayerEntity player = getPlayer();
			if (player != null && player.world != null) {
				final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
				final List<LivingEntity> tames = NostrumMagica.getTamedEntities(player);
				if (selectedPet == null) {
					// Try and select a pet
					RayTraceResult result = RayTrace.raytraceApprox(
							player.world, player,
							player.getEyePosition(partialTicks),
							player.getLook(partialTicks),
							100, (e) -> { return e != player && tames.contains(e);},
							.1);
					if (result != null && result.getType() == RayTraceResult.Type.ENTITY) {
						selectedPet = RayTrace.livingFromRaytrace(result);
						if (selectedPet.world.isRemote) {
							selectedPet.setGlowing(true);
						}
					}
				} else {
					// Find target
					RayTraceResult result = RayTrace.raytraceApprox(
							player.world, player,
							player.getEyePosition(partialTicks),
							player.getLook(partialTicks),
							100, (e) -> { return e != player && e instanceof LivingEntity && !player.isOnSameTeam(e) && !tames.contains(e);},
							1);
					if (result != null && result.getType() == RayTraceResult.Type.ENTITY) {
						NetworkHandler.sendToServer(PetCommandMessage.PetAttack(selectedPet, RayTrace.livingFromRaytrace(result)));
					}
					
					// Clear out pet
					if (selectedPet.world.isRemote) {
						selectedPet.setGlowing(false);
					}
					selectedPet = null;
				}
			}
		} else if (bindingPetAllStop.isPressed()) {
			NetworkHandler.sendToServer(PetCommandMessage.AllStop());
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
						"info.spell.tome_weak", new Object[0]));
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
								player.posX + offsetx, player.posY, player.posZ + offsetz,
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
						"info.spell.low_tech", new Object[0]));
				System.out.println("LOUD LOW TECH"); // TODO remove
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			} else if (spell.getElementCount() > maxElems) {
				player.sendMessage(new TranslationTextComponent(
						"info.spell.low_control", new Object[0]));
				System.out.println("LOUD LOW CONTROL"); // TODO remove
				NostrumMagicaSounds.CAST_FAIL.play(player);
				return;
			} else if (spell.getTriggerCount() > maxTriggers) {
				player.sendMessage(new TranslationTextComponent(
						"info.spell.low_finesse", new Object[0]));
				System.out.println("LOUD LOW FINESSE"); // TODO remove
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
							"info.spell.low_mastery", neededMastery.name().toLowerCase(), elem.getName(), currentMastery.name().toLowerCase()));
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
						player.sendMessage(new TranslationTextComponent("info.spell.bad_reagent", row.getKey().prettyName()));
						System.out.println("LOUD BAD REAGENT"); // TODO remove
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
	public void openPetGUI(PlayerEntity player, IEntityPet pet) {
		// Integrated clients still need to open the gui...
		//if (!player.world.isRemote) {
//			DragonContainer container = dragon.getGUIContainer();
//			DragonGUI gui = new DragonGUI(container);
//			FMLCommonHandler.instance().showGuiScreen(gui);
			super.openPetGUI(player, pet);
		//}
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
	
	@SubscribeEvent
	public void stitchEventPre(TextureStitchEvent.Pre event) {
		// Note: called multiple times for different texture atlases.
		// Using what Botania does
		if(event.getMap() != Minecraft.getInstance().getTextureMap()) {
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
		
		/*
		SHIELD("shield", false),
		TING1("ting1", false),
		TING2("ting2", false),
		TING3("ting3", false),
		TING4("ting4", false),
		TING5("ting5", false),
		CYL("cyl", true),
		SHELL("shell", true),
		ARROWU("arrow_up", false),
		ARROWD("arrow_down", false),
		ORB_CLOUDY("orb_cloudy", true),
		ORB_SCALED("orb_scaled", true),
		ORB_PURE("orb_pure", true),
		THORN_0("thorn0", true),
		THORN_1("thorn1", true),
		THORN_2("thorn2", true),
		THORN_3("thorn3", true),
		THORN_4("thorn4", true),
		 */
    }
    
//	private static final TRSRTransformation THIRD_PERSON_BLOCK = Transforms.convert(0, 2.5f, 0, 75, 45, 0, 0.375f);
//    private static final ImmutableMap<TransformType, TRSRTransformation> BLOCK_TRANSFORMS = ImmutableMap.<TransformType, TRSRTransformation>builder()
//            .put(TransformType.GUI, Transforms.convert(0, 0, 0, 30, 225, 0, 0.625f))
//            .put(TransformType.GROUND, Transforms.convert(0, 3, 0, 0, 0, 0, 0.25f))
//            .put(TransformType.FIXED, Transforms.convert(0, 0, 0, 0, 0, 0, 0.5f))
//            .put(TransformType.THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_BLOCK)
//            .put(TransformType.THIRD_PERSON_LEFT_HAND, Transforms.leftify(THIRD_PERSON_BLOCK))
//            .put(TransformType.FIRST_PERSON_RIGHT_HAND, Transforms.convert(0, 0, 0, 0, 45, 0, 0.4f))
//            .put(TransformType.FIRST_PERSON_LEFT_HAND, Transforms.convert(0, 0, 0, 0, 225, 0, 0.4f))
//            .build();
		
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		for (ClientEffectIcon icon : ClientEffectIcon.values()) {
			if (!icon.getModelKey().endsWith(".obj")) {
				// json
//				final String modelLoc = "effect/" + icon.getModelKey();
//				IUnbakedModel model = event.getModelLoader().getUnbakedModel(new ResourceLocation(NostrumMagica.MODID, modelLoc));
//				//IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(NostrumMagica.MODID, modelLoc), "Failed to get json model for " + modelLoc);
//				
//				HashSet<String> missingTextureErrors = new HashSet<>();
//				
//				if (model != null && model != ModelLoaderRegistry.getMissingModel()) {
//					model.getTextures(event.getModelLoader()::getUnbakedModel, missingTextureErrors);
//					IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
//					event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, modelLoc)), bakedModel);
//				} else {
//					model.getClass();
//				}
			} else {
				//"effect/orb_cloudy", "effect/orb_scaled", "effects/cyl", 
				final String modelLoc = "effect/" + icon.getKey();
				IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(NostrumMagica.MODID, modelLoc + ".obj"), "Failed to get obj model for " + modelLoc);
				
				if (model != null && model instanceof OBJModel) {
					IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
					// Note: putting as ModelResourceLocation to match RenderObj. Note creating like the various RenderObj users do.
					event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, modelLoc)), bakedModel);
				}
			}
		}
		
		for (String key : new String[] {"block/orb_crystal",
				"entity/orb", "entity/sprite_core", "entity/sprite_arms", "entity/magic_saucer", "entity/koid"}) {
			IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(new ResourceLocation(NostrumMagica.MODID, key + ".obj"), "Failed to get obj model for " + key);
			
			if (model != null && model instanceof OBJModel) {
				IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
				// Note: putting as ModelResourceLocation to match RenderObj. Note creating like the various RenderObj users do.
				event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(new ResourceLocation(NostrumMagica.MODID, key)), bakedModel);
			}
		}
		
		for (ResourceLocation loc : ModelDragonRed.getModelParts()) {
			ResourceLocation fullLoc = new ResourceLocation(loc.getNamespace(), loc.getPath() + ".obj");
			IUnbakedModel model = ModelLoaderRegistry.getModelOrLogError(fullLoc, "Failed to get obj model for " + fullLoc);
			
			if (model != null && model instanceof OBJModel) {
				IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.ITEM);
				// Note: putting as ModelResourceLocation to match RenderObj. Note creating like the various RenderObj users do.
				event.getModelRegistry().put(RenderFuncs.makeDefaultModelLocation(loc), bakedModel);
			}
		}
		
		//AllCapeModels
		
//    	for (String key : new String[] {
//    			"pedestal", "crystal_standing", "crystal_embedded", "crystal_hanging", "mirror"}) {
//			IUnbakedModel model;
//			try {
//				model = ModelLoaderRegistry.getModel(new ResourceLocation(NostrumMagica.MODID, "block/" + key + ".obj"));
//				
//				if (model != null && model instanceof OBJModel) {
//					IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.BLOCK);
////					event.getModelRegistry().put(new ResourceLocation(NostrumMagica.MODID + ":" + key), bakedModel);
////					event.getModelRegistry().put(new ResourceLocation(NostrumMagica.MODID + ":" + key + ".obj"), bakedModel);
//					event.getModelRegistry().put(new ResourceLocation(NostrumMagica.MODID + ":models/block/" + key), bakedModel);
////					event.getModelRegistry().put(new ResourceLocation(NostrumMagica.MODID + ":block/" + key + ".obj"), bakedModel);
//					
//					IBakedModel bakedInvModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(),
//	                        new BasicState(model.getDefaultState(), true), DefaultVertexFormats.ITEM);
//	                bakedInvModel = new PerspectiveMapWrapper(bakedInvModel, BLOCK_TRANSFORMS);
//	                event.getModelRegistry().put(new ModelResourceLocation(NostrumMagica.MODID + ":" + key, "inventory"), bakedInvModel);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				NostrumMagica.logger.warn("Failed to load effect " + key);
//			}	
//		}
    	
//		{
//			IUnbakedModel model = null;
//			try {
//				model = ModelLoaderRegistry.getModel(new ResourceLocation(NostrumMagica.MODID, "block/mirror.obj"));
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	    	IBakedModel bakedModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.BLOCK);
//			IBakedModel existing = null;
//			String[] paths = {"dirt", "block/dirt", "minecraft:dirt", "minecraft:block/dirt"};
//			String[] variants = {"", "inventory", "normal"};
//			for (String path : paths) {
//				for (String variant : variants) {
//					existing = event.getModelRegistry().get(new ModelResourceLocation(path, variant));
//					boolean exists = existing != null && existing != event.getModelManager().getMissingModel();
//					NostrumMagica.logger.info((!exists ? "FAIL: " : "EXIST: ")
//						+ path + "#" + variant);
//					System.out.println((!exists ? "FAIL: " : "EXIST: ")
//							+ path + "#" + variant);
//					
//					if (exists) {
//						event.getModelRegistry().put(new ModelResourceLocation(path, variant), bakedModel);
//						break;
//					}
//				}
//				if (existing != null && existing != event.getModelManager().getMissingModel()) {
//					break;
//				}
//			}
//		}
    	
    	// Warlock blade; put obj in place for item model
    	{
    		IUnbakedModel model = ModelLoaderRegistry.getModelOrMissing(new ResourceLocation(NostrumMagica.MODID, "item/warlock_sword.obj"));
			if (model != null && model instanceof OBJModel) {
				IBakedModel bakedInvModel = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(),
                        new BasicState(model.getDefaultState(), true), DefaultVertexFormats.ITEM);
				
				final ImmutableMap<TransformType, TRSRTransformation> SWORD_TRANSFORMS = ImmutableMap.<TransformType, TRSRTransformation>builder()
			            .put(TransformType.GUI, Transforms.convert(-3.2f, -4f, 0.f, 0, 0, -45f, .125f))
			            .put(TransformType.GROUND, Transforms.convert(0, 0, 0, 0, 0, 0, .125f))
			            .put(TransformType.FIXED, Transforms.convert(0, 0, 0, 0, 0, 0, .125f))
			            .put(TransformType.THIRD_PERSON_RIGHT_HAND, Transforms.convert(0.4f, 0, 0.4f, 0, 90, 0, 1.6f * .125f))
			            .put(TransformType.THIRD_PERSON_LEFT_HAND, Transforms.leftify(Transforms.convert(0.05f, 0, 0.05f, 0, 90, 0, 1.6f * .125f)))
			            .put(TransformType.FIRST_PERSON_RIGHT_HAND, Transforms.convert(0, 0, 0, 0, 90, 15, .125f))
			            .put(TransformType.FIRST_PERSON_LEFT_HAND, Transforms.leftify(Transforms.convert(0, 0, 0, 0, 90, 15, .125f)))
			            .build();
				
                bakedInvModel = new PerspectiveMapWrapper(bakedInvModel, SWORD_TRANSFORMS);
				
				event.getModelRegistry().put(new ModelResourceLocation(NostrumMagica.MODID + ":warlock_sword", "inventory"), bakedInvModel);
				
//				"transform": {
//		            "scale": 0.125,
//		            "thirdperson": {
//		                "scale": 1.6,
//		                "translation": [0.05, 0, 0.05],
//		                "rotation": [ { "x": 0 }, { "y": 90 }, { "z": 0} ]
//		            },
//		            "firstperson": {
//		                "rotation": [ { "x": 0 }, { "y": 90 }, { "z": 15} ]
//		            },
//		            "gui": {
//		                "rotation": [ { "x": 0 }, { "y": 0 }, { "z": -45} ],
//		                "translation": [-0.2, -0.25, 0]
//		            }
//		        }
				
				//event.getModelRegistry().put(new ModelResourceLocation("minecraft:item/stick", "inventory"), bakedModel);
			}
    	}
    	
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
	
	private static void initDefaultEffects(ClientEffectRenderer renderer) {
		
		renderer.registerEffect(new SpellComponentWrapper(AoEShape.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					// TODO get the shape params in here to modify scale
					// TODO get whether it's a good thing or not
					ClientEffect effect = new ClientEffectMajorSphere(target == null ? targetPos : new Vec3d(0, 0, 0),
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
						ClientEffect effect = new ClientEffectMirrored(target == null ? targetPos : new Vec3d(0, 0, 0),
								new ClientEffectFormFlat(ClientEffectIcon.TING1, 0, 0, 0),
								500L, 5);
						
						if (target != null)
							effect.modify(new ClientEffectModifierFollow(target));
						
						effect
						.modify(new ClientEffectModifierColor(element.getColor(), element.getColor()))
						.modify(new ClientEffectModifierRotate(0f, .4f, 0f))
						.modify(new ClientEffectModifierTranslate(0, 0, -1))
						.modify(new ClientEffectModifierMove(new Vec3d(0, 1.5, 0), new Vec3d(0, .5, .7), .5f, 1f))
						.modify(new ClientEffectModifierGrow(.1f, .3f, .2f, .8f, .5f))
						;
						return effect;
					});
		}
		
		// triggers (that have them)
		renderer.registerEffect(new SpellComponentWrapper(BeamTrigger.instance()),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectBeam(sourcePos == null ? source.getPositionVector() : sourcePos,
							targetPos == null ? target.getPositionVector() : targetPos,
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
//					ClientEffect effect = new ClientEffect(source == null ? sourcePos : source.getPositionVector(),
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
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, -2, 0), .3f, 1f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.RESIST),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 1.5, 0), 0f, .7f))
					.modify(new ClientEffectModifierGrow(.8f, .2f, 1f, 1f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, 1f, 0f, .8f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.GROWTH),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectEchoed(targetPos == null ? target.getPositionVector() : targetPos, 
							new ClientEffectMirrored(new Vec3d(0,0,0),
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
						effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
								new ClientEffectFormBasic(ClientEffectIcon.SHIELD, 0, 0, 0),
								3L * 500L, 5);
						isShield = true;
					} else {
						effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVector() : targetPos).add(0, 1, 0),
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							3L * 500L, 6, new Vec3d(1, 0, 0));
					
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
					
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.SUMMON),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored(targetPos == null ? target.getPositionVector() : targetPos,
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
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 1.5, 0), 0f, .3f))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, 0, 1.5)))
					.modify(new ClientEffectModifierMove(new Vec3d(0, 0, 0), new Vec3d(0, -2, 0), 0f, 1f))
					.modify(new ClientEffectModifierRotate(1f, 0f, 0f))
					.modify(new ClientEffectModifierGrow(.6f, .2f, .7f, .6f, .5f))
					.modify(new ClientEffectModifierShrink(1f, 1f, .5f, 0f, .6f))
					;
					return effect;
				});

		renderer.registerEffect(new SpellComponentWrapper(EAlteration.RUIN),
				(source, sourcePos, target, targetPos, flavor, negative, param) -> {
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVector() : targetPos).add(0, 1, 0),
							new ClientEffectFormFlat(ClientEffectIcon.TING4, 0, 0, 0),
							2L * 500L, 6, new Vec3d(.5, .5, 0));
					
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
	}
	
	@Override
	public void spawnEffect(World world, SpellComponentWrapper comp,
			LivingEntity caster, Vec3d casterPos,
			LivingEntity target, Vec3d targetPos,
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
//				targetPos = target.getPositionVector();
//			else
				targetPos = new Vec3d(0, 0, 0);
		
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
			mc.player.sendMessage(
					new TranslationTextComponent("info.nostrumwelcome.text", new Object[]{
							this.bindingInfo.getLocalizedName()
					}));
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
	
	public @Nullable LivingEntity getCurrentPet() {
		return this.selectedPet;
	}
}
