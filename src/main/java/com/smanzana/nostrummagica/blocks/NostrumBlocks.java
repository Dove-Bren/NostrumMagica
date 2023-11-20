package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.fluids.NostrumFluids;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
	@ObjectHolder(LoreTable.ID) public static LoreTable loreTable;
	@ObjectHolder(MagicDirt.ID) public static MagicDirt magicDirt;
	@ObjectHolder(MagicWall.ID) public static MagicWall magicWall;
	@ObjectHolder(ManaArmorerBlock.ID) public static ManaArmorerBlock manaArmorerBlock;
	@ObjectHolder(ManiCrystal.ID_MANI) public static ManiCrystal maniCrystalBlock;
	@ObjectHolder(ManiCrystal.ID_KANI) public static ManiCrystal kaniCrystalBlock;
	@ObjectHolder(ManiCrystal.ID_VANI) public static ManiCrystal vaniCrystalBlock;
	@ObjectHolder(ManiOre.ID) public static ManiOre maniOre;
	@ObjectHolder(MimicOnesidedBlock.ID_FACADE) public static MimicBlock mimicFacade;
	@ObjectHolder(MimicOnesidedBlock.ID_FACADE_UNBREAKABLE) public static MimicBlock mimicFacadeUnbreakable;
	@ObjectHolder(MimicOnesidedBlock.ID_DOOR) public static MimicBlock mimicDoor;
	@ObjectHolder(MimicOnesidedBlock.ID_DOOR_UNBREAKABLE) public static MimicBlock mimicDoorUnbreakable;
	@ObjectHolder(ModificationTable.ID) public static ModificationTable modificationTable;
	@ObjectHolder(NostrumMagicaFlower.ID_MIDNIGHT_IRIS) public static NostrumMagicaFlower midnightIris;
	@ObjectHolder(NostrumMagicaFlower.ID_CRYSTABLOOM) public static NostrumMagicaFlower crystabloom;
	@ObjectHolder(NostrumMirrorBlock.ID) public static NostrumMirrorBlock mirrorBlock;
	@ObjectHolder(NostrumObelisk.ID) public static NostrumObelisk obelisk;
	@ObjectHolder(NostrumSingleSpawner.ID) public static NostrumSingleSpawner singleSpawner;
	@ObjectHolder(NostrumSpawnAndTrigger.ID) public static NostrumSpawnAndTrigger triggerSpawner;
	@ObjectHolder(ObeliskPortal.ID) public static ObeliskPortal obeliskPortal;
	@ObjectHolder(ParadoxMirrorBlock.ID) public static ParadoxMirrorBlock paradoxMirror;
	@ObjectHolder(PoisonWaterBlock.ID_BREAKABLE) public static PoisonWaterBlock poisonWaterBlock;
	@ObjectHolder(PoisonWaterBlock.ID_UNBREAKABLE) public static PoisonWaterBlock unbreakablePoisonWaterBlock;
	@ObjectHolder(ProgressionDoor.ID) public static ProgressionDoor progressionDoor;
	@ObjectHolder(PutterBlock.ID) public static PutterBlock putterBlock;
	@ObjectHolder(ShrineBlock.ID) public static ShrineBlock shrineBlock;
	@ObjectHolder(SorceryPortal.ID) public static SorceryPortal sorceryPortal;
	@ObjectHolder(SorceryPortalSpawner.ID) public static SorceryPortalSpawner sorceryPortalSpawner;
	@ObjectHolder(SpellTable.ID) public static SpellTable spellTable;
	@ObjectHolder(SwitchBlock.ID) public static SwitchBlock switchBlock;
	@ObjectHolder(TeleportationPortal.ID) public static TeleportationPortal teleportationPortal;
	@ObjectHolder(SymbolBlock.ID) public static SymbolBlock symbolBlock;
	@ObjectHolder(TeleportRune.ID) public static TeleportRune teleportRune;
	@ObjectHolder(TemporaryTeleportationPortal.ID) public static TemporaryTeleportationPortal temporaryTeleportationPortal;
	@ObjectHolder(RuneShaper.ID) public static RuneShaper runeShaper;
	@ObjectHolder(LockedChest.ID) public static LockedChest lockedChest;
	@ObjectHolder(KeySwitchBlock.ID) public static KeySwitchBlock keySwitch;
	@ObjectHolder(MysticAnchor.ID) public static MysticAnchor mysticAnchor;
	@ObjectHolder(ToggleLogicDoor.ID) public static ToggleLogicDoor toggleDoor;
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, Item.Properties builder, IForgeRegistry<Item> registry) {
		BlockItem item = new BlockItem(block, builder);
    	item.setRegistryName(registryName);
    	registry.register(item);
	}
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, IForgeRegistry<Item> registry) {
		registerBlockItem(block, registryName, NostrumItems.PropBase(), registry);
	}
	
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
    	final IForgeRegistry<Item> registry = event.getRegistry();
    	
    	registerBlockItem(activeHopper, activeHopper.getRegistryName(), registry);
    	//registerBlockItem(altar, altar.getRegistryName(), registry);
    	registerBlockItem(candle, candle.getRegistryName(), registry);
    	//registerBlockItem(chalk, chalk.getRegistryName(), registry);
    	//registerBlockItem(essenceCrop, essenceCrop.getRegistryName(), registry);
    	//registerBlockItem(ginsengCrop, ginsengCrop.getRegistryName(), registry);
    	//registerBlockItem(mandrakeCrop, mandrakeCrop.getRegistryName(), registry);
    	registerBlockItem(cursedIce, cursedIce.getRegistryName(), registry);
    	registerBlockItem(dungeonAir, dungeonAir.getRegistryName(), registry);
    	registerBlockItem(dungeonBars, dungeonBars.getRegistryName(), registry);
    	registerBlockItem(lightDungeonBlock, lightDungeonBlock.getRegistryName(), registry);
    	registerBlockItem(dungeonBlock, dungeonBlock.getRegistryName(), registry);
    	registerBlockItem(essenceOre, essenceOre.getRegistryName(), registry);
    	registerBlockItem(itemDuct, itemDuct.getRegistryName(), registry);
    	registerBlockItem(logicDoor, logicDoor.getRegistryName(), registry);
    	registerBlockItem(loreTable, loreTable.getRegistryName(), registry);
    	registerBlockItem(magicDirt, magicDirt.getRegistryName(), registry);
    	registerBlockItem(magicWall, magicWall.getRegistryName(), registry);
    	registerBlockItem(manaArmorerBlock, manaArmorerBlock.getRegistryName(), registry);
    	//registerBlockItem(maniCrystalBlock, maniCrystalBlock.getRegistryName(), registry);
    	//registerBlockItem(kaniCrystalBlock, kaniCrystalBlock.getRegistryName(), registry);
    	//registerBlockItem(vaniCrystalBlock, vaniCrystalBlock.getRegistryName(), registry);
    	registerBlockItem(maniOre, maniOre.getRegistryName(), registry);
    	registerBlockItem(mimicFacade, mimicFacade.getRegistryName(), registry);
    	registerBlockItem(mimicFacadeUnbreakable, mimicFacadeUnbreakable.getRegistryName(), registry);
    	registerBlockItem(mimicDoor, mimicDoor.getRegistryName(), registry);
    	registerBlockItem(mimicDoorUnbreakable, mimicDoorUnbreakable.getRegistryName(), registry);
    	registerBlockItem(modificationTable, modificationTable.getRegistryName(), registry);
    	//registerBlockItem(midnightIris, midnightIris.getRegistryName(), registry);
    	//registerBlockItem(crystabloom, crystabloom.getRegistryName(), registry);
    	//registerBlockItem(mirrorBlock, mirrorBlock.getRegistryName(), registry);
    	//registerBlockItem(obelisk, obelisk.getRegistryName(), registry);
    	registerBlockItem(singleSpawner, singleSpawner.getRegistryName(), registry);
    	registerBlockItem(triggerSpawner, triggerSpawner.getRegistryName(), registry);
    	//registerBlockItem(obeliskPortal, obeliskPortal.getRegistryName(), registry);
    	registerBlockItem(paradoxMirror, paradoxMirror.getRegistryName(), registry);
    	//registerBlockItem(poisonWaterBlock, poisonWaterBlock.getRegistryName(), registry);
    	//registerBlockItem(unbreakablePoisonWaterBlock, unbreakablePoisonWaterBlock.getRegistryName(), registry);
    	registerBlockItem(progressionDoor, progressionDoor.getRegistryName(), registry);
    	registerBlockItem(putterBlock, putterBlock.getRegistryName(), registry);
    	//registerBlockItem(shrineBlock, shrineBlock.getRegistryName(), registry);
    	//registerBlockItem(sorceryPortal, sorceryPortal.getRegistryName(), registry);
    	//registerBlockItem(sorceryPortalSpawner, sorceryPortalSpawner.getRegistryName(), registry);
    	//registerBlockItem(spellTable, spellTable.getRegistryName(), registry);
    	registerBlockItem(switchBlock, switchBlock.getRegistryName(), registry);
    	//registerBlockItem(teleportationPortal, teleportationPortal.getRegistryName(), registry);
    	registerBlockItem(symbolBlock, symbolBlock.getRegistryName(), registry);
    	registerBlockItem(teleportRune, teleportRune.getRegistryName(), registry);
    	//registerBlockItem(temporaryTeleportationPortal, temporaryTeleportationPortal.getRegistryName(), registry);
    	registerBlockItem(runeShaper, runeShaper.getRegistryName(), registry);
    	registerBlockItem(keySwitch, keySwitch.getRegistryName(), registry);
    	registerBlockItem(mysticAnchor, mysticAnchor.getRegistryName(), registry);
    	registerBlockItem(toggleDoor, toggleDoor.getRegistryName(), registry);
    	

