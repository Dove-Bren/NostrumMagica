package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class InfoScreenTab {
	private static InfoScreenTab RITUALS;
	private static InfoScreenTab INFO_REAGENTS;
	private static InfoScreenTab INFO_TOMES;
	private static InfoScreenTab INFO_SPELLS;
	private static InfoScreenTab INFO_BLOCKS;
	private static InfoScreenTab INFO_ITEMS;
	private static InfoScreenTab INFO_ENTITY;
	
	public static void init() {
		if (INFO_ITEMS != null)
			return;
		
		INFO_REAGENTS = new InfoScreenTab(InfoScreenTabs.INFO_REAGENTS,
				ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1)) {

			private List<ILoreTagged> getAvailable(INostrumMagic attr) {
				List<ILoreTagged> list = new LinkedList<>();
				for (ILoreTagged tag : attr.getAllLore()) {
					if (tag.getTab() == this.tab && attr.hasLore(tag))
						list.add(tag);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				return (!lore.isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (ILoreTagged tag : lore) {
					buttons.add(new LoreInfoButton(offset++, tag));
				}
				return buttons;
			}
			
		};
		
		INFO_BLOCKS = new InfoScreenTab(InfoScreenTabs.INFO_BLOCKS,
				new ItemStack(Item.getItemFromBlock(Blocks.GRASS))) {

			private List<ILoreTagged> getAvailable(INostrumMagic attr) {
				List<ILoreTagged> list = new LinkedList<>();
				for (ILoreTagged tag : attr.getAllLore()) {
					if (tag.getTab() == this.tab && attr.hasLore(tag))
						list.add(tag);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				return (!lore.isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (ILoreTagged tag : lore) {
					buttons.add(new LoreInfoButton(offset++, tag));
				}
				return buttons;
			}
			
		};
		
		INFO_TOMES = new InfoScreenTab(InfoScreenTabs.INFO_TOMES,
				new ItemStack(SpellTome.instance())) {

			private List<ILoreTagged> getAvailable(INostrumMagic attr) {
				List<ILoreTagged> list = new LinkedList<>();
				for (ILoreTagged tag : attr.getAllLore()) {
					if (tag.getTab() == this.tab && attr.hasLore(tag))
						list.add(tag);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				return (!lore.isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (ILoreTagged tag : lore) {
					buttons.add(new LoreInfoButton(offset++, tag));
				}
				return buttons;
			}
			
		};
		
		INFO_SPELLS = new InfoScreenTab(InfoScreenTabs.INFO_SPELLS,
				SpellRune.getRune(EMagicElement.FIRE, 1)) {

			private List<ILoreTagged> getAvailable(INostrumMagic attr) {
				List<ILoreTagged> list = new LinkedList<>();
				for (ILoreTagged tag : attr.getAllLore()) {
					if (tag.getTab() == this.tab && attr.hasLore(tag))
						list.add(tag);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				return (!lore.isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (ILoreTagged tag : lore) {
					buttons.add(new LoreInfoButton(offset++, tag));
				}
				return buttons;
			}
			
		};
		
		INFO_ENTITY = new InfoScreenTab(InfoScreenTabs.INFO_ENTITY,
				new ItemStack(Items.SKULL)) {

			private List<ILoreTagged> getAvailable(INostrumMagic attr) {
				List<ILoreTagged> list = new LinkedList<>();
				for (ILoreTagged tag : attr.getAllLore()) {
					if (tag.getTab() == this.tab && attr.hasLore(tag))
						list.add(tag);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				return (!lore.isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (ILoreTagged tag : lore) {
					buttons.add(new LoreInfoButton(offset++, tag));
				}
				return buttons;
			}
			
		};
		
		INFO_ITEMS = new InfoScreenTab(InfoScreenTabs.INFO_ITEMS,
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)) {

			private List<ILoreTagged> getAvailable(INostrumMagic attr) {
				List<ILoreTagged> list = new LinkedList<>();
				for (ILoreTagged tag : attr.getAllLore()) {
					if (tag.getTab() == this.tab && attr.hasLore(tag))
						list.add(tag);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				return (!lore.isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<ILoreTagged> lore = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (ILoreTagged tag : lore) {
					buttons.add(new LoreInfoButton(offset++, tag));
				}
				return buttons;
			}
			
		};
		
		RITUALS = new InfoScreenTab(InfoScreenTabs.RITUALS,
				new ItemStack(Candle.instance())) {

			private List<RitualRecipe> getAvailable(INostrumMagic attr) {
				List<RitualRecipe> list = new LinkedList<>();
				for (RitualRecipe ritual : RitualRegistry.instance().getRegisteredRituals()) {
					if (ritual.getRequirement() == null || ritual.getRequirement().matches(Minecraft.getMinecraft().thePlayer, attr))
						list.add(ritual);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				return (!getAvailable(attr).isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<RitualRecipe> rituals = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (RitualRecipe tag : rituals) {
					buttons.add(new RitualInfoButton(offset++, tag));
				}
				return buttons;
			}
			
		};
	}
	
	public static InfoScreenTab get(InfoScreenTabs tab) {
		InfoScreenTab ret = null;
		switch (tab) {
		case INFO_BLOCKS:
			ret = INFO_BLOCKS;
			break;
		case INFO_REAGENTS:
			ret = INFO_REAGENTS;
			break;
		case INFO_SPELLS:
			ret = INFO_SPELLS;
			break;
		case INFO_TOMES:
			ret = INFO_TOMES;
			break;
		case RITUALS:
			ret = RITUALS;
			break;
		case INFO_ENTITY:
			ret = INFO_ENTITY;
			break;
		case INFO_ITEMS:
			ret = INFO_ITEMS;
			break;
		}
		
		if (ret == null) // not default so we get warning if not all are in switch!
			NostrumMagica.logger.warn("Missing switch statement for tab type " + tab.name());
		
		return ret;
	}
	
	protected InfoScreenTabs tab;
	private ItemStack icon;
	private InfoScreenTab(InfoScreenTabs tab, ItemStack icon) {
		this.tab = tab;
		this.icon = icon;
	}
	
	public abstract boolean isVisible(INostrumMagic attr);
	
	public abstract List<InfoButton> getButtons(int IDOffset, INostrumMagic attr);
	
	public ItemStack getIcon() {
		return this.icon;
	}
}
