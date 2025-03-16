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
public class NostrumTileEntities {
	
	private static final String ID_SpellTableEntity = "spell_table";
	private static final String ID_SingleSpawnerTileEntity = "nostrum_mob_spawner_te";
	private static final String ID_MatchSpawnerTileEntity = "nostrum_mob_spawner_trigger_te";
	private static final String ID_TriggeredMatchSpawnerTileEntity = "triggered_match_spawner_te";
	private static final String ID_AltarTileEntity = "nostrum_altar_te";
	private static final String ID_CandleTileEntity = "nostrum_candle_te";
	private static final String ID_NostrumObeliskEntity = "nostrum_obelisk";
	private static final String ID_ObeliskPortalTileEntity = "obelisk_portal";
	private static final String ID_ModificationTableEntity = "modification_table";
	private static final String ID_LoreTableEntity = "lore_table";
	private static final String ID_SorceryPortalTileEntity = "sorcery_portal";
	private static final String ID_TeleportationPortalTileEntity = "teleportation_portal";
	private static final String ID_TemporaryPortalTileEntity = "limited_teleportation_portal";
	private static final String ID_ProgressionDoorTileEntity = "progression_door";
	private static final String ID_SwitchBlockTileEntity = "switch_block_tile_entity";
	private static final String ID_TeleportRuneTileEntity = "teleport_rune";
	private static final String ID_PutterBlockTileEntity = "putter_entity";
	private static final String ID_ActiveHopperTileEntity = "active_hopper_te";
	private static final String ID_ItemDuctTileEntity = "item_duct_te";
	private static final String ID_ParadoxMirrorTileEntity = "paradox_mirror_te";
	private static final String ID_ManaArmorerTileEntity = "mana_armorer";
	private static final String ID_MimicBlockTileEntity = "mimic_block_te";
	private static final String ID_RuneShaper = "rune_shaper_te";
	private static final String ID_LockedChest = "locked_chest_te";
	private static final String ID_KeySwitch = "key_switch_te";
	private static final String ID_TrialBlockTileEntity = "trial_block_te";
	private static final String ID_TriggerRepeaterTileEntity = "trigger_repeater_te";
	private static final String ID_DelayedMimicBlockTileEntity = "delayed_mimic_block_te";
	private static final String ID_BasicSpellTableTileEntity = "spelltable_basic_te";
	private static final String ID_AdvancedSpellTableTileEntity = "spelltable_advanced_te";
	private static final String ID_MysticSpellTableTileEntity = "spelltable_mystic_te";
	private static final String ID_RuneLibraryTileEntity = "rune_library_te";
	private static final String ID_LockedDoor = "locked_door";
	private static final String ID_ElementShrine = "element_shrine_te";
	private static final String ID_AlterationShrine = "alteration_shrine_te";
	private static final String ID_ShapeShrine = "shape_shrine_te";
	private static final String ID_TierShrine = "tier_shrine_te";
	private static final String ID_DungeonLauncher = "launcher_te";
	private static final String ID_CursedGlass = "cursed_glass_te";
	private static final String ID_DungeonKeyChest = "dungeon_key_chest_te";
	private static final String ID_DungeonDoor = "dungeon_door_te";
	

