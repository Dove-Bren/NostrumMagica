package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity;
import com.smanzana.nostrummagica.entity.dragon.TameRedDragonEntity.TameRedDragonLore;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.progression.research.NostrumResearches;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRegistry;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.MagicCapability;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.log.SpellLogEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public abstract class InfoScreenTab {
	private static InfoScreenTab PERSONAL;
	private static InfoScreenTab RITUALS;
	private static InfoScreenTab INFO_GUIDES;
	private static InfoScreenTab INFO_TRIALS;
	private static InfoScreenTab INFO_DRAGONS;
	private static InfoScreenTab LORE;
	private static InfoScreenTab SPELL_LOG;
	
	public static void init() {
		if (RITUALS != null)
			return;
		
		RITUALS = new InfoScreenTab(InfoScreenTabs.RITUALS,
				new ItemStack(NostrumBlocks.candle)) {

			private List<RitualRecipe> getAvailable(INostrumMagic attr) {
				List<RitualRecipe> list = new ArrayList<>();
				for (RitualRecipe ritual : RitualRegistry.instance().getRegisteredRituals()) {
					if (ritual.getRequirement() == null || ritual.getRequirement().matches(Minecraft.getInstance().player))
						list.add(ritual);
				}
				
				return list;
			}
			
			@Override
			public boolean isVisible(INostrumMagic attr) {
				return (MagicCapability.RITUAL_ENABLED.matches(attr) && !getAvailable(attr).isEmpty());
			}

			@Override
			public List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr) {
				List<RitualRecipe> rituals = getAvailable(attr);
				List<InfoButton> buttons = new ArrayList<>();
				for (RitualRecipe tag : rituals) {
					RitualInfoButton button = new RitualInfoButton(screen, tag);
					buttons.add(button);
					index(tag.getInfoScreenKey(), button);
				}
				return buttons;
			}
			
		};
		
		PERSONAL = new InfoScreenTab(InfoScreenTabs.PERSONAL,
				new ItemStack(Items.PLAYER_HEAD, 1)) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return true;
			}

			@Override
			public List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr) {
				List<InfoButton> buttons = new ArrayList<>();
				
				buttons.add(new SubscreenInfoButton(screen, "growth",
						new PersonalSubScreen.PersonalGrowthScreen(attr),
						new ItemStack(Items.COMPASS)));
				
				if (attr.isUnlocked()) {
					
//					buttons.add(new SubscreenInfoButton(screen, "incantation",
//							new PersonalSubScreen.PersonalIncantationScreen(attr, screen),
//							new ItemStack(NostrumItems.mageStaff)));
					
					buttons.add(new SubscreenInfoButton(screen, "exploration",
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
			public List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr) {
				List<InfoButton> buttons = new ArrayList<>();
				InfoButton button;
				
				button = new SubscreenInfoButton(screen, "first_steps",
						new PaginatedInfoSubScreen(screen, "first_steps"),
						new ItemStack(Items.LEATHER_BOOTS));
				buttons.add(button);
				index("builtin::guides::first_steps", button);
				
				button = new SubscreenInfoButton(screen, "spell_casting",
						new PaginatedInfoSubScreen(screen, "spell_casting"),
						new ItemStack(NostrumItems.mageStaff));
				buttons.add(button);
				index("builtin::guides::spell_casting", button);
				
				button = new SubscreenInfoButton(screen, "incantations",
						new PaginatedInfoSubScreen(screen, "incantations"),
						new ItemStack(NostrumItems.resourceSkillFlute));
				buttons.add(button);
				index("builtin::guides::incantations", button);
				
				button = new SubscreenInfoButton(screen, "spells",
						new PaginatedInfoSubScreen(screen, "spells"),
						new ItemStack(NostrumItems.spellScroll));
				buttons.add(button);
				index("builtin::guides::spells", button);
				
				button = new SubscreenInfoButton(screen, "shape",
						new PaginatedInfoSubScreen(screen, "shape"),
						SpellRune.getRune(NostrumSpellShapes.Touch));
				buttons.add(button);
				index("builtin::guides::shape", button);
				
				button = new SubscreenInfoButton(screen, "element",
						new PaginatedInfoSubScreen(screen, "element"),
						SpellRune.getRune(EMagicElement.FIRE));
				buttons.add(button);
				index("builtin::guides::element", button);
				
				button = new SubscreenInfoButton(screen, "alteration",
						new PaginatedInfoSubScreen(screen, "alteration"),
						SpellRune.getRune(EAlteration.INFLICT));
				buttons.add(button);
				index("builtin::guides::alteration", button);
				
				button = new SubscreenInfoButton(screen, "spell_weight",
						new PaginatedInfoSubScreen(screen, "spell_weight"),
						new ItemStack(Items.CHAIN));
				buttons.add(button);
				index("builtin::guides::spell_weight", button);
				
				button = new SubscreenInfoButton(screen, "levelup",
						new PaginatedInfoSubScreen(screen, "levelup"),
						new ItemStack(Items.PLAYER_HEAD));
				buttons.add(button);
				index("builtin::guides::levelup", button);
				
				button = new SubscreenInfoButton(screen, "elementdamage",
						new PaginatedInfoSubScreen(screen, "elementdamage"),
						new ItemStack(Items.DIAMOND_SWORD));
				buttons.add(button);
				index("builtin::guides::elementdamage", button);
				
				button = new SubscreenInfoButton(screen, "reagents",
						new PaginatedInfoSubScreen(screen, "reagents"),
						ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1));
				buttons.add(button);
				index("builtin::guides::reagents", button);
				
				if (attr.getCompletedResearches().contains(NostrumResearches.ID_Obelisks)) {
					button = new SubscreenInfoButton(screen, "obelisks",
							new PaginatedInfoSubScreen(screen, "obelisks"),
							new ItemStack(Items.ENDER_PEARL));
					buttons.add(button);
					index("builtin::guides::obelisks", button);
				}
				
				if (attr.getCompletedResearches().contains(NostrumResearches.ID_Soulbound_Pets)) {
					button = new SubscreenInfoButton(screen, "soulbound_pets",
							new PaginatedInfoSubScreen(screen, "soulbound_pets"),
							new ItemStack(NostrumItems.dragonSoulItem));
					buttons.add(button);
					index("builtin::guides::soulbound_pets", button);
				}
					
				return buttons;
			}
			
		};
		
		INFO_TRIALS = new InfoScreenTab(InfoScreenTabs.INFO_TRIALS,
				new ItemStack(NostrumItems.masteryOrb)) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return MagicCapability.ELEMENTAL_TRIALS.matches(attr);
			}

			@Override
			public List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr) {
				List<InfoButton> buttons = new ArrayList<>();
				InfoButton button;
				
				button = new SubscreenInfoButton(screen, "trial.fire",
						new PaginatedInfoSubScreen(screen, "trial.fire"),
						SpellRune.getRune(EMagicElement.FIRE));
				buttons.add(button);
				this.index("builtin::trials::fire", button);
				
				button = new SubscreenInfoButton(screen, "trial.ice",
						new PaginatedInfoSubScreen(screen, "trial.ice"),
						SpellRune.getRune(EMagicElement.ICE));
				buttons.add(button);
				this.index("builtin::trials::ice", button);
				
				button = new SubscreenInfoButton(screen, "trial.earth",
						new PaginatedInfoSubScreen(screen, "trial.earth"),
						SpellRune.getRune(EMagicElement.EARTH));
				buttons.add(button);
				this.index("builtin::trials::earth", button);
				
				button = new SubscreenInfoButton(screen, "trial.wind",
						new PaginatedInfoSubScreen(screen, "trial.wind"),
						SpellRune.getRune(EMagicElement.WIND));
				buttons.add(button);
				this.index("builtin::trials::wind", button);
				
				button = new SubscreenInfoButton(screen, "trial.ender",
						new PaginatedInfoSubScreen(screen, "trial.ender"),
						SpellRune.getRune(EMagicElement.ENDER));
				buttons.add(button);
				this.index("builtin::trials::ender", button);
				
				button = new SubscreenInfoButton(screen, "trial.lightning",
						new PaginatedInfoSubScreen(screen, "trial.lightning"),
						SpellRune.getRune(EMagicElement.LIGHTNING));
				buttons.add(button);
				this.index("builtin::trials::lightning", button);
				
				button = new SubscreenInfoButton(screen, "trial.neutral",
						new PaginatedInfoSubScreen(screen, "trial.neutral"),
						SpellRune.getRune(EMagicElement.NEUTRAL));
				buttons.add(button);
				this.index("builtin::trials::neutral", button);
					
				return buttons;
			}
			
		};
		
		INFO_DRAGONS = new InfoScreenTab(InfoScreenTabs.INFO_DRAGONS,
				new ItemStack(NostrumItems.dragonEgg)) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return attr.isUnlocked() &&
						attr.hasLore(TameRedDragonLore.instance());
			}

			@Override
			public List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr) {
				List<InfoButton> buttons = new ArrayList<>();
				
				buttons.add(new SubscreenInfoButton(screen, "tamed_dragon.intro",
						new PaginatedInfoSubScreen(screen, "tamed_dragon.intro"),
						new ItemStack(Items.BOOK)));
				
				buttons.add(new SubscreenInfoButton(screen, "tamed_dragon.attributes",
						new PaginatedInfoSubScreen(screen, "tamed_dragon.attributes"),
						new ItemStack(Items.PAPER)));
				
				buttons.add(new SubscreenInfoButton(screen, "tamed_dragon.bonding",
						new PaginatedInfoSubScreen(screen, "tamed_dragon.bonding"),
						new ItemStack(Items.PLAYER_HEAD, 1)));
				
				buttons.add(new SubscreenInfoButton(screen, "tamed_dragon.experience",
						new PaginatedInfoSubScreen(screen, "tamed_dragon.experience"),
						new ItemStack(Items.IRON_SWORD)));
				
				if (attr.hasLore(TameRedDragonEntity.SoulBoundDragonLore.instance())) {
					buttons.add(new SubscreenInfoButton(screen, TameRedDragonEntity.SoulBoundDragonLore.instance().getLoreKey(),
							new PaginatedInfoSubScreen(screen, TameRedDragonEntity.SoulBoundDragonLore.instance().getLoreKey()),
							new ItemStack(NostrumItems.dragonSoulItem)));
				}
					
				return buttons;
			}
			
		};
		
		LORE = new InfoScreenTab(InfoScreenTabs.LORE, new ItemStack(NostrumItems.blankScroll)) {
			@Override
			public boolean isVisible(INostrumMagic attr) {
				return attr.isUnlocked();
			}

			@Override
			public List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr) {
				final List<InfoButton> buttons = new ArrayList<>();
				Map<ELoreCategory, LoreCategoryButton> catMap = new EnumMap<>(ELoreCategory.class);
				for (ELoreCategory category : ELoreCategory.values()) {
					LoreCategoryButton button = new LoreCategoryButton(screen, category);
					catMap.put(category, button);
					buttons.add(button);
				}
				
				Collection<ILoreTagged> lore = LoreRegistry.instance().allLore();
				for (ILoreTagged tag : lore) {
					// Index each tag
					index(ILoreTagged.GetInfoKey(tag), catMap.get(tag.getCategory()));
				}
				
				return buttons;
			}
		};
		
		SPELL_LOG = new InfoScreenTab(InfoScreenTabs.SPELL_LOG,
				new ItemStack(NostrumItems.spellScroll, 1)) {

			@Override
			public boolean isVisible(INostrumMagic attr) {
				return attr.isUnlocked() && SpellLogEntry.LAST != null;
			}

			@Override
			public List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr) {
				List<InfoButton> buttons = new ArrayList<>();
				
				for (SpellLogEntry log : SpellLogEntry.LAST) {
					buttons.add(new SpellLogButton(screen, log));
				}
				
				return buttons;
			}
			
		};
	}
	
	public static InfoScreenTab get(InfoScreenTabs tab) {
		InfoScreenTab ret = null;
		switch (tab) {
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
		case SPELL_LOG:
			ret = SPELL_LOG;
			break;
		case LORE:
			ret = LORE;
			break;
		case RITUALS:
			ret = RITUALS;
			break;
		}
		
		if (ret == null) // not default so we get warning if not all are in switch!
			NostrumMagica.logger.warn("Missing switch statement for tab type " + tab.name());
		
		return ret;
	}
	
	protected InfoScreenTabs tab;
	private @Nonnull ItemStack icon;
	protected Map<String, InfoButton> index;
	private InfoScreenTab(InfoScreenTabs tab, @Nonnull ItemStack icon) {
		this.tab = tab;
		this.icon = icon;
		index = new HashMap<>();
	}
	
	public abstract boolean isVisible(INostrumMagic attr);
	
	public abstract List<InfoButton> getButtons(InfoScreen screen, INostrumMagic attr);
	
	public @Nonnull ItemStack getIcon() {
		return this.icon;
	}
	
	public InfoButton lookup(String lookupString) {
		return index.get(lookupString);
	}
	
	protected void index(String key, InfoButton button) {
		index.put(key, button);
	}
}
