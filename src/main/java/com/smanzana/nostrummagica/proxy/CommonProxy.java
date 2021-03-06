package com.smanzana.nostrummagica.proxy;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ActiveHopper;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.blocks.CropEssence;
import com.smanzana.nostrummagica.blocks.CropGinseng;
import com.smanzana.nostrummagica.blocks.CropMandrakeRoot;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.ItemDuct;
import com.smanzana.nostrummagica.blocks.LogicDoor;
import com.smanzana.nostrummagica.blocks.LoreTable;
import com.smanzana.nostrummagica.blocks.MagicDirt;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.ManiCrystal;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.blocks.NostrumSpawnAndTrigger;
import com.smanzana.nostrummagica.blocks.ObeliskPortal;
import com.smanzana.nostrummagica.blocks.ProgressionDoor;
import com.smanzana.nostrummagica.blocks.PutterBlock;
import com.smanzana.nostrummagica.blocks.ShrineBlock;
import com.smanzana.nostrummagica.blocks.SorceryPortal;
import com.smanzana.nostrummagica.blocks.SorceryPortalSpawner;
import com.smanzana.nostrummagica.blocks.SpellTable;
import com.smanzana.nostrummagica.blocks.SwitchBlock;
import com.smanzana.nostrummagica.blocks.SymbolBlock;
import com.smanzana.nostrummagica.blocks.TeleportRune;
import com.smanzana.nostrummagica.blocks.TeleportationPortal;
import com.smanzana.nostrummagica.blocks.TemporaryTeleportationPortal;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.config.network.ServerConfigMessage;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityHookShot;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.entity.EntitySprite;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.NostrumTameLightning;
import com.smanzana.nostrummagica.entity.PetInfo;
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
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ChalkItem;
import com.smanzana.nostrummagica.items.DragonArmor;
import com.smanzana.nostrummagica.items.DragonEgg;
import com.smanzana.nostrummagica.items.DragonEggFragment;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.HookshotItem;
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
import com.smanzana.nostrummagica.items.NostrumRoseItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentSeed;
import com.smanzana.nostrummagica.items.RuneBag;
import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.items.ShrineSeekingGem;
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
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientEffectRenderMessage;
import com.smanzana.nostrummagica.network.messages.MagicEffectUpdate;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.network.messages.TamedDragonGUIOpenMessage;
import com.smanzana.nostrummagica.potions.FamiliarPotion;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.LightningChargePotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicBuffPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DamagedTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.DelayTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator;
import com.smanzana.nostrummagica.world.NostrumFlowerGenerator;
import com.smanzana.nostrummagica.world.NostrumOreGenerator;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
	
	public CapabilityHandler capabilityHandler;
	
	public void preinit() {
		CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic.class);
		capabilityHandler = new CapabilityHandler();
		NetworkHandler.getInstance();
		NostrumMagicaSounds.registerSounds();
		
		PetInfo.PetAction.Init();
		
    	registerShapes();
    	registerTriggers();
    	
    	int entityID = 0;
    	EntityRegistry.registerModEntity(EntitySpellProjectile.class, "spell_projectile",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			true
    			);
    	EntityRegistry.registerModEntity(EntityGolemPhysical.class, "physical_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemLightning.class, "lightning_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemFire.class, "fire_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemEarth.class, "earth_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemIce.class, "ice_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemWind.class, "wind_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGolemEnder.class, "ender_golem",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityKoid.class, "entity_koid",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntityDragonRed.class, "entity_dragon_red", 
    			entityID++,
    			NostrumMagica.instance,
    			128,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntityTameDragonRed.class, "entity_tame_dragon_red", 
    			entityID++,
    			NostrumMagica.instance,
    			128,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntityShadowDragonRed.class, "entity_shadow_dragon_red", 
    			entityID++,
    			NostrumMagica.instance,
    			128,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntitySprite.class, "entity_sprite", 
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntityDragonEgg.class, "entity_dragon_egg",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntityChakramSpellSaucer.class, "entity_internal_spellsaucer_chakram",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			true);
    	EntityRegistry.registerModEntity(EntityCyclerSpellSaucer.class, "entity_internal_spellsaucer_cycler",
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			true);
    	EntityRegistry.registerModEntity(EntitySwitchTrigger.class, "entity_switch_trigger",
    			entityID++,
    			NostrumMagica.instance,
    			128,
    			1,
    			false);
    	EntityRegistry.registerModEntity(NostrumTameLightning.class, "nostrum_lightning",
    			entityID++,
    			NostrumMagica.instance,
    			128,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntityHookShot.class, "nostrum_hookshot",
    			entityID++,
    			NostrumMagica.instance,
    			128,
    			1,
    			true);
    	EntityRegistry.registerModEntity(EntityWisp.class, "entity_wisp", 
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false);
    	EntityRegistry.registerModEntity(EntityAreaEffect.class, "entity_effect_cloud", 
    			entityID++,
    			NostrumMagica.instance,
    			64,
    			1,
    			false);
    	
    	EntityRegistry.addSpawn(EntityKoid.class, 20, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.MAGICAL));
    	EntityRegistry.addSpawn(EntityKoid.class, 20, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.FOREST));
    	EntityRegistry.addSpawn(EntityKoid.class, 20, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.SNOWY));
    	EntityRegistry.addSpawn(EntityKoid.class, 20, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.NETHER));
    	EntityRegistry.addSpawn(EntityKoid.class, 20, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.SPOOKY));
    	EntityRegistry.addSpawn(EntitySprite.class, 15, 1, 3, EnumCreatureType.MONSTER,
    			ForgeRegistries.BIOMES.getValues().stream().filter( (biome) -> {
    				return !BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.END)
    						&& BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.NETHER);
    			}).collect(Collectors.toList()).toArray(new Biome[0]));
    	EntityRegistry.addSpawn(EntityWisp.class, 1, 1, 2, EnumCreatureType.AMBIENT, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.MAGICAL));
    	EntityRegistry.addSpawn(EntityWisp.class, 1, 1, 2, EnumCreatureType.AMBIENT, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.FOREST));
    	EntityRegistry.addSpawn(EntityWisp.class, 1, 1, 2, EnumCreatureType.AMBIENT, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.SNOWY));
    	EntityRegistry.addSpawn(EntityWisp.class, 13, 1, 2, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.NETHER));
    	EntityRegistry.addSpawn(EntityWisp.class, 1, 1, 2, EnumCreatureType.AMBIENT, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.SPOOKY));
    	

    	EntityRegistry.addSpawn(EntityShadowDragonRed.class, 15, 1, 2, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.NETHER));
    	EntityRegistry.addSpawn(EntityTameDragonRed.class, 2, 1, 1, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.NETHER));

    	registerItems();
    	registerBlocks();
    	
    	EntityTameDragonRed.init();
	}
	
	public void init() {
    	registerPotions();
    	
    	// Moved here because this loads NostrumDungeonGenerator which needs to be after DungeonRoomRegistry is initted
    	// BUT we can't just move that to before we register blocks or all the sudden all the blueprints are lying
    	ShrineSeekingGem.init();
    	
    	GameRegistry.register(EnchantmentManaRecovery.instance(),
    			new ResourceLocation(NostrumMagica.MODID, EnchantmentManaRecovery.ID));
    	
    	GameRegistry.registerWorldGenerator(new NostrumOreGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumFlowerGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumDungeonGenerator(), 0);
    	
    	NetworkRegistry.INSTANCE.registerGuiHandler(NostrumMagica.instance, new NostrumGui());
    	
    	LoreRegistry.instance();
	}
	
	public void postinit() {
		for (Biome biome : Biome.REGISTRY) {
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.MIDNIGHT_IRIS), 8);
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.CRYSTABLOOM), 7);
		}
		
		NostrumQuest.Validate();
		NostrumResearch.Validate();
	}
    
    private void registerShapes() {
    	SpellShape.register(SingleShape.instance());
    	SpellShape.register(AoEShape.instance());
    	SpellShape.register(ChainShape.instance());
    }
    
    private void registerTriggers() {
    	SpellTrigger.register(SelfTrigger.instance());
    	SpellTrigger.register(TouchTrigger.instance());
    	SpellTrigger.register(AITargetTrigger.instance());
    	SpellTrigger.register(ProjectileTrigger.instance());
    	SpellTrigger.register(BeamTrigger.instance());
    	SpellTrigger.register(DelayTrigger.instance());
    	SpellTrigger.register(ProximityTrigger.instance());
    	SpellTrigger.register(HealthTrigger.instance());
    	SpellTrigger.register(FoodTrigger.instance());
    	SpellTrigger.register(ManaTrigger.instance());
    	SpellTrigger.register(DamagedTrigger.instance());
    	SpellTrigger.register(OtherTrigger.instance());
    	SpellTrigger.register(MagicCutterTrigger.instance());
    	SpellTrigger.register(MagicCyclerTrigger.instance());
    }
    
    private void registerPotions() {
    	RootedPotion.instance();
    	MagicResistPotion.instance();
    	PhysicalShieldPotion.instance();
    	MagicShieldPotion.instance();
    	FrostbitePotion.instance();
    	MagicBoostPotion.instance();
    	MagicBuffPotion.instance();
    	FamiliarPotion.instance();
    	LightningChargePotion.instance();
    }
    
    private void registerItems() {
    	NostrumGuide.instance().setRegistryName(NostrumMagica.MODID, NostrumGuide.id);
    	GameRegistry.register(NostrumGuide.instance());
    	NostrumGuide.init();
    	
    	SpellcraftGuide.instance().setRegistryName(NostrumMagica.MODID, SpellcraftGuide.id);
    	GameRegistry.register(SpellcraftGuide.instance());
    	SpellcraftGuide.init();
    	
    	SpellTome.instance().setRegistryName(NostrumMagica.MODID, SpellTome.id);
    	GameRegistry.register(SpellTome.instance());
    	SpellPlate.instance().setRegistryName(NostrumMagica.MODID, SpellPlate.id);
    	GameRegistry.register(SpellPlate.instance());
    	BlankScroll.instance().setRegistryName(NostrumMagica.MODID, BlankScroll.id);
    	GameRegistry.register(BlankScroll.instance());
    	BlankScroll.init();
    	
    	SpellScroll.instance().setRegistryName(NostrumMagica.MODID, SpellScroll.id);
    	GameRegistry.register(SpellScroll.instance());
    	SpellScroll.init();
    	
    	SpellTableItem.instance().setRegistryName(NostrumMagica.MODID, SpellTableItem.ID);
    	GameRegistry.register(SpellTableItem.instance());
    	SpellTableItem.init();
    	
    	MirrorItem.instance().setRegistryName(NostrumMagica.MODID, MirrorItem.ID);
    	GameRegistry.register(MirrorItem.instance());
    	MirrorItem.init();
    	
    	MagicSwordBase.init();
    	MagicArmorBase.init();
    	EnchantedWeapon.registerWeapons();
    	EnchantedArmor.registerArmors();
    	DragonArmor.registerArmors();
    	
    	MirrorShield.instance().setRegistryName(NostrumMagica.MODID, MirrorShield.id);
    	GameRegistry.register(MirrorShield.instance());
    	
    	MirrorShieldImproved.instance().setRegistryName(NostrumMagica.MODID, MirrorShieldImproved.id);
    	GameRegistry.register(MirrorShieldImproved.instance());
    	
    	ReagentItem.instance().setRegistryName(NostrumMagica.MODID, ReagentItem.ID);
    	GameRegistry.register(ReagentItem.instance());
    	ReagentItem.init();
    	InfusedGemItem.instance().setRegistryName(NostrumMagica.MODID, InfusedGemItem.ID);
    	GameRegistry.register(InfusedGemItem.instance());
    	InfusedGemItem.init();
    	SpellRune.instance().setRegistryName(NostrumMagica.MODID, SpellRune.ID);
    	GameRegistry.register(SpellRune.instance());
    	SpellRune.init();
    	
    	NostrumResourceItem.instance().setRegistryName(NostrumMagica.MODID, NostrumResourceItem.ID);
    	GameRegistry.register(NostrumResourceItem.instance());
    	NostrumResourceItem.init();
    	
    	ReagentBag.instance().setRegistryName(NostrumMagica.MODID, ReagentBag.id);
    	GameRegistry.register(ReagentBag.instance());
    	ReagentBag.init();
    	
    	SeekerIdol.instance().setRegistryName(NostrumMagica.MODID, SeekerIdol.id);
    	GameRegistry.register(SeekerIdol.instance());
    	SeekerIdol.init();
    	
    	ShrineSeekingGem.instance().setRegistryName(NostrumMagica.MODID, ShrineSeekingGem.id);
    	GameRegistry.register(ShrineSeekingGem.instance());
    	
    	ChalkItem.instance().setRegistryName(NostrumMagica.MODID, ChalkItem.ID);
    	GameRegistry.register(ChalkItem.instance());
    	ChalkItem.init();
    	
    	AltarItem.instance().setRegistryName(NostrumMagica.MODID, AltarItem.ID);
    	GameRegistry.register(AltarItem.instance());
    	AltarItem.init();
    	
    	PositionCrystal.instance().setRegistryName(NostrumMagica.MODID, PositionCrystal.ID);
    	GameRegistry.register(PositionCrystal.instance());
    	PositionCrystal.init();
    	
    	PositionToken.instance().setRegistryName(NostrumMagica.MODID, PositionToken.ID);
    	GameRegistry.register(PositionToken.instance());
    	PositionToken.init();
    	
    	SpellTomePage.instance().setRegistryName(NostrumMagica.MODID, SpellTomePage.id);
    	GameRegistry.register(SpellTomePage.instance());
    	SpellTomePage.init();
    	
    	EssenceItem.instance().setRegistryName(NostrumMagica.MODID, EssenceItem.ID);
    	GameRegistry.register(EssenceItem.instance());
    	EssenceItem.init();
    	
    	MageStaff.instance().setRegistryName(NostrumMagica.MODID, MageStaff.ID);
    	GameRegistry.register(MageStaff.instance());
    	MageStaff.init();
    	
    	ThanoPendant.instance().setRegistryName(NostrumMagica.MODID, ThanoPendant.ID);
    	GameRegistry.register(ThanoPendant.instance());
    	
    	ThanosStaff.instance().setRegistryName(NostrumMagica.MODID, ThanosStaff.ID);
    	GameRegistry.register(ThanosStaff.instance());
    	ThanosStaff.init();
    	
    	MagicCharm.instance().setRegistryName(NostrumMagica.MODID, MagicCharm.ID);
    	GameRegistry.register(MagicCharm.instance());
    	
    	RuneBag.instance().setRegistryName(NostrumMagica.MODID, RuneBag.id);
    	GameRegistry.register(RuneBag.instance());
    	RuneBag.init();
    	
    	DragonEggFragment.instance().setRegistryName(NostrumMagica.MODID, DragonEggFragment.id);
    	GameRegistry.register(DragonEggFragment.instance());
    	DragonEggFragment.init();
    	
    	DragonEgg.instance().setRegistryName(NostrumMagica.MODID, DragonEgg.ID);
    	GameRegistry.register(DragonEgg.instance());
    	DragonEgg.init();
    	
    	NostrumSkillItem.instance().setRegistryName(NostrumMagica.MODID, NostrumSkillItem.ID);
    	GameRegistry.register(NostrumSkillItem.instance());
    	//NostrumSkillItem.init();
    	
    	NostrumRoseItem.instance().setRegistryName(NostrumMagica.MODID, NostrumRoseItem.ID);
    	GameRegistry.register(NostrumRoseItem.instance());
    	
    	WarlockSword.instance().setRegistryName(NostrumMagica.MODID, WarlockSword.ID);
    	GameRegistry.register(WarlockSword.instance());
    	WarlockSword.init();
    	
    	HookshotItem.instance().setRegistryName(NostrumMagica.MODID, HookshotItem.ID);
    	GameRegistry.register(HookshotItem.instance());
    	HookshotItem.init();
    	
    	ReagentSeed.mandrake.setRegistryName(NostrumMagica.MODID, ReagentSeed.mandrake.getItemID());
    	GameRegistry.register(ReagentSeed.mandrake);
    	ReagentSeed.ginseng.setRegistryName(NostrumMagica.MODID, ReagentSeed.ginseng.getItemID());
    	GameRegistry.register(ReagentSeed.ginseng);
    	ReagentSeed.essence.setRegistryName(NostrumMagica.MODID, ReagentSeed.essence.getItemID());
    	GameRegistry.register(ReagentSeed.essence);
    }
    
    private void registerBlocks() {
    	GameRegistry.register(MagicWall.instance(),
    			new ResourceLocation(NostrumMagica.MODID, MagicWall.ID));
    	GameRegistry.register(
    			(new ItemBlock(MagicWall.instance())).setRegistryName(MagicWall.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(MagicWall.ID));

    	GameRegistry.register(CursedIce.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CursedIce.ID));
    	GameRegistry.register(
    			(new ItemBlock(CursedIce.instance())).setRegistryName(CursedIce.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(CursedIce.ID));
    	
    	GameRegistry.register(ManiOre.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ManiOre.ID));
    	GameRegistry.register(
    			(new ItemBlock(ManiOre.instance())).setRegistryName(ManiOre.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(ManiOre.ID));
    	
    	GameRegistry.register(MagicDirt.instance(),
    			new ResourceLocation(NostrumMagica.MODID, MagicDirt.ID));
    	GameRegistry.register(
    			(new ItemBlock(MagicDirt.instance())).setRegistryName(MagicDirt.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(MagicDirt.ID));
    	MagicDirt.init();
    	
    	GameRegistry.register(SpellTable.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SpellTable.ID));
    	SpellTable.init();
    	
    	NostrumMagicaFlower.init();
    	
    	GameRegistry.register(CropMandrakeRoot.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CropMandrakeRoot.ID));
    	
    	GameRegistry.register(CropGinseng.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CropGinseng.ID));
    	
    	GameRegistry.register(NostrumSingleSpawner.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumSingleSpawner.ID));
    	GameRegistry.register(
    			(new ItemBlock(NostrumSingleSpawner.instance())).setRegistryName(NostrumSingleSpawner.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(NostrumSingleSpawner.ID));
    	NostrumSingleSpawner.init();
    	
    	GameRegistry.register(NostrumSpawnAndTrigger.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumSpawnAndTrigger.ID));
    	GameRegistry.register(
    			(new ItemBlock(NostrumSpawnAndTrigger.instance())).setRegistryName(NostrumSpawnAndTrigger.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(NostrumSpawnAndTrigger.ID));
    	NostrumSpawnAndTrigger.init();
    	
    	GameRegistry.register(DungeonBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, DungeonBlock.ID));
    	String[] variants = new String[DungeonBlock.Type.values().length];
    	for (DungeonBlock.Type type : DungeonBlock.Type.values()) {
    		variants[type.ordinal()] = type.getName().toLowerCase();
    	}
    	GameRegistry.register(
    			(new ItemMultiTexture(DungeonBlock.instance(), DungeonBlock.instance(), variants))
    			.setRegistryName(DungeonBlock.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(DungeonBlock.ID));
    	
    	GameRegistry.register(SymbolBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SymbolBlock.ID));
    	SymbolBlock.init();

    	GameRegistry.register(ShrineBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ShrineBlock.ID));
    	
    	GameRegistry.register(NostrumMirrorBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumMirrorBlock.ID));
    	
    	GameRegistry.register(ChalkBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ChalkBlock.ID));
    	
    	GameRegistry.register(AltarBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, AltarBlock.ID));
    	AltarBlock.init();
    	
    	GameRegistry.register(Candle.instance(),
    			new ResourceLocation(NostrumMagica.MODID, Candle.ID));
    	GameRegistry.register(
    			(new ItemBlock(Candle.instance())).setRegistryName(Candle.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(Candle.ID));
    	Candle.init();
    	
    	GameRegistry.register(NostrumObelisk.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumObelisk.ID));
    	NostrumObelisk.init();
    	GameRegistry.register(ObeliskPortal.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ObeliskPortal.ID));
    	ObeliskPortal.init();
    	
    	GameRegistry.register(EssenceOre.instance(),
    			new ResourceLocation(NostrumMagica.MODID, EssenceOre.ID));
    	GameRegistry.register(
    			(new ItemBlock(EssenceOre.instance())).setRegistryName(EssenceOre.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(EssenceOre.ID));
    	
    	GameRegistry.register(MasteryOrb.instance(),
    			new ResourceLocation(NostrumMagica.MODID, MasteryOrb.id));
    	
    	GameRegistry.register(ModificationTable.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ModificationTable.ID));
    	GameRegistry.register(
    			(new ItemBlock(ModificationTable.instance()).setRegistryName(ModificationTable.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(ModificationTable.ID))
    			);
    	ModificationTable.init();
    	
    	GameRegistry.register(LoreTable.instance(),
    			new ResourceLocation(NostrumMagica.MODID, LoreTable.ID));
    	GameRegistry.register(
    			(new ItemBlock(LoreTable.instance()).setRegistryName(LoreTable.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(LoreTable.ID))
    			);
    	LoreTable.init();
    	
    	GameRegistry.register(SorceryPortal.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SorceryPortal.ID));
    	GameRegistry.register(
    			(new ItemBlock(SorceryPortal.instance()).setRegistryName(SorceryPortal.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(SorceryPortal.ID))
    			);
    	SorceryPortal.init();
    	
    	GameRegistry.register(TeleportationPortal.instance(),
    			new ResourceLocation(NostrumMagica.MODID, TeleportationPortal.ID));
    	TeleportationPortal.init();
    	
    	GameRegistry.register(TemporaryTeleportationPortal.instance(),
    			new ResourceLocation(NostrumMagica.MODID, TemporaryTeleportationPortal.ID));
    	TemporaryTeleportationPortal.init();
    	
    	GameRegistry.register(ProgressionDoor.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ProgressionDoor.ID));
    	GameRegistry.register(
    			(new ItemBlock(ProgressionDoor.instance()).setRegistryName(ProgressionDoor.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(ProgressionDoor.ID))
    			);
    	ProgressionDoor.init();
    	
    	GameRegistry.register(LogicDoor.instance(),
    			new ResourceLocation(NostrumMagica.MODID, LogicDoor.ID));
    	GameRegistry.register(
    			(new ItemBlock(LogicDoor.instance()).setRegistryName(LogicDoor.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(LogicDoor.ID))
    			);
    	LogicDoor.init();
    	
    	GameRegistry.register(SwitchBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SwitchBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(SwitchBlock.instance()).setRegistryName(SwitchBlock.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(SwitchBlock.ID))
    			);
    	SwitchBlock.init();
    	
    	GameRegistry.register(SorceryPortalSpawner.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SorceryPortalSpawner.ID));
    	SorceryPortalSpawner.init();
    	
    	GameRegistry.register(ManiCrystal.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ManiCrystal.ID));
    	
    	GameRegistry.register(MimicBlock.door(),
    			new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_DOOR));
    	GameRegistry.register(
    			(new ItemBlock(MimicBlock.door()).setRegistryName(MimicBlock.ID_DOOR)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(MimicBlock.ID_DOOR).setHasSubtypes(true))
    			);
    	
    	GameRegistry.register(MimicBlock.facade(),
    			new ResourceLocation(NostrumMagica.MODID, MimicBlock.ID_FACADE));
    	GameRegistry.register(
    			(new ItemBlock(MimicBlock.facade()).setRegistryName(MimicBlock.ID_FACADE)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(MimicBlock.ID_FACADE).setHasSubtypes(true))
    			);
    	
    	GameRegistry.register(TeleportRune.instance(),
    			new ResourceLocation(NostrumMagica.MODID, TeleportRune.ID));
    	GameRegistry.register(
    			(new ItemBlock(TeleportRune.instance()).setRegistryName(TeleportRune.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(TeleportRune.ID))
    			);
    	TeleportRune.init();
    	
    	GameRegistry.register(PutterBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, PutterBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(PutterBlock.instance()).setRegistryName(PutterBlock.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(PutterBlock.ID))
    			);
    	PutterBlock.init();
    	
    	GameRegistry.register(CropEssence.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CropEssence.ID));
    	//CropEssence.init();
    	
    	GameRegistry.register(ActiveHopper.instance,
    			new ResourceLocation(NostrumMagica.MODID, ActiveHopper.ID));
    	GameRegistry.register(
    			(new ItemBlock(ActiveHopper.instance).setRegistryName(ActiveHopper.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(ActiveHopper.ID))
    			);
    	ActiveHopper.init();
    	
    	GameRegistry.register(ItemDuct.instance,
    			new ResourceLocation(NostrumMagica.MODID, ItemDuct.ID));
    	GameRegistry.register(
    			(new ItemBlock(ItemDuct.instance).setRegistryName(ItemDuct.ID)
    					.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(ItemDuct.ID))
    			);
    	ItemDuct.init();
    }
    
    public void syncPlayer(EntityPlayerMP player) {
    	INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
    	attr.refresh(player);
    	NetworkHandler.getSyncChannel().sendTo(
    			new StatSyncMessage(NostrumMagica.getMagicWrapper(player)),
    			player);
    	NetworkHandler.getSyncChannel().sendTo(
    			new SpellRequestReplyMessage(NostrumMagica.getSpellRegistry().getAllSpells(), true),
    			player);
    }
    
    public void updatePlayerEffect(EntityPlayerMP player, SpecialEffect effectType, EffectData data) {
    	NetworkHandler.getSyncChannel().sendTo(
    			new MagicEffectUpdate(effectType, data),
    			player);
    }

	public EntityPlayer getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public void receiveStatOverrides(INostrumMagic override) {
		return; // Server side doesn't do anything
	}
	
	public void applyOverride() {
		; // do nothing
	}

	public boolean isServer() {
		return true;
	}
	
	public void openBook(EntityPlayer player, GuiBook book, Object userdata) {
		; // Server does nothing
	}
	
	public void openDragonGUI(EntityPlayer player, ITameDragon dragon) {
		// This code is largely taken from FMLNetworkHandler's openGui method, but we use our own message to open the GUI on the client
		EntityPlayerMP mpPlayer = (EntityPlayerMP) player;
		DragonContainer container = dragon.getGUIContainer(player);
		mpPlayer.getNextWindowId();
		mpPlayer.closeContainer();
        int windowId = mpPlayer.currentWindowId;
        mpPlayer.openContainer = container;
        mpPlayer.openContainer.windowId = windowId;
        mpPlayer.openContainer.addListener(mpPlayer);
        
        // Open GUI on client
        TamedDragonGUIOpenMessage message = new TamedDragonGUIOpenMessage(dragon, windowId, container.getContainerID(), container.getSheetCount());
        NetworkHandler.getSyncChannel().sendTo(message, mpPlayer);
	}

	public void sendServerConfig(EntityPlayerMP player) {
		ModConfig.channel.sendTo(new ServerConfigMessage(ModConfig.config), player);
	}
	
	public void sendSpellDebug(EntityPlayer player, ITextComponent comp) {
		NetworkHandler.getSyncChannel().sendTo(new SpellDebugMessage(comp), (EntityPlayerMP) player);
	}
	
	public String getTranslation(String key) {
		return key; // This is the server, silly!
	}
	
	public void requestObeliskTransportation(BlockPos origin, BlockPos target) {
		; // server does nothing
	}
	
	public void setObeliskIndex(BlockPos obeliskPos, int index) {
		; // server does nothing
	}
	
	public void requestStats(EntityLivingBase entity) {
		;
	}
	
	/**
	 * Spawns an client-rendered effect.
	 * @param world Only needed if caster and target are null
	 * @param comp
	 * @param caster
	 * @param casterPos
	 * @param target
	 * @param targetPos
	 * @param flavor Optional component used to flavor the effect.
	 * @param negative whether the effect should be considered 'negative'/harmful
	 * @param param optional extra float param for display
	 */
	public void spawnEffect(World world, SpellComponentWrapper comp,
			EntityLivingBase caster, Vec3d casterPos,
			EntityLivingBase target, Vec3d targetPos,
			SpellComponentWrapper flavor, boolean isNegative, float compParam) {
		if (world == null) {
			if (caster == null)
				world = target.worldObj; // If you NPE here you suck. Supply a world!
			else
				world = caster.worldObj;
		}
		
		final double MAX_RANGE_SQR = 2500.0;
		
		Set<EntityPlayer> players = new HashSet<>();
		
		if (caster != null) {
			//caster.addTrackingPlayer(player);
			players.addAll(((WorldServer) world).getEntityTracker()
				.getTrackingPlayers(caster));
		}
		
		if (target != null) {
			//caster.addTrackingPlayer(player);
			players.addAll(((WorldServer) world).getEntityTracker()
				.getTrackingPlayers(target));
		}
		
		if (caster != null && caster == target && caster instanceof EntityPlayer) {
			// Very specific case here
			players.add((EntityPlayer) caster);
		}
		
		if (players.isEmpty()) {
			// Fall back to distance check against locations
			if (casterPos != null) {
				for (EntityPlayer player : world.playerEntities) {
					if (player.getDistanceSq(casterPos.xCoord, casterPos.yCoord, casterPos.zCoord) <= MAX_RANGE_SQR)
						players.add(player);
				}
			}
			
			if (targetPos != null) {
				for (EntityPlayer player : world.playerEntities) {
					if (player.getDistanceSq(targetPos.xCoord, targetPos.yCoord, targetPos.zCoord) <= MAX_RANGE_SQR)
						players.add(player);
				}
			}
		}
		
		if (!players.isEmpty()) {
			ClientEffectRenderMessage message = new ClientEffectRenderMessage(
					caster, casterPos,
					target, targetPos,
					comp, flavor,
					isNegative, compParam);
			for (EntityPlayer player : players) {
				NetworkHandler.getSyncChannel().sendTo(message, (EntityPlayerMP) player);
			}
		}
	}
	
	public void sendMana(EntityPlayer player) {
		EntityTracker tracker = ((WorldServer) player.worldObj).getEntityTracker();
		if (tracker == null)
			return;
		
		INostrumMagic stats = NostrumMagica.getMagicWrapper(player);
		final int mana;
		if (stats == null) {
			mana = 0;
		} else {
			mana = stats.getMana();
		}
		
		tracker.sendToTrackingAndSelf(player, NetworkHandler.getSyncChannel()
				.getPacketFrom(new ManaMessage(player, mana)));
	}
}
