package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.Candle;
import com.smanzana.nostrummagica.blocks.ModificationTable;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed.TameRedDragonLore;
import com.smanzana.nostrummagica.items.AltarItem;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.DragonEgg;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.SelfTrigger;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class InfoScreenTab {
	private static InfoScreenTab PERSONAL;
	private static InfoScreenTab RITUALS;
	private static InfoScreenTab INFO_REAGENTS;
	private static InfoScreenTab INFO_TOMES;
	private static InfoScreenTab INFO_SPELLS;
	private static InfoScreenTab INFO_BLOCKS;
	private static InfoScreenTab INFO_ITEMS;
	private static InfoScreenTab INFO_ENTITY;
	private static InfoScreenTab INFO_GUIDES;
	private static InfoScreenTab INFO_TRIALS;
	private static InfoScreenTab INFO_DRAGONS;
	
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
					InfoButton button = new LoreInfoButton(offset++, tag);
					buttons.add(button);
					index(ILoreTagged.GetInfoKey(tag), button);
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
					InfoButton button = new LoreInfoButton(offset++, tag);
					buttons.add(button);
					index(ILoreTagged.GetInfoKey(tag), button);
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
					InfoButton button = new LoreInfoButton(offset++, tag);
					buttons.add(button);
					index(ILoreTagged.GetInfoKey(tag), button);
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
					InfoButton button = new LoreInfoButton(offset++, tag);
					buttons.add(button);
					index(ILoreTagged.GetInfoKey(tag), button);
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
					InfoButton button = new LoreInfoButton(offset++, tag);
					buttons.add(button);
					index(ILoreTagged.GetInfoKey(tag), button);
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
					InfoButton button = new LoreInfoButton(offset++, tag);
					buttons.add(button);
					index(ILoreTagged.GetInfoKey(tag), button);
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
				return (attr.isUnlocked() && !getAvailable(attr).isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<RitualRecipe> rituals = getAvailable(attr);
				List<InfoButton> buttons = new LinkedList<>();
				for (RitualRecipe tag : rituals) {
					RitualInfoButton button = new RitualInfoButton(offset++, tag);
					buttons.add(button);
					index(tag.getInfoScreenKey(), button);
				}
				return buttons;
			}
			
		};
		
		PERSONAL = new InfoScreenTab(InfoScreenTabs.PERSONAL,
				new ItemStack(Items.SKULL, 1, 3)) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return true;
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<InfoButton> buttons = new LinkedList<>();
				
				buttons.add(new SubscreenInfoButton(offset++, "discovery",
						new PersonalSubScreen.PersonalDiscoveryScreen(attr),
						new ItemStack(SpellTome.instance())));
				
				if (attr.isUnlocked()) {
					
					buttons.add(new SubscreenInfoButton(offset++, "stats",
							new PersonalSubScreen.PersonalStatsScreen(attr),
							new ItemStack(SpellTomePage.instance())));
					
					buttons.add(new SubscreenInfoButton(offset++, "growth",
							new PersonalSubScreen.PersonalGrowthScreen(attr),
							new ItemStack(Items.COMPASS)));
					
					buttons.add(new SubscreenInfoButton(offset++, "exploration",
							new PersonalSubScreen.PersonalExplorationScreen(attr),
							new ItemStack(Items.MAP)));
				}
				
				return buttons;
			}
			
		};
		
		INFO_GUIDES = new InfoScreenTab(InfoScreenTabs.INFO_GUIDES,
				new ItemStack(Items.BOOK)) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return attr.isUnlocked();
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<InfoButton> buttons = new LinkedList<>();
				
				buttons.add(new SubscreenInfoButton(offset++, "shrines",
						new PaginatedInfoSubScreen("shrines"),
						new ItemStack(MirrorItem.instance())));
				
				buttons.add(new SubscreenInfoButton(offset++, "spells",
						new PaginatedInfoSubScreen("spells"),
						new ItemStack(SpellScroll.instance())));
				
				buttons.add(new SubscreenInfoButton(offset++, "trigger",
						new PaginatedInfoSubScreen("trigger"),
						SpellRune.getRune(SelfTrigger.instance())));
				
				buttons.add(new SubscreenInfoButton(offset++, "shape",
						new PaginatedInfoSubScreen("shape"),
						SpellRune.getRune(SingleShape.instance())));
				
				buttons.add(new SubscreenInfoButton(offset++, "element",
						new PaginatedInfoSubScreen("element"),
						SpellRune.getRune(EMagicElement.FIRE, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "alteration",
						new PaginatedInfoSubScreen("alteration"),
						SpellRune.getRune(EAlteration.INFLICT)));
				
				buttons.add(new SubscreenInfoButton(offset++, "spellmaking",
						new PaginatedInfoSubScreen("spellmaking"),
						new ItemStack(BlankScroll.instance())));
				
				buttons.add(new SubscreenInfoButton(offset++, "spellbinding",
						new PaginatedInfoSubScreen("spellbinding"),
						new ItemStack(Items.WRITABLE_BOOK)));
				
				buttons.add(new SubscreenInfoButton(offset++, "levelup",
						new PaginatedInfoSubScreen("levelup"),
						new ItemStack(Items.SKULL, 1, 3)));
				
				buttons.add(new SubscreenInfoButton(offset++, "tomes",
						new PaginatedInfoSubScreen("tomes"),
						new ItemStack(SpellTome.instance(), 1, 4)));
				
				buttons.add(new SubscreenInfoButton(offset++, "rituals",
						new PaginatedInfoSubScreen("rituals"),
						new ItemStack(AltarItem.instance())));
				
				buttons.add(new SubscreenInfoButton(offset++, "modification",
						new PaginatedInfoSubScreen("modification"),
						new ItemStack(ModificationTable.instance())));
				
				buttons.add(new SubscreenInfoButton(offset++, "elementdamage",
						new PaginatedInfoSubScreen("elementdamage"),
						new ItemStack(Items.DIAMOND_SWORD)));
				
				buttons.add(new SubscreenInfoButton(offset++, "reagents",
						new PaginatedInfoSubScreen("reagents"),
						ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "obelisks",
						new PaginatedInfoSubScreen("obelisks"),
						new ItemStack(Items.ENDER_PEARL)));
					
				return buttons;
			}
			
		};
		
		INFO_TRIALS = new InfoScreenTab(InfoScreenTabs.INFO_TRIALS,
				new ItemStack(MasteryOrb.instance())) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return attr.isUnlocked() &&
						attr.hasLore(MasteryOrb.instance());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<InfoButton> buttons = new LinkedList<>();
				
				buttons.add(new SubscreenInfoButton(offset++, "trial.fire",
						new PaginatedInfoSubScreen("trial.fire"),
						SpellRune.getRune(EMagicElement.FIRE, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "trial.ice",
						new PaginatedInfoSubScreen("trial.ice"),
						SpellRune.getRune(EMagicElement.ICE, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "trial.earth",
						new PaginatedInfoSubScreen("trial.earth"),
						SpellRune.getRune(EMagicElement.EARTH, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "trial.wind",
						new PaginatedInfoSubScreen("trial.wind"),
						SpellRune.getRune(EMagicElement.WIND, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "trial.ender",
						new PaginatedInfoSubScreen("trial.ender"),
						SpellRune.getRune(EMagicElement.ENDER, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "trial.lightning",
						new PaginatedInfoSubScreen("trial.lightning"),
						SpellRune.getRune(EMagicElement.LIGHTNING, 1)));
				
				buttons.add(new SubscreenInfoButton(offset++, "trial.physical",
						new PaginatedInfoSubScreen("trial.physical"),
						SpellRune.getRune(EMagicElement.PHYSICAL, 1)));
					
				return buttons;
			}
			
		};
		
		INFO_DRAGONS = new InfoScreenTab(InfoScreenTabs.INFO_DRAGONS,
				new ItemStack(DragonEgg.instance())) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return attr.isUnlocked() &&
						attr.hasLore(TameRedDragonLore.instance());
			}

			@Override
			public List<InfoButton> getButtons(int offset, INostrumMagic attr) {
				List<InfoButton> buttons = new LinkedList<>();
				
				buttons.add(new SubscreenInfoButton(offset++, "tamed_dragon.intro",
						new PaginatedInfoSubScreen("tamed_dragon.intro"),
						new ItemStack(Items.BOOK)));
				
				buttons.add(new SubscreenInfoButton(offset++, "tamed_dragon.attributes",
						new PaginatedInfoSubScreen("tamed_dragon.attributes"),
						new ItemStack(Items.PAPER)));
				
				buttons.add(new SubscreenInfoButton(offset++, "tamed_dragon.bonding",
						new PaginatedInfoSubScreen("tamed_dragon.bonding"),
						new ItemStack(Items.SKULL, 1, 3)));
				
				buttons.add(new SubscreenInfoButton(offset++, "tamed_dragon.experience",
						new PaginatedInfoSubScreen("tamed_dragon.experience"),
						new ItemStack(Items.IRON_SWORD)));
					
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
		case PERSONAL:
			ret = PERSONAL;
			break;
		case INFO_GUIDES:
			ret = INFO_GUIDES;
			break;
		case INFO_TRIALS:
			ret = INFO_TRIALS;
			break;
		case INFO_DRAGONS:
			ret = INFO_DRAGONS;
			break;
		}
		
		if (ret == null) // not default so we get warning if not all are in switch!
			NostrumMagica.logger.warn("Missing switch statement for tab type " + tab.name());
		
		return ret;
	}
	
	protected InfoScreenTabs tab;
	private ItemStack icon;
	protected Map<String, InfoButton> index;
	private InfoScreenTab(InfoScreenTabs tab, ItemStack icon) {
		this.tab = tab;
		this.icon = icon;
		index = new HashMap<>();
	}
	
	public abstract boolean isVisible(INostrumMagic attr);
	
	public abstract List<InfoButton> getButtons(int IDOffset, INostrumMagic attr);
	
	public ItemStack getIcon() {
		return this.icon;
	}
	
	public InfoButton lookup(String lookupString) {
		return index.get(lookupString);
	}
	
	protected void index(String key, InfoButton button) {
		index.put(key, button);
	}
}