	@ObjectHolder(ID_SpellTableEntity) public static BlockEntityType<SpellTableTileEntity> SpellTableEntityType;
	@ObjectHolder(ID_SingleSpawnerTileEntity) public static BlockEntityType<SingleSpawnerTileEntity> SingleSpawnerTileEntityType;
	@ObjectHolder(ID_MatchSpawnerTileEntity) public static BlockEntityType<MatchSpawnerTileEntity> MatchSpawnerTileEntityType;
	@ObjectHolder(ID_TriggeredMatchSpawnerTileEntity) public static BlockEntityType<TriggeredMatchSpawnerTileEntity> TriggeredMatchSpawnerTileEntityType;
	@ObjectHolder(ID_AltarTileEntity) public static BlockEntityType<AltarTileEntity> AltarTileEntityType;
	@ObjectHolder(ID_CandleTileEntity) public static BlockEntityType<CandleTileEntity> CandleTileEntityType;
	@ObjectHolder(ID_NostrumObeliskEntity) public static BlockEntityType<ObeliskTileEntity> NostrumObeliskEntityType;
	@ObjectHolder(ID_ObeliskPortalTileEntity) public static BlockEntityType<ObeliskPortalTileEntity> ObeliskPortalTileEntityType;
	@ObjectHolder(ID_ModificationTableEntity) public static BlockEntityType<ModificationTableTileEntity> ModificationTableEntityType;
	@ObjectHolder(ID_LoreTableEntity) public static BlockEntityType<LoreTableTileEntity> LoreTableEntityType;
	@ObjectHolder(ID_SorceryPortalTileEntity) public static BlockEntityType<SorceryPortalTileEntity> SorceryPortalTileEntityType;
	@ObjectHolder(ID_TeleportationPortalTileEntity) public static BlockEntityType<TeleportationPortalTileEntity> TeleportationPortalTileEntityType;
	@ObjectHolder(ID_TemporaryPortalTileEntity) public static BlockEntityType<TemporaryPortalTileEntity> TemporaryPortalTileEntityType;
	@ObjectHolder(ID_ProgressionDoorTileEntity) public static BlockEntityType<ProgressionDoorTileEntity> ProgressionDoorTileEntityType;
	@ObjectHolder(ID_SwitchBlockTileEntity) public static BlockEntityType<SwitchBlockTileEntity> SwitchBlockTileEntityType;
	@ObjectHolder(ID_TeleportRuneTileEntity) public static BlockEntityType<TeleportRuneTileEntity> TeleportRuneTileEntityType;
	@ObjectHolder(ID_PutterBlockTileEntity) public static BlockEntityType<PutterBlockTileEntity> PutterBlockTileEntityType;
	@ObjectHolder(ID_ActiveHopperTileEntity) public static BlockEntityType<ActiveHopperTileEntity> ActiveHopperTileEntityType;
	@ObjectHolder(ID_ItemDuctTileEntity) public static BlockEntityType<ItemDuctTileEntity> ItemDuctTileEntityType;
	@ObjectHolder(ID_ParadoxMirrorTileEntity) public static BlockEntityType<ParadoxMirrorTileEntity> ParadoxMirrorTileEntityType;
	@ObjectHolder(ID_ManaArmorerTileEntity) public static BlockEntityType<ManaArmorerTileEntity> ManaArmorerTileEntityType;
	@ObjectHolder(ID_MimicBlockTileEntity) public static BlockEntityType<MimicBlockTileEntity> MimicBlockTileEntityType;
	@ObjectHolder(ID_RuneShaper) public static BlockEntityType<RuneShaperTileEntity> RuneShaperEntityType;
	@ObjectHolder(ID_LockedChest) public static BlockEntityType<LockedChestTileEntity> LockedChestEntityType;
	@ObjectHolder(ID_KeySwitch) public static BlockEntityType<KeySwitchBlockTileEntity> KeySwitchTileEntityType;
	@ObjectHolder(ID_TrialBlockTileEntity) public static BlockEntityType<TrialBlockTileEntity> TrialBlockEntityType;
	@ObjectHolder(ID_TriggerRepeaterTileEntity) public static BlockEntityType<TriggerRepeaterTileEntity> TriggerRepeaterTileEntityType;
	@ObjectHolder(ID_DelayedMimicBlockTileEntity) public static BlockEntityType<DelayLoadedMimicBlockTileEntity> DelayedMimicBlockTileEntityType;
	@ObjectHolder(ID_BasicSpellTableTileEntity) public static BlockEntityType<BasicSpellTableTileEntity> BasicSpellTableType;
	@ObjectHolder(ID_AdvancedSpellTableTileEntity) public static BlockEntityType<AdvancedSpellTableTileEntity> AdvancedSpellTableType;
	@ObjectHolder(ID_MysticSpellTableTileEntity) public static BlockEntityType<MysticSpellTableTileEntity> MysticSpellTableType;
	@ObjectHolder(ID_RuneLibraryTileEntity) public static BlockEntityType<RuneLibraryTileEntity> RuneLibraryType;
	@ObjectHolder(ID_LockedDoor) public static BlockEntityType<LockedDoorTileEntity> LockedDoorType;
	@ObjectHolder(ID_ElementShrine) public static BlockEntityType<ShrineTileEntity.Element> ElementShrineTileType;
	@ObjectHolder(ID_AlterationShrine) public static BlockEntityType<ShrineTileEntity.Alteration> AlterationShrineTileType;
	@ObjectHolder(ID_ShapeShrine) public static BlockEntityType<ShrineTileEntity.Shape> ShapeShrineTileType;
	@ObjectHolder(ID_TierShrine) public static BlockEntityType<ShrineTileEntity.Tier> TierShrineTileType;
	@ObjectHolder(ID_DungeonLauncher) public static BlockEntityType<DungeonLauncherTileEntity> DungeonLauncherTileType;
	@ObjectHolder(ID_CursedGlass) public static BlockEntityType<CursedGlassTileEntity> CursedGlassType;
	@ObjectHolder(ID_DungeonKeyChest) public static BlockEntityType<DungeonKeyChestTileEntity> DungeonKeyChestTileEntityType;
	@ObjectHolder(ID_DungeonDoor) public static BlockEntityType<DungeonDoorTileEntity> DungeonDoorTileEntityType;
	
