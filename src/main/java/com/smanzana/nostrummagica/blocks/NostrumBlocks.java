package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.ActiveHopperTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.CandleTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.ItemDuctTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.LoreTableEntity;
import com.smanzana.nostrummagica.blocks.tiles.ManaArmorerTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.ModificationTableEntity;
import com.smanzana.nostrummagica.blocks.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.blocks.tiles.ObeliskPortalTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.ParadoxMirrorTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.ProgressionDoorTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.PutterBlockTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SingleSpawnerTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SorceryPortalTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SpawnerTriggerTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SpellTableEntity;
import com.smanzana.nostrummagica.blocks.tiles.SwitchBlockTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.SymbolTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.TeleportRuneTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.TeleportationPortalTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.TemporaryPortalTileEntity;
import com.smanzana.nostrummagica.fluids.NostrumFluids;
import com.smanzana.nostrummagica.items.AltarItem;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumBlocks {
	
	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
	@ObjectHolder(AltarBlock.ID) public static AltarBlock altar;
	@ObjectHolder(Candle.ID) public static Candle candle;
	@ObjectHolder(ChalkBlock.ID) public static ChalkBlock chalk;
	@ObjectHolder(CropEssence.ID) public static CropEssence essenceCrop;
	@ObjectHolder(CropGinseng.ID) public static CropGinseng ginsengCrop;
	@ObjectHolder(CropMandrakeRoot.ID) public static CropMandrakeRoot mandrakeCrop;
	@ObjectHolder(CursedIce.ID) public static CursedIce cursedIce;
	@ObjectHolder(DungeonAir.ID) public static DungeonAir dungeonAir;
	@ObjectHolder(DungeonBars.ID) public static DungeonBars dungeonBars;
	@ObjectHolder(DungeonBlock.ID_LIGHT) public static DungeonBlock lightDungeonBlock;
	@ObjectHolder(DungeonBlock.ID_DARK) public static DungeonBlock dungeonBlock;
	@ObjectHolder(EssenceOre.ID) public static EssenceOre essenceOre;
	@ObjectHolder(ItemDuct.ID) public static ItemDuct itemDuct;
	@ObjectHolder(LogicDoor.ID) public static LogicDoor logicDoor;
	@ObjectHolder(MagicDirt.ID) public static MagicDirt magicDirt;
	@ObjectHolder(MagicWall.ID) public static MagicWall magicWall;
	@ObjectHolder(ManaArmorerBlock.ID) public static ManaArmorerBlock manaArmorerBlock;
	@ObjectHolder(ManiCrystal.ID) public static ManiCrystal maniCrystalBlock;
	@ObjectHolder(ManiOre.ID) public static ManiOre maniOre;
	@ObjectHolder(MimicBlock.ID_FACADE) public static MimicBlock mimicFacade;
	@ObjectHolder(MimicBlock.ID_DOOR) public static MimicBlock mimicDoor;
	@ObjectHolder(ModificationTable.ID) public static ModificationTable modificationTable;
	@ObjectHolder(NostrumMagicaFlower.ID_MIDNIGHT_IRIS) public static NostrumMagicaFlower midnightIris;
	@ObjectHolder(NostrumMagicaFlower.ID_CRYSTABLOOM) public static NostrumMagicaFlower crystabloom;
	@ObjectHolder(NostrumMirrorBlock.ID) public static NostrumMirrorBlock mirrorBlock;
	@ObjectHolder(NostrumObelisk.ID) public static NostrumObelisk obelisk;
	@ObjectHolder(NostrumSingleSpawner.ID) public static NostrumSingleSpawner singleSpawner;
	@ObjectHolder(NostrumSpawnAndTrigger.ID) public static NostrumSpawnAndTrigger triggerSpawner;
	@ObjectHolder(ObeliskPortal.ID) public static ObeliskPortal obeliskPortal;
	@ObjectHolder(ParadoxMirrorBlock.ID) public static ParadoxMirrorBlock paradoxMirror;
	@ObjectHolder(ProgressionDoor.ID) public static ProgressionDoor progressionDoor;
	@ObjectHolder(PutterBlock.ID) public static PutterBlock putterBlock;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
//	@ObjectHolder(ActiveHopper.ID) public static ActiveHopper activeHopper;
	
	private static void registerBlockItem(Block block, String registryName, @Nullable CreativeTabs tab, IForgeRegistry<Item> registry) {
		BlockItem item = new BlockItem(block);
    	item.setRegistryName(registryName);
    	item.setUnlocalizedName(registryName);
    	item.setCreativeTab(tab == null ? NostrumMagica.creativeTab : tab);
    	registry.register(item);
	}
	
	private static void registerBlockItem(Block block, String registryName, IForgeRegistry<Item> registry) {
		registerBlockItem(block, registryName, null, registry);
	}
	
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	
    	registerBlockItem(MagicWall.instance(), MagicWall.ID, registry);
    	registerBlockItem(CursedIce.instance(), CursedIce.ID, registry);
    	registerBlockItem(ManiOre.instance(), ManiOre.ID, registry);
    	registerBlockItem(MagicDirt.instance(), MagicDirt.ID, registry);
    	registerBlockItem(NostrumSingleSpawner.instance(), NostrumSingleSpawner.ID, registry);
    	registerBlockItem(NostrumSpawnAndTrigger.instance(), NostrumSpawnAndTrigger.ID, registry);
    	registerBlockItem(Candle.instance(), Candle.ID, registry);
    	registerBlockItem(EssenceOre.instance(), EssenceOre.ID, registry);
    	registerBlockItem(ModificationTable.instance(), ModificationTable.ID, registry);
    	registerBlockItem(LoreTable.instance(), LoreTable.ID, registry);
    	registerBlockItem(SorceryPortal.instance(), SorceryPortal.ID, registry);
    	registerBlockItem(ProgressionDoor.instance(), ProgressionDoor.ID, registry);
    	registerBlockItem(LogicDoor.instance(), LogicDoor.ID, registry);
    	registerBlockItem(SwitchBlock.instance(), SwitchBlock.ID, registry);
//    	registerBlockItem(MimicBlock.door(), MimicBlock.ID_DOOR, registry);
//    	registerBlockItem(MimicBlock.facade(), MimicBlock.ID_FACADE, registry);
    	registerBlockItem(TeleportRune.instance(), TeleportRune.ID, registry);
    	registerBlockItem(PutterBlock.instance(), PutterBlock.ID, registry);
    	registerBlockItem(ActiveHopper.instance, ActiveHopper.ID, registry);
    	registerBlockItem(ItemDuct.instance, ItemDuct.ID, registry);
    	registerBlockItem(DungeonBars.instance(), DungeonBars.ID, registry);
    	registerBlockItem(ParadoxMirrorBlock.instance(), ParadoxMirrorBlock.ID, registry);
    	registerBlockItem(ManaArmorerBlock.instance(), ManaArmorerBlock.ID, registry);
    	

    	String[] variants = new String[DungeonBlock.Type.values().length];
    	for (DungeonBlock.Type type : DungeonBlock.Type.values()) {
    		variants[type.ordinal()] = type.getName().toLowerCase();
    	}
    	registry.register(
    			(new ItemMultiTexture(DungeonBlock.instance(), DungeonBlock.instance(), variants))
    			.setRegistryName(DungeonBlock.ID)
    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(DungeonBlock.ID));
    	
    	registry.register((new ItemMultiTexture(MimicBlock.door(), MimicBlock.door(), new String[] {
    			"breakable", // 0 meta
    			"unbreakable", // 1 meta
    		})).setRegistryName(MimicBlock.ID_DOOR).setCreativeTab(NostrumMagica.creativeTab));
    	registry.register((new ItemMultiTexture(MimicBlock.facade(), MimicBlock.facade(), new String[] {
    			"breakable", // 0 meta
    			"unbreakable", // 1 meta
    		})).setRegistryName(MimicBlock.ID_FACADE).setCreativeTab(NostrumMagica.creativeTab));
    	
    	registry.register((new ItemMultiTexture(DungeonAir.instance(), DungeonAir.instance(), new String[] {
    			"single", // 0 meta
    			"flood", // 1 meta
    		})).setRegistryName(DungeonAir.ID).setCreativeTab(NostrumMagica.creativeTab));
    	
    	
    	
    	registerFluidItems(registry);
    }
    
    private static void registerBlock(Block block, String registryName, IForgeRegistry<Block> registry) {
    	block.setRegistryName(registryName);
    	registry.register(block);
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
    	final IForgeRegistry<Block> registry = event.getRegistry();
    	
    	registerBlock(SpellTable.instance(), SpellTable.ID, registry);
    	registerBlock(NostrumMagicaFlower.instance(), NostrumMagicaFlower.ID, registry);
    	registerBlock(CropMandrakeRoot.instance(), CropMandrakeRoot.ID, registry);
    	registerBlock(CropGinseng.instance(), CropGinseng.ID, registry);
    	
    	// DungeonBlock item variants registered by hand in item register method
    	registerBlock(DungeonBlock.instance(), DungeonBlock.ID, registry);
    	
    	registerBlock(SymbolBlock.instance(), SymbolBlock.ID, registry);
    	registerBlock(ShrineBlock.instance(), ShrineBlock.ID, registry);
    	registerBlock(NostrumMirrorBlock.instance(), NostrumMirrorBlock.ID, registry);
    	registerBlock(ChalkBlock.instance(), ChalkBlock.ID, registry);
    	registerBlock(AltarBlock.instance(), AltarBlock.ID, registry);
    	registerBlock(NostrumObelisk.instance(), NostrumObelisk.ID, registry);
    	registerBlock(ObeliskPortal.instance(), ObeliskPortal.ID, registry);
    	registerBlock(TeleportationPortal.instance(), TeleportationPortal.ID, registry);
    	registerBlock(TemporaryTeleportationPortal.instance(), TemporaryTeleportationPortal.ID, registry);
    	registerBlock(SorceryPortalSpawner.instance(), SorceryPortalSpawner.ID, registry);
    	registerBlock(ManiCrystal.instance(), ManiCrystal.ID, registry);
    	registerBlock(CropEssence.instance(), CropEssence.ID, registry);
    	
    	registerBlock(MagicWall.instance(), MagicWall.ID, registry);
    	registerBlock(CursedIce.instance(), CursedIce.ID, registry);
    	registerBlock(ManiOre.instance(), ManiOre.ID, registry);
    	registerBlock(MagicDirt.instance(), MagicDirt.ID, registry);
    	registerBlock(NostrumSingleSpawner.instance(), NostrumSingleSpawner.ID, registry);
    	registerBlock(NostrumSpawnAndTrigger.instance(), NostrumSpawnAndTrigger.ID, registry);
    	registerBlock(Candle.instance(), Candle.ID, registry);
    	registerBlock(EssenceOre.instance(), EssenceOre.ID, registry);
    	registerBlock(ModificationTable.instance(), ModificationTable.ID, registry);
    	registerBlock(LoreTable.instance(), LoreTable.ID, registry);
    	registerBlock(SorceryPortal.instance(), SorceryPortal.ID, registry);
    	registerBlock(ProgressionDoor.instance(), ProgressionDoor.ID, registry);
    	registerBlock(LogicDoor.instance(), LogicDoor.ID, registry);
    	registerBlock(SwitchBlock.instance(), SwitchBlock.ID, registry);
    	registerBlock(MimicBlock.door(), MimicBlock.ID_DOOR, registry);
    	registerBlock(MimicBlock.facade(), MimicBlock.ID_FACADE, registry);
    	registerBlock(TeleportRune.instance(), TeleportRune.ID, registry);
    	registerBlock(PutterBlock.instance(), PutterBlock.ID, registry);
    	registerBlock(ActiveHopper.instance, ActiveHopper.ID, registry);
    	registerBlock(ItemDuct.instance, ItemDuct.ID, registry);
    	registerBlock(DungeonBars.instance(), DungeonBars.ID, registry);
    	registerBlock(DungeonAir.instance(), DungeonAir.ID, registry);
    	registerBlock(ParadoxMirrorBlock.instance(), ParadoxMirrorBlock.ID, registry);
    	registerBlock(ManaArmorerBlock.instance(), ManaArmorerBlock.ID, registry);
    	
    	
//			GameRegistry.addRecipe(new ItemStack(MagicDirt.instance()), " D ", "DCD", " D ",
//					'D', new ItemStack(Blocks.DIRT, 1, OreDictionary.WILDCARD_VALUE),
//					'C', InfusedGemItem.instance().getGem(null, 0));

//			GameRegistry.addShapedRecipe(new ItemStack(Candle.instance()),
//					"W",
//					"F",
//					'W', ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1),
//					'F', Items.ROTTEN_FLESH);
//			GameRegistry.addShapedRecipe(new ItemStack(Candle.instance()),
//					"W",
//					"F",
//					'W', Items.STRING,
//					'F', Items.ROTTEN_FLESH);
    	
    	registerFluidBlocks(registry);
    	registerTileEntities();
    	

    	
    }
    
    private void registerTileEntities() {
    	GameRegistry.registerTileEntity(SpellTableEntity.class, new ResourceLocation(NostrumMagica.MODID, "spell_table"));
    	GameRegistry.registerTileEntity(SingleSpawnerTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_mob_spawner_te"));
    	GameRegistry.registerTileEntity(SpawnerTriggerTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_mob_spawner_trigger_te"));
    	GameRegistry.registerTileEntity(SymbolTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_symbol_te"));
    	GameRegistry.registerTileEntity(AltarTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_altar_te"));
    	GameRegistry.registerTileEntity(CandleTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_candle_te"));
    	GameRegistry.registerTileEntity(NostrumObeliskEntity.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_obelisk"));
    	GameRegistry.registerTileEntity(ObeliskPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "obelisk_portal"));
    	GameRegistry.registerTileEntity(ModificationTableEntity.class, new ResourceLocation(NostrumMagica.MODID, "modification_table"));
    	GameRegistry.registerTileEntity(LoreTableEntity.class, new ResourceLocation(NostrumMagica.MODID, "lore_table"));
    	GameRegistry.registerTileEntity(SorceryPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "sorcery_portal"));
    	GameRegistry.registerTileEntity(TeleportationPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "teleportation_portal"));
    	GameRegistry.registerTileEntity(TemporaryPortalTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "limited_teleportation_portal"));
    	GameRegistry.registerTileEntity(ProgressionDoorTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "progression_door"));
    	GameRegistry.registerTileEntity(SwitchBlockTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "switch_block_tile_entity"));
    	GameRegistry.registerTileEntity(TeleportRuneTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "teleport_rune"));
    	GameRegistry.registerTileEntity(PutterBlockTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "putter_entity"));
    	GameRegistry.registerTileEntity(ActiveHopperTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "active_hopper_te"));
    	GameRegistry.registerTileEntity(ItemDuctTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "item_duct_te"));
    	GameRegistry.registerTileEntity(ParadoxMirrorTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "paradox_mirror_te"));
    	GameRegistry.registerTileEntity(ManaArmorerTileEntity.class, new ResourceLocation(NostrumMagica.MODID, "mana_armorer"));
    }
    
    private static void registerFluidBlocks(IForgeRegistry<Block> registry) {
    	for (NostrumFluids fluid : NostrumFluids.values()) {
    		if (fluid.getFluid().getBlock() != null) {
    			registry.register(fluid.getFluid().getBlock());
    		}
    	}
    }
    
    private static void registerFluidItems(IForgeRegistry<Item> registry) {
    	for (NostrumFluids fluid : NostrumFluids.values()) {
    		if (fluid.getFluid().getBlock() != null) {
    			FluidRegistry.addBucketForFluid(fluid.getFluid());
    		}
    	}
    }
	
}
