package com.smanzana.nostrummagica.research;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenIndexed;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.quests.NostrumQuest;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents a piece of research a player can complete.
 * These are similar to NostrumQuest objects but are purchased with research points and are on the research tab.
 * Quests are for personal improvement, while research items are for crafting/ritual progression.
 * @author Skyler
 *
 */
public class NostrumResearch {

	public static enum Size {
		NORMAL,
		LARGE,
		GIANT,
	}
	
	protected final String key;
	
	/**
	 * Size to display the research task as
	 */
	protected final Size size;
	
	/**
	 * List of parent research items. This becomes a sanitized array and expects each to exist.
	 */
	protected String[] parentKeys;
	
	/**
	 * Parent research items that are not displayed as parents in the GUI
	 */
	protected String[] hiddenParentKeys;
	
	/**
	 * List of lore keys that are required before this research can be seen/purchased.
	 */
	protected String[] loreKeys;
	
	/**
	 * List of quest keys. Each quest must be completed before this research item can be seen/purchased.
	 */
	protected String[] questKeys;
	
	/**
	 * Positional offsets for display.
	 */
	protected final int x;
	protected final int y;
	
	/**
	 * If true, don't show the research in the mirror until all requirements have been satisfied.
	 * (Instead of if 1+ parents have for the player to see and mouseover and find out what's needed)
	 */
	protected final boolean hidden;
	
	/**
	 * Icon to display
	 */
	protected final ItemStack iconItem;
	
	/**
	 * Things to link to when the player is viewing the details of their completed research.
	 * I'm thinking these will be keys that the infoscreen system can open up to via a click or something.
	 * This list is not sanitized.
	 */
	protected final String[] references;
	protected final String[] referenceDisplays;
	
	protected final NostrumResearchTab tab;
	
	protected String[] allParents; // Filled in during validation
	
	private NostrumResearch(String key, NostrumResearchTab tab, Size size, int x, int y, boolean hidden, ItemStack icon,
			String[] parents, String[] hiddenParents,
			String[] lore,
			String[] quests,
			String[] references, String[] referenceDisplays) {
		this.key = key;
		this.size = size;
		this.x = x;
		this.y = y;
		this.hidden = hidden;
		this.iconItem = icon;
		this.parentKeys = (parents != null && parents.length == 0) ? null : parents; // empty->null
		this.hiddenParentKeys = (hiddenParents != null && hiddenParents.length == 0) ? null : hiddenParents;
		this.loreKeys = (lore != null && lore.length == 0) ? null : lore;
		this.questKeys = (quests != null && quests.length == 0) ? null : quests;
		this.references = (references != null && references.length == 0) ? null : references;
		this.referenceDisplays = (references != null && references.length == 0) ? null : referenceDisplays; // note uses 'references'
		this.tab = tab;
		
		NostrumResearch.register(this);
	}
	
	public String getKey() {
		return key;
	}

