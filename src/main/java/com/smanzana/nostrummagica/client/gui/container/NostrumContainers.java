package com.smanzana.nostrummagica.client.gui.container;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumContainers {

	@ObjectHolder(ActiveHopperGui.ActiveHopperContainer.ID) public static MenuType<ActiveHopperGui.ActiveHopperContainer> ActiveHopper;
	@ObjectHolder(LoreTableGui.LoreTableContainer.ID) public static MenuType<LoreTableGui.LoreTableContainer> LoreTable;
	@ObjectHolder(ModificationTableGui.ModificationTableContainer.ID) public static MenuType<ModificationTableGui.ModificationTableContainer> ModificationTable;
	@ObjectHolder(PutterBlockGui.PutterBlockContainer.ID) public static MenuType<PutterBlockGui.PutterBlockContainer> Putter;
	@ObjectHolder(ReagentBagGui.BagContainer.ID) public static MenuType<ReagentBagGui.BagContainer> ReagentBag;
	@ObjectHolder(RuneBagGui.BagContainer.ID) public static MenuType<RuneBagGui.BagContainer> RuneBag;
	@ObjectHolder(MasterSpellCreationGui.SpellCreationContainer.ID) public static MenuType<MasterSpellCreationGui.SpellCreationContainer> SpellCreationMaster;
	@ObjectHolder(BasicSpellCraftGui.BasicSpellCraftContainer.ID) public static MenuType<BasicSpellCraftGui.BasicSpellCraftContainer> SpellCreationBasic;
	@ObjectHolder(RuneShaperGui.RuneShaperContainer.ID) public static MenuType<RuneShaperGui.RuneShaperContainer> RuneShaper;
	@ObjectHolder(RedwoodSpellCraftGui.RedwoodContainer.ID) public static MenuType<RedwoodSpellCraftGui.RedwoodContainer> SpellCreationRedwood;
	@ObjectHolder(MysticSpellCraftGui.MysticContainer.ID) public static MenuType<MysticSpellCraftGui.MysticContainer> SpellCreationMystic;
	@ObjectHolder(RuneLibraryGui.RuneLibraryContainer.ID) public static MenuType<RuneLibraryGui.RuneLibraryContainer> RuneLibrary;
	@ObjectHolder(LauncherBlockGui.LauncherBlockContainer.ID) public static MenuType<LauncherBlockGui.LauncherBlockContainer> Launcher;
	@ObjectHolder(SilverMirrorGui.MirrorContainer.ID) public static MenuType<SilverMirrorGui.MirrorContainer> SilverMirror;
	
	@SubscribeEvent
	public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event) {
		final IForgeRegistry<MenuType<?>> registry = event.getRegistry();
		
		registry.register(IForgeContainerType.create(ActiveHopperGui.ActiveHopperContainer::FromNetwork).setRegistryName(ActiveHopperGui.ActiveHopperContainer.ID));
		registry.register(IForgeContainerType.create(LoreTableGui.LoreTableContainer::FromNetwork).setRegistryName(LoreTableGui.LoreTableContainer.ID));
		registry.register(IForgeContainerType.create(ModificationTableGui.ModificationTableContainer::FromNetwork).setRegistryName(ModificationTableGui.ModificationTableContainer.ID));
		registry.register(IForgeContainerType.create(PutterBlockGui.PutterBlockContainer::FromNetwork).setRegistryName(PutterBlockGui.PutterBlockContainer.ID));
		registry.register(IForgeContainerType.create(ReagentBagGui.BagContainer::FromNetwork).setRegistryName(ReagentBagGui.BagContainer.ID));
		registry.register(IForgeContainerType.create(RuneBagGui.BagContainer::FromNetwork).setRegistryName(RuneBagGui.BagContainer.ID));
		registry.register(IForgeContainerType.create(MasterSpellCreationGui.SpellCreationContainer::FromNetwork).setRegistryName(MasterSpellCreationGui.SpellCreationContainer.ID));
		registry.register(IForgeContainerType.create(BasicSpellCraftGui.BasicSpellCraftContainer::FromNetwork).setRegistryName(BasicSpellCraftGui.BasicSpellCraftContainer.ID));
		registry.register(IForgeContainerType.create(RuneShaperGui.RuneShaperContainer::FromNetwork).setRegistryName(RuneShaperGui.RuneShaperContainer.ID));
		registry.register(IForgeContainerType.create(RedwoodSpellCraftGui.RedwoodContainer::FromNetwork).setRegistryName(RedwoodSpellCraftGui.RedwoodContainer.ID));
		registry.register(IForgeContainerType.create(MysticSpellCraftGui.MysticContainer::FromNetwork).setRegistryName(MysticSpellCraftGui.MysticContainer.ID));
		registry.register(IForgeContainerType.create(RuneLibraryGui.RuneLibraryContainer::FromNetwork).setRegistryName(RuneLibraryGui.RuneLibraryContainer.ID));
		registry.register(IForgeContainerType.create(LauncherBlockGui.LauncherBlockContainer::FromNetwork).setRegistryName(LauncherBlockGui.LauncherBlockContainer.ID));
		registry.register(IForgeContainerType.create(SilverMirrorGui.MirrorContainer::FromNetwork).setRegistryName(SilverMirrorGui.MirrorContainer.ID));
	}
}
