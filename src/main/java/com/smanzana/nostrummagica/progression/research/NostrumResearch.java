package com.smanzana.nostrummagica.progression.research;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenIndexed;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.progression.requirement.IRequirement;
import com.smanzana.nostrummagica.progression.requirement.LoreRequirement;
import com.smanzana.nostrummagica.progression.requirement.QuestRequirement;
import com.smanzana.nostrummagica.progression.requirement.SpellKnowledgeRequirement;
import com.smanzana.nostrummagica.progression.requirement.TierRequirement;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

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
	
	protected final ResourceLocation id;
	
	/**
	 * Size to display the research task as
	 */
	protected final Size size;
	
	/**
	 * List of parent research items. This becomes a sanitized array and expects each to exist.
	 */
	protected ResourceLocation[] parentKeys;
	
	/**
	 * Parent research items that are not displayed as parents in the GUI
	 */
	protected ResourceLocation[] hiddenParentKeys;
	
	/**
	 * Requirements (besides parent keys) that are required to be able to see/purchase this research
	 */
	protected IRequirement[] requirements;
	
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
	 * If true, don't let players purchase the research like normal.
	 * Typically this is because the research is awarded from other means, like a ResearchTranscriptItem.
	 */
	protected final boolean disallowPurchase;
	
	/**
	 * Icon to display
	 */
	protected final @Nonnull ItemStack iconItem;
	
	/**
	 * Things to link to when the player is viewing the details of their completed research.
	 * I'm thinking these will be keys that the infoscreen system can open up to via a click or something.
	 * This list is not sanitized.
	 */
	protected final String[] references;
	protected final String[] referenceDisplays;
	
	/**
	 * Other research items that should automatically be unlocked/purchased when this one is
	 */
	protected final ResourceLocation[] linkedKeys;
	
	protected final NostrumResearchTab tab;
	
	protected ResourceLocation[] allParents; // Filled in during validation
	
	private NostrumResearch(ResourceLocation id, NostrumResearchTab tab, Size size, int x, int y, boolean hidden, boolean disallowPurchase, @Nonnull ItemStack icon,
			ResourceLocation[] parents, ResourceLocation[] hiddenParents, ResourceLocation[] linked,
			IRequirement[] requirements,
			String[] references, String[] referenceDisplays) {
		this.id = id;
		this.size = size;
		this.x = x;
		this.y = y;
		this.hidden = hidden;
		this.disallowPurchase = disallowPurchase;
		this.iconItem = icon;
		this.parentKeys = (parents != null && parents.length == 0) ? null : parents; // empty->null
		this.hiddenParentKeys = (hiddenParents != null && hiddenParents.length == 0) ? null : hiddenParents;
		this.linkedKeys = (linked != null && linked.length == 0) ? null : linked;
		this.requirements = (requirements != null && requirements.length == 0) ? null : requirements;
		this.references = (references != null && references.length == 0) ? null : references;
		this.referenceDisplays = (references != null && references.length == 0) ? null : referenceDisplays; // note uses 'references'
		this.tab = tab;
		
		NostrumResearch.register(this);
	}
	
	public ResourceLocation getID() {
		return id;
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

	public @Nonnull ItemStack getIconItem() {
		return iconItem;
	}
	
	public String[] getRawReferences() {
		return references;
	}
	
	public String[] getDisplayedReferences() {
		return referenceDisplays;
	}

	public String getNameKey() {
		return "research." + id.toString().replace(':', '.') + ".name";
	}
	
	public String getDescKey() {
		return "research." + id.toString().replace(':', '.') + ".desc";
	}
	
	public String getInfoKey() {
		return "research." + id.toString().replace(':', '.') + ".info";
	}
	
	public ResourceLocation[] getParentKeys() {
		return this.parentKeys;
	}
	
	public ResourceLocation[] getHiddenParentKeys() {
		return this.hiddenParentKeys;
	}
	
	public ResourceLocation[] getAllParents() {
		return allParents;
	}
	
	public IRequirement[] getRequirements() {
		return this.requirements;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public boolean isPurchaseDisallowed() {
		return disallowPurchase;
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
	public static void unlockResearch(Player player, NostrumResearch research) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		attr.completeResearch(research.getID());
		if (research.linkedKeys != null) {
			for (ResourceLocation link : research.linkedKeys) {
				attr.completeResearch(link);
			}
		}
		if (!player.level.isClientSide) {
			NostrumMagicaSounds.SUCCESS_RESEARCH.play(player.level, player.getX(), player.getY(), player.getZ());
			NostrumMagicaSounds.UI_RESEARCH.play(player.level, player.getX(), player.getY(), player.getZ());
		} else {
			NostrumMagica.Proxy.syncPlayer((ServerPlayer) player);
		}
	}


	/***********************************************
	 *             Builder methods
	 ***********************************************/
	public static final class Builder {
		protected final List<ResourceLocation> parentKeys;
		protected final List<ResourceLocation> hiddenParentKeys;
		protected final List<ResourceLocation> linkedKeys;
		protected final List<String> references;
		protected final List<String> referenceDisplays;
		protected final List<IRequirement> requirements;
		
		protected Builder() {
			parentKeys = new ArrayList<>();
			hiddenParentKeys = new ArrayList<>();
			linkedKeys = new ArrayList<>();
			requirements = new ArrayList<>();
			references = new ArrayList<>();
			referenceDisplays = new ArrayList<>();
		}
		
		public Builder parent(ResourceLocation parent) {
			parentKeys.add(parent);
			return this;
		}
		
		public Builder hiddenParent(ResourceLocation parent) {
			hiddenParentKeys.add(parent);
			return this;
		}
		
		public Builder link(ResourceLocation key) {
			this.linkedKeys.add(key);
			return this;
		}
		
		public Builder lore(String lore) {
			this.requirements.add(new LoreRequirement(lore));
			return this;
		}
		
		public Builder lore(ILoreTagged lore) {
			this.requirements.add(new LoreRequirement(lore));
			return this;
		}
		
		public Builder quest(String quest) {
			this.requirements.add(new QuestRequirement(quest));
			return this;
		}
		
		public Builder spellComponent(EMagicElement element, EAlteration alteration) {
			this.requirements.add(new SpellKnowledgeRequirement(element, alteration));
			return this;
		}
		
		public Builder tier(EMagicTier tier) {
			this.requirements.add(new TierRequirement(tier));
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
				return this.reference((InfoScreenIndexed) item, item.getDescriptionId());
			} else if (item instanceof ILoreTagged) {
				return this.reference(ILoreTagged.GetInfoKey((ILoreTagged) item), item.getDescriptionId());
			} else {
				NostrumMagica.logger.error(
						"%s: Provided item reference (%s) does not extend the required interfaces (ILoreTagged or InfoScreenIndexed) and cannot be a reference".formatted(item.toString()));
				return this;
			}
		}
		
		public Builder reference(Block block) {
			if (block instanceof InfoScreenIndexed) {
				return this.reference((InfoScreenIndexed) block, block.getDescriptionId());
			} else if (block instanceof ILoreTagged) {
				return this.reference(ILoreTagged.GetInfoKey((ILoreTagged) block), block.getDescriptionId());
			} else {
				NostrumMagica.logger.error(
						"%s: Provided block reference (%s) does not extend the required interfaces (ILoreTagged or InfoScreenIndexed) and cannot be a reference".formatted(block.toString()));
				return this;
			}
		}
		
		public NostrumResearch build(ResourceLocation id, NostrumResearchTab tab, Size size, int x, int y, boolean hidden, @Nonnull ItemStack icon) {
			return build(id, tab, size, x, y, hidden, false, icon);
		}
		
		public NostrumResearch build(ResourceLocation id, NostrumResearchTab tab, Size size, int x, int y, boolean hidden, boolean disallowPurchase, @Nonnull ItemStack icon) {
			return new NostrumResearch(id, tab, size, x, y, hidden, disallowPurchase, icon,
					parentKeys.isEmpty() ? null : parentKeys.toArray(new ResourceLocation[parentKeys.size()]),
					hiddenParentKeys.isEmpty() ? null : hiddenParentKeys.toArray(new ResourceLocation[hiddenParentKeys.size()]),
					linkedKeys.isEmpty() ? null : linkedKeys.toArray(new ResourceLocation[linkedKeys.size()]),
					requirements.isEmpty() ? null : requirements.toArray(new IRequirement[requirements.size()]),
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
	
	private static Map<ResourceLocation, NostrumResearch> Registry = new HashMap<>();
	
	private static void register(NostrumResearch research) {
		if (Registry.containsKey(research.id)) {
			NostrumMagica.logger.error("Duplicate research registration for key " + research.id);
			return;
		}
		
		Registry.put(research.id, research);
	}
	
	public static NostrumResearch lookup(ResourceLocation id) {
		return Registry.get(id);
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
				List<ResourceLocation> outList = new ArrayList<>();
				for (ResourceLocation dep : research.parentKeys) {
					if (Registry.containsKey(dep))
						outList.add(dep);
				}
				
				if (outList.isEmpty())
					research.parentKeys = null;
				else
					research.parentKeys = outList.toArray(new ResourceLocation[0]);
				
				counted = true;
			}
			
			if (research.hiddenParentKeys != null) {
				List<ResourceLocation> outList = new ArrayList<>();
				for (ResourceLocation dep : research.hiddenParentKeys) {
					if (Registry.containsKey(dep))
						outList.add(dep);
				}
				
				if (outList.isEmpty())
					research.hiddenParentKeys = null;
				else
					research.hiddenParentKeys = outList.toArray(new ResourceLocation[0]);
				
				counted = true;
			}
			
			if (counted) {
				count++;
				research.allParents = new ResourceLocation[(research.parentKeys == null ? 0 : research.parentKeys.length)
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
			if (research.requirements == null) {
				continue;
			}
			
			count++;
			
			List<IRequirement> outList = new ArrayList<>();
			for (IRequirement req : research.getRequirements()) {
				if (req.isValid()) {
					outList.add(req);
				}
			}
			
			if (outList.isEmpty())
				research.requirements = null;
			else
				research.requirements = outList.toArray(new IRequirement[0]);
		}
		
		if (count != 0)
			NostrumMagica.logger.info("Validated " + count + " research requirements");
	}
}