	public Size getSize() {
		return size;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public ItemStack getIconItem() {
		return iconItem;
	}
	
	public String[] getRawReferences() {
		return references;
	}
	
	public String[] getDisplayedReferences() {
		return referenceDisplays;
	}

	public String getNameKey() {
		return "research." + key + ".name";
	}
	
	public String getDescKey() {
		return "research." + key + ".desc";
	}
	
	public String getInfoKey() {
		return "research." + key + ".info";
	}
	
	public String[] getParentKeys() {
		return this.parentKeys;
	}
	
	public String[] getHiddenParentKeys() {
		return this.hiddenParentKeys;
	}
	
	public String[] getAllParents() {
		return allParents;
	}
	
	public String[] getRequiredLore() {
		return this.loreKeys;
	}
	
	public String[] getRequiredQuests() {
		return this.questKeys;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public NostrumResearchTab getTab() {
		return tab;
	}
	
	
	/***********************************************
	 *             Interaction methods
	 ***********************************************/
	
	/**
	 * Mark the provided research as unlocked, allowing all the things that depend on it to
	 * Work.
	 * Note: This doesn't deduct research points from the player.
	 * @param research
	 */
	public static void unlockResearch(EntityPlayer player, NostrumResearch research) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		attr.completeResearch(research.getKey());
		if (!player.worldObj.isRemote)
			NostrumMagicaSounds.AMBIENT_WOOSH.play(player.worldObj, player.posX, player.posY, player.posZ);
		else
			NostrumMagica.proxy.syncPlayer((EntityPlayerMP) player);
	}


	/***********************************************
	 *             Builder methods
	 ***********************************************/
	public static final class Builder {
		protected List<String> parentKeys;
		protected List<String> hiddenParentKeys;
		protected List<String> loreKeys;
		protected List<String> questKeys;
		protected List<String> references;
		protected List<String> referenceDisplays;
		
		protected Builder() {
			parentKeys = new LinkedList<>();
			hiddenParentKeys = new LinkedList<>();
			loreKeys = new LinkedList<>();
			questKeys = new LinkedList<>();
			references = new LinkedList<>();
			referenceDisplays = new LinkedList<>();
		}
		
		public Builder parent(String parent) {
			parentKeys.add(parent);
			return this;
		}
		
		public Builder hiddenParent(String parent) {
			hiddenParentKeys.add(parent);
			return this;
		}
		
		public Builder lore(String lore) {
			loreKeys.add(lore);
			return this;
		}
		
		public Builder lore(ILoreTagged lore) {
			return this.lore(lore.getLoreKey());
		}
		
		public Builder quest(String quest) {
			questKeys.add(quest);
			return this;
		}
		
		public Builder reference(String reference, String displayAs) {
			references.add(reference);
			referenceDisplays.add(displayAs);
			return this;
		}
		
		public Builder reference(InfoScreenIndexed reference, String displayAs) {
			return this.reference(reference.getInfoScreenKey(), displayAs);
		}
		
		public Builder reference(Item item) {
			if (item instanceof InfoScreenIndexed) {
				return this.reference((InfoScreenIndexed) item, item.getUnlocalizedName() + ".name");
			} else if (item instanceof ILoreTagged) {
				return this.reference(ILoreTagged.GetInfoKey((ILoreTagged) item), item.getUnlocalizedName() + ".name");
			} else {
				NostrumMagica.logger.error("Provided item reference does not extend the required interfaces (ILoreTagged or InfoScreenIndexed) and cannot be a reference");
				return this;
			}
		}
		
		public NostrumResearch build(String key, NostrumResearchTab tab, Size size, int x, int y, boolean hidden, ItemStack icon) {
			return new NostrumResearch(key, tab, size, x, y, hidden, icon,
					parentKeys.isEmpty() ? null : parentKeys.toArray(new String[parentKeys.size()]),
					hiddenParentKeys.isEmpty() ? null : hiddenParentKeys.toArray(new String[hiddenParentKeys.size()]),
					loreKeys.isEmpty() ? null : loreKeys.toArray(new String[loreKeys.size()]),
					questKeys.isEmpty() ? null : questKeys.toArray(new String[questKeys.size()]),
					references.isEmpty() ? null : references.toArray(new String[references.size()]),
					referenceDisplays.isEmpty() ? null : referenceDisplays.toArray(new String[referenceDisplays.size()])
					);
		}
		
//		public NostrumResearch build(String key, NostrumResearchTab tab, Size size, int x, int y, boolean hidden) {
//			return build(key, tab, size, x, y, hidden, null);
//		}
	}
	
	public static Builder startBuilding() {
		return new Builder();
	}
	
	
	/***********************************************
	 *             Registry methods
	 ***********************************************/
	
	private static Map<String, NostrumResearch> Registry = new HashMap<>();
	
	private static void register(NostrumResearch research) {
		if (Registry.containsKey(research.key)) {
			NostrumMagica.logger.error("Duplicate research registration for key " + research.key);
			return;
		}
		
		Registry.put(research.key, research);
	}
	
	public static NostrumResearch lookup(String key) {
		return Registry.get(key);
	}
	
	public static Collection<NostrumResearch> AllResearch() {
		return Registry.values();
	}
	
	public static void ClearAllResearch() {
		Registry.clear();
	}
	
	/**
	 * Iterate over all registered researches.
	 * Perform parent checks. Fix up dependencies.
	 */
	public static void Validate() {
		int count = 0;
		for (NostrumResearch research : Registry.values()) {
			boolean counted = false;
			if (research.parentKeys != null) {
				List<String> outList = new LinkedList<>();
				for (String dep : research.parentKeys) {
					if (Registry.containsKey(dep))
						outList.add(dep);
				}
				
				if (outList.isEmpty())
					research.parentKeys = null;
				else
					research.parentKeys = outList.toArray(new String[0]);
				
				counted = true;
			}
			
			if (research.hiddenParentKeys != null) {
				List<String> outList = new LinkedList<>();
				for (String dep : research.hiddenParentKeys) {
					if (Registry.containsKey(dep))
						outList.add(dep);
				}
				
				if (outList.isEmpty())
					research.hiddenParentKeys = null;
				else
					research.hiddenParentKeys = outList.toArray(new String[0]);
				
				counted = true;
			}
			
			if (counted) {
				count++;
				research.allParents = new String[(research.parentKeys == null ? 0 : research.parentKeys.length)
													+ (research.hiddenParentKeys == null ? 0 : research.hiddenParentKeys.length)];
				if (research.parentKeys != null) {
					System.arraycopy(research.parentKeys, 0, research.allParents, 0, research.parentKeys.length);
				}
				if (research.hiddenParentKeys != null) {
					System.arraycopy(research.hiddenParentKeys, 0, research.allParents, research.parentKeys == null ? 0 : research.parentKeys.length, research.hiddenParentKeys.length);
				}
			}
		}
		
		if (count != 0)
			NostrumMagica.logger.info("Validated " + count + " research parent dependencies");
		
		count = 0;
		for (NostrumResearch research : Registry.values()) {
			if (research.loreKeys == null) {
				continue;
			}
			
			count++;
			
			List<String> outList = new LinkedList<>();
			for (String dep : research.loreKeys) {
				if (LoreRegistry.instance().lookup(dep) != null)
					outList.add(dep);
			}
			
			if (outList.isEmpty())
				research.loreKeys = null;
			else
				research.loreKeys = outList.toArray(new String[0]);
		}
		
		if (count != 0)
			NostrumMagica.logger.info("Validated " + count + " research lore dependencies");
		
		count = 0;
		for (NostrumResearch research : Registry.values()) {
			if (research.questKeys == null) {
				continue;
			}
			
			count++;
			
			List<String> outList = new LinkedList<>();
			for (String dep : research.questKeys) {
				if (NostrumQuest.lookup(dep) != null)
					outList.add(dep);
			}
			
			if (outList.isEmpty())
				research.questKeys = null;
			else
				research.questKeys = outList.toArray(new String[0]);
		}
		
		if (count != 0)
			NostrumMagica.logger.info("Validated " + count + " research quest dependencies");
	}
	
	
	/***********************************************
	 *             Organization methods
	 ***********************************************/
	
	/**
	 * Tab key class that helps organize researches into different tabs.
	 * All checks are done based on reference equality. Instantiate once and use that same instance in all calls.
	 * @author Skyler
	 *
	 */
	public static class NostrumResearchTab {
		
		/**
		 * Unique key for this tab.
		 * Drives translations.
		 */
		private final String key;
		
		/**
		 * The icon to display on the tab. Required.
		 */
		private final ItemStack icon;
		
		/**
		 * Client-only. Whether a new research event has happened on this tab.
		 */
		private boolean hasNew;
		
		public NostrumResearchTab(String key, ItemStack icon) {
			this.key = key;
			this.icon = icon;
			
			AllTabs.add(this);
		}
		
		public String getRawKey() {
			return key;
		}
		
		public ItemStack getIcon() {
			return icon;
		}
		
		public String getNameKey() {
			return "research.tab." + key + ".name";
		}
		
		@SideOnly(Side.CLIENT)
		public void markHasNew() {
			hasNew = true;
		}
		
		@SideOnly(Side.CLIENT)
		public void clearNew() {
			hasNew = false;
		}
		
		@SideOnly(Side.CLIENT)
		public boolean hasNew() {
			return hasNew;
		}
		
		public static NostrumResearchTab MAGICA = null;
		public static NostrumResearchTab ADVANCED_MAGICA = null;
		public static NostrumResearchTab MYSTICISM = null;
		public static NostrumResearchTab OUTFITTING = null;
		
		private static List<NostrumResearchTab> AllTabs = new LinkedList<>();
		
		public static List<NostrumResearchTab> All() {
			return AllTabs;
		}
	}
	
	
}
