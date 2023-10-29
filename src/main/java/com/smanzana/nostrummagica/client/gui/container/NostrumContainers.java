package com.smanzana.nostrummagica.client.gui.container;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetGUIContainer;
import com.smanzana.nostrummagica.entity.IEntityPet;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
	@ObjectHolder(SpellCreationGui.SpellCreationContainer.ID) public static ContainerType<SpellCreationGui.SpellCreationContainer> SpellCreation;
	@ObjectHolder(PetGUI.PetContainer.ID) public static ContainerType<PetGUI.PetContainer<?>> PetGui;
	
	
	
	@SubscribeEvent
	public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
		final IForgeRegistry<ContainerType<?>> registry = event.getRegistry();
		
		registry.register(IForgeContainerType.create(ActiveHopperGui.ActiveHopperContainer::FromNetwork).setRegistryName(ActiveHopperGui.ActiveHopperContainer.ID));
		registry.register(IForgeContainerType.create(LoreTableGui.LoreTableContainer::FromNetwork).setRegistryName(LoreTableGui.LoreTableContainer.ID));
		registry.register(IForgeContainerType.create(ModificationTableGui.ModificationTableContainer::FromNetwork).setRegistryName(ModificationTableGui.ModificationTableContainer.ID));
		registry.register(IForgeContainerType.create(PutterBlockGui.PutterBlockContainer::FromNetwork).setRegistryName(PutterBlockGui.PutterBlockContainer.ID));
		registry.register(IForgeContainerType.create(ReagentBagGui.BagContainer::FromNetwork).setRegistryName(ReagentBagGui.BagContainer.ID));
		registry.register(IForgeContainerType.create(RuneBagGui.BagContainer::FromNetwork).setRegistryName(RuneBagGui.BagContainer.ID));
		registry.register(IForgeContainerType.create(SpellCreationGui.SpellCreationContainer::FromNetwork).setRegistryName(SpellCreationGui.SpellCreationContainer.ID));
		registry.register(IForgeContainerType.create(PetGUI.PetContainer::FromNetwork).setRegistryName(PetGUI.PetContainer.ID));
		
		if (NostrumMagica.instance.aetheria.isEnabled()) {
			
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SubscribeEvent
	public static void registerContainerScreens(FMLClientSetupEvent event) {
		ScreenManager.registerFactory(ActiveHopper, ActiveHopperGui.ActiveHopperGuiContainer::new);
		ScreenManager.registerFactory(LoreTable, LoreTableGui.LoreTableGuiContainer::new);
		ScreenManager.registerFactory(ModificationTable, ModificationTableGui.ModificationGui::new);
		ScreenManager.registerFactory(Putter, PutterBlockGui.PutterBlockGuiContainer::new);
		ScreenManager.registerFactory(ReagentBag, ReagentBagGui.BagGui::new);
		ScreenManager.registerFactory(RuneBag, RuneBagGui.BagGui::new);
		ScreenManager.registerFactory(SpellCreation, SpellCreationGui.SpellGui::new);
		
		ScreenManager.registerFactory(PetGui, new PetGUIFactory());
	}
	
	// To get around bounds matching. D:
	protected static class PetGUIFactory<T extends IEntityPet> implements ScreenManager.IScreenFactory<PetGUI.PetContainer<T>, PetGUI.PetGUIContainer<T>> {

			@Override
			public PetGUIContainer<T> create(PetContainer<T> c, PlayerInventory p,
					ITextComponent n) {
				return new PetGUI.PetGUIContainer<T>(c, p, n);
			}
	}
}
