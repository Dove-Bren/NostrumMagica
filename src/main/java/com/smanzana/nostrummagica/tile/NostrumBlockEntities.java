package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumBlockEntities {
	
	private static final String ID_SpellTable = "spell_table";
	private static final String ID_SingleSpawner = "nostrum_mob_spawner_te";
	private static final String ID_MatchSpawner = "nostrum_mob_spawner_trigger_te";
	private static final String ID_TriggeredMatchSpawner = "triggered_match_spawner_te";
	private static final String ID_Altar = "nostrum_altar_te";
	private static final String ID_Candle = "nostrum_candle_te";
	private static final String ID_NostrumObelisk = "nostrum_obelisk";
	private static final String ID_ObeliskPortal = "obelisk_portal";
	private static final String ID_ModificationTable = "modification_table";
	private static final String ID_LoreTable = "lore_table";
	private static final String ID_SorceryPortal = "sorcery_portal";
	private static final String ID_TeleportationPortal = "teleportation_portal";
	private static final String ID_TemporaryPortal = "limited_teleportation_portal";
	private static final String ID_ProgressionDoor = "progression_door";
	private static final String ID_SwitchBlock = "switch_block_tile_entity";
	private static final String ID_TeleportRune = "teleport_rune";
	private static final String ID_PutterBlock = "putter_entity";
	private static final String ID_ActiveHopper = "active_hopper_te";
	private static final String ID_ItemDuct = "item_duct_te";
	private static final String ID_ParadoxMirror = "paradox_mirror_te";
	private static final String ID_ManaArmorer = "mana_armorer";
	private static final String ID_MimicBlock = "mimic_block_te";
	private static final String ID_RuneShaper = "rune_shaper_te";
	private static final String ID_LockedChest = "locked_chest_te";
	private static final String ID_KeySwitch = "key_switch_te";
	private static final String ID_TrialBlock = "trial_block_te";
	private static final String ID_TriggerRepeater = "trigger_repeater_te";
	private static final String ID_DelayedMimicBlock = "delayed_mimic_block_te";
	private static final String ID_BasicSpellTable = "spelltable_basic_te";
	private static final String ID_AdvancedSpellTable = "spelltable_advanced_te";
	private static final String ID_MysticSpellTable = "spelltable_mystic_te";
	private static final String ID_RuneLibrary = "rune_library_te";
	private static final String ID_LockedDoor = "locked_door";
	private static final String ID_ElementShrine = "element_shrine_te";
	private static final String ID_AlterationShrine = "alteration_shrine_te";
	private static final String ID_ShapeShrine = "shape_shrine_te";
	private static final String ID_TierShrine = "tier_shrine_te";
	private static final String ID_DungeonLauncher = "launcher_te";
	private static final String ID_CursedGlass = "cursed_glass_te";
	private static final String ID_DungeonKeyChest = "dungeon_key_chest_te";
	private static final String ID_DungeonDoor = "dungeon_door_te";
	private static final String ID_PushBlock = "push_block_te";
	private static final String ID_BreakContainer = "break_container_te";
	private static final String ID_SummonGhostBlock = "conjure_ghost_block_te";
	private static final String ID_LaserBlock = "laser_block_ent";
	private static final String ID_ElementalCrystal = "elemental_crystal";
	private static final String ID_LaserTrigger = "laser_trigger";
	

	@ObjectHolder(ID_SpellTable) public static BlockEntityType<SpellTableTileEntity> SpellTable;
	@ObjectHolder(ID_SingleSpawner) public static BlockEntityType<SingleSpawnerTileEntity> SingleSpawner;
	@ObjectHolder(ID_MatchSpawner) public static BlockEntityType<MatchSpawnerTileEntity> MatchSpawner;
	@ObjectHolder(ID_TriggeredMatchSpawner) public static BlockEntityType<TriggeredMatchSpawnerTileEntity> TriggeredMatchSpawner;
	@ObjectHolder(ID_Altar) public static BlockEntityType<AltarTileEntity> Altar;
	@ObjectHolder(ID_Candle) public static BlockEntityType<CandleTileEntity> Candle;
	@ObjectHolder(ID_NostrumObelisk) public static BlockEntityType<ObeliskTileEntity> NostrumObelisk;
	@ObjectHolder(ID_ObeliskPortal) public static BlockEntityType<ObeliskPortalTileEntity> ObeliskPortal;
	@ObjectHolder(ID_ModificationTable) public static BlockEntityType<ModificationTableTileEntity> ModificationTable;
	@ObjectHolder(ID_LoreTable) public static BlockEntityType<LoreTableTileEntity> LoreTable;
	@ObjectHolder(ID_SorceryPortal) public static BlockEntityType<SorceryPortalTileEntity> SorceryPortal;
	@ObjectHolder(ID_TeleportationPortal) public static BlockEntityType<TeleportationPortalTileEntity> TeleportationPortal;
	@ObjectHolder(ID_TemporaryPortal) public static BlockEntityType<TemporaryPortalTileEntity> TemporaryPortal;
	@ObjectHolder(ID_ProgressionDoor) public static BlockEntityType<ProgressionDoorTileEntity> ProgressionDoor;
	@ObjectHolder(ID_SwitchBlock) public static BlockEntityType<SwitchBlockTileEntity> SwitchBlock;
	@ObjectHolder(ID_TeleportRune) public static BlockEntityType<TeleportRuneTileEntity> TeleportRune;
	@ObjectHolder(ID_PutterBlock) public static BlockEntityType<PutterBlockTileEntity> PutterBlock;
	@ObjectHolder(ID_ActiveHopper) public static BlockEntityType<ActiveHopperTileEntity> ActiveHopper;
	@ObjectHolder(ID_ItemDuct) public static BlockEntityType<ItemDuctTileEntity> ItemDuct;
	@ObjectHolder(ID_ParadoxMirror) public static BlockEntityType<ParadoxMirrorTileEntity> ParadoxMirror;
	@ObjectHolder(ID_ManaArmorer) public static BlockEntityType<ManaArmorerTileEntity> ManaArmorer;
	@ObjectHolder(ID_MimicBlock) public static BlockEntityType<MimicBlockTileEntity> MimicBlock;
	@ObjectHolder(ID_RuneShaper) public static BlockEntityType<RuneShaperTileEntity> RuneShaper;
	@ObjectHolder(ID_LockedChest) public static BlockEntityType<LockedChestTileEntity> LockedChest;
	@ObjectHolder(ID_KeySwitch) public static BlockEntityType<KeySwitchBlockTileEntity> KeySwitch;
	@ObjectHolder(ID_TrialBlock) public static BlockEntityType<TrialBlockTileEntity> TrialBlock;
	@ObjectHolder(ID_TriggerRepeater) public static BlockEntityType<TriggerRepeaterTileEntity> TriggerRepeater;
	@ObjectHolder(ID_DelayedMimicBlock) public static BlockEntityType<DelayLoadedMimicBlockTileEntity> DelayedMimicBlock;
	@ObjectHolder(ID_BasicSpellTable) public static BlockEntityType<BasicSpellTableTileEntity> BasicSpellTable;
	@ObjectHolder(ID_AdvancedSpellTable) public static BlockEntityType<AdvancedSpellTableTileEntity> AdvancedSpellTable;
	@ObjectHolder(ID_MysticSpellTable) public static BlockEntityType<MysticSpellTableTileEntity> MysticSpellTable;
	@ObjectHolder(ID_RuneLibrary) public static BlockEntityType<RuneLibraryTileEntity> RuneLibrary;
	@ObjectHolder(ID_LockedDoor) public static BlockEntityType<LockedDoorTileEntity> LockedDoor;
	@ObjectHolder(ID_ElementShrine) public static BlockEntityType<ShrineTileEntity.Element> ElementShrine;
	@ObjectHolder(ID_AlterationShrine) public static BlockEntityType<ShrineTileEntity.Alteration> AlterationShrine;
	@ObjectHolder(ID_ShapeShrine) public static BlockEntityType<ShrineTileEntity.Shape> ShapeShrine;
	@ObjectHolder(ID_TierShrine) public static BlockEntityType<ShrineTileEntity.Tier> TierShrine;
	@ObjectHolder(ID_DungeonLauncher) public static BlockEntityType<DungeonLauncherTileEntity> DungeonLauncher;
	@ObjectHolder(ID_CursedGlass) public static BlockEntityType<CursedGlassTileEntity> CursedGlass;
	@ObjectHolder(ID_DungeonKeyChest) public static BlockEntityType<DungeonKeyChestTileEntity> DungeonKeyChest;
	@ObjectHolder(ID_DungeonDoor) public static BlockEntityType<DungeonDoorTileEntity> DungeonDoor;
	@ObjectHolder(ID_PushBlock) public static BlockEntityType<PushBlockTileEntity> PushBlock;
	@ObjectHolder(ID_BreakContainer) public static BlockEntityType<BreakContainerTileEntity> BreakContainer;
	@ObjectHolder(ID_SummonGhostBlock) public static BlockEntityType<SummonGhostBlockEntity> SummonGhostBlock;
	@ObjectHolder(ID_LaserBlock) public static BlockEntityType<LaserBlockEntity> Laser;
	@ObjectHolder(ID_ElementalCrystal) public static BlockEntityType<ElementalCrystalBlockEntity> ElementalCrystal;
	@ObjectHolder(ID_LaserTrigger) public static BlockEntityType<LaserTriggerBlockEntity> LaserTrigger;
	
	private static void register(IForgeRegistry<BlockEntityType<?>> registry, BlockEntityType<?> type, String ID) {
		registry.register(type.setRegistryName(ID));
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
		final IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
		
		register(registry, BlockEntityType.Builder.of(SpellTableTileEntity::new, NostrumBlocks.spellTable).build(null), ID_SpellTable);
		register(registry, BlockEntityType.Builder.of(SingleSpawnerTileEntity::new, NostrumBlocks.singleSpawner).build(null), ID_SingleSpawner);
		register(registry, BlockEntityType.Builder.of(MatchSpawnerTileEntity::new, NostrumBlocks.matchSpawner).build(null), ID_MatchSpawner);
		register(registry, BlockEntityType.Builder.of(TriggeredMatchSpawnerTileEntity::new, NostrumBlocks.triggeredMatchSpawner).build(null), ID_TriggeredMatchSpawner);
		register(registry, BlockEntityType.Builder.of(AltarTileEntity::new, NostrumBlocks.altar).build(null), ID_Altar);
		register(registry, BlockEntityType.Builder.of(CandleTileEntity::new, NostrumBlocks.candle).build(null), ID_Candle);
		register(registry, BlockEntityType.Builder.of(ObeliskTileEntity::new, NostrumBlocks.obelisk).build(null), ID_NostrumObelisk);
		register(registry, BlockEntityType.Builder.of(ObeliskPortalTileEntity::new, NostrumBlocks.obeliskPortal).build(null), ID_ObeliskPortal);
		register(registry, BlockEntityType.Builder.of(ModificationTableTileEntity::new, NostrumBlocks.modificationTable).build(null), ID_ModificationTable);
		register(registry, BlockEntityType.Builder.of(LoreTableTileEntity::new, NostrumBlocks.loreTable).build(null), ID_LoreTable);
		register(registry, BlockEntityType.Builder.of(SorceryPortalTileEntity::new, NostrumBlocks.sorceryPortal).build(null), ID_SorceryPortal);
		register(registry, BlockEntityType.Builder.of(TeleportationPortalTileEntity::new, NostrumBlocks.teleportationPortal).build(null), ID_TeleportationPortal);
		register(registry, BlockEntityType.Builder.of(TemporaryPortalTileEntity::new, NostrumBlocks.temporaryTeleportationPortal).build(null), ID_TemporaryPortal);
		register(registry, BlockEntityType.Builder.of(ProgressionDoorTileEntity::new, NostrumBlocks.progressionDoor).build(null), ID_ProgressionDoor);
		register(registry, BlockEntityType.Builder.of(SwitchBlockTileEntity::new, NostrumBlocks.switchBlock).build(null), ID_SwitchBlock);
		register(registry, BlockEntityType.Builder.of(TeleportRuneTileEntity::new, NostrumBlocks.teleportRune).build(null), ID_TeleportRune);
		register(registry, BlockEntityType.Builder.of(PutterBlockTileEntity::new, NostrumBlocks.putterBlock).build(null), ID_PutterBlock);
		register(registry, BlockEntityType.Builder.of(ActiveHopperTileEntity::new, NostrumBlocks.activeHopper).build(null), ID_ActiveHopper);
		register(registry, BlockEntityType.Builder.of(ItemDuctTileEntity::new, NostrumBlocks.itemDuct).build(null), ID_ItemDuct);
		register(registry, BlockEntityType.Builder.of(ParadoxMirrorTileEntity::new, NostrumBlocks.paradoxMirror).build(null), ID_ParadoxMirror);
		register(registry, BlockEntityType.Builder.of(ManaArmorerTileEntity::new, NostrumBlocks.manaArmorerBlock).build(null), ID_ManaArmorer);
		register(registry, BlockEntityType.Builder.of(MimicBlockTileEntity::new, NostrumBlocks.mimicDoor, NostrumBlocks.mimicFacade, NostrumBlocks.mimicDoorUnbreakable, NostrumBlocks.mimicFacadeUnbreakable).build(null), ID_MimicBlock);
		register(registry, BlockEntityType.Builder.of(RuneShaperTileEntity::new, NostrumBlocks.runeShaper).build(null), ID_RuneShaper);
		register(registry, BlockEntityType.Builder.of(LockedChestTileEntity::new, NostrumBlocks.lockedChest).build(null), ID_LockedChest);
		register(registry, BlockEntityType.Builder.of(KeySwitchBlockTileEntity::new, NostrumBlocks.keySwitch).build(null), ID_KeySwitch);
		register(registry, BlockEntityType.Builder.of(TrialBlockTileEntity::new, NostrumBlocks.trialBlock).build(null), ID_TrialBlock);
		register(registry, BlockEntityType.Builder.of(TriggerRepeaterTileEntity::new, NostrumBlocks.triggerRepeater, NostrumBlocks.redstoneTrigger).build(null), ID_TriggerRepeater);
		register(registry, BlockEntityType.Builder.of(DelayLoadedMimicBlockTileEntity::new, NostrumBlocks.mimicDoor, NostrumBlocks.mimicFacade, NostrumBlocks.mimicDoorUnbreakable, NostrumBlocks.mimicFacadeUnbreakable).build(null), ID_DelayedMimicBlock);
		register(registry, BlockEntityType.Builder.of(BasicSpellTableTileEntity::new, NostrumBlocks.basicSpellTable).build(null), ID_BasicSpellTable);
		register(registry, BlockEntityType.Builder.of(AdvancedSpellTableTileEntity::new, NostrumBlocks.advancedSpellTable).build(null), ID_AdvancedSpellTable);
		register(registry, BlockEntityType.Builder.of(MysticSpellTableTileEntity::new, NostrumBlocks.mysticSpellTable).build(null), ID_MysticSpellTable);
		register(registry, BlockEntityType.Builder.of(RuneLibraryTileEntity::new, NostrumBlocks.runeLibrary).build(null), ID_RuneLibrary);
		register(registry, BlockEntityType.Builder.of(LockedDoorTileEntity::new, NostrumBlocks.lockedDoor).build(null), ID_LockedDoor);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Element::new, NostrumBlocks.elementShrineBlock).build(null), ID_ElementShrine);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Alteration::new, NostrumBlocks.alterationShrineBlock).build(null), ID_AlterationShrine);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Shape::new, NostrumBlocks.shapeShrineBlock).build(null), ID_ShapeShrine);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Tier::new, NostrumBlocks.tierShrineBlock).build(null), ID_TierShrine);
		register(registry, BlockEntityType.Builder.of(DungeonLauncherTileEntity::new, NostrumBlocks.dungeonLauncher).build(null), ID_DungeonLauncher);
		register(registry, BlockEntityType.Builder.of(CursedGlassTileEntity::new, NostrumBlocks.cursedGlass).build(null), ID_CursedGlass);
		register(registry, BlockEntityType.Builder.of(DungeonKeyChestTileEntity::new, NostrumBlocks.smallDungeonKeyChest, NostrumBlocks.largeDungeonKeyChest).build(null), ID_DungeonKeyChest);
		register(registry, BlockEntityType.Builder.of(DungeonDoorTileEntity::new, NostrumBlocks.smallDungeonDoor, NostrumBlocks.largeDungeonDoor).build(null), ID_DungeonDoor);
		register(registry, BlockEntityType.Builder.of(PushBlockTileEntity::new, NostrumBlocks.pushBlock, NostrumBlocks.pushPassthroughBlock).build(null), ID_PushBlock);
		register(registry, BlockEntityType.Builder.of(BreakContainerTileEntity::new, NostrumBlocks.breakContainerBlock).build(null), ID_BreakContainer);
		register(registry, BlockEntityType.Builder.of(SummonGhostBlockEntity::new, NostrumBlocks.summonGhostBlock).build(null), ID_SummonGhostBlock);
		register(registry, BlockEntityType.Builder.of(LaserBlockEntity::new, NostrumBlocks.laser).build(null), ID_LaserBlock);
		register(registry, BlockEntityType.Builder.of(ElementalCrystalBlockEntity::new, NostrumBlocks.elementalCrystal).build(null), ID_ElementalCrystal);
		register(registry, BlockEntityType.Builder.of(LaserTriggerBlockEntity::new, NostrumBlocks.laserTrigger).build(null), ID_LaserTrigger);
    }
}
