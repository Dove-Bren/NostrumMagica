package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.dungeon.CursedGlass;
import com.smanzana.nostrummagica.block.dungeon.DungeonAirBlock;
import com.smanzana.nostrummagica.block.dungeon.DungeonBarsBlock;
import com.smanzana.nostrummagica.block.dungeon.DungeonBlock;
import com.smanzana.nostrummagica.block.dungeon.DungeonDoorBlock;
import com.smanzana.nostrummagica.block.dungeon.DungeonKeyChestBlock;
import com.smanzana.nostrummagica.block.dungeon.DungeonLauncherBlock;
import com.smanzana.nostrummagica.block.dungeon.FogBlock;
import com.smanzana.nostrummagica.block.dungeon.KeySwitchBlock;
import com.smanzana.nostrummagica.block.dungeon.LaserBlock;
import com.smanzana.nostrummagica.block.dungeon.LaserLight;
import com.smanzana.nostrummagica.block.dungeon.LaserTrigger;
import com.smanzana.nostrummagica.block.dungeon.LockedChestBlock;
import com.smanzana.nostrummagica.block.dungeon.LockedDoorBlock;
import com.smanzana.nostrummagica.block.dungeon.LogicDoorBlock;
import com.smanzana.nostrummagica.block.dungeon.MagicBreakableBlock;
import com.smanzana.nostrummagica.block.dungeon.MagicBreakableContainerBlock;
import com.smanzana.nostrummagica.block.dungeon.MatchSpawnerBlock;
import com.smanzana.nostrummagica.block.dungeon.MimicBlock;
import com.smanzana.nostrummagica.block.dungeon.MimicOnesidedBlock;
import com.smanzana.nostrummagica.block.dungeon.MysticAnchorBlock;
import com.smanzana.nostrummagica.block.dungeon.ProgressionDoorBlock;
import com.smanzana.nostrummagica.block.dungeon.PushBlock;
import com.smanzana.nostrummagica.block.dungeon.PushPassthroughBlock;
import com.smanzana.nostrummagica.block.dungeon.RedstoneTriggerBlock;
import com.smanzana.nostrummagica.block.dungeon.RootingAirBlock;
import com.smanzana.nostrummagica.block.dungeon.ShrineBlock;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.block.dungeon.SummonGhostBlock;
import com.smanzana.nostrummagica.block.dungeon.SwitchBlock;
import com.smanzana.nostrummagica.block.dungeon.TemplateStamperBlock;
import com.smanzana.nostrummagica.block.dungeon.ToggleLogicDoor;
import com.smanzana.nostrummagica.block.dungeon.TogglePlatformBlock;
import com.smanzana.nostrummagica.block.dungeon.TrialBlock;
import com.smanzana.nostrummagica.block.dungeon.TriggerRepeaterBlock;
import com.smanzana.nostrummagica.block.dungeon.TriggeredMatchSpawnerBlock;
import com.smanzana.nostrummagica.fluid.NostrumFluids;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.mapmaking.BreakBlockContainerItem;
import com.smanzana.nostrummagica.item.mapmaking.ConjureGhostBlockItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
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
	@ObjectHolder(DungeonAirBlock.ID) public static DungeonAirBlock dungeonAir;
	@ObjectHolder(DungeonBarsBlock.ID) public static DungeonBarsBlock dungeonBars;
	@ObjectHolder(DungeonBlock.ID_LIGHT) public static DungeonBlock lightDungeonBlock;
	@ObjectHolder(DungeonBlock.ID_DARK) public static DungeonBlock dungeonBlock;
	@ObjectHolder(DungeonBlock.ID_STAIR_LIGHT) public static StairBlock lightDungeonStairs;
	@ObjectHolder(DungeonBlock.ID_STAIR_DARK) public static StairBlock dungeonStairs;
	@ObjectHolder(EssenceOreBlock.ID) public static EssenceOreBlock essenceOre;
	@ObjectHolder(ItemDuctBlock.ID) public static ItemDuctBlock itemDuct;
	@ObjectHolder(LogicDoorBlock.ID) public static LogicDoorBlock logicDoor;
	@ObjectHolder(LoreTableBlock.ID) public static LoreTableBlock loreTable;
	@ObjectHolder(MagicDirtBlock.ID) public static MagicDirtBlock magicDirt;
	@ObjectHolder(MagicWallBlock.ID) public static MagicWallBlock magicWall;
	@ObjectHolder(ManaArmorerBlock.ID) public static ManaArmorerBlock manaArmorerBlock;
	@ObjectHolder(ManiCrystalBlock.ID_MANI) public static ManiCrystalBlock maniCrystalBlock;
	@ObjectHolder(ManiCrystalBlock.ID_KANI) public static ManiCrystalBlock kaniCrystalBlock;
	@ObjectHolder(ManiCrystalBlock.ID_VANI) public static ManiCrystalBlock vaniCrystalBlock;
	@ObjectHolder(ManiOreBlock.ID_STONE) public static ManiOreBlock maniOreStone;
	@ObjectHolder(ManiOreBlock.ID_DEEPSLATE) public static ManiOreBlock maniOreDeepslate;
	@ObjectHolder(MimicOnesidedBlock.ID_FACADE) public static MimicBlock mimicFacade;
	@ObjectHolder(MimicOnesidedBlock.ID_FACADE_UNBREAKABLE) public static MimicBlock mimicFacadeUnbreakable;
	@ObjectHolder(MimicOnesidedBlock.ID_DOOR) public static MimicBlock mimicDoor;
	@ObjectHolder(MimicOnesidedBlock.ID_DOOR_UNBREAKABLE) public static MimicBlock mimicDoorUnbreakable;
	@ObjectHolder(ModificationTableBlock.ID) public static ModificationTableBlock modificationTable;
	@ObjectHolder(MagicaFlowerBlock.ID_MIDNIGHT_IRIS) public static MagicaFlowerBlock midnightIris;
	@ObjectHolder(MagicaFlowerBlock.ID_CRYSTABLOOM) public static MagicaFlowerBlock crystabloom;
	@ObjectHolder(MirrorBlock.ID) public static MirrorBlock mirrorBlock;
	@ObjectHolder(ObeliskBlock.ID) public static ObeliskBlock obelisk;
	@ObjectHolder(SingleSpawnerBlock.ID) public static SingleSpawnerBlock singleSpawner;
	@ObjectHolder(MatchSpawnerBlock.ID) public static MatchSpawnerBlock matchSpawner;
	@ObjectHolder(TriggeredMatchSpawnerBlock.ID) public static TriggeredMatchSpawnerBlock triggeredMatchSpawner;
	@ObjectHolder(ObeliskPortal.ID) public static ObeliskPortal obeliskPortal;
	@ObjectHolder(ParadoxMirrorBlock.ID) public static ParadoxMirrorBlock paradoxMirror;
	@ObjectHolder(PoisonWaterBlock.ID_BREAKABLE) public static PoisonWaterBlock poisonWaterBlock;
	@ObjectHolder(PoisonWaterBlock.ID_UNBREAKABLE) public static PoisonWaterBlock unbreakablePoisonWaterBlock;
	@ObjectHolder(ProgressionDoorBlock.ID) public static ProgressionDoorBlock progressionDoor;
	@ObjectHolder(PutterBlock.ID) public static PutterBlock putterBlock;
	@ObjectHolder(ShrineBlock.ID_ELEMENT) public static ShrineBlock.Element elementShrineBlock;
	@ObjectHolder(ShrineBlock.ID_SHAPE) public static ShrineBlock.Shape shapeShrineBlock;
	@ObjectHolder(ShrineBlock.ID_ALTERATION) public static ShrineBlock.Alteration alterationShrineBlock;
	@ObjectHolder(ShrineBlock.ID_TIER) public static ShrineBlock.Tier tierShrineBlock;
	@ObjectHolder(SorceryPortalBlock.ID) public static SorceryPortalBlock sorceryPortal;
	@ObjectHolder(SorceryPortalSpawnerBlock.ID) public static SorceryPortalSpawnerBlock sorceryPortalSpawner;
	@ObjectHolder(MasterSpellTableBlock.ID) public static MasterSpellTableBlock spellTable;
	@ObjectHolder(SwitchBlock.ID) public static SwitchBlock switchBlock;
	@ObjectHolder(TeleportationPortalBlock.ID) public static TeleportationPortalBlock teleportationPortal;
	@ObjectHolder(TeleportRuneBlock.ID) public static TeleportRuneBlock teleportRune;
	@ObjectHolder(TemporaryTeleportationPortalBlock.ID) public static TemporaryTeleportationPortalBlock temporaryTeleportationPortal;
	@ObjectHolder(RuneShaperBlock.ID) public static RuneShaperBlock runeShaper;
	@ObjectHolder(LockedChestBlock.ID) public static LockedChestBlock lockedChest;
	@ObjectHolder(KeySwitchBlock.ID) public static KeySwitchBlock keySwitch;
	@ObjectHolder(MysticAnchorBlock.ID) public static MysticAnchorBlock mysticAnchor;
	@ObjectHolder(ToggleLogicDoor.ID) public static ToggleLogicDoor toggleDoor;
	@ObjectHolder(TrialBlock.ID) public static TrialBlock trialBlock;
	@ObjectHolder(TriggerRepeaterBlock.ID) public static TriggerRepeaterBlock triggerRepeater;
	@ObjectHolder(MineBlock.ID) public static MineBlock mineBlock;
	@ObjectHolder(TomeWorkshopBlock.ID) public static TomeWorkshopBlock tomeWorkshop;
	@ObjectHolder(BasicSpellTableBlock.ID) public static BasicSpellTableBlock basicSpellTable;
	@ObjectHolder(AdvancedSpellTableBlock.ID) public static AdvancedSpellTableBlock advancedSpellTable;
	@ObjectHolder(MysticSpellTableBlock.ID) public static MysticSpellTableBlock mysticSpellTable;
	@ObjectHolder(RuneLibraryBlock.ID) public static RuneLibraryBlock runeLibrary;
	@ObjectHolder(CursedFireBlock.ID) public static CursedFireBlock cursedFire;
	@ObjectHolder(MysticWaterBlock.ID) public static MysticWaterBlock mysticWaterBlock;
	@ObjectHolder(LockedDoorBlock.ID) public static LockedDoorBlock lockedDoor;
	@ObjectHolder(DungeonLauncherBlock.ID) public static DungeonLauncherBlock dungeonLauncher;
	@ObjectHolder(TogglePlatformBlock.ID) public static TogglePlatformBlock togglePlatform;
	@ObjectHolder(CursedGlass.ID) public static CursedGlass cursedGlass;
	@ObjectHolder(RedstoneTriggerBlock.ID) public static RedstoneTriggerBlock redstoneTrigger;
	@ObjectHolder(MysticSnowLayerBlock.ID) public static MysticSnowLayerBlock mysticSnowLayer;
	@ObjectHolder(DungeonKeyChestBlock.Small.ID) public static DungeonKeyChestBlock.Small smallDungeonKeyChest;
	@ObjectHolder(DungeonKeyChestBlock.Large.ID) public static DungeonKeyChestBlock.Large largeDungeonKeyChest;
	@ObjectHolder(DungeonDoorBlock.Small.ID) public static DungeonDoorBlock.Small smallDungeonDoor;
	@ObjectHolder(DungeonDoorBlock.Large.ID) public static DungeonDoorBlock.Large largeDungeonDoor;
	@ObjectHolder(PushBlock.ID) public static PushBlock pushBlock;
	@ObjectHolder(PushPassthroughBlock.ID) public static PushPassthroughBlock pushPassthroughBlock;
	@ObjectHolder(MagicBreakableBlock.ID) public static MagicBreakableBlock breakBlock;
	@ObjectHolder(MagicBreakableContainerBlock.ID) public static MagicBreakableContainerBlock breakContainerBlock;
	@ObjectHolder(SummonGhostBlock.ID) public static SummonGhostBlock summonGhostBlock;
	@ObjectHolder(LaserBlock.ID) public static LaserBlock laser;
	@ObjectHolder(ElementalCrystalBlock.ID) public static ElementalCrystalBlock elementalCrystal;
	@ObjectHolder(PureWaterBlock.ID) public static PureWaterBlock pureWater; 
	@ObjectHolder(MageLightBlock.ID) public static MageLightBlock mageLight;
	@ObjectHolder(RootingAirBlock.ID) public static RootingAirBlock rootingAir;
	@ObjectHolder(ElementalStoneBlock.ID_FIRE_STONE) public static ElementalStoneBlock fireStone;
	@ObjectHolder(ElementalStoneBlock.ID_ICE_STONE) public static ElementalStoneBlock iceStone;
	@ObjectHolder(ElementalStoneBlock.ID_WIND_STONE) public static ElementalStoneBlock windStone;
	@ObjectHolder(ElementalStoneBlock.ID_EARTH_STONE) public static ElementalStoneBlock earthStone;
	@ObjectHolder(ElementalStoneBlock.ID_LIGHTNING_STONE) public static ElementalStoneBlock lightningStone;
	@ObjectHolder(ElementalStoneBlock.ID_ENDER_STONE) public static ElementalStoneBlock enderStone;
	@ObjectHolder(FogBlock.Deep.ID) public static FogBlock.Deep fogBlock;
	@ObjectHolder(FogBlock.Edge.ID) public static FogBlock.Edge fogEdgeBlock;
	@ObjectHolder(FogBlock.Hidden.ID) public static FogBlock.Hidden fogHiddenBlock;
	@ObjectHolder(LaserLight.ID) public static LaserLight laserLight;
	@ObjectHolder(LaserTrigger.ID) public static LaserTrigger laserTrigger;
	@ObjectHolder(TemplateStamperBlock.ID) public static TemplateStamperBlock templateStamper;
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, Item.Properties builder, IForgeRegistry<Item> registry) {
		BlockItem item = new BlockItem(block, builder);
    	item.setRegistryName(registryName);
    	registry.register(item);
	}
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, IForgeRegistry<Item> registry) {
		registerBlockItem(block, registryName, NostrumItems.PropBase(), registry);
	}
	
	private static void registerBlockItem(Block block, Item.Properties builder, IForgeRegistry<Item> registry) {
		registerBlockItem(block, block.getRegistryName(), builder, registry);
	}
	
	private static void registerBlockItem(Block block, IForgeRegistry<Item> registry) {
		registerBlockItem(block, block.getRegistryName(), registry);
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
    	registerBlockItem(lightDungeonStairs, lightDungeonStairs.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(dungeonStairs, dungeonStairs.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
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
    	registerBlockItem(maniOreStone, maniOreStone.getRegistryName(), registry);
    	registerBlockItem(maniOreDeepslate, maniOreDeepslate.getRegistryName(), registry);
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
    	registerBlockItem(dungeonLauncher, dungeonLauncher.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(togglePlatform, togglePlatform.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(cursedGlass, cursedGlass.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(redstoneTrigger, redstoneTrigger.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(smallDungeonKeyChest, smallDungeonKeyChest.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(largeDungeonKeyChest, largeDungeonKeyChest.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(smallDungeonDoor, smallDungeonDoor.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(largeDungeonDoor, largeDungeonDoor.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(pushBlock, pushBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(breakBlock, breakBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	//registerBlockItem(breakContainerBlock, breakContainerBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registry.register(new BreakBlockContainerItem(breakContainerBlock, NostrumItems.PropDungeonBase()).setRegistryName(breakContainerBlock.getRegistryName()));
    	registry.register(new ConjureGhostBlockItem(summonGhostBlock, NostrumItems.PropDungeonBase()).setRegistryName(summonGhostBlock.getRegistryName()));
    	registerBlockItem(laser, laser.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(elementalCrystal, elementalCrystal.getRegistryName(), NostrumItems.PropUnstackable().rarity(Rarity.EPIC), registry);
    	registerBlockItem(mageLight, mageLight.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(pushPassthroughBlock, pushPassthroughBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(rootingAir, rootingAir.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(fireStone, registry);
    	registerBlockItem(iceStone, registry);
    	registerBlockItem(windStone, registry);
    	registerBlockItem(earthStone, registry);
    	registerBlockItem(lightningStone, registry);
    	registerBlockItem(enderStone, registry);
    	registerBlockItem(fogBlock, fogBlock.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	//registerBlockItem(fogEdgeBlock, fogEdgeBlock.getRegistryName(), registry); No need
    	registerBlockItem(laserTrigger, laserTrigger.getRegistryName(), NostrumItems.PropDungeonBase(), registry);
    	registerBlockItem(templateStamper, NostrumItems.PropDungeonBase(), registry);
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
    	registerBlock(new DungeonAirBlock(), DungeonAirBlock.ID, registry);
    	registerBlock(new DungeonBarsBlock(), DungeonBarsBlock.ID, registry);
    	
    	final DungeonBlock lightDungeonBlock = new DungeonBlock(DungeonBlock.Type.LIGHT);
    	final DungeonBlock darkDungeonBlock = new DungeonBlock(DungeonBlock.Type.DARK);
    	registerBlock(lightDungeonBlock, DungeonBlock.ID_LIGHT, registry);
    	registerBlock(darkDungeonBlock, DungeonBlock.ID_DARK, registry);
    	registerBlock(new StairBlock(() -> lightDungeonBlock.defaultBlockState(), BlockBehaviour.Properties.copy(lightDungeonBlock)), DungeonBlock.ID_STAIR_LIGHT, registry);
    	registerBlock(new StairBlock(() -> darkDungeonBlock.defaultBlockState(), BlockBehaviour.Properties.copy(darkDungeonBlock)), DungeonBlock.ID_STAIR_DARK, registry);
    	registerBlock(new EssenceOreBlock(), EssenceOreBlock.ID, registry);
    	registerBlock(new ItemDuctBlock(), ItemDuctBlock.ID, registry);
    	registerBlock(new LogicDoorBlock(), LogicDoorBlock.ID, registry);
    	registerBlock(new LoreTableBlock(), LoreTableBlock.ID, registry);
    	registerBlock(new MagicDirtBlock(), MagicDirtBlock.ID, registry);
    	registerBlock(new MagicWallBlock(), MagicWallBlock.ID, registry);
    	registerBlock(new ManaArmorerBlock(), ManaArmorerBlock.ID, registry);
    	registerBlock(new ManiCrystalBlock(0), ManiCrystalBlock.ID_MANI, registry);
    	registerBlock(new ManiCrystalBlock(1), ManiCrystalBlock.ID_KANI, registry);
    	registerBlock(new ManiCrystalBlock(2), ManiCrystalBlock.ID_VANI, registry);
    	registerBlock(new ManiOreBlock(Block.Properties.of(Material.STONE)
    			.strength(3.0f, 5.f)
    			.color(MaterialColor.STONE)
    			.sound(SoundType.STONE)
    			.requiresCorrectToolForDrops()), ManiOreBlock.ID_STONE, registry);
    	registerBlock(new ManiOreBlock(Block.Properties.of(Material.STONE)
    			.strength(4.5f, 5.f)
    			.color(MaterialColor.DEEPSLATE)
    			.sound(SoundType.DEEPSLATE)
    			.requiresCorrectToolForDrops()), ManiOreBlock.ID_DEEPSLATE, registry);
    	registerBlock(new MimicOnesidedBlock(false, false), MimicOnesidedBlock.ID_FACADE, registry);
    	registerBlock(new MimicOnesidedBlock(false, true), MimicOnesidedBlock.ID_FACADE_UNBREAKABLE, registry);
    	registerBlock(new MimicOnesidedBlock(true, false), MimicOnesidedBlock.ID_DOOR, registry);
    	registerBlock(new MimicOnesidedBlock(true, true), MimicOnesidedBlock.ID_DOOR_UNBREAKABLE, registry);
    	registerBlock(new ModificationTableBlock(), ModificationTableBlock.ID, registry);
    	registerBlock(new MagicaFlowerBlock(MagicaFlowerBlock.Type.MIDNIGHT_IRIS), MagicaFlowerBlock.ID_MIDNIGHT_IRIS, registry);
    	registerBlock(new MagicaFlowerBlock(MagicaFlowerBlock.Type.CRYSTABLOOM), MagicaFlowerBlock.ID_CRYSTABLOOM, registry);
    	registerBlock(new MirrorBlock(), MirrorBlock.ID, registry);
    	registerBlock(new ObeliskBlock(), ObeliskBlock.ID, registry);
    	registerBlock(new SingleSpawnerBlock(), SingleSpawnerBlock.ID, registry);
    	registerBlock(new MatchSpawnerBlock(), MatchSpawnerBlock.ID, registry);
    	registerBlock(new TriggeredMatchSpawnerBlock(), TriggeredMatchSpawnerBlock.ID, registry);
    	registerBlock(new ObeliskPortal(), ObeliskPortal.ID, registry);
    	registerBlock(new ParadoxMirrorBlock(), ParadoxMirrorBlock.ID, registry);
    	registerBlock(new PoisonWaterBlock(() -> {return NostrumFluids.poisonWater;}, false), PoisonWaterBlock.ID_BREAKABLE, registry);
    	registerBlock(new PoisonWaterBlock(() -> {return NostrumFluids.unbreakablePoisonWater;}, true), PoisonWaterBlock.ID_UNBREAKABLE, registry);
    	registerBlock(new ProgressionDoorBlock(), ProgressionDoorBlock.ID, registry);
    	registerBlock(new PutterBlock(), PutterBlock.ID, registry);
    	registerBlock(new ShrineBlock.Element(), ShrineBlock.ID_ELEMENT, registry);
    	registerBlock(new ShrineBlock.Shape(), ShrineBlock.ID_SHAPE, registry);
    	registerBlock(new ShrineBlock.Alteration(), ShrineBlock.ID_ALTERATION, registry);
    	registerBlock(new ShrineBlock.Tier(), ShrineBlock.ID_TIER, registry);
    	registerBlock(new SorceryPortalBlock(), SorceryPortalBlock.ID, registry);
    	registerBlock(new SorceryPortalSpawnerBlock(), SorceryPortalSpawnerBlock.ID, registry);
    	registerBlock(new MasterSpellTableBlock(), MasterSpellTableBlock.ID, registry);
    	registerBlock(new SwitchBlock(), SwitchBlock.ID, registry);
    	registerBlock(new TeleportationPortalBlock(), TeleportationPortalBlock.ID, registry);
    	registerBlock(new TeleportRuneBlock(), TeleportRuneBlock.ID, registry);
    	registerBlock(new TemporaryTeleportationPortalBlock(), TemporaryTeleportationPortalBlock.ID, registry);
    	registerBlock(new RuneShaperBlock(), RuneShaperBlock.ID, registry);
    	registerBlock(new LockedChestBlock(), LockedChestBlock.ID, registry);
    	registerBlock(new KeySwitchBlock(), KeySwitchBlock.ID, registry);
    	registerBlock(new MysticAnchorBlock(), MysticAnchorBlock.ID, registry);
    	registerBlock(new ToggleLogicDoor(), ToggleLogicDoor.ID, registry);
    	registerBlock(new TrialBlock(), TrialBlock.ID, registry);
    	registerBlock(new TriggerRepeaterBlock(), TriggerRepeaterBlock.ID, registry);
    	registerBlock(new MineBlock(), MineBlock.ID, registry);
    	registerBlock(new TomeWorkshopBlock(), TomeWorkshopBlock.ID, registry);
    	registerBlock(new BasicSpellTableBlock(), BasicSpellTableBlock.ID, registry);
    	registerBlock(new AdvancedSpellTableBlock(), AdvancedSpellTableBlock.ID, registry);
    	registerBlock(new MysticSpellTableBlock(), MysticSpellTableBlock.ID, registry);
    	registerBlock(new RuneLibraryBlock(), RuneLibraryBlock.ID, registry);
    	registerBlock(new CursedFireBlock(), CursedFireBlock.ID, registry);
    	registerBlock(new MysticWaterBlock(() -> NostrumFluids.mysticWater), MysticWaterBlock.ID, registry);
    	registerBlock(new LockedDoorBlock(), LockedDoorBlock.ID, registry);
    	registerBlock(new DungeonLauncherBlock(), DungeonLauncherBlock.ID, registry);
    	registerBlock(new TogglePlatformBlock(), TogglePlatformBlock.ID, registry);
    	registerBlock(new CursedGlass(), CursedGlass.ID, registry);
    	registerBlock(new RedstoneTriggerBlock(), RedstoneTriggerBlock.ID, registry);
    	registerBlock(new MysticSnowLayerBlock(), MysticSnowLayerBlock.ID, registry);
    	registerBlock(new DungeonKeyChestBlock.Small(), DungeonKeyChestBlock.Small.ID, registry);
    	registerBlock(new DungeonKeyChestBlock.Large(), DungeonKeyChestBlock.Large.ID, registry);
    	
    	registerBlock(new DungeonDoorBlock.Small(), DungeonDoorBlock.Small.ID, registry);
    	registerBlock(new DungeonDoorBlock.Large(), DungeonDoorBlock.Large.ID, registry);
    	registerBlock(new PushBlock(), PushBlock.ID, registry);
    	registerBlock(new MagicBreakableBlock(), MagicBreakableBlock.ID, registry);
    	registerBlock(new MagicBreakableContainerBlock(), MagicBreakableContainerBlock.ID, registry);
    	registerBlock(new SummonGhostBlock(), SummonGhostBlock.ID, registry);
    	registerBlock(new LaserBlock(), LaserBlock.ID, registry);
    	registerBlock(new ElementalCrystalBlock(), ElementalCrystalBlock.ID, registry);
    	registerBlock(new PureWaterBlock(() -> NostrumFluids.pureWater), PureWaterBlock.ID, registry);
    	registerBlock(new MageLightBlock(), MageLightBlock.ID, registry);
    	registerBlock(new PushPassthroughBlock(), PushPassthroughBlock.ID, registry);
    	registerBlock(new RootingAirBlock(), RootingAirBlock.ID, registry);
    	
    	final ElementalStoneBlock fireStone = new ElementalStoneBlock(BlockBehaviour.Properties.of(Material.AMETHYST).sound(SoundType.AMETHYST_CLUSTER).noOcclusion().strength(2.0F, 6.0F).requiresCorrectToolForDrops(), EMagicElement.FIRE);
    	registerBlock(fireStone, ElementalStoneBlock.ID_FIRE_STONE, registry);
    	registerBlock(new ElementalStoneBlock(BlockBehaviour.Properties.copy(fireStone), EMagicElement.ICE), ElementalStoneBlock.ID_ICE_STONE, registry);
    	registerBlock(new ElementalStoneBlock(BlockBehaviour.Properties.copy(fireStone), EMagicElement.WIND), ElementalStoneBlock.ID_WIND_STONE, registry);
    	registerBlock(new ElementalStoneBlock(BlockBehaviour.Properties.copy(fireStone), EMagicElement.EARTH), ElementalStoneBlock.ID_EARTH_STONE, registry);
    	registerBlock(new ElementalStoneBlock(BlockBehaviour.Properties.copy(fireStone), EMagicElement.LIGHTNING), ElementalStoneBlock.ID_LIGHTNING_STONE, registry);
    	registerBlock(new ElementalStoneBlock(BlockBehaviour.Properties.copy(fireStone), EMagicElement.ENDER), ElementalStoneBlock.ID_ENDER_STONE, registry);
    	
    	registerBlock(new FogBlock.Deep(), FogBlock.Deep.ID, registry);
    	registerBlock(new FogBlock.Edge(), FogBlock.Edge.ID, registry);
    	registerBlock(new FogBlock.Hidden(), FogBlock.Hidden.ID, registry);
    	registerBlock(new LaserLight(), LaserLight.ID, registry);
    	registerBlock(new LaserTrigger(), LaserTrigger.ID, registry);
    	registerBlock(new TemplateStamperBlock(), TemplateStamperBlock.ID, registry);
    }

	public static Block elementalStone(EMagicElement element) {
		switch (element) {
		case EARTH:
			return earthStone;
		case ENDER:
			return enderStone;
		case FIRE:
			return fireStone;
		case ICE:
			return iceStone;
		case LIGHTNING:
			return lightningStone;
		case PHYSICAL:
			return null;
		case WIND:
			return windStone;
		}
		
		return null;
	}
    
}
