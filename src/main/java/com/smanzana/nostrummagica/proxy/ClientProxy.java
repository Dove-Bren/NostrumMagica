package com.smanzana.nostrummagica.proxy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ActiveHopper;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.DungeonAir;
import com.smanzana.nostrummagica.blocks.DungeonBars;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.ItemDuct;
import com.smanzana.nostrummagica.blocks.LogicDoor;
import com.smanzana.nostrummagica.blocks.LoreTable;
import com.smanzana.nostrummagica.blocks.MagicDirt;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.ManaArmorerBlock;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.blocks.NostrumSpawnAndTrigger;
import com.smanzana.nostrummagica.blocks.ParadoxMirrorBlock;
import com.smanzana.nostrummagica.blocks.ProgressionDoor;
import com.smanzana.nostrummagica.blocks.PutterBlock;
import com.smanzana.nostrummagica.blocks.SorceryPortal;
import com.smanzana.nostrummagica.blocks.SwitchBlock;
import com.smanzana.nostrummagica.blocks.TeleportRune;
import com.smanzana.nostrummagica.capabilities.IManaArmor;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.effects.ClientEffect;
import com.smanzana.nostrummagica.client.effects.ClientEffectBeam;
import com.smanzana.nostrummagica.client.effects.ClientEffectEchoed;
import com.smanzana.nostrummagica.client.effects.ClientEffectFormBasic;
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
import com.smanzana.nostrummagica.client.gui.ScrollScreen;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;
import com.smanzana.nostrummagica.client.model.MimicBlockBakedModel;
import com.smanzana.nostrummagica.client.overlay.OverlayRenderer;
import com.smanzana.nostrummagica.client.particles.NostrumParticleData;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.render.entity.ModelGolem;
import com.smanzana.nostrummagica.client.render.entity.RenderArcaneWolf;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonEgg;
import com.smanzana.nostrummagica.client.render.entity.RenderDragonRed;
import com.smanzana.nostrummagica.client.render.entity.RenderGolem;
import com.smanzana.nostrummagica.client.render.entity.RenderHookShot;
import com.smanzana.nostrummagica.client.render.entity.RenderKoid;
import com.smanzana.nostrummagica.client.render.entity.RenderLux;
import com.smanzana.nostrummagica.client.render.entity.RenderMagicSaucer;
import com.smanzana.nostrummagica.client.render.entity.RenderPlantBoss;
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
import com.smanzana.nostrummagica.client.render.tile.TileEntityManaArmorerRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityObeliskRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityPortalRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntityProgressionDoorRenderer;
import com.smanzana.nostrummagica.client.render.tile.TileEntitySymbolRenderer;
import com.smanzana.nostrummagica.command.CommandDebugClientEffect;
import com.smanzana.nostrummagica.command.CommandInfoScreenGoto;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntityLux;
import com.smanzana.nostrummagica.entity.EntitySpellBullet;
import com.smanzana.nostrummagica.entity.EntitySpellMortar;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
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
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;
import com.smanzana.nostrummagica.fluids.NostrumFluids;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.ArcaneWolfSoulItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ChalkItem;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonEgg;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.items.DragonSoulItem;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.HookshotItem;
import com.smanzana.nostrummagica.items.HookshotItem.HookshotType;
import com.smanzana.nostrummagica.items.ISpellArmor;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MageStaff;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicCharm;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.MirrorShield;
import com.smanzana.nostrummagica.items.MirrorShieldImproved;
import com.smanzana.nostrummagica.items.NostrumGuide;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumRoseItem.RoseType;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.ReagentSeed;
import com.smanzana.nostrummagica.items.RuneBag;
import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.items.ShrineSeekingGem;
import com.smanzana.nostrummagica.items.SoulDagger;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTableItem;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.items.SpellcraftGuide;
import com.smanzana.nostrummagica.items.ThanoPendant;
import com.smanzana.nostrummagica.items.ThanosStaff;
import com.smanzana.nostrummagica.items.WarlockSword;
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
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLightningBolt;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

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
		
		
    	
    	TileEntitySymbolRenderer.init();
    	TileEntityCandleRenderer.init();
    	TileEntityAltarRenderer.init();
    	TileEntityObeliskRenderer.init();
    	TileEntityPortalRenderer.init();
    	TileEntityProgressionDoorRenderer.init();
    	TileEntityManaArmorerRenderer.init();
    	
    	EnchantedArmor.ClientInit();
    	
    	// idk why this is deprecated. It's what's in the docs and in the forge samples.
    	// https://github.com/MinecraftForge/MinecraftForge/blob/1.14.x/src/test/java/net/minecraftforge/testmods/TestOBJModelMod.java
    	OBJLoader.INSTANCE.addDomain(NostrumMagica.MODID);
    	
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
		ClientCommandHandler.instance.registerCommand(new CommandInfoScreenGoto());
		ClientCommandHandler.instance.registerCommand(new CommandDebugClientEffect());
		
		super.postinit();
	}
	
	private void registerItemVariants(ModelRegistryEvent event) {
		ResourceLocation variants[] = new ResourceLocation[ReagentType.values().length];
		int i = 0;
		for (ReagentType type : ReagentType.values()) {
			variants[i++] = new ResourceLocation(NostrumMagica.MODID,
					ReagentItem.getNameFromMeta(type.getMeta()));
		}
		ModelBakery.registerItemVariants(ReagentItem.instance(), variants);
		
		variants = new ResourceLocation[EMagicElement.values().length];
		i = 0;
		for (EMagicElement type : EMagicElement.values()) {
			if (type == EMagicElement.PHYSICAL)
				continue;
			variants[i++] = new ResourceLocation(NostrumMagica.MODID,
					"gem_" + InfusedGemItem.instance().getNameFromMeta(InfusedGemItem.instance()
							.getMetaFromElement(type)));
		}
		variants[i++] = new ResourceLocation(NostrumMagica.MODID,
				"gem_basic");
		ModelBakery.registerItemVariants(InfusedGemItem.instance(), variants);
		
		List<ResourceLocation> list = new LinkedList<>();
		for (EMagicElement type : EMagicElement.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name().toLowerCase()));
    	}
    	for (EAlteration type : EAlteration.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name().toLowerCase()));
    	}
    	for (SpellShape type : SpellShape.getAllShapes()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getShapeKey()));
    	}
    	for (SpellTrigger type : SpellTrigger.getAllTriggers()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getTriggerKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(SpellRune.instance(), variants);
    	
    	variants = new ResourceLocation[EMagicElement.values().length];
		i = 0;
		for (EMagicElement type : EMagicElement.values()) {
			variants[i++] = new ResourceLocation(NostrumMagica.MODID,
					"charm_" + MagicCharm.getNameFromMeta(MagicCharm
							.getMetaFromType(type)));
		}
		ModelBakery.registerItemVariants(MagicCharm.instance(), variants);
    	
    	list = new LinkedList<>();
    	List<ResourceLocation> list2 = new LinkedList<>();
    	for (i = 1; i <= SpellTome.MAX_TOME_COUNT; i++) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, SpellTome.id + i));
    		list2.add(new ResourceLocation(NostrumMagica.MODID, SpellPlate.id + i));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(SpellTome.instance(), variants);
    	variants = list2.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(SpellPlate.instance(), variants);
    	
    	list = new LinkedList<>();
    	for (ResourceType type : ResourceType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(NostrumResourceItem.instance(), variants);
    	
    	list = new LinkedList<>();
    	for (SkillItemType type : SkillItemType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(NostrumSkillItem.instance(), variants);
    	
    	list = new LinkedList<>();
    	for (RoseType type : RoseType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, type.getUnlocalizedKey()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(NostrumRoseItem.instance(), variants);
    	
    	list = new LinkedList<>();
    	for (DungeonBlock.Type type : DungeonBlock.Type.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, DungeonBlock.ID + "_" + type.getName()));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(Item.getItemFromBlock(DungeonBlock.instance()), variants);
    	
    	ModelBakery.registerItemVariants(ThanosStaff.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ThanosStaff.ID),
    			new ResourceLocation(NostrumMagica.MODID, ThanosStaff.ID + "_activated"));
    	
    	ModelBakery.registerItemVariants(Item.getItemFromBlock(SwitchBlock.instance()),
    			new ResourceLocation(NostrumMagica.MODID, SwitchBlock.ID));
    	
    	list = new LinkedList<>();
    	for (HookshotType type : HookshotType.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, HookshotItem.ID + "_" + HookshotItem.GetTypeSuffix(type)));
    		list.add(new ResourceLocation(NostrumMagica.MODID, HookshotItem.ID + "_" + HookshotItem.GetTypeSuffix(type) + "_extended"));
    	}
    	
    	variants = list.toArray(new ResourceLocation[0]);
    	ModelBakery.registerItemVariants(HookshotItem.instance(), variants);
	}
	
	@SubscribeEvent
	public void registerAllModels(ModelRegistryEvent event) {
		
		registerItemVariants(event);
		
		//registerModel(SpellTome.instance(), 0, SpellTome.id);
		registerModel(NostrumGuide.instance(), 0, NostrumGuide.id);
		registerModel(SpellcraftGuide.instance(), 0, SpellcraftGuide.id);
		registerModel(SpellScroll.instance(), 0, SpellScroll.id);
		registerModel(SpellScroll.instance(), 1, SpellScroll.id);
		registerModel(SpellScroll.instance(), 2, SpellScroll.id);
		registerModel(BlankScroll.instance(), 0, BlankScroll.id);
		registerModel(DragonEggFragment.instance(), 0, DragonEggFragment.id);
		registerModel(DragonEgg.instance(), 0, DragonEgg.ID);
		registerModel(ReagentBag.instance(), 0, ReagentBag.id);
		registerModel(RuneBag.instance(), 0, RuneBag.id);
		registerModel(SeekerIdol.instance(), 0, SeekerIdol.id);
		registerModel(ShrineSeekingGem.instance(), 0, ShrineSeekingGem.id);
		for (EnchantedWeapon weapon : EnchantedWeapon.getAll()) {
			registerModel(weapon, 0, weapon.getModelID());
		}
		for (EnchantedArmor armor : EnchantedArmor.getAll()) {
			registerModel(armor, 0, armor.getModelID());
		}
		for (DragonArmor armor : DragonArmor.GetAllArmors()) {
			registerModel(armor, 0, armor.getResourceLocation());
		}
		registerModel(MirrorShield.instance(), 0, MirrorShield.ID);
		registerModel(MirrorShieldImproved.instance(), 0, MirrorShieldImproved.id);
		
		registerModel(MagicSwordBase.instance(), 0, MagicSwordBase.instance().getModelID());
		registerModel(MagicArmorBase.helm(), 0, MagicArmorBase.helm().getModelID());
		registerModel(MagicArmorBase.chest(), 0, MagicArmorBase.chest().getModelID());
		registerModel(MagicArmorBase.legs(), 0, MagicArmorBase.legs().getModelID());
		registerModel(MagicArmorBase.feet(), 0, MagicArmorBase.feet().getModelID());
		
		registerModel(WarlockSword.instance(), 0, WarlockSword.ID);
		ModelLoader.setCustomModelResourceLocation(WarlockSword.instance(), 0, new ModelResourceLocation(NostrumMagica.MODID + ":" + WarlockSword.ID, "inventory"));
		
		for (ReagentItem.ReagentType type : ReagentItem.ReagentType.values()) {
			registerModel(ReagentItem.instance(), type.getMeta(),
					ReagentItem.getNameFromMeta(type.getMeta()));
		}
		
		InfusedGemItem gem = InfusedGemItem.instance();
		
		registerModel(gem, 0, "gem_basic");
		
		for (EMagicElement type : EMagicElement.values()) {
			if (type == EMagicElement.PHYSICAL)
				continue;
			int meta = gem.getMetaFromElement(type);
			registerModel(gem, meta,
					"gem_" + gem.getNameFromMeta(meta));
		}
		
		for (EMagicElement type : EMagicElement.values()) {
			int meta = MagicCharm.getMetaFromType(type);
			registerModel(MagicCharm.instance(), meta,
					"charm_" + MagicCharm.getNameFromMeta(meta));
		}
		
		ModelLoader.setCustomMeshDefinition(SpellRune.instance(), new SpellRune.ModelMesher());
		ModelLoader.setCustomMeshDefinition(ThanosStaff.instance(), new ThanosStaff.ModelMesher());
		
		registerModel(new BlockItem(NostrumMagicaFlower.instance()), 
				NostrumMagicaFlower.Type.CRYSTABLOOM.getMeta(),
				NostrumMagicaFlower.Type.CRYSTABLOOM.getName()
				);
		registerModel(new BlockItem(NostrumMagicaFlower.instance()), 
				NostrumMagicaFlower.Type.MIDNIGHT_IRIS.getMeta(),
				NostrumMagicaFlower.Type.MIDNIGHT_IRIS.getName()
				);
		
		registerModel(Item.getItemFromBlock(MagicWall.instance()),
				0,
				MagicWall.ID);
		registerModel(Item.getItemFromBlock(NostrumSingleSpawner.instance()),
				0,
				NostrumSingleSpawner.ID);
		registerModel(Item.getItemFromBlock(NostrumSpawnAndTrigger.instance()),
				0,
				NostrumSpawnAndTrigger.ID);
		registerModel(Item.getItemFromBlock(CursedIce.instance()),
				0,
				CursedIce.ID);
		registerModel(Item.getItemFromBlock(Candle.instance()),
				0,
				Candle.ID);
		registerModel(Item.getItemFromBlock(ManiOre.instance()),
				0,
				ManiOre.ID);
		registerModel(Item.getItemFromBlock(MagicDirt.instance()),
				0,
				MagicDirt.ID);
		registerModel(Item.getItemFromBlock(EssenceOre.instance()),
				0,
				EssenceOre.ID);
		registerModel(SpellTableItem.instance(),
				0,
				SpellTableItem.ID);
		registerModel(MirrorItem.instance(),
				0,
				MirrorItem.ID);
		registerModel(Item.getItemFromBlock(ModificationTable.instance()),
				0,
				ModificationTable.ID);
		registerModel(Item.getItemFromBlock(LoreTable.instance()),
				0,
				LoreTable.ID);
		registerModel(Item.getItemFromBlock(SorceryPortal.instance()),
				0,
				SorceryPortal.ID);
		registerModel(Item.getItemFromBlock(ProgressionDoor.instance()),
				0,
				ProgressionDoor.ID);
		registerModel(Item.getItemFromBlock(LogicDoor.instance()),
				0,
				LogicDoor.ID);
		
		registerModel(Item.getItemFromBlock(DungeonBlock.instance()), 
				DungeonBlock.Type.DARK.ordinal(),
				DungeonBlock.ID + "_" + DungeonBlock.Type.DARK.getName()
				);
		registerModel(Item.getItemFromBlock(DungeonBlock.instance()), 
				DungeonBlock.Type.LIGHT.ordinal(),
				DungeonBlock.ID + "_" + DungeonBlock.Type.LIGHT.getName()
				);
		
		registerModel(new BlockItem(NostrumMirrorBlock.instance()),
				0,
				NostrumMirrorBlock.ID);
		
		registerModel(ChalkItem.instance(),
				0,
				ChalkItem.ID);
		
		registerModel(AltarItem.instance(),
				0,
				AltarItem.ID);
		
		registerModel(Item.getItemFromBlock(SwitchBlock.instance()),
				0,
				SwitchBlock.ID);
		
		for (ResourceType type : ResourceType.values()) {
			registerModel(NostrumResourceItem.instance(),
					NostrumResourceItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
		
		for (SkillItemType type : SkillItemType.values()) {
			registerModel(NostrumSkillItem.instance(),
					NostrumSkillItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
		
		for (RoseType type : RoseType.values()) {
			registerModel(NostrumRoseItem.instance(),
					NostrumRoseItem.getMetaFromType(type),
					type.getUnlocalizedKey());
		}
		
		registerModel(PositionCrystal.instance(),
				0,
				PositionCrystal.ID);
		
		registerModel(PositionToken.instance(),
				0,
				PositionToken.ID);
		
		registerModel(SpellTomePage.instance(),
				0,
				SpellTomePage.id);
		registerModel(MasteryOrb.instance(),
				0,
				MasteryOrb.id);
		
    	for (int i = 1; i <= SpellTome.MAX_TOME_COUNT; i++) {
    		registerModel(SpellTome.instance(), i - 1, SpellTome.id + i);
    		registerModel(SpellPlate.instance(), i - 1, SpellPlate.id + i);
    	}
    	
    	registerModel(MageStaff.instance(),
    			0,
    			MageStaff.ID);
    	
    	registerModel(ThanoPendant.instance(),
    			0,
    			ThanoPendant.ID);
		
		for (EMagicElement element : EMagicElement.values()) {
			registerModel(EssenceItem.instance(),
					element.ordinal(),
					EssenceItem.ID);
		}
		
		for (HookshotType type : HookshotType.values()) {
			registerModel(HookshotItem.instance(),
					HookshotItem.MakeMeta(type, true),
					HookshotItem.ID + "_" + HookshotItem.GetTypeSuffix(type));
			registerModel(HookshotItem.instance(),
					HookshotItem.MakeMeta(type, false),
					HookshotItem.ID + "_" + HookshotItem.GetTypeSuffix(type));
		}
		
		registerModel(ReagentSeed.mandrake, 0, ReagentSeed.mandrake.getItemID());
		registerModel(ReagentSeed.ginseng, 0, ReagentSeed.ginseng.getItemID());
		registerModel(ReagentSeed.essence, 0, ReagentSeed.essence.getItemID());
		
		registerModel(Item.getItemFromBlock(MimicBlock.door()),
				0,
				MimicBlock.ID_DOOR);
		registerModel(Item.getItemFromBlock(MimicBlock.door()),
				1,
				MimicBlock.ID_DOOR);
		registerModel(Item.getItemFromBlock(MimicBlock.facade()),
				0,
				MimicBlock.ID_FACADE);
		registerModel(Item.getItemFromBlock(MimicBlock.facade()),
				1,
				MimicBlock.ID_FACADE);

		registerModel(Item.getItemFromBlock(TeleportRune.instance()),
				0,
				TeleportRune.ID);
		
		registerModel(Item.getItemFromBlock(PutterBlock.instance()),
				0,
				PutterBlock.ID);
		registerModel(Item.getItemFromBlock(ActiveHopper.instance),
				0,
				ActiveHopper.ID);
		registerModel(Item.getItemFromBlock(ItemDuct.instance),
				0,
				ItemDuct.ID);
		registerModel(Item.getItemFromBlock(DungeonBars.instance()),
				0,
				DungeonBars.ID);
		registerModel(Item.getItemFromBlock(DungeonAir.instance()),
				0,
				DungeonAir.ID);
		registerModel(Item.getItemFromBlock(DungeonAir.instance()),
				1,
				DungeonAir.ID);
		registerModel(Item.getItemFromBlock(ParadoxMirrorBlock.instance()),
				0,
				ParadoxMirrorBlock.ID);
		registerModel(Item.getItemFromBlock(ManaArmorerBlock.instance()),
				0,
				ManaArmorerBlock.ID);
		
		registerModel(DragonSoulItem.instance(),
				0,
				DragonSoulItem.ID);
		registerModel(SoulDagger.instance(),
				0,
				SoulDagger.ID);
		
		registerModel(ArcaneWolfSoulItem.instance(),
				0,
				ArcaneWolfSoulItem.ID);
		
		registerEntityRenderers();
		
		// Register fluid handlers which for some reason are required. Otherwise blockstate variants for each fluid level are needed.
		for (NostrumFluids fluid : NostrumFluids.values()) {
			if (fluid.getFluid().getBlock() == null) {
				continue;
			}
			
			final ModelResourceLocation locationIgnoringVariant = new ModelResourceLocation(
					fluid.getFluid().getBlock().getRegistryName(),
					"normal"
					);
			
			ModelLoader.setCustomStateMapper(fluid.getFluid().getBlock(), new StateMapperBase() {
				@Override
				protected ModelResourceLocation getModelResourceLocation(final BlockState state) {
					return locationIgnoringVariant;
				}
			});
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void onIconLoad(TextureStitchEvent.Pre event) {
		// Maybe not needed. Copied from EnderIO when textures weren't showing up but found another issue (variants need [])
		for (NostrumFluids fluid : NostrumFluids.values()) {
			event.getMap().registerSprite(fluid.getFluid().getStill());
			event.getMap().registerSprite(fluid.getFluid().getFlowing());
		}
	}
	
	@SubscribeEvent
	public void registerColorHandlers(ColorHandlerEvent.Item ev) {
		IItemColor tinter = new IItemColor() {
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
				EMagicElement element = EssenceItem.findType(stack);
				return element.getColor();
			}
			
		};
		ev.getItemColors().registerItemColorHandler(tinter,
				EssenceItem.instance()
				);
	}
	
	public static void registerModel(Item item, int meta, String modelName) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(NostrumMagica.MODID + ":" + modelName, "inventory"));
	}
	
	private void registerEntityRenderers() {
		
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellProjectile.class, new IRenderFactory<EntitySpellProjectile>() {
			@Override
			public Render<? super EntitySpellProjectile> createRenderFor(RenderManager manager) {
				return new RenderSpellProjectile(manager, 1f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellBullet.class, new IRenderFactory<EntitySpellBullet>() {
			@Override
			public Render<? super EntitySpellBullet> createRenderFor(RenderManager manager) {
				return new RenderSpellBullet(manager, 1f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGolem.class, new IRenderFactory<EntityGolem>() {
			@Override
			public Render<? super EntityGolem> createRenderFor(RenderManager manager) {
				return new RenderGolem(manager, new ModelGolem(), .8f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityKoid.class, new IRenderFactory<EntityKoid>() {
			@Override
			public Render<? super EntityKoid> createRenderFor(RenderManager manager) {
				return new RenderKoid(manager, .3f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonRed.class, new IRenderFactory<EntityDragonRed>() {
			@Override
			public Render<? super EntityDragonRed> createRenderFor(RenderManager manager) {
				return new RenderDragonRed(manager, 5);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityTameDragonRed.class, new IRenderFactory<EntityTameDragonRed>() {
			@Override
			public Render<? super EntityTameDragonRed> createRenderFor(RenderManager manager) {
				return new RenderTameDragonRed(manager, 2);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityShadowDragonRed.class, new IRenderFactory<EntityShadowDragonRed>() {
			@Override
			public Render<? super EntityShadowDragonRed> createRenderFor(RenderManager manager) {
				return new RenderShadowDragonRed(manager, 2);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySprite.class, new IRenderFactory<EntitySprite>() {
			@Override
			public Render<? super EntitySprite> createRenderFor(RenderManager manager) {
				return  new RenderSprite(manager, .7f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonEgg.class, new IRenderFactory<EntityDragonEgg>() {
			@Override
			public Render<? super EntityDragonEgg> createRenderFor(RenderManager manager) {
				return new RenderDragonEgg(manager, .45f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellSaucer.class, new IRenderFactory<EntitySpellSaucer>() {
			@Override
			public Render<? super EntitySpellSaucer> createRenderFor(RenderManager manager) {
				return new RenderMagicSaucer(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySwitchTrigger.class, new IRenderFactory<EntitySwitchTrigger>() {
			@Override
			public Render<? super EntitySwitchTrigger> createRenderFor(RenderManager manager) {
				return new RenderSwitchTrigger(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(NostrumTameLightning.class, new IRenderFactory<NostrumTameLightning>() {
			@Override
			public Render<? super NostrumTameLightning> createRenderFor(RenderManager manager) {
				return new RenderLightningBolt(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityHookShot.class, new IRenderFactory<EntityHookShot>() {
			@Override
			public Render<? super EntityHookShot> createRenderFor(RenderManager manager) {
				return new RenderHookShot(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityWisp.class, new IRenderFactory<EntityWisp>() {
			@Override
			public Render<? super EntityWisp> createRenderFor(RenderManager manager) {
				return new RenderWisp(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityLux.class, new IRenderFactory<EntityLux>() {
			@Override
			public Render<? super EntityLux> createRenderFor(RenderManager manager) {
				return new RenderLux(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityWillo.class, new IRenderFactory<EntityWillo>() {
			@Override
			public Render<? super EntityWillo> createRenderFor(RenderManager manager) {
				return new RenderWillo(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityArcaneWolf.class, new IRenderFactory<EntityArcaneWolf>() {
			@Override
			public Render<? super EntityArcaneWolf> createRenderFor(RenderManager manager) {
				return new RenderArcaneWolf(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlantBoss.class, new IRenderFactory<EntityPlantBoss>() {
			@Override
			public Render<? super EntityPlantBoss> createRenderFor(RenderManager manager) {
				return new RenderPlantBoss(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlantBoss.PlantBossLeafLimb.class, new IRenderFactory<EntityPlantBoss.PlantBossLeafLimb>() {
			@Override
			public Render<? super EntityPlantBoss.PlantBossLeafLimb> createRenderFor(RenderManager manager) {
				return new RenderPlantBossLeaf(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellMortar.class, new IRenderFactory<EntitySpellMortar>() {
			@Override
			public Render<? super EntitySpellMortar> createRenderFor(RenderManager manager) {
				return new RenderSpellMortar(manager, 1f);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlantBossBramble.class, new IRenderFactory<EntityPlantBossBramble>() {
			@Override
			public Render<? super EntityPlantBossBramble> createRenderFor(RenderManager manager) {
				return new RenderPlantBossBramble(manager);
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
	public void onMouse(MouseEvent event) {
		int wheel = event.getDwheel();
		if (wheel != 0) {
			
			if (!NostrumMagica.getMagicWrapper(Minecraft.getInstance().player)
					.isUnlocked())
				return;
			ItemStack tome = NostrumMagica.getCurrentTome(Minecraft.getInstance().player);
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
		if (bindingCast.isPressed())
			doCast();
		else if (bindingInfo.isPressed()) {
			PlayerEntity player = Minecraft.getInstance().player;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			Minecraft.getInstance().displayGuiScreen(new InfoScreen(attr, (String) null));
//			player.openGui(NostrumMagica.instance,
//					NostrumGui.infoscreenID, player.world, 0, 0, 0);
		} else if (Minecraft.getInstance().gameSettings.keyBindJump.isPressed()) {
			PlayerEntity player = Minecraft.getInstance().player;
			if (player.isRiding() && player.getRidingEntity() instanceof EntityTameDragonRed) {
				((EntityDragon) player.getRidingEntity()).dragonJump();
			} else if (player.isRiding() && player.getRidingEntity() instanceof EntityArcaneWolf) {
				((EntityArcaneWolf) player.getRidingEntity()).wolfJump();
			}
		} else if (bindingBladeCast.isPressed()) {
			PlayerEntity player = Minecraft.getInstance().player;
			if (player.getCooledAttackStrength(0.5F) > .95) {
				player.resetCooldown();
				//player.swingArm(Hand.MAIN_HAND);
				doBladeCast();
			}
			
		} else if (bindingPetPlacementModeCycle.isPressed()) {
			// Cycle placement mode
			final PetPlacementMode current = NostrumMagica.getPetCommandManager().getPlacementMode(this.getPlayer());
			final PetPlacementMode next = PetPlacementMode.values()[(current.ordinal() + 1) % PetPlacementMode.values().length];
			
			// Set up client to have this locally
			NostrumMagica.getPetCommandManager().setPlacementMode(getPlayer(), next);
			
			// Update client icon
			this.overlayRenderer.changePetPlacementIcon();
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllPlacementMode(next));
		} else if (bindingPetTargetModeCycle.isPressed()) {
			// Cycle target mode
			final PetTargetMode current = NostrumMagica.getPetCommandManager().getTargetMode(this.getPlayer());
			final PetTargetMode next = PetTargetMode.values()[(current.ordinal() + 1) % PetTargetMode.values().length];
			
			// Update client icon
			this.overlayRenderer.changePetTargetIcon();
			
			// Set up client to have this locally
			NostrumMagica.getPetCommandManager().setTargetMode(getPlayer(), next);
			
			// Send change to server
			NetworkHandler.sendToServer(PetCommandMessage.AllTargetMode(next));
		} else if (bindingPetAttackAll.isPressed()) {
			// Raytrace, find tar get, and set all to attack
			final PlayerEntity player = getPlayer();
			if (player != null && player.world != null) {
				final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
				final List<LivingEntity> tames = NostrumMagica.getTamedEntities(player);
				RayTraceResult result = RayTrace.raytraceApprox(
						player.world,
						player.getEyePosition(partialTicks),
						player.getLook(partialTicks),
						100, (e) -> { return e != player && e instanceof LivingEntity && !player.isOnSameTeam(e) && !tames.contains(e);},
						1);
				if (result != null && result.entityHit != null) {
					NetworkHandler.sendToServer(PetCommandMessage.AllAttack((LivingEntity) result.entityHit));
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
							player.world,
							player.getEyePosition(partialTicks),
							player.getLook(partialTicks),
							100, (e) -> { return e != player && tames.contains(e);},
							.1);
					if (result != null && result.entityHit != null) {
						selectedPet = (LivingEntity) result.entityHit;
						if (selectedPet.world.isRemote) {
							selectedPet.setGlowing(true);
						}
					}
				} else {
					// Find target
					RayTraceResult result = RayTrace.raytraceApprox(
							player.world,
							player.getEyePosition(partialTicks),
							player.getLook(partialTicks),
							100, (e) -> { return e != player && e instanceof LivingEntity && !player.isOnSameTeam(e) && !tames.contains(e);},
							1);
					if (result != null && result.entityHit != null) {
						NetworkHandler.sendToServer(PetCommandMessage.PetAttack(selectedPet, (LivingEntity) result.entityHit));
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
		
		Spell spell = NostrumMagica.getCurrentSpell(Minecraft.getInstance().player);
		if (spell == null) {
			System.out.println("LOUD NULL SPELL"); // TODO remove
			return;
		}
		
		// Do mana check here (it's also done on server)
		// to stop redundant checks and get mana looking good
		// on client side immediately
		PlayerEntity player = Minecraft.getInstance().player;
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
		IInventory baubles = NostrumMagica.baubles.getBaubles(player);
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
				if (dragon.sharesMana(Minecraft.getInstance().player)) {
					mana += dragon.getMana();
				}
			}
		}
		
		if (!Minecraft.getInstance().player.isCreative()) {
			// Check mana
			if (mana < cost) {
				
				for (int i = 0; i < 15; i++) {
					double offsetx = Math.cos(i * (2 * Math.PI / 15)) * 1.0;
					double offsetz = Math.sin(i * (2 * Math.PI / 15)) * 1.0;
					player.world
						.addParticle(ParticleTypes.SMOKE_LARGE,
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
	    		
	    		if (level == 1) {
	    			Boolean know = att.getKnownElements().get(elem);
	    			if (know == null || !know) {
	    				player.sendMessage(new TranslationTextComponent(
								"info.spell.no_mastery", new Object[] {elem.getName()}));
	    				System.out.println("LOUD NO MASTERY"); // TODO remove
						NostrumMagicaSounds.CAST_FAIL.play(player);
						return;
	    			}
				} else {
		    		Integer mast = att.getElementMastery().get(elem);
		    		int mastery = (mast == null ? 0 : mast);
		    		if (mastery < level) {
		    			player.sendMessage(new TranslationTextComponent(
							"info.spell.low_mastery", new Object[] {elem.getName(), level, mastery}));
						NostrumMagicaSounds.CAST_FAIL.play(player);
						return;
		    		}
				}
	    	}
			
			// Check reagents
			// Skip check if there's a server-side chance of it still working anyways
			if (summary.getReagentCost() >= 1f) {
				Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(player, row.getKey());
					if (count < row.get()) {
						player.sendMessage(new TranslationTextComponent("info.spell.bad_reagent", row.getKey().prettyName()));
						System.out.println("LOUD BAD REAGENT"); // TODO remove
						return;
					}
				}
				
				// Don't actually deduct on client.
				// Response from server will result in deduct if it goes through
			}
			
			NostrumMagica.getMagicWrapper(Minecraft.getInstance().player)
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
		return Minecraft.getInstance().player;
	}
	
	private INostrumMagic overrides = null;
	@Override
	public void receiveStatOverrides(INostrumMagic override) {
		// If we can look up stats, apply them.
		// Otherwise, stash them for loading when we apply attributes
		PlayerEntity player = Minecraft.getInstance().player;
		INostrumMagic existing = NostrumMagica.getMagicWrapper(player);
		if (existing != null && !player.isDead) {
			// apply them
			existing.copy(override);
			
			// If we're on a screen that cares, refresh it
			if (Minecraft.getInstance().currentScreen instanceof MirrorGui) {
				((MirrorGui) Minecraft.getInstance().currentScreen).refresh();
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
		
		INostrumMagic existing = NostrumMagica.getMagicWrapper(Minecraft.getInstance().player);
		
		if (existing == null)
			return; // Mana got here before we attached
		
		existing.copy(overrides);
		
		overrides = null;
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
		; // Do nothing
	}
	
	@Override
	public void openSpellScreen(Spell spell) {
		Minecraft.getInstance().displayGuiScreen(new ScrollScreen(spell));
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
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/koid"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/golem_ender"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/dragon_C"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/dragon_scales"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/dragon_scales_gold"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/dragon_scales_diamond"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/sprite_core"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/sprite_arms"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/magic_blade"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "blocks/portal"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/blade"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/hilt"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/ruby"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/item/wood"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/white"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/crystal"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/crystal_blank"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "entity/dragonflightwing"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/aether_cloak_decor"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/aether_cloak_inside"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "models/armor/aether_cloak_outside"));
		

		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/mist_bad"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/mist_good"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/thornskin"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting1"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting2"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting3"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting4"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/ting5"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/shield"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/arrow_down"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/arrow_up"));
		event.getMap().registerSprite(new ResourceLocation(
				NostrumMagica.MODID, "effects/slate"));
		event.getMap().registerSprite(new ResourceLocation(
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
    	
		
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
    	for (ClientEffectIcon icon: ClientEffectIcon.values()) {
    		IModel model;
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(
						NostrumMagica.MODID, "effect/" + icon.getModelKey()
						));
				IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
	    		event.getModelRegistry().putObject(
	    				new ModelResourceLocation(NostrumMagica.MODID + ":effects/" + icon.getKey(), "normal"),
	    				bakedModel);
			} catch (Exception e) {
				e.printStackTrace();
				NostrumMagica.logger.warn("Failed to load effect " + icon.getKey());
			}
    		
    	}
    	
//    	for (String key : new String[] {"orb_cloudy", "orb_scaled"}) {
//    		IModel model;
//			try {
//				model = ModelLoaderRegistry.getModel(new ResourceLocation(
//						NostrumMagica.MODID, "effect/" + key + ".obj"
//						));
//				IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, 
//	    				(location) -> {return Minecraft.getInstance().getTextureMapBlocks().getAtlasSprite(location.toString());});
//	    		event.getModelRegistry().putObject(
//	    				new ModelResourceLocation(NostrumMagica.MODID + ":effects/" + key, "normal"),
//	    				bakedModel);
//			} catch (Exception e) {
//				e.printStackTrace();
//				NostrumMagica.logger.warn("Failed to load effect " + key);
//			}	
//    	}
    	
    	MimicBlockBakedModel model = new MimicBlockBakedModel();
    	for (Direction facing : Direction.values()) {
    		event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_DOOR), "facing=" + facing.name().toLowerCase() + ",unbreakable=false"),
    				model);
    		event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_DOOR), "facing=" + facing.name().toLowerCase() + ",unbreakable=true"),
    				model);
    		event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_FACADE), "facing=" + facing.name().toLowerCase() + ",unbreakable=false"),
    				model);
    		event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_FACADE), "facing=" + facing.name().toLowerCase() + ",unbreakable=true"),
    				model);
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
								new ClientEffectFormBasic(ClientEffectIcon.TING1, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING3, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.ARROWD, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.ARROWU, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING3, 0, 0, 0),
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
								new ClientEffectFormBasic(ClientEffectIcon.TING5, 0, 0, 0),
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
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVector() : targetPos).addVector(0, 1, 0),
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
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
							new ClientEffectFormBasic(ClientEffectIcon.TING1, 0, 0, 0),
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
					ClientEffect effect = new ClientEffectMirrored((targetPos == null ? target.getPositionVector() : targetPos).addVector(0, 1, 0),
							new ClientEffectFormBasic(ClientEffectIcon.TING4, 0, 0, 0),
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
			Minecraft.getInstance().player.sendMessage(
					new TranslationTextComponent("info.nostrumwelcome.text", new Object[]{
							this.bindingInfo.getDisplayName()
					}));
			ClientProxy.shownText = true;
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
