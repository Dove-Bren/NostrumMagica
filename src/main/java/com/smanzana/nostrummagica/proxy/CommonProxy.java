package com.smanzana.nostrummagica.proxy;

import java.util.HashSet;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.AltarBlock;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ChalkBlock;
import com.smanzana.nostrummagica.blocks.CropGinseng;
import com.smanzana.nostrummagica.blocks.CropMandrakeRoot;
import com.smanzana.nostrummagica.blocks.CursedIce;
import com.smanzana.nostrummagica.blocks.DungeonBlock;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.LoreTable;
import com.smanzana.nostrummagica.blocks.MagicWall;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.blocks.NostrumMagicaFlower;
import com.smanzana.nostrummagica.blocks.NostrumMirrorBlock;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;
import com.smanzana.nostrummagica.blocks.ShrineBlock;
import com.smanzana.nostrummagica.blocks.SpellTable;
import com.smanzana.nostrummagica.blocks.SymbolBlock;
import com.smanzana.nostrummagica.capabilities.CapabilityHandler;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagicStorage;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.NostrumGui;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.config.network.ServerConfigMessage;
import com.smanzana.nostrummagica.enchantments.EnchantmentManaRecovery;
import com.smanzana.nostrummagica.entity.EntityGolemEarth;
import com.smanzana.nostrummagica.entity.EntityGolemEnder;
import com.smanzana.nostrummagica.entity.EntityGolemFire;
import com.smanzana.nostrummagica.entity.EntityGolemIce;
import com.smanzana.nostrummagica.entity.EntityGolemLightning;
import com.smanzana.nostrummagica.entity.EntityGolemPhysical;
import com.smanzana.nostrummagica.entity.EntityGolemWind;
import com.smanzana.nostrummagica.entity.EntityKoid;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ChalkItem;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.items.EnchantedWeapon;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MageStaff;
import com.smanzana.nostrummagica.items.MagicArmorBase;
import com.smanzana.nostrummagica.items.MagicSwordBase;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.NostrumGuide;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
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
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ClientEffectRenderMessage;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.potions.MagicBoostPotion;
import com.smanzana.nostrummagica.potions.MagicResistPotion;
import com.smanzana.nostrummagica.potions.MagicShieldPotion;
import com.smanzana.nostrummagica.potions.PhysicalShieldPotion;
import com.smanzana.nostrummagica.potions.RootedPotion;
import com.smanzana.nostrummagica.quests.NostrumQuest;
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
import com.smanzana.nostrummagica.spells.components.triggers.FoodTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.HealthTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ManaTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.OtherTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProjectileTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.TouchTrigger;
import com.smanzana.nostrummagica.world.NostrumFlowerGenerator;
import com.smanzana.nostrummagica.world.NostrumOreGenerator;
import com.smanzana.nostrummagica.world.NostrumShrineGenerator;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {
	public CapabilityHandler capabilityHandler;
	
	public void preinit() {
		CapabilityManager.INSTANCE.register(INostrumMagic.class, new NostrumMagicStorage(), NostrumMagic::new);
		capabilityHandler = new CapabilityHandler();
		NetworkHandler.getInstance();
		//NostrumMagicaSounds.registerSounds();
		
    	registerShapes();
    	registerTriggers();
    	
    	MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void init() {
    	//registerPotions();
    	//registerItems();
    	//registerBlocks();
    	
//    	GameRegistry.register(EnchantmentManaRecovery.instance(),
//    			);
    	
    	GameRegistry.registerWorldGenerator(new NostrumOreGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumFlowerGenerator(), 0);
    	GameRegistry.registerWorldGenerator(new NostrumShrineGenerator(), 0);
    	
    	NetworkRegistry.INSTANCE.registerGuiHandler(NostrumMagica.instance, new NostrumGui());
    	
    	LoreRegistry.instance();
	}
	
	public void postinit() {
		for (Biome biome : Biome.REGISTRY) {
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.MIDNIGHT_IRIS), 8);
			biome.addFlower(NostrumMagicaFlower.instance().getState(NostrumMagicaFlower.Type.CRYSTABLOOM), 7);
		}
		
		NostrumQuest.Validate();
	}
	
	@SubscribeEvent
	public void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
		event.getRegistry().register(EnchantmentManaRecovery.instance());
	}
	
	@SubscribeEvent
	public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		NostrumMagicaSounds.registerSounds(event);
	}
	
	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
		int entityID = 0;
		IForgeRegistry<EntityEntry> registry = event.getRegistry();
		
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntitySpellProjectile.class)
				.name("spell_projectile")
				.id("spell_projectile", entityID++)
				.tracker(64, 1, true)
				.build());
		
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityGolemPhysical.class)
				.name("physical_golem")
				.id("physical_golem", entityID++)
				.tracker(64, 1, true)
				.egg(EMagicElement.PHYSICAL.getColor(), 0xFF000000)
				.build());
		
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityGolemLightning.class)
				.name("lightning_golem")
				.id("lightning_golem", entityID++)
				.tracker(64, 1, true)
				.egg(EMagicElement.LIGHTNING.getColor(), 0xFF000000)
				.build());
		
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityGolemFire.class)
				.name("fire_golem")
				.id("fire_golem", entityID++)
				.tracker(64, 1, true)
				.egg(EMagicElement.FIRE.getColor(), 0xFF000000)
				.build());
    	
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityGolemEarth.class)
				.name("earth_golem")
				.id("earth_golem", entityID++)
				.tracker(64, 1, true)
				.egg(EMagicElement.EARTH.getColor(), 0xFF000000)
				.build());
    	
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityGolemIce.class)
				.name("ice_golem")
				.id("ice_golem", entityID++)
				.tracker(64, 1, true)
				.egg(EMagicElement.ICE.getColor(), 0xFF000000)
				.build());
    	
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityGolemWind.class)
				.name("wind_golem")
				.id("wind_golem", entityID++)
				.tracker(64, 1, true)
				.egg(EMagicElement.WIND.getColor(), 0xFF000000)
				.build());
		
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityGolemEnder.class)
				.name("ender_golem")
				.id("ender_golem", entityID++)
				.tracker(64, 1, true)
				.egg(EMagicElement.ENDER.getColor(), 0xFF000000)
				.build());
    	
		registry.register(
				EntityEntryBuilder.create()
				.entity(EntityKoid.class)
				.name("entity_koid")
				.id("entity_koid", entityID++)
				.tracker(64, 1, true)
				.egg(0xFF4842F4, 0xFF420EF4)
				.spawn(EnumCreatureType.MONSTER, 10, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
				.spawn(EnumCreatureType.MONSTER, 10, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.HOT))
				.spawn(EnumCreatureType.MONSTER, 10, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.WET))
				.spawn(EnumCreatureType.MONSTER, 10, 1, 1, BiomeDictionary.getBiomes(BiomeDictionary.Type.NETHER))
				.build());
	}
    
    private void registerShapes() {
    	SpellShape.register(SingleShape.instance());
    	SpellShape.register(AoEShape.instance());
    	SpellShape.register(ChainShape.instance());
    }
    
    public void registerTriggers() {
    	
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
    }
    
    @SubscribeEvent
    public void registerPotions(RegistryEvent.Register<Potion> event) {
    	event.getRegistry().register(RootedPotion.instance());
    	event.getRegistry().register(MagicResistPotion.instance());
    	event.getRegistry().register(PhysicalShieldPotion.instance());
    	event.getRegistry().register(MagicShieldPotion.instance());
    	event.getRegistry().register(FrostbitePotion.instance());
    	event.getRegistry().register(MagicBoostPotion.instance());
    }
    
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
    	IForgeRegistry<Item> registry = event.getRegistry();
    	
    	NostrumGuide.instance().setRegistryName(NostrumMagica.MODID, NostrumGuide.id);
    	registry.register(NostrumGuide.instance());
    	NostrumGuide.init();
    	
    	SpellcraftGuide.instance().setRegistryName(NostrumMagica.MODID, SpellcraftGuide.id);
    	registry.register(SpellcraftGuide.instance());
    	SpellcraftGuide.init();
    	
    	SpellTome.instance().setRegistryName(NostrumMagica.MODID, SpellTome.id);
    	registry.register(SpellTome.instance());
    	SpellPlate.instance().setRegistryName(NostrumMagica.MODID, SpellPlate.id);
    	registry.register(SpellPlate.instance());
    	
    	BlankScroll.instance().setRegistryName(NostrumMagica.MODID, BlankScroll.id);
    	registry.register(BlankScroll.instance());
    	BlankScroll.init();
    	
    	SpellScroll.instance().setRegistryName(NostrumMagica.MODID, SpellScroll.id);
    	registry.register(SpellScroll.instance());
    	SpellScroll.init();
    	
    	SpellTableItem.instance().setRegistryName(NostrumMagica.MODID, SpellTableItem.ID);
    	registry.register(SpellTableItem.instance());
    	SpellTableItem.init();
    	
    	MirrorItem.instance().setRegistryName(NostrumMagica.MODID, MirrorItem.ID);
    	registry.register(MirrorItem.instance());
    	MirrorItem.init();
    	
    	MagicArmorBase.init();
    	
    	ReagentItem.instance().setRegistryName(NostrumMagica.MODID, ReagentItem.ID);
    	registry.register(ReagentItem.instance());
    	ReagentItem.init();
    	InfusedGemItem.instance().setRegistryName(NostrumMagica.MODID, InfusedGemItem.ID);
    	registry.register(InfusedGemItem.instance());
    	InfusedGemItem.init();
    	SpellRune.instance().setRegistryName(NostrumMagica.MODID, SpellRune.ID);
    	registry.register(SpellRune.instance());
    	SpellRune.init();
    	
    	NostrumResourceItem.instance().setRegistryName(NostrumMagica.MODID, NostrumResourceItem.ID);
    	registry.register(NostrumResourceItem.instance());
    	NostrumResourceItem.init();
    	
    	ReagentBag.instance().setRegistryName(NostrumMagica.MODID, ReagentBag.id);
    	registry.register(ReagentBag.instance());
    	ReagentBag.init();
    	
    	SeekerIdol.instance().setRegistryName(NostrumMagica.MODID, SeekerIdol.id);
    	registry.register(SeekerIdol.instance());
    	SeekerIdol.init();
    	
    	ShrineSeekingGem.instance().setRegistryName(NostrumMagica.MODID, ShrineSeekingGem.id);
    	registry.register(ShrineSeekingGem.instance());
    	ShrineSeekingGem.init();
    	
    	ChalkItem.instance().setRegistryName(NostrumMagica.MODID, ChalkItem.ID);
    	registry.register(ChalkItem.instance());
    	ChalkItem.init();
    	
    	AltarItem.instance().setRegistryName(NostrumMagica.MODID, AltarItem.ID);
    	registry.register(AltarItem.instance());
    	AltarItem.init();
    	
    	PositionCrystal.instance().setRegistryName(NostrumMagica.MODID, PositionCrystal.ID);
    	registry.register(PositionCrystal.instance());
    	PositionCrystal.init();
    	
    	PositionToken.instance().setRegistryName(NostrumMagica.MODID, PositionToken.ID);
    	registry.register(PositionToken.instance());
    	PositionToken.init();
    	
    	SpellTomePage.instance().setRegistryName(NostrumMagica.MODID, SpellTomePage.id);
    	registry.register(SpellTomePage.instance());
    	
    	EssenceItem.instance().setRegistryName(NostrumMagica.MODID, EssenceItem.ID);
    	registry.register(EssenceItem.instance());
    	EssenceItem.init();
    	
    	MageStaff.instance().setRegistryName(NostrumMagica.MODID, MageStaff.ID);
    	registry.register(MageStaff.instance());
    	MageStaff.init();
    	
    	ThanoPendant.instance().setRegistryName(NostrumMagica.MODID, ThanoPendant.ID);
    	registry.register(ThanoPendant.instance());
    	
    	ThanosStaff.instance().setRegistryName(NostrumMagica.MODID, ThanosStaff.ID);
    	registry.register(ThanosStaff.instance());
    	ThanosStaff.init();
    	
    	MagicArmorBase.helm.setRegistryName(NostrumMagica.MODID, "magichelmbase");
    	registry.register(MagicArmorBase.helm);
    	MagicArmorBase.chest.setRegistryName(NostrumMagica.MODID, "magicchestbase");
    	registry.register(MagicArmorBase.chest);
    	MagicArmorBase.legs.setRegistryName(NostrumMagica.MODID, "magicleggingsbase");
    	registry.register(MagicArmorBase.legs);
    	MagicArmorBase.feet.setRegistryName(NostrumMagica.MODID, "magicfeetbase");
    	registry.register(MagicArmorBase.feet);
		
		MagicSwordBase.instance().setRegistryName(NostrumMagica.MODID, MagicSwordBase.ID);
		registry.register(MagicSwordBase.instance());
		
		MasteryOrb.instance().setRegistryName(NostrumMagica.MODID, MasteryOrb.id);
		registry.register(MasteryOrb.instance());
		
		EnchantedWeapon.registerWeapons(event);
		EnchantedArmor.registerArmors(event);
		
		// Register block items
		registry.register(
				new ItemBlock(MagicWall.instance())
				.setRegistryName(MagicWall.ID)
	    		.setCreativeTab(NostrumMagica.creativeTab)
	    		.setUnlocalizedName(MagicWall.ID)
				);
		
		registry.register(
				new ItemBlock(CursedIce.instance())
				.setRegistryName(CursedIce.ID)
	    		.setCreativeTab(NostrumMagica.creativeTab)
	    		.setUnlocalizedName(CursedIce.ID)
				);
		
		registry.register(
				new ItemBlock(ManiOre.instance())
				.setRegistryName(ManiOre.ID)
	    		.setCreativeTab(NostrumMagica.creativeTab)
	    		.setUnlocalizedName(ManiOre.ID)
				);
		
		registry.register(
				new ItemBlock(NostrumSingleSpawner.instance())
				.setRegistryName(NostrumSingleSpawner.ID)
	    		.setCreativeTab(NostrumMagica.creativeTab)
	    		.setUnlocalizedName(NostrumSingleSpawner.ID)
				);
		
		registry.register(
				new ItemBlock(DungeonBlock.instance())
				.setRegistryName(DungeonBlock.ID)
	    		.setCreativeTab(NostrumMagica.creativeTab)
	    		.setUnlocalizedName(DungeonBlock.ID)
				);
		
		registry.register(
				new ItemBlock(Candle.instance())
				.setRegistryName(Candle.ID)
	    		.setCreativeTab(NostrumMagica.creativeTab)
	    		.setUnlocalizedName(Candle.ID)
				);
		
		registry.register(
				new ItemBlock(EssenceOre.instance())
				.setRegistryName(EssenceOre.ID)
	    		.setCreativeTab(NostrumMagica.creativeTab)
	    		.setUnlocalizedName(EssenceOre.ID)
				);
		
		registry.register(
				new ItemBlock(ModificationTable.instance())
				.setRegistryName(ModificationTable.ID)
    			.setCreativeTab(NostrumMagica.creativeTab)
    			.setUnlocalizedName(ModificationTable.ID)
				);
		
		registry.register(
				new ItemBlock(LoreTable.instance())
				.setRegistryName(LoreTable.ID)
				.setCreativeTab(NostrumMagica.creativeTab)
				.setUnlocalizedName(LoreTable.ID)
				);
    }
    
    private void registerBlock(IForgeRegistry<Block> registry, Block block, ResourceLocation registryName) {
    	// This is dumb. I just don't want to go and set this for each below
    	block.setRegistryName(registryName);
    	registry.register(block);
    }
    
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
    	IForgeRegistry<Block> registry = event.getRegistry();
    	
    	registerBlock(registry, MagicWall.instance(),
    			new ResourceLocation(NostrumMagica.MODID, MagicWall.ID));

    	registerBlock(registry, CursedIce.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CursedIce.ID));
    	
    	registerBlock(registry, ManiOre.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ManiOre.ID));
    	
    	registerBlock(registry, SpellTable.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SpellTable.ID));
    	SpellTable.init();
    	
    	NostrumMagicaFlower.init();
    	registry.register(NostrumMagicaFlower.instance());
    	
    	registerBlock(registry, CropMandrakeRoot.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CropMandrakeRoot.ID));
    	
    	registerBlock(registry, CropGinseng.instance(),
    			new ResourceLocation(NostrumMagica.MODID, CropGinseng.ID));
    	
    	registerBlock(registry, NostrumSingleSpawner.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumSingleSpawner.ID));
    	NostrumSingleSpawner.init();
    	
    	registerBlock(registry, DungeonBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, DungeonBlock.ID));
    	
    	registerBlock(registry, SymbolBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, SymbolBlock.ID));
    	SymbolBlock.init();

    	registerBlock(registry, ShrineBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ShrineBlock.ID));
    	
    	registerBlock(registry, NostrumMirrorBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumMirrorBlock.ID));
    	
    	registerBlock(registry, ChalkBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ChalkBlock.ID));
    	
    	registerBlock(registry, AltarBlock.instance(),
    			new ResourceLocation(NostrumMagica.MODID, AltarBlock.ID));
    	AltarBlock.init();
    	
    	registerBlock(registry, Candle.instance(),
    			new ResourceLocation(NostrumMagica.MODID, Candle.ID));
    	Candle.init();
    	
    	registerBlock(registry, NostrumObelisk.instance(),
    			new ResourceLocation(NostrumMagica.MODID, NostrumObelisk.ID));
    	NostrumObelisk.init();
    	
    	registerBlock(registry, EssenceOre.instance(),
    			new ResourceLocation(NostrumMagica.MODID, EssenceOre.ID));
    	
    	registerBlock(registry, ModificationTable.instance(),
    			new ResourceLocation(NostrumMagica.MODID, ModificationTable.ID));
    	ModificationTable.init();
    	
    	registerBlock(registry, LoreTable.instance(),
    			new ResourceLocation(NostrumMagica.MODID, LoreTable.ID));
    	LoreTable.init();
    	
    }
    
    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
    	GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "modification_table"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(ModificationTable.instance()),
				"WPW", "WCW", "WWW",
				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
    	
    	GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "lore_table"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(LoreTable.instance()),
				"WGW", "WCW", "WWW",
				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
				'G', new ItemStack(Blocks.GLASS_PANE, 1, OreDictionary.WILDCARD_VALUE),
				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
    	
    	GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "nostrum_candle"),
				new ResourceLocation(NostrumMagica.MODID, "nostrum_candle"),
				new ItemStack(Candle.instance()),
				"W",
				"F",
				'W', ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1),
				'F', Items.ROTTEN_FLESH);
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "nostrum_candle"),
				new ResourceLocation(NostrumMagica.MODID, "nostrum_candle"),
				new ItemStack(Candle.instance()),
				"W",
				"F",
				'W', Items.STRING,
				'F', Items.ROTTEN_FLESH);
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "mage_staff_item"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(MageStaff.instance()), " WW", " WC", "W  ",
				'W', Blocks.PLANKS, 
				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1));
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "infused_gem_item"),
				new ResourceLocation(NostrumMagica.MODID, "infused_gem_item"),
				new ItemStack(InfusedGemItem.instance()), " G ", "DED", "BGB",
				'D', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
				'G', ReagentItem.instance().getReagent(ReagentType.GRAVE_DUST, 1),
				'E', Items.ENDER_PEARL,
				'B', ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1));
		
		GameRegistry.addShapelessRecipe(
				new ResourceLocation(NostrumMagica.MODID, "infused_gem_essence_item"),
				new ResourceLocation(NostrumMagica.MODID, "infused_gem_item"),
				InfusedGemItem.instance().getGem(null, 1),
				Ingredient.fromStacks(EssenceItem.getEssence(EMagicElement.EARTH, 1)),
				Ingredient.fromStacks(EssenceItem.getEssence(EMagicElement.ENDER, 1)),
				Ingredient.fromStacks(EssenceItem.getEssence(EMagicElement.FIRE, 1)),
				Ingredient.fromStacks(EssenceItem.getEssence(EMagicElement.ICE, 1)),
				Ingredient.fromStacks(EssenceItem.getEssence(EMagicElement.LIGHTNING, 1)),
				Ingredient.fromStacks(EssenceItem.getEssence(EMagicElement.PHYSICAL, 1)),
				Ingredient.fromStacks(EssenceItem.getEssence(EMagicElement.WIND, 1)));
		
		EMagicElement[] all = EMagicElement.values();
		EMagicElement last = all[all.length - 1];
		for (EMagicElement element : all) {
			GameRegistry.addShapelessRecipe(
					new ResourceLocation(NostrumMagica.MODID, "essence_" + element.getName() + "_item"),
					null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
					EssenceItem.getEssence(element, 1),
					Ingredient.fromStacks(EssenceItem.getEssence(last, 1)),
					Ingredient.fromStacks(new ItemStack(EssenceItem.instance(), 1, OreDictionary.WILDCARD_VALUE)));
			last = element;
		}
		
		GameRegistry.addShapelessRecipe(
				new ResourceLocation(NostrumMagica.MODID, "chalk_item"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(ChalkItem.instance()),
				Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 15)),
				Ingredient.fromStacks(new ItemStack(Items.DYE, 1, 15)),
				Ingredient.fromStacks(new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE)));
		
		GameRegistry.addShapelessRecipe(
				new ResourceLocation(NostrumMagica.MODID, "blank_scroll_item"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(BlankScroll.instance()),
				Ingredient.fromStacks(ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1)),
				Ingredient.fromItem(Items.PAPER),
				Ingredient.fromItem(Items.PAPER),
				Ingredient.fromStacks(ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1)));
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "alter_item"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(AltarItem.instance()),
				"SSS", " T ", "TRT",
				'S', Blocks.STONE_SLAB,
				'T', Blocks.STONE,
				'R', NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1)
				);
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "magic_armor_helm"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(MagicArmorBase.helm), "CCC", "C C", " D ",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_HELMET);
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "magic_armor_chest"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(MagicArmorBase.chest), "CDC", "CCC", "CCC",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_CHESTPLATE);
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "magic_armor_legs"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(MagicArmorBase.legs), "CCC", "CDC", "C C",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_LEGGINGS);
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "magic_armor_feet"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(MagicArmorBase.feet), " D ", "C C", "C C",
				'C', InfusedGemItem.instance().getGem(null, 1),
				'D', Items.IRON_BOOTS);
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "magic_sword_base"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(MagicSwordBase.instance()), " C ", " C ", " S ",
				'S', Items.IRON_SWORD, 
				'C', InfusedGemItem.instance().getGem(null, 1));
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "mirror_item"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(MirrorItem.instance()), "RQR", "QGQ", "SSS",
				'R', new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE),
				'Q', Items.QUARTZ,
				'G', Blocks.GLASS,
				'S', Blocks.STONE);
		
		GameRegistry.addShapelessRecipe(
				new ResourceLocation(NostrumMagica.MODID, "nostrum_guide"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(NostrumGuide.instance()),
				Ingredient.fromItem(Items.LEATHER),
				Ingredient.fromItem(Items.LEATHER),
				Ingredient.fromItem(Items.LEATHER),
				Ingredient.fromItem(BlankScroll.instance()));
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "nostrum_crystal_easy"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), " MR", "MDM", "RM ",
				'D', Items.DIAMOND,
				'M', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
				'R', new ItemStack(ReagentItem.instance(), 1, OreDictionary.WILDCARD_VALUE));
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "reagent_bag"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(ReagentBag.instance()), "GLG", "LRL", "LLL",
				'L', Items.LEATHER,
				'G', Items.GOLD_INGOT,
				'R', ReagentItem.instance());
		
		GameRegistry.addShapelessRecipe(
				new ResourceLocation(NostrumMagica.MODID, "spider_silk_craft"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1),
				Ingredient.fromItem(Items.STRING),
				Ingredient.fromItem(Items.STRING),
				Ingredient.fromItem(Items.SUGAR));
		
		ForgeRegistries.RECIPES.register(
				new SeekerIdol.IdolRecipe());
		
		ForgeRegistries.RECIPES.register(
				new ShrineSeekingGem.SeekingGemRecipe());
		
		GameRegistry.addShapelessRecipe(
				new ResourceLocation(NostrumMagica.MODID, "spellcraft_guide"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(SpellcraftGuide.instance()),
				Ingredient.fromItem(Items.LEATHER),
				Ingredient.fromItem(Items.LEATHER),
				Ingredient.fromItem(Items.LEATHER),
				Ingredient.fromStacks(new ItemStack(SpellRune.instance(), 1, OreDictionary.WILDCARD_VALUE)));
		
		ForgeRegistries.RECIPES.register(
				new SpellRune.RuneRecipe());
		
		ForgeRegistries.RECIPES.register(
				new SpellScroll.ActivatedRecipe());
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "spell_table_item"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(SpellTableItem.instance()), "CBD", "PPP", "L L",
				'P', Blocks.PLANKS,
				'L', Blocks.LOG,
				'C', ReagentItem.instance().getReagent(ReagentType.CRYSTABLOOM, 1),
				'B', ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
				'D', ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
		
		GameRegistry.addShapedRecipe(
				new ResourceLocation(NostrumMagica.MODID, "thanos_staff"),
				null, //new ResourceLocation(NostrumMagica.MODID, NostrumMagica.MODID),
				new ItemStack(ThanosStaff.instance()), "  T", " S ", "S  ",
				'T', new ItemStack(ThanoPendant.instance()), 
				'S', new ItemStack(MageStaff.instance(), 1, 0));
    }
    
    public void syncPlayer(EntityPlayerMP player) {
    	NetworkHandler.getSyncChannel().sendTo(
    			new StatSyncMessage(NostrumMagica.getMagicWrapper(player)),
    			player);
    	NetworkHandler.getSyncChannel().sendTo(
    			new SpellRequestReplyMessage(NostrumMagica.spellRegistry.getAllSpells(), true),
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
	 */
	public void spawnEffect(World world, SpellComponentWrapper comp,
			EntityLivingBase caster, Vec3d casterPos,
			EntityLivingBase target, Vec3d targetPos,
			SpellComponentWrapper flavor) {
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
					comp, flavor);
			for (EntityPlayer player : players) {
				NetworkHandler.getSyncChannel().sendTo(message, (EntityPlayerMP) player);
			}
		}
	}
}