	private static void register(IForgeRegistry<BlockEntityType<?>> registry, BlockEntityType<?> type, String ID) {
		registry.register(type.setRegistryName(ID));
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
		final IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
		
		register(registry, BlockEntityType.Builder.of(SpellTableTileEntity::new, NostrumBlocks.spellTable).build(null), ID_SpellTableEntity);
		register(registry, BlockEntityType.Builder.of(SingleSpawnerTileEntity::new, NostrumBlocks.singleSpawner).build(null), ID_SingleSpawnerTileEntity);
		register(registry, BlockEntityType.Builder.of(MatchSpawnerTileEntity::new, NostrumBlocks.matchSpawner).build(null), ID_MatchSpawnerTileEntity);
		register(registry, BlockEntityType.Builder.of(TriggeredMatchSpawnerTileEntity::new, NostrumBlocks.triggeredMatchSpawner).build(null), ID_TriggeredMatchSpawnerTileEntity);
		register(registry, BlockEntityType.Builder.of(AltarTileEntity::new, NostrumBlocks.altar).build(null), ID_AltarTileEntity);
		register(registry, BlockEntityType.Builder.of(CandleTileEntity::new, NostrumBlocks.candle).build(null), ID_CandleTileEntity);
		register(registry, BlockEntityType.Builder.of(ObeliskTileEntity::new, NostrumBlocks.obelisk).build(null), ID_NostrumObeliskEntity);
		register(registry, BlockEntityType.Builder.of(ObeliskPortalTileEntity::new, NostrumBlocks.obeliskPortal).build(null), ID_ObeliskPortalTileEntity);
		register(registry, BlockEntityType.Builder.of(ModificationTableTileEntity::new, NostrumBlocks.modificationTable).build(null), ID_ModificationTableEntity);
		register(registry, BlockEntityType.Builder.of(LoreTableTileEntity::new, NostrumBlocks.loreTable).build(null), ID_LoreTableEntity);
		register(registry, BlockEntityType.Builder.of(SorceryPortalTileEntity::new, NostrumBlocks.sorceryPortal).build(null), ID_SorceryPortalTileEntity);
		register(registry, BlockEntityType.Builder.of(TeleportationPortalTileEntity::new, NostrumBlocks.teleportationPortal).build(null), ID_TeleportationPortalTileEntity);
		register(registry, BlockEntityType.Builder.of(TemporaryPortalTileEntity::new, NostrumBlocks.temporaryTeleportationPortal).build(null), ID_TemporaryPortalTileEntity);
		register(registry, BlockEntityType.Builder.of(ProgressionDoorTileEntity::new, NostrumBlocks.progressionDoor).build(null), ID_ProgressionDoorTileEntity);
		register(registry, BlockEntityType.Builder.of(SwitchBlockTileEntity::new, NostrumBlocks.switchBlock).build(null), ID_SwitchBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(TeleportRuneTileEntity::new, NostrumBlocks.teleportRune).build(null), ID_TeleportRuneTileEntity);
		register(registry, BlockEntityType.Builder.of(PutterBlockTileEntity::new, NostrumBlocks.putterBlock).build(null), ID_PutterBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(ActiveHopperTileEntity::new, NostrumBlocks.activeHopper).build(null), ID_ActiveHopperTileEntity);
		register(registry, BlockEntityType.Builder.of(ItemDuctTileEntity::new, NostrumBlocks.itemDuct).build(null), ID_ItemDuctTileEntity);
		register(registry, BlockEntityType.Builder.of(ParadoxMirrorTileEntity::new, NostrumBlocks.paradoxMirror).build(null), ID_ParadoxMirrorTileEntity);
		register(registry, BlockEntityType.Builder.of(ManaArmorerTileEntity::new, NostrumBlocks.manaArmorerBlock).build(null), ID_ManaArmorerTileEntity);
		register(registry, BlockEntityType.Builder.of(MimicBlockTileEntity::new, NostrumBlocks.mimicDoor, NostrumBlocks.mimicFacade, NostrumBlocks.mimicDoorUnbreakable, NostrumBlocks.mimicFacadeUnbreakable).build(null), ID_MimicBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(RuneShaperTileEntity::new, NostrumBlocks.runeShaper).build(null), ID_RuneShaper);
		register(registry, BlockEntityType.Builder.of(LockedChestTileEntity::new, NostrumBlocks.lockedChest).build(null), ID_LockedChest);
		register(registry, BlockEntityType.Builder.of(KeySwitchBlockTileEntity::new, NostrumBlocks.keySwitch).build(null), ID_KeySwitch);
		register(registry, BlockEntityType.Builder.of(TrialBlockTileEntity::new, NostrumBlocks.trialBlock).build(null), ID_TrialBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(TriggerRepeaterTileEntity::new, NostrumBlocks.triggerRepeater, NostrumBlocks.redstoneTrigger).build(null), ID_TriggerRepeaterTileEntity);
		register(registry, BlockEntityType.Builder.of(DelayLoadedMimicBlockTileEntity::new, NostrumBlocks.mimicDoor, NostrumBlocks.mimicFacade, NostrumBlocks.mimicDoorUnbreakable, NostrumBlocks.mimicFacadeUnbreakable).build(null), ID_DelayedMimicBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(BasicSpellTableTileEntity::new, NostrumBlocks.basicSpellTable).build(null), ID_BasicSpellTableTileEntity);
		register(registry, BlockEntityType.Builder.of(AdvancedSpellTableTileEntity::new, NostrumBlocks.advancedSpellTable).build(null), ID_AdvancedSpellTableTileEntity);
		register(registry, BlockEntityType.Builder.of(MysticSpellTableTileEntity::new, NostrumBlocks.mysticSpellTable).build(null), ID_MysticSpellTableTileEntity);
		register(registry, BlockEntityType.Builder.of(RuneLibraryTileEntity::new, NostrumBlocks.runeLibrary).build(null), ID_RuneLibraryTileEntity);
		register(registry, BlockEntityType.Builder.of(LockedDoorTileEntity::new, NostrumBlocks.lockedDoor).build(null), ID_LockedDoor);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Element::new, NostrumBlocks.elementShrineBlock).build(null), ID_ElementShrine);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Alteration::new, NostrumBlocks.alterationShrineBlock).build(null), ID_AlterationShrine);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Shape::new, NostrumBlocks.shapeShrineBlock).build(null), ID_ShapeShrine);
		register(registry, BlockEntityType.Builder.of(ShrineTileEntity.Tier::new, NostrumBlocks.tierShrineBlock).build(null), ID_TierShrine);
		register(registry, BlockEntityType.Builder.of(DungeonLauncherTileEntity::new, NostrumBlocks.dungeonLauncher).build(null), ID_DungeonLauncher);
		register(registry, BlockEntityType.Builder.of(CursedGlassTileEntity::new, NostrumBlocks.cursedGlass).build(null), ID_CursedGlass);
		register(registry, BlockEntityType.Builder.of(DungeonKeyChestTileEntity::new, NostrumBlocks.smallDungeonKeyChest, NostrumBlocks.largeDungeonKeyChest).build(null), ID_DungeonKeyChest);
		register(registry, BlockEntityType.Builder.of(DungeonDoorTileEntity::new, NostrumBlocks.smallDungeonDoor, NostrumBlocks.largeDungeonDoor).build(null), ID_DungeonDoor);
    }
}
