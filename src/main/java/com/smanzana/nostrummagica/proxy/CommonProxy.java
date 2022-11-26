package com.smanzana.nostrummagica.proxy;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.config.network.ServerConfigMessage;
import com.smanzana.nostrummagica.crafting.SpellTomePageCombineRecipe;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityAreaEffect;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntityHookShot;
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
import com.smanzana.nostrummagica.entity.dragon.EntityDragonEgg;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityShadowDragonRed;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.golem.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.golem.EntityGolemFire;
import com.smanzana.nostrummagica.entity.golem.EntityGolemIce;
import com.smanzana.nostrummagica.entity.golem.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.golem.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.golem.EntityGolemWind;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBoss;
import com.smanzana.nostrummagica.entity.plantboss.EntityPlantBossBramble;
import com.smanzana.nostrummagica.fluids.FluidPoisonWater;
import com.smanzana.nostrummagica.fluids.FluidPoisonWater.FluidPoisonWaterBlock;
import com.smanzana.nostrummagica.fluids.NostrumFluids;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientEffectRenderMessage;
import com.smanzana.nostrummagica.network.messages.MagicEffectUpdate;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.network.messages.PetGUIOpenMessage;
import com.smanzana.nostrummagica.network.messages.SpawnNostrumRitualEffectMessage;
import com.smanzana.nostrummagica.network.messages.SpawnPredefinedEffectMessage;
import com.smanzana.nostrummagica.network.messages.SpawnPredefinedEffectMessage.PredefinedEffect;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.potions.FamiliarPotion;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.LightningAttackPotion;
import com.smanzana.nostrummagica.potions.LightningChargePotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicBuffPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.NaturesBlessingPotion;
import com.smanzana.nostrummagica.potions.NostrumTransformationPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.serializers.ArcaneWolfElementalTypeSerializer;
import com.smanzana.nostrummagica.serializers.DragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializers.FloatArraySerializer;
import com.smanzana.nostrummagica.serializers.HookshotTypeDataSerializer;
import com.smanzana.nostrummagica.serializers.MagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.OptionalDragonArmorMaterialSerializer;
import com.smanzana.nostrummagica.serializers.OptionalMagicElementDataSerializer;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.serializers.PlantBossTreeTypeSerializer;
import com.smanzana.nostrummagica.serializers.WilloStatusSerializer;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.EMagicElement;
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
import com.smanzana.nostrummagica.spells.components.triggers.FieldTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCutterTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MagicCyclerTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.MortarTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.WallTrigger;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator;
import com.smanzana.nostrummagica.world.NostrumFlowerGenerator;
import com.smanzana.nostrummagica.world.NostrumOreGenerator;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {
	
	public CapabilityHandler capabilityHandler;
	
	public CommonProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void preinit() {
		//MinecraftForge.EVENT_BUS.register(this);
		
		CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic::new);
		capabilityHandler = new CapabilityHandler();
		NetworkHandler.getInstance();
		
		// Fluids registered with blocks
		//registerFluids();
		
    	registerShapes();
    	registerTriggers();
    	
    	EntityTameDragonRed.init();
    	
    	new NostrumItems();
    	new NostrumBlocks();
	}
	
	public void init() {
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
    	SpellTrigger.register(SeekingBulletTrigger.instance());
    	SpellTrigger.register(WallTrigger.instance());
    	SpellTrigger.register(MortarTrigger.instance());
    	SpellTrigger.register(FieldTrigger.instance());
    }
    
    @SubscribeEvent
    public void registerPotions(RegistryEvent.Register<Potion> event) {
    	final IForgeRegistry<Potion> registry = event.getRegistry();
    	
    	registry.register(RootedPotion.instance());
    	registry.register(MagicResistPotion.instance());
    	registry.register(PhysicalShieldPotion.instance());
    	registry.register(MagicShieldPotion.instance());
    	registry.register(FrostbitePotion.instance());
    	registry.register(MagicBoostPotion.instance());
    	registry.register(MagicBuffPotion.instance());
    	registry.register(FamiliarPotion.instance());
    	registry.register(LightningChargePotion.instance());
    	registry.register(LightningAttackPotion.instance());
    	registry.register(NaturesBlessingPotion.instance());
    	registry.register(NostrumTransformationPotion.instance());
    }
    
   
    
    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
    	int entityID = 0;
    	final IForgeRegistry<EntityEntry> registry = event.getRegistry();
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySpellProjectile.class)
    			.id("spell_projectile", entityID++)
    			.name(NostrumMagica.MODID + ".spell_projectile")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemPhysical.class)
    			.id("physical_golem", entityID++)
    			.name(NostrumMagica.MODID + ".physical_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemLightning.class)
    			.id("lightning_golem", entityID++)
    			.name(NostrumMagica.MODID + ".lightning_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemFire.class)
    			.id("fire_golem", entityID++)
    			.name(NostrumMagica.MODID + ".fire_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemEarth.class)
    			.id("earth_golem", entityID++)
    			.name(NostrumMagica.MODID + ".earth_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemIce.class)
    			.id("ice_golem", entityID++)
    			.name(NostrumMagica.MODID + ".ice_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemWind.class)
    			.id("wind_golem", entityID++)
    			.name(NostrumMagica.MODID + ".wind_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityGolemEnder.class)
    			.id("ender_golem", entityID++)
    			.name(NostrumMagica.MODID + ".ender_golem")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityKoid.class)
    			.id("entity_koid", entityID++)
    			.name(NostrumMagica.MODID + ".entity_koid")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
    			.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
				.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY))
				.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
				.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityDragonRed.class)
    			.id("entity_dragon_red", entityID++)
    			.name(NostrumMagica.MODID + ".entity_dragon_red")
    			.tracker(128, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityTameDragonRed.class)
    			.id("entity_tame_dragon_red", entityID++)
    			.name(NostrumMagica.MODID + ".entity_tame_dragon_red")
    			.tracker(128, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 2, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityShadowDragonRed.class)
    			.id("entity_shadow_dragon_red", entityID++)
    			.name(NostrumMagica.MODID + ".entity_shadow_dragon_red")
    			.tracker(128, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 15, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySprite.class)
    			.id("entity_sprite", entityID++)
    			.name(NostrumMagica.MODID + ".entity_sprite")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 15, 1, 3, ForgeRegistries.BIOMES.getValuesCollection().stream().filter( (biome) -> {
    				return !BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)
    						&& BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER);
    			}).collect(Collectors.toList()))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySpellBullet.class)
    			.id("spell_bullet", entityID++)
    			.name(NostrumMagica.MODID + ".spell_bullet")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityLux.class)
    			.id("entity_lux", entityID++)
    			.name(NostrumMagica.MODID + ".entity_lux")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.CREATURE, 20, 1, 3, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
    			.spawn(EnumCreatureType.CREATURE, 15, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
				.spawn(EnumCreatureType.CREATURE, 10, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.JUNGLE))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityDragonEgg.class)
    			.id("entity_dragon_egg", entityID++)
    			.name(NostrumMagica.MODID + ".entity_dragon_egg")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityChakramSpellSaucer.class)
    			.id("entity_internal_spellsaucer_chakram", entityID++)
    			.name(NostrumMagica.MODID + ".entity_internal_spellsaucer_chakram")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityCyclerSpellSaucer.class)
    			.id("entity_internal_spellsaucer_cycler", entityID++)
    			.name(NostrumMagica.MODID + ".entity_internal_spellsaucer_cycler")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySwitchTrigger.class)
    			.id("entity_switch_trigger", entityID++)
    			.name(NostrumMagica.MODID + ".entity_switch_trigger")
    			.tracker(128, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(NostrumTameLightning.class)
    			.id("nostrum_lightning", entityID++)
    			.name(NostrumMagica.MODID + ".nostrum_lightning")
    			.tracker(128, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityHookShot.class)
    			.id("nostrum_hookshot", entityID++)
    			.name(NostrumMagica.MODID + ".nostrum_hookshot")
    			.tracker(128, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityWisp.class)
    			.id("entity_wisp", entityID++)
    			.name(NostrumMagica.MODID + ".entity_wisp")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY))
    			.spawn(EnumCreatureType.AMBIENT, 1, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY))
    			.spawn(EnumCreatureType.MONSTER, 13, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityAreaEffect.class)
    			.id("entity_effect_cloud", entityID++)
    			.name(NostrumMagica.MODID + ".entity_effect_cloud")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityWillo.class)
    			.id("entity_willo", entityID++)
    			.name(NostrumMagica.MODID + ".entity_willo")
    			.tracker(64, 1, false)
    			.spawn(EnumCreatureType.MONSTER, 30, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
    			.spawn(EnumCreatureType.MONSTER, 15, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
    			.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SNOWY))
    			.spawn(EnumCreatureType.MONSTER, 30, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY))
    			.spawn(EnumCreatureType.MONSTER, 20, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.DRY))
    			.spawn(EnumCreatureType.MONSTER, 8, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityArcaneWolf.class)
    			.id("entity_arcane_wolf", entityID++)
    			.name(NostrumMagica.MODID + ".entity_arcane_wolf")
    			.tracker(64, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityPlantBoss.class)
    			.id("entity_plant_boss", entityID++)
    			.name(NostrumMagica.MODID + ".entity_plant_boss")
    			.tracker(128, 1, false)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntitySpellMortar.class)
    			.id("spell_mortar", entityID++)
    			.name(NostrumMagica.MODID + ".spell_mortar")
    			.tracker(64, 1, true)
    		.build());
    	registry.register(EntityEntryBuilder.create()
    			.entity(EntityPlantBossBramble.class)
    			.id("entity_plant_boss.bramble", entityID++)
    			.name(NostrumMagica.MODID + ".entity_plant_boss.bramble")
    			.tracker(64, 1, true)
    		.build());
    }
    
    @SubscribeEvent
    public void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
    	event.getRegistry().register(EnchantmentManaRecovery.instance());
    }
    
    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for (NostrumMagicaSounds sound : NostrumMagicaSounds.values()) {
			event.getRegistry().register(sound.getEvent());
		}
    }
    
    @SubscribeEvent
    public void registerCustomRecipes(RegistryEvent.Register<IRecipe> event) {
    	final IForgeRegistry<IRecipe> registry = event.getRegistry();
    	
    	registry.register(new SpellRune.RuneRecipe());
    	registry.register(new SpellTomePageCombineRecipe());
    }
    
    @SubscribeEvent
    public void registerDataSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
    	final IForgeRegistry<DataSerializerEntry> registry = event.getRegistry();
    	
    	registry.register(new DataSerializerEntry(DragonArmorMaterialSerializer.instance).setRegistryName("nostrum.serial.dragon_armor"));
    	registry.register(new DataSerializerEntry(OptionalDragonArmorMaterialSerializer.instance).setRegistryName("nostrum.serial.dragon_armor_opt"));
    	registry.register(new DataSerializerEntry(MagicElementDataSerializer.instance).setRegistryName("nostrum.serial.element"));
    	registry.register(new DataSerializerEntry(HookshotTypeDataSerializer.instance).setRegistryName("nostrum.serial.hookshot_type"));
    	registry.register(new DataSerializerEntry(PetJobSerializer.instance).setRegistryName("nostrum.serial.pet_job"));
    	registry.register(new DataSerializerEntry(WilloStatusSerializer.instance).setRegistryName("nostrum.serial.willo_status"));
    	registry.register(new DataSerializerEntry(ArcaneWolfElementalTypeSerializer.instance).setRegistryName("nostrum.serial.arcane_wolf_type"));
    	registry.register(new DataSerializerEntry(FloatArraySerializer.instance).setRegistryName("nostrum.serial.float_array"));
    	registry.register(new DataSerializerEntry(OptionalMagicElementDataSerializer.instance).setRegistryName("nostrum.serial.element_opt"));
    	registry.register(new DataSerializerEntry(PlantBossTreeTypeSerializer.instance).setRegistryName("nostrum.serial.plantboss_tree_type"));
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH) // Register fluids before blocks so fluids will be there for blocks
    public void registerFluids(RegistryEvent.Register<Block> event) {
    	for (NostrumFluids fluid : NostrumFluids.values()) {
    		FluidRegistry.registerFluid(fluid.getFluid());
    	}
    	
    	NostrumFluids.POISON_WATER.getFluid().setBlock(new FluidPoisonWaterBlock((FluidPoisonWater) NostrumFluids.POISON_WATER.getFluid(), new MaterialLiquid(MapColor.GREEN_STAINED_HARDENED_CLAY) {
			@Override
			public boolean isReplaceable() {
				return true;
			}
			
			@Override
			public boolean blocksMovement() {
				return true; // so our liquids are not replaced by water
			}
		}, false));
    	NostrumFluids.POISON_WATER_UNBREAKABLE.getFluid().setBlock(new FluidPoisonWaterBlock((FluidPoisonWater) NostrumFluids.POISON_WATER_UNBREAKABLE.getFluid(), new MaterialLiquid(MapColor.GREEN_STAINED_HARDENED_CLAY) {
			@Override
			public boolean isReplaceable() {
				return false;
			}
			
			@Override
			public boolean blocksMovement() {
				return true; // so our liquids are not replaced by water
			}
		}, true));
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
    
    public void updateEntityEffect(EntityPlayerMP player, EntityLivingBase entity, SpecialEffect effectType, EffectData data) {
    	NetworkHandler.getSyncChannel().sendTo(
    			new MagicEffectUpdate(entity, effectType, data),
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
	
	public <T extends IEntityPet> void openPetGUI(EntityPlayer player, T pet) {
		// This code is largely taken from FMLNetworkHandler's openGui method, but we use our own message to open the GUI on the client
		EntityPlayerMP mpPlayer = (EntityPlayerMP) player;
		PetContainer<?> container = pet.getGUIContainer(player);
		mpPlayer.getNextWindowId();
		mpPlayer.closeContainer();
        int windowId = mpPlayer.currentWindowId;
        mpPlayer.openContainer = container;
        mpPlayer.openContainer.windowId = windowId;
        mpPlayer.openContainer.addListener(mpPlayer);
        
        // Open GUI on client
        PetGUIOpenMessage message = new PetGUIOpenMessage(pet, windowId, container.getContainerID(), container.getSheetCount());
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
				world = target.world; // If you NPE here you suck. Supply a world!
			else
				world = caster.world;
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
					if (player.getDistanceSq(casterPos.x, casterPos.y, casterPos.z) <= MAX_RANGE_SQR)
						players.add(player);
				}
			}
			
			if (targetPos != null) {
				for (EntityPlayer player : world.playerEntities) {
					if (player.getDistanceSq(targetPos.x, targetPos.y, targetPos.z) <= MAX_RANGE_SQR)
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
		EntityTracker tracker = ((WorldServer) player.world).getEntityTracker();
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
	
	public void playRitualEffect(World world, BlockPos pos, EMagicElement element,
			ItemStack center, @Nullable NonNullList<ItemStack> extras, ReagentType[] types, ItemStack output) {
		Set<EntityPlayer> players = new HashSet<>();
		final double MAX_RANGE_SQR = 2500.0;
		if (pos != null) {
			for (EntityPlayer player : world.playerEntities) {
				if (player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <= MAX_RANGE_SQR)
					players.add(player);
			}
		}
		
		if (!players.isEmpty()) {
			SpawnNostrumRitualEffectMessage message = new SpawnNostrumRitualEffectMessage(
					//int dimension, BlockPos pos, ReagentType[] reagents, ItemStack center, @Nullable NonNullList<ItemStack> extras, ItemStack output
					world.provider.getDimension(),
					pos, element, types, center, extras, output
					);
			for (EntityPlayer player : players) {
				NetworkHandler.getSyncChannel().sendTo(message, (EntityPlayerMP) player);
			}
		}
	}
	
	public void playPredefinedEffect(PredefinedEffect type, int duration, World world, Vec3d position) {
		playPredefinedEffect(new SpawnPredefinedEffectMessage(type, duration, world.provider.getDimension(), position), world, position);
	}
	
	public void playPredefinedEffect(PredefinedEffect type, int duration, World world, Entity entity) {
		playPredefinedEffect(new SpawnPredefinedEffectMessage(type, duration, world.provider.getDimension(), entity.getEntityId()), world, entity.getPositionVector());
	}
	
	private void playPredefinedEffect(SpawnPredefinedEffectMessage message, World world, Vec3d center) {
		Set<EntityPlayer> players = new HashSet<>();
		final double MAX_RANGE_SQR = 2500.0;
		if (center != null) {
			for (EntityPlayer player : world.playerEntities) {
				if (player.getDistanceSq(center.x, center.y, center.z) <= MAX_RANGE_SQR)
					players.add(player);
			}
		}
		
		if (!players.isEmpty()) {
			for (EntityPlayer player : players) {
				NetworkHandler.getSyncChannel().sendTo(message, (EntityPlayerMP) player);
			}
		}
	}
}
