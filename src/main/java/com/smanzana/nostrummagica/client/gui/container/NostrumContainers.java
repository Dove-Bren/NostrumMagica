package com.smanzana.nostrummagica.client.gui.container;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumMagica.MODID)
public class NostrumContainers {

	@ObjectHolder(ActiveHopperGui.ActiveHopperContainer.ID) public static ContainerType<ActiveHopperGui.ActiveHopperContainer> ActiveHopper;
	@ObjectHolder(LoreTableGui.LoreTableContainer.ID) public static ContainerType<LoreTableGui.LoreTableContainer> LoreTable;
	@ObjectHolder(ModificationTableGui.ModificationTableContainer.ID) public static ContainerType<ModificationTableGui.ModificationTableContainer> ModificationTable;
	@ObjectHolder(PutterBlockGui.PutterBlockContainer.ID) public static ContainerType<PutterBlockGui.PutterBlockContainer> Putter;
	@ObjectHolder(ReagentBagGui.BagContainer.ID) public static ContainerType<ReagentBagGui.BagContainer> ReagentBag;
	@ObjectHolder(RuneBagGui.BagContainer.ID) public static ContainerType<RuneBagGui.BagContainer> RuneBag;
	@ObjectHolder(MasterSpellCreationGui.SpellCreationContainer.ID) public static ContainerType<MasterSpellCreationGui.SpellCreationContainer> SpellCreationMaster;
	@ObjectHolder(BasicSpellCraftGui.BasicSpellCraftContainer.ID) public static ContainerType<BasicSpellCraftGui.BasicSpellCraftContainer> SpellCreationBasic;
	@ObjectHolder(RuneShaperGui.RuneShaperContainer.ID) public static ContainerType<RuneShaperGui.RuneShaperContainer> RuneShaper;
	@ObjectHolder(RedwoodSpellCraftGui.RedwoodContainer.ID) public static ContainerType<RedwoodSpellCraftGui.RedwoodContainer> SpellCreationRedwood;
	@ObjectHolder(MysticSpellCraftGui.MysticContainer.ID) public static ContainerType<MysticSpellCraftGui.MysticContainer> SpellCreationMystic;
	@ObjectHolder(RuneLibraryGui.RuneLibraryContainer.ID) public static ContainerType<RuneLibraryGui.RuneLibraryContainer> RuneLibrary;
	@ObjectHolder(LauncherBlockGui.LauncherBlockContainer.ID) public static ContainerType<LauncherBlockGui.LauncherBlockContainer> Launcher;
	
	@SubscribeEvent
	public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
		final IForgeRegistry<ContainerType<?>> registry = event.getRegistry();
		
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
	}
}