//    	String[] variants = new String[DungeonBlock.Type.values().length];
//    	for (DungeonBlock.Type type : DungeonBlock.Type.values()) {
//    		variants[type.ordinal()] = type.getName().toLowerCase();
//    	}
//    	registry.register(
//    			(new ItemMultiTexture(DungeonBlock.instance(), DungeonBlock.instance(), variants))
//    			.setRegistryName(DungeonBlock.ID)
//    		.setCreativeTab(NostrumMagica.creativeTab).setUnlocalizedName(DungeonBlock.ID));
//    	
//    	registry.register((new ItemMultiTexture(MimicBlock.door(), MimicBlock.door(), new String[] {
//    			"breakable", // 0 meta
//    			"unbreakable", // 1 meta
//    		})).setRegistryName(MimicBlock.ID_DOOR).setCreativeTab(NostrumMagica.creativeTab));
//    	registry.register((new ItemMultiTexture(MimicBlock.facade(), MimicBlock.facade(), new String[] {
//    			"breakable", // 0 meta
//    			"unbreakable", // 1 meta
//    		})).setRegistryName(MimicBlock.ID_FACADE).setCreativeTab(NostrumMagica.creativeTab));
//    	
//    	registry.register((new ItemMultiTexture(DungeonAir.instance(), DungeonAir.instance(), new String[] {
//    			"single", // 0 meta
//    			"flood", // 1 meta
//    		})).setRegistryName(DungeonAir.ID).setCreativeTab(NostrumMagica.creativeTab));
    	
    }
    
    private static void registerBlock(Block block, String registryName, IForgeRegistry<Block> registry) {
    	block.setRegistryName(registryName);
    	registry.register(block);
    	
    	if (block instanceof ILoreTagged) {
    		LoreRegistry.instance().register((ILoreTagged)block);
    	}
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
    	final IForgeRegistry<Block> registry = event.getRegistry();
    	
    	registerBlock(new ActiveHopper(), ActiveHopper.ID, registry);
    	registerBlock(new AltarBlock(), AltarBlock.ID, registry);
    	registerBlock(new Candle(), Candle.ID, registry);
    	registerBlock(new ChalkBlock(), ChalkBlock.ID, registry);
    	registerBlock(new CropEssence(), CropEssence.ID, registry);
    	registerBlock(new CropGinseng(), CropGinseng.ID, registry);
    	registerBlock(new CropMandrakeRoot(), CropMandrakeRoot.ID, registry);
    	registerBlock(new CursedIce(), CursedIce.ID, registry);
    	registerBlock(new DungeonAir(), DungeonAir.ID, registry);
    	registerBlock(new DungeonBars(), DungeonBars.ID, registry);
    	registerBlock(new DungeonBlock(DungeonBlock.Type.LIGHT), DungeonBlock.ID_LIGHT, registry);
    	registerBlock(new DungeonBlock(DungeonBlock.Type.DARK), DungeonBlock.ID_DARK, registry);
    	registerBlock(new EssenceOre(), EssenceOre.ID, registry);
    	registerBlock(new ItemDuct(), ItemDuct.ID, registry);
    	registerBlock(new LogicDoor(), LogicDoor.ID, registry);
    	registerBlock(new LoreTable(), LoreTable.ID, registry);
    	registerBlock(new MagicDirt(), MagicDirt.ID, registry);
    	registerBlock(new MagicWall(), MagicWall.ID, registry);
    	registerBlock(new ManaArmorerBlock(), ManaArmorerBlock.ID, registry);
    	registerBlock(new ManiCrystal(0), ManiCrystal.ID_MANI, registry);
    	registerBlock(new ManiCrystal(1), ManiCrystal.ID_KANI, registry);
    	registerBlock(new ManiCrystal(2), ManiCrystal.ID_VANI, registry);
    	registerBlock(new ManiOre(), ManiOre.ID, registry);
    	registerBlock(new MimicOnesidedBlock(false, false), MimicOnesidedBlock.ID_FACADE, registry);
    	registerBlock(new MimicOnesidedBlock(false, true), MimicOnesidedBlock.ID_FACADE_UNBREAKABLE, registry);
    	registerBlock(new MimicOnesidedBlock(true, false), MimicOnesidedBlock.ID_DOOR, registry);
    	registerBlock(new MimicOnesidedBlock(true, true), MimicOnesidedBlock.ID_DOOR_UNBREAKABLE, registry);
    	registerBlock(new ModificationTable(), ModificationTable.ID, registry);
    	registerBlock(new NostrumMagicaFlower(NostrumMagicaFlower.Type.MIDNIGHT_IRIS), NostrumMagicaFlower.ID_MIDNIGHT_IRIS, registry);
    	registerBlock(new NostrumMagicaFlower(NostrumMagicaFlower.Type.CRYSTABLOOM), NostrumMagicaFlower.ID_CRYSTABLOOM, registry);
    	registerBlock(new NostrumMirrorBlock(), NostrumMirrorBlock.ID, registry);
    	registerBlock(new NostrumObelisk(), NostrumObelisk.ID, registry);
    	registerBlock(new NostrumSingleSpawner(), NostrumSingleSpawner.ID, registry);
    	registerBlock(new NostrumSpawnAndTrigger(), NostrumSpawnAndTrigger.ID, registry);
    	registerBlock(new ObeliskPortal(), ObeliskPortal.ID, registry);
    	registerBlock(new ParadoxMirrorBlock(), ParadoxMirrorBlock.ID, registry);
    	registerBlock(new PoisonWaterBlock(() -> {return NostrumFluids.poisonWater;}, false), PoisonWaterBlock.ID_BREAKABLE, registry);
    	registerBlock(new PoisonWaterBlock(() -> {return NostrumFluids.unbreakablePoisonWater;}, true), PoisonWaterBlock.ID_UNBREAKABLE, registry);
    	registerBlock(new ProgressionDoor(), ProgressionDoor.ID, registry);
    	registerBlock(new PutterBlock(), PutterBlock.ID, registry);
    	registerBlock(new ShrineBlock(), ShrineBlock.ID, registry);
    	registerBlock(new SorceryPortal(), SorceryPortal.ID, registry);
    	registerBlock(new SorceryPortalSpawner(), SorceryPortalSpawner.ID, registry);
    	registerBlock(new SpellTable(), SpellTable.ID, registry);
    	registerBlock(new SwitchBlock(), SwitchBlock.ID, registry);
    	registerBlock(new TeleportationPortal(), TeleportationPortal.ID, registry);
    	registerBlock(new SymbolBlock(), SymbolBlock.ID, registry);
    	registerBlock(new TeleportRune(), TeleportRune.ID, registry);
    	registerBlock(new TemporaryTeleportationPortal(), TemporaryTeleportationPortal.ID, registry);
    	registerBlock(new RuneShaper(), RuneShaper.ID, registry);
    	registerBlock(new LockedChest(), LockedChest.ID, registry);
    	registerBlock(new KeySwitchBlock(), KeySwitchBlock.ID, registry);
    	registerBlock(new MysticAnchor(), MysticAnchor.ID, registry);
    	registerBlock(new ToggleLogicDoor(), ToggleLogicDoor.ID, registry);
    }
    
}
