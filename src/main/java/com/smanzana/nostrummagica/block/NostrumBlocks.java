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
	@ObjectHolder(SorceryPortal.ID) public static SorceryPortal sorceryPortal;
	@ObjectHolder(SorceryPortalSpawner.ID) public static SorceryPortalSpawner sorceryPortalSpawner;
	@ObjectHolder(MasterSpellTable.ID) public static MasterSpellTable spellTable;
	@ObjectHolder(SwitchBlock.ID) public static SwitchBlock switchBlock;
	@ObjectHolder(TeleportationPortal.ID) public static TeleportationPortal teleportationPortal;
	@ObjectHolder(TeleportRune.ID) public static TeleportRune teleportRune;
	@ObjectHolder(TemporaryTeleportationPortal.ID) public static TemporaryTeleportationPortal temporaryTeleportationPortal;
	@ObjectHolder(RuneShaper.ID) public static RuneShaper runeShaper;
	@ObjectHolder(LockedChest.ID) public static LockedChest lockedChest;
	@ObjectHolder(KeySwitchBlock.ID) public static KeySwitchBlock keySwitch;
	@ObjectHolder(MysticAnchor.ID) public static MysticAnchor mysticAnchor;
	@ObjectHolder(ToggleLogicDoor.ID) public static ToggleLogicDoor toggleDoor;
	@ObjectHolder(TrialBlock.ID) public static TrialBlock trialBlock;
	@ObjectHolder(TriggerRepeater.ID) public static TriggerRepeater triggerRepeater;
	@ObjectHolder(MineBlock.ID) public static MineBlock mineBlock;
	@ObjectHolder(TomeWorkshopBlock.ID) public static TomeWorkshopBlock tomeWorkshop;
	@ObjectHolder(BasicSpellTable.ID) public static BasicSpellTable basicSpellTable;
	@ObjectHolder(AdvancedSpellTable.ID) public static AdvancedSpellTable advancedSpellTable;
	@ObjectHolder(MysticSpellTable.ID) public static MysticSpellTable mysticSpellTable;
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
    	registerBlock(new SorceryPortal(), SorceryPortal.ID, registry);
    	registerBlock(new SorceryPortalSpawner(), SorceryPortalSpawner.ID, registry);
    	registerBlock(new MasterSpellTable(), MasterSpellTable.ID, registry);
    	registerBlock(new SwitchBlock(), SwitchBlock.ID, registry);
    	registerBlock(new TeleportationPortal(), TeleportationPortal.ID, registry);
    	registerBlock(new TeleportRune(), TeleportRune.ID, registry);
    	registerBlock(new TemporaryTeleportationPortal(), TemporaryTeleportationPortal.ID, registry);
    	registerBlock(new RuneShaper(), RuneShaper.ID, registry);
    	registerBlock(new LockedChest(), LockedChest.ID, registry);
    	registerBlock(new KeySwitchBlock(), KeySwitchBlock.ID, registry);
    	registerBlock(new MysticAnchor(), MysticAnchor.ID, registry);
    	registerBlock(new ToggleLogicDoor(), ToggleLogicDoor.ID, registry);
    	registerBlock(new TrialBlock(), TrialBlock.ID, registry);
    	registerBlock(new TriggerRepeater(), TriggerRepeater.ID, registry);
    	registerBlock(new MineBlock(), MineBlock.ID, registry);
    	registerBlock(new TomeWorkshopBlock(), TomeWorkshopBlock.ID, registry);
    	registerBlock(new BasicSpellTable(), BasicSpellTable.ID, registry);
    	registerBlock(new AdvancedSpellTable(), AdvancedSpellTable.ID, registry);
    	registerBlock(new MysticSpellTable(), MysticSpellTable.ID, registry);
    	registerBlock(new RuneLibraryBlock(), RuneLibraryBlock.ID, registry);
    	registerBlock(new CursedFireBlock(), CursedFireBlock.ID, registry);
    	registerBlock(new MysticWaterBlock(() -> NostrumFluids.mysticWater), MysticWaterBlock.ID, registry);
    	registerBlock(new LockedDoor(), LockedDoor.ID, registry);
    }
    
}
