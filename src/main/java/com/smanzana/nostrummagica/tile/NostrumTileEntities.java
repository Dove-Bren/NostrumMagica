package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;

import net.minecraft.tileentity.TileEntityType;
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
	private static final String ID_SymbolTileEntity = "nostrum_symbol_te";
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
	

	@ObjectHolder(ID_SpellTableEntity) public static TileEntityType<SpellTableEntity> SpellTableEntityType;
	@ObjectHolder(ID_SingleSpawnerTileEntity) public static TileEntityType<SingleSpawnerTileEntity> SingleSpawnerTileEntityType;
	@ObjectHolder(ID_MatchSpawnerTileEntity) public static TileEntityType<MatchSpawnerTileEntity> MatchSpawnerTileEntityType;
	@ObjectHolder(ID_TriggeredMatchSpawnerTileEntity) public static TileEntityType<TriggeredMatchSpawnerTileEntity> TriggeredMatchSpawnerTileEntityType;
	@ObjectHolder(ID_SymbolTileEntity) public static TileEntityType<SymbolTileEntity> SymbolTileEntityType;
	@ObjectHolder(ID_AltarTileEntity) public static TileEntityType<AltarTileEntity> AltarTileEntityType;
	@ObjectHolder(ID_CandleTileEntity) public static TileEntityType<CandleTileEntity> CandleTileEntityType;
	@ObjectHolder(ID_NostrumObeliskEntity) public static TileEntityType<NostrumObeliskEntity> NostrumObeliskEntityType;
	@ObjectHolder(ID_ObeliskPortalTileEntity) public static TileEntityType<ObeliskPortalTileEntity> ObeliskPortalTileEntityType;
	@ObjectHolder(ID_ModificationTableEntity) public static TileEntityType<ModificationTableEntity> ModificationTableEntityType;
	@ObjectHolder(ID_LoreTableEntity) public static TileEntityType<LoreTableEntity> LoreTableEntityType;
	@ObjectHolder(ID_SorceryPortalTileEntity) public static TileEntityType<SorceryPortalTileEntity> SorceryPortalTileEntityType;
	@ObjectHolder(ID_TeleportationPortalTileEntity) public static TileEntityType<TeleportationPortalTileEntity> TeleportationPortalTileEntityType;
	@ObjectHolder(ID_TemporaryPortalTileEntity) public static TileEntityType<TemporaryPortalTileEntity> TemporaryPortalTileEntityType;
	@ObjectHolder(ID_ProgressionDoorTileEntity) public static TileEntityType<ProgressionDoorTileEntity> ProgressionDoorTileEntityType;
	@ObjectHolder(ID_SwitchBlockTileEntity) public static TileEntityType<SwitchBlockTileEntity> SwitchBlockTileEntityType;
	@ObjectHolder(ID_TeleportRuneTileEntity) public static TileEntityType<TeleportRuneTileEntity> TeleportRuneTileEntityType;
	@ObjectHolder(ID_PutterBlockTileEntity) public static TileEntityType<PutterBlockTileEntity> PutterBlockTileEntityType;
	@ObjectHolder(ID_ActiveHopperTileEntity) public static TileEntityType<ActiveHopperTileEntity> ActiveHopperTileEntityType;
	@ObjectHolder(ID_ItemDuctTileEntity) public static TileEntityType<ItemDuctTileEntity> ItemDuctTileEntityType;
	@ObjectHolder(ID_ParadoxMirrorTileEntity) public static TileEntityType<ParadoxMirrorTileEntity> ParadoxMirrorTileEntityType;
	@ObjectHolder(ID_ManaArmorerTileEntity) public static TileEntityType<ManaArmorerTileEntity> ManaArmorerTileEntityType;
	@ObjectHolder(ID_MimicBlockTileEntity) public static TileEntityType<MimicBlockTileEntity> MimicBlockTileEntityType;
	@ObjectHolder(ID_RuneShaper) public static TileEntityType<RuneShaperEntity> RuneShaperEntityType;
	@ObjectHolder(ID_LockedChest) public static TileEntityType<LockedChestEntity> LockedChestEntityType;
	@ObjectHolder(ID_KeySwitch) public static TileEntityType<KeySwitchBlockTileEntity> KeySwitchTileEntityType;
	@ObjectHolder(ID_TrialBlockTileEntity) public static TileEntityType<TrialBlockTileEntity> TrialBlockEntityType;
	@ObjectHolder(ID_TriggerRepeaterTileEntity) public static TileEntityType<TriggerRepeaterEntity> TriggerRepeaterTileEntityType;
	@ObjectHolder(ID_DelayedMimicBlockTileEntity) public static TileEntityType<DelayLoadedMimicBlockTileEntity> DelayedMimicBlockTileEntityType;
	@ObjectHolder(ID_BasicSpellTableTileEntity) public static TileEntityType<BasicSpellTableEntity> BasicSpellTableType;
	@ObjectHolder(ID_AdvancedSpellTableTileEntity) public static TileEntityType<AdvancedSpellTableEntity> AdvancedSpellTableType;
	@ObjectHolder(ID_MysticSpellTableTileEntity) public static TileEntityType<MysticSpellTableEntity> MysticSpellTableType;
	@ObjectHolder(ID_RuneLibraryTileEntity) public static TileEntityType<RuneLibraryTileEntity> RuneLibraryType;
	
	private static void register(IForgeRegistry<TileEntityType<?>> registry, TileEntityType<?> type, String ID) {
		registry.register(type.setRegistryName(ID));
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		final IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
		
		register(registry, TileEntityType.Builder.create(SpellTableEntity::new, NostrumBlocks.spellTable).build(null), ID_SpellTableEntity);
		register(registry, TileEntityType.Builder.create(SingleSpawnerTileEntity::new, NostrumBlocks.singleSpawner).build(null), ID_SingleSpawnerTileEntity);
		register(registry, TileEntityType.Builder.create(MatchSpawnerTileEntity::new, NostrumBlocks.matchSpawner).build(null), ID_MatchSpawnerTileEntity);
		register(registry, TileEntityType.Builder.create(TriggeredMatchSpawnerTileEntity::new, NostrumBlocks.triggeredMatchSpawner).build(null), ID_TriggeredMatchSpawnerTileEntity);
		register(registry, TileEntityType.Builder.create(SymbolTileEntity::new, NostrumBlocks.symbolBlock, NostrumBlocks.shrineBlock).build(null), ID_SymbolTileEntity);
		register(registry, TileEntityType.Builder.create(AltarTileEntity::new, NostrumBlocks.altar).build(null), ID_AltarTileEntity);
		register(registry, TileEntityType.Builder.create(CandleTileEntity::new, NostrumBlocks.candle).build(null), ID_CandleTileEntity);
		register(registry, TileEntityType.Builder.create(NostrumObeliskEntity::new, NostrumBlocks.obelisk).build(null), ID_NostrumObeliskEntity);
		register(registry, TileEntityType.Builder.create(ObeliskPortalTileEntity::new, NostrumBlocks.obeliskPortal).build(null), ID_ObeliskPortalTileEntity);
		register(registry, TileEntityType.Builder.create(ModificationTableEntity::new, NostrumBlocks.modificationTable).build(null), ID_ModificationTableEntity);
		register(registry, TileEntityType.Builder.create(LoreTableEntity::new, NostrumBlocks.loreTable).build(null), ID_LoreTableEntity);
		register(registry, TileEntityType.Builder.create(SorceryPortalTileEntity::new, NostrumBlocks.sorceryPortal).build(null), ID_SorceryPortalTileEntity);
		register(registry, TileEntityType.Builder.create(TeleportationPortalTileEntity::new, NostrumBlocks.teleportationPortal).build(null), ID_TeleportationPortalTileEntity);
		register(registry, TileEntityType.Builder.create(TemporaryPortalTileEntity::new, NostrumBlocks.temporaryTeleportationPortal).build(null), ID_TemporaryPortalTileEntity);
		register(registry, TileEntityType.Builder.create(ProgressionDoorTileEntity::new, NostrumBlocks.progressionDoor).build(null), ID_ProgressionDoorTileEntity);
		register(registry, TileEntityType.Builder.create(SwitchBlockTileEntity::new, NostrumBlocks.switchBlock).build(null), ID_SwitchBlockTileEntity);
		register(registry, TileEntityType.Builder.create(TeleportRuneTileEntity::new, NostrumBlocks.teleportRune).build(null), ID_TeleportRuneTileEntity);
		register(registry, TileEntityType.Builder.create(PutterBlockTileEntity::new, NostrumBlocks.putterBlock).build(null), ID_PutterBlockTileEntity);
		register(registry, TileEntityType.Builder.create(ActiveHopperTileEntity::new, NostrumBlocks.activeHopper).build(null), ID_ActiveHopperTileEntity);
		register(registry, TileEntityType.Builder.create(ItemDuctTileEntity::new, NostrumBlocks.itemDuct).build(null), ID_ItemDuctTileEntity);
		register(registry, TileEntityType.Builder.create(ParadoxMirrorTileEntity::new, NostrumBlocks.paradoxMirror).build(null), ID_ParadoxMirrorTileEntity);
		register(registry, TileEntityType.Builder.create(ManaArmorerTileEntity::new, NostrumBlocks.manaArmorerBlock).build(null), ID_ManaArmorerTileEntity);
		register(registry, TileEntityType.Builder.create(MimicBlockTileEntity::new, NostrumBlocks.mimicDoor, NostrumBlocks.mimicFacade, NostrumBlocks.mimicDoorUnbreakable, NostrumBlocks.mimicFacadeUnbreakable).build(null), ID_MimicBlockTileEntity);
		register(registry, TileEntityType.Builder.create(RuneShaperEntity::new, NostrumBlocks.runeShaper).build(null), ID_RuneShaper);
		register(registry, TileEntityType.Builder.create(LockedChestEntity::new, NostrumBlocks.lockedChest).build(null), ID_LockedChest);
		register(registry, TileEntityType.Builder.create(KeySwitchBlockTileEntity::new, NostrumBlocks.keySwitch).build(null), ID_KeySwitch);
		register(registry, TileEntityType.Builder.create(TrialBlockTileEntity::new, NostrumBlocks.trialBlock).build(null), ID_TrialBlockTileEntity);
		register(registry, TileEntityType.Builder.create(TriggerRepeaterEntity::new, NostrumBlocks.triggerRepeater).build(null), ID_TriggerRepeaterTileEntity);
		register(registry, TileEntityType.Builder.create(DelayLoadedMimicBlockTileEntity::new, NostrumBlocks.mimicDoor, NostrumBlocks.mimicFacade, NostrumBlocks.mimicDoorUnbreakable, NostrumBlocks.mimicFacadeUnbreakable).build(null), ID_DelayedMimicBlockTileEntity);
		register(registry, TileEntityType.Builder.create(BasicSpellTableEntity::new, NostrumBlocks.basicSpellTable).build(null), ID_BasicSpellTableTileEntity);
		register(registry, TileEntityType.Builder.create(AdvancedSpellTableEntity::new, NostrumBlocks.advancedSpellTable).build(null), ID_AdvancedSpellTableTileEntity);
		register(registry, TileEntityType.Builder.create(MysticSpellTableEntity::new, NostrumBlocks.mysticSpellTable).build(null), ID_MysticSpellTableTileEntity);
		register(registry, TileEntityType.Builder.create(RuneLibraryTileEntity::new, NostrumBlocks.runeLibrary).build(null), ID_RuneLibraryTileEntity);
		
    }
}
