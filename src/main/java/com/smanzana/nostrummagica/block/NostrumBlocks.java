package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.DungeonAir;
import com.smanzana.nostrummagica.block.dungeon.DungeonBars;
import com.smanzana.nostrummagica.block.dungeon.DungeonBlock;
import com.smanzana.nostrummagica.block.dungeon.KeySwitchBlock;
import com.smanzana.nostrummagica.block.dungeon.LockedChest;
import com.smanzana.nostrummagica.block.dungeon.LockedDoor;
import com.smanzana.nostrummagica.block.dungeon.LogicDoor;
import com.smanzana.nostrummagica.block.dungeon.MimicBlock;
import com.smanzana.nostrummagica.block.dungeon.MimicOnesidedBlock;
import com.smanzana.nostrummagica.block.dungeon.MysticAnchor;
import com.smanzana.nostrummagica.block.dungeon.NostrumMatchSpawner;
import com.smanzana.nostrummagica.block.dungeon.NostrumSingleSpawner;
import com.smanzana.nostrummagica.block.dungeon.NostrumTriggeredMatchSpawner;
import com.smanzana.nostrummagica.block.dungeon.ProgressionDoor;
import com.smanzana.nostrummagica.block.dungeon.ShrineBlock;
import com.smanzana.nostrummagica.block.dungeon.SwitchBlock;
import com.smanzana.nostrummagica.block.dungeon.ToggleLogicDoor;
import com.smanzana.nostrummagica.block.dungeon.TrialBlock;
import com.smanzana.nostrummagica.block.dungeon.TriggerRepeater;
import com.smanzana.nostrummagica.fluid.NostrumFluids;
import com.smanzana.nostrummagica.item.NostrumItems;
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
	
	@ObjectHolder(ActiveHopperBlock.ID) public static ActiveHopperBlock activeHopper;
	@ObjectHolder(AltarBlock.ID) public static AltarBlock altar;
	@ObjectHolder(CandleBlock.ID) public static CandleBlock candle;
	@ObjectHolder(ChalkBlock.ID) public static ChalkBlock chalk;
	@ObjectHolder(EssenceCropBlock.ID) public static EssenceCropBlock essenceCrop;
	@ObjectHolder(GinsengCropBlock.ID) public static GinsengCropBlock ginsengCrop;
	@ObjectHolder(MandrakeRootCropBlock.ID) public static MandrakeRootCropBlock mandrakeCrop;
	@ObjectHolder(CursedIceBlock.ID) public static CursedIceBlock cursedIce;
	@ObjectHolder(DungeonAir.ID) public static DungeonAir dungeonAir;
	@ObjectHolder(DungeonBars.ID) public static DungeonBars dungeonBars;
	@ObjectHolder(DungeonBlock.ID_LIGHT) public static DungeonBlock lightDungeonBlock;
	@ObjectHolder(DungeonBlock.ID_DARK) public static DungeonBlock dungeonBlock;
	@ObjectHolder(EssenceOreBlock.ID) public static EssenceOreBlock essenceOre;
	@ObjectHolder(ItemDuctBlock.ID) public static ItemDuctBlock itemDuct;
	@ObjectHolder(LogicDoor.ID) public static LogicDoor logicDoor;
	@ObjectHolder(LoreTableBlock.ID) public static LoreTableBlock loreTable;
	@ObjectHolder(MagicDirtBlock.ID) public static MagicDirtBlock magicDirt;
	@ObjectHolder(MagicWallBlock.ID) public static MagicWallBlock magicWall;
	@ObjectHolder(ManaArmorerBlock.ID) public static ManaArmorerBlock manaArmorerBlock;
	@ObjectHolder(ManiCrystalBlock.ID_MANI) public static ManiCrystalBlock maniCrystalBlock;
	@ObjectHolder(ManiCrystalBlock.ID_KANI) public static ManiCrystalBlock kaniCrystalBlock;
	@ObjectHolder(ManiCrystalBlock.ID_VANI) public static ManiCrystalBlock vaniCrystalBlock;
	@ObjectHolder(ManiOreBlock.ID) public static ManiOreBlock maniOre;
	@ObjectHolder(MimicOnesidedBlock.ID_FACADE) public static MimicBlock mimicFacade;
	@ObjectHolder(MimicOnesidedBlock.ID_FACADE_UNBREAKABLE) public static MimicBlock mimicFacadeUnbreakable;
	@ObjectHolder(MimicOnesidedBlock.ID_DOOR) public static MimicBlock mimicDoor;
	@ObjectHolder(MimicOnesidedBlock.ID_DOOR_UNBREAKABLE) public static MimicBlock mimicDoorUnbreakable;
	@ObjectHolder(ModificationTableBlock.ID) public static ModificationTableBlock modificationTable;
	@ObjectHolder(MagicaFlowerBlock.ID_MIDNIGHT_IRIS) public static MagicaFlowerBlock midnightIris;
	@ObjectHolder(MagicaFlowerBlock.ID_CRYSTABLOOM) public static MagicaFlowerBlock crystabloom;
	@ObjectHolder(MirrorBlock.ID) public static MirrorBlock mirrorBlock;
	@ObjectHolder(ObeliskBlock.ID) public static ObeliskBlock obelisk;
	@ObjectHolder(NostrumSingleSpawner.ID) public static NostrumSingleSpawner singleSpawner;
	@ObjectHolder(NostrumMatchSpawner.ID) public static NostrumMatchSpawner matchSpawner;
	@ObjectHolder(NostrumTriggeredMatchSpawner.ID) public static NostrumTriggeredMatchSpawner triggeredMatchSpawner;
	@ObjectHolder(ObeliskPortal.ID) public static ObeliskPortal obeliskPortal;
	@ObjectHolder(ParadoxMirrorBlock.ID) public static ParadoxMirrorBlock paradoxMirror;
	@ObjectHolder(PoisonWaterBlock.ID_BREAKABLE) public static PoisonWaterBlock poisonWaterBlock;
	@ObjectHolder(PoisonWaterBlock.ID_UNBREAKABLE) public static PoisonWaterBlock unbreakablePoisonWaterBlock;
	@ObjectHolder(ProgressionDoor.ID) public static ProgressionDoor progressionDoor;
	@ObjectHolder(PutterBlock.ID) public static PutterBlock putterBlock;
	@ObjectHolder(ShrineBlock.ID_ELEMENT) public static ShrineBlock.Element elementShrineBlock;
	@ObjectHolder(ShrineBlock.ID_SHAPE) public static ShrineBlock.Shape shapeShrineBlock;
	@ObjectHolder(ShrineBlock.ID_ALTERATION) public static ShrineBlock.Alteration alterationShrineBlock;
	@ObjectHolder(SorceryPortalBlock.ID) public static SorceryPortalBlock sorceryPortal;
	@ObjectHolder(SorceryPortalSpawnerBlock.ID) public static SorceryPortalSpawnerBlock sorceryPortalSpawner;
	@ObjectHolder(MasterSpellTableBlock.ID) public static MasterSpellTableBlock spellTable;
	@ObjectHolder(SwitchBlock.ID) public static SwitchBlock switchBlock;
	@ObjectHolder(TeleportationPortalBlock.ID) public static TeleportationPortalBlock teleportationPortal;
	@ObjectHolder(TeleportRuneBlock.ID) public static TeleportRuneBlock teleportRune;
	@ObjectHolder(TemporaryTeleportationPortalBlock.ID) public static TemporaryTeleportationPortalBlock temporaryTeleportationPortal;
	@ObjectHolder(RuneShaperBlock.ID) public static RuneShaperBlock runeShaper;
	@ObjectHolder(LockedChest.ID) public static LockedChest lockedChest;
	@ObjectHolder(KeySwitchBlock.ID) public static KeySwitchBlock keySwitch;
	@ObjectHolder(MysticAnchor.ID) public static MysticAnchor mysticAnchor;
	@ObjectHolder(ToggleLogicDoor.ID) public static ToggleLogicDoor toggleDoor;
	@ObjectHolder(TrialBlock.ID) public static TrialBlock trialBlock;
	@ObjectHolder(TriggerRepeater.ID) public static TriggerRepeater triggerRepeater;
	@ObjectHolder(MineBlock.ID) public static MineBlock mineBlock;
	@ObjectHolder(TomeWorkshopBlock.ID) public static TomeWorkshopBlock tomeWorkshop;
	@ObjectHolder(BasicSpellTableBlock.ID) public static BasicSpellTableBlock basicSpellTable;
	@ObjectHolder(AdvancedSpellTableBlock.ID) public static AdvancedSpellTableBlock advancedSpellTable;
	@ObjectHolder(MysticSpellTableBlock.ID) public static MysticSpellTableBlock mysticSpellTable;
	@ObjectHolder(RuneLibraryBlock.ID) public static RuneLibraryBlock runeLibrary;
	@ObjectHolder(CursedFireBlock.ID) public static CursedFireBlock cursedFire;
	@ObjectHolder(MysticWaterBlock.ID) public static MysticWaterBlock mysticWaterBlock;
	@ObjectHolder(LockedDoor.ID) public static LockedDoor lockedDoor;
	
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
    	registerBlockItem(dungeonAir, dungeonAir.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(dungeonBars, dungeonBars.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(lightDungeonBlock, lightDungeonBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(dungeonBlock, dungeonBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(essenceOre, essenceOre.getRegistryName(), registry);
    	registerBlockItem(itemDuct, itemDuct.getRegistryName(), registry);
    	registerBlockItem(logicDoor, logicDoor.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(loreTable, loreTable.getRegistryName(), registry);
    	registerBlockItem(magicDirt, magicDirt.getRegistryName(), registry);
    	registerBlockItem(magicWall, magicWall.getRegistryName(), registry);
    	registerBlockItem(manaArmorerBlock, manaArmorerBlock.getRegistryName(), registry);
    	//registerBlockItem(maniCrystalBlock, maniCrystalBlock.getRegistryName(), registry);
    	//registerBlockItem(kaniCrystalBlock, kaniCrystalBlock.getRegistryName(), registry);
    	//registerBlockItem(vaniCrystalBlock, vaniCrystalBlock.getRegistryName(), registry);
    	registerBlockItem(maniOre, maniOre.getRegistryName(), registry);
    	registerBlockItem(mimicFacade, mimicFacade.getRegistryName(), registry);
    	registerBlockItem(mimicFacadeUnbreakable, mimicFacadeUnbreakable.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(mimicDoor, mimicDoor.getRegistryName(), registry);
    	registerBlockItem(mimicDoorUnbreakable, mimicDoorUnbreakable.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(modificationTable, modificationTable.getRegistryName(), registry);
    	//registerBlockItem(midnightIris, midnightIris.getRegistryName(), registry);
    	//registerBlockItem(crystabloom, crystabloom.getRegistryName(), registry);
    	registerBlockItem(mirrorBlock, mirrorBlock.getRegistryName(), registry);
    	//registerBlockItem(obelisk, obelisk.getRegistryName(), registry);
    	registerBlockItem(singleSpawner, singleSpawner.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(matchSpawner, matchSpawner.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(triggeredMatchSpawner, triggeredMatchSpawner.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	//registerBlockItem(obeliskPortal, obeliskPortal.getRegistryName(), registry);
    	registerBlockItem(paradoxMirror, paradoxMirror.getRegistryName(), registry);
    	//registerBlockItem(poisonWaterBlock, poisonWaterBlock.getRegistryName(), registry);
    	//registerBlockItem(unbreakablePoisonWaterBlock, unbreakablePoisonWaterBlock.getRegistryName(), registry);
    	registerBlockItem(progressionDoor, progressionDoor.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(putterBlock, putterBlock.getRegistryName(), registry);
    	//registerBlockItem(shrineBlock, shrineBlock.getRegistryName(), registry);
    	//registerBlockItem(sorceryPortal, sorceryPortal.getRegistryName(), registry);
    	//registerBlockItem(sorceryPortalSpawner, sorceryPortalSpawner.getRegistryName(), registry);
    	registerBlockItem(spellTable, spellTable.getRegistryName(), registry);
    	registerBlockItem(switchBlock, switchBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	//registerBlockItem(teleportationPortal, teleportationPortal.getRegistryName(), registry);
    	registerBlockItem(teleportRune, teleportRune.getRegistryName(), registry);
    	//registerBlockItem(temporaryTeleportationPortal, temporaryTeleportationPortal.getRegistryName(), registry);
    	registerBlockItem(runeShaper, runeShaper.getRegistryName(), registry);
    	registerBlockItem(keySwitch, keySwitch.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(mysticAnchor, mysticAnchor.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(toggleDoor, toggleDoor.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(triggerRepeater, triggerRepeater.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(tomeWorkshop, tomeWorkshop.getRegistryName(), registry);
    	registerBlockItem(basicSpellTable, basicSpellTable.getRegistryName(), registry);
    	registerBlockItem(advancedSpellTable, advancedSpellTable.getRegistryName(), registry);
    	registerBlockItem(mysticSpellTable, mysticSpellTable.getRegistryName(), registry);
    	registerBlockItem(runeLibrary, runeLibrary.getRegistryName(), registry);
    	registerBlockItem(lockedDoor, lockedDoor.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
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
    	
    	registerBlock(new ActiveHopperBlock(), ActiveHopperBlock.ID, registry);
    	registerBlock(new AltarBlock(), AltarBlock.ID, registry);
    	registerBlock(new CandleBlock(), CandleBlock.ID, registry);
    	registerBlock(new ChalkBlock(), ChalkBlock.ID, registry);
    	registerBlock(new EssenceCropBlock(), EssenceCropBlock.ID, registry);
    	registerBlock(new GinsengCropBlock(), GinsengCropBlock.ID, registry);
    	registerBlock(new MandrakeRootCropBlock(), MandrakeRootCropBlock.ID, registry);
    	registerBlock(new CursedIceBlock(), CursedIceBlock.ID, registry);
    	registerBlock(new DungeonAir(), DungeonAir.ID, registry);
    	registerBlock(new DungeonBars(), DungeonBars.ID, registry);
    	registerBlock(new DungeonBlock(DungeonBlock.Type.LIGHT), DungeonBlock.ID_LIGHT, registry);
    	registerBlock(new DungeonBlock(DungeonBlock.Type.DARK), DungeonBlock.ID_DARK, registry);
    	registerBlock(new EssenceOreBlock(), EssenceOreBlock.ID, registry);
    	registerBlock(new ItemDuctBlock(), ItemDuctBlock.ID, registry);
    	registerBlock(new LogicDoor(), LogicDoor.ID, registry);
    	registerBlock(new LoreTableBlock(), LoreTableBlock.ID, registry);
    	registerBlock(new MagicDirtBlock(), MagicDirtBlock.ID, registry);
    	registerBlock(new MagicWallBlock(), MagicWallBlock.ID, registry);
    	registerBlock(new ManaArmorerBlock(), ManaArmorerBlock.ID, registry);
    	registerBlock(new ManiCrystalBlock(0), ManiCrystalBlock.ID_MANI, registry);
    	registerBlock(new ManiCrystalBlock(1), ManiCrystalBlock.ID_KANI, registry);
    	registerBlock(new ManiCrystalBlock(2), ManiCrystalBlock.ID_VANI, registry);
    	registerBlock(new ManiOreBlock(), ManiOreBlock.ID, registry);
    	registerBlock(new MimicOnesidedBlock(false, false), MimicOnesidedBlock.ID_FACADE, registry);
    	registerBlock(new MimicOnesidedBlock(false, true), MimicOnesidedBlock.ID_FACADE_UNBREAKABLE, registry);
    	registerBlock(new MimicOnesidedBlock(true, false), MimicOnesidedBlock.ID_DOOR, registry);
    	registerBlock(new MimicOnesidedBlock(true, true), MimicOnesidedBlock.ID_DOOR_UNBREAKABLE, registry);
    	registerBlock(new ModificationTableBlock(), ModificationTableBlock.ID, registry);
    	registerBlock(new MagicaFlowerBlock(MagicaFlowerBlock.Type.MIDNIGHT_IRIS), MagicaFlowerBlock.ID_MIDNIGHT_IRIS, registry);
    	registerBlock(new MagicaFlowerBlock(MagicaFlowerBlock.Type.CRYSTABLOOM), MagicaFlowerBlock.ID_CRYSTABLOOM, registry);
    	registerBlock(new MirrorBlock(), MirrorBlock.ID, registry);
    	registerBlock(new ObeliskBlock(), ObeliskBlock.ID, registry);
    	registerBlock(new NostrumSingleSpawner(), NostrumSingleSpawner.ID, registry);
    	registerBlock(new NostrumMatchSpawner(), NostrumMatchSpawner.ID, registry);
    	registerBlock(new NostrumTriggeredMatchSpawner(), NostrumTriggeredMatchSpawner.ID, registry);
    	registerBlock(new ObeliskPortal(), ObeliskPortal.ID, registry);
    	registerBlock(new ParadoxMirrorBlock(), ParadoxMirrorBlock.ID, registry);
    	registerBlock(new PoisonWaterBlock(() -> {return NostrumFluids.poisonWater;}, false), PoisonWaterBlock.ID_BREAKABLE, registry);
    	registerBlock(new PoisonWaterBlock(() -> {return NostrumFluids.unbreakablePoisonWater;}, true), PoisonWaterBlock.ID_UNBREAKABLE, registry);
    	registerBlock(new ProgressionDoor(), ProgressionDoor.ID, registry);
    	registerBlock(new PutterBlock(), PutterBlock.ID, registry);
    	registerBlock(new ShrineBlock.Element(), ShrineBlock.ID_ELEMENT, registry);
    	registerBlock(new ShrineBlock.Shape(), ShrineBlock.ID_SHAPE, registry);
    	registerBlock(new ShrineBlock.Alteration(), ShrineBlock.ID_ALTERATION, registry);
    	registerBlock(new SorceryPortalBlock(), SorceryPortalBlock.ID, registry);
    	registerBlock(new SorceryPortalSpawnerBlock(), SorceryPortalSpawnerBlock.ID, registry);
    	registerBlock(new MasterSpellTableBlock(), MasterSpellTableBlock.ID, registry);
    	registerBlock(new SwitchBlock(), SwitchBlock.ID, registry);
    	registerBlock(new TeleportationPortalBlock(), TeleportationPortalBlock.ID, registry);
    	registerBlock(new TeleportRuneBlock(), TeleportRuneBlock.ID, registry);
    	registerBlock(new TemporaryTeleportationPortalBlock(), TemporaryTeleportationPortalBlock.ID, registry);
    	registerBlock(new RuneShaperBlock(), RuneShaperBlock.ID, registry);
    	registerBlock(new LockedChest(), LockedChest.ID, registry);
    	registerBlock(new KeySwitchBlock(), KeySwitchBlock.ID, registry);
    	registerBlock(new MysticAnchor(), MysticAnchor.ID, registry);
    	registerBlock(new ToggleLogicDoor(), ToggleLogicDoor.ID, registry);
    	registerBlock(new TrialBlock(), TrialBlock.ID, registry);
    	registerBlock(new TriggerRepeater(), TriggerRepeater.ID, registry);
    	registerBlock(new MineBlock(), MineBlock.ID, registry);
    	registerBlock(new TomeWorkshopBlock(), TomeWorkshopBlock.ID, registry);
    	registerBlock(new BasicSpellTableBlock(), BasicSpellTableBlock.ID, registry);
    	registerBlock(new AdvancedSpellTableBlock(), AdvancedSpellTableBlock.ID, registry);
    	registerBlock(new MysticSpellTableBlock(), MysticSpellTableBlock.ID, registry);
    	registerBlock(new RuneLibraryBlock(), RuneLibraryBlock.ID, registry);
    	registerBlock(new CursedFireBlock(), CursedFireBlock.ID, registry);
    	registerBlock(new MysticWaterBlock(() -> NostrumFluids.mysticWater), MysticWaterBlock.ID, registry);
    	registerBlock(new LockedDoor(), LockedDoor.ID, registry);
    }
    
}
