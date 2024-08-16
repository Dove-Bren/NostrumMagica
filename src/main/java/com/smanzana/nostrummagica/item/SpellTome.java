package com.smanzana.nostrummagica.item;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.HSplitPage;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.LinedTextPage;
import com.smanzana.nostrummagica.client.gui.book.SpellPreviewPage;
import com.smanzana.nostrummagica.client.gui.book.TitlePage;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.SpellRequestMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class SpellTome extends Item implements GuiBook, ILoreTagged, IRaytraceOverlay, ISpellCastingTool, ISpellContainerItem {
	
	public static enum TomeStyle {
		NOVICE, // Blue and simple
		ADVANCED, // Red
		COMBAT, // Sword icon
		DEATH, // Cross/grave icon
		SPOOKY, // Creepy
		MUTED, //  Plain but creepy design
		LIVING; // Organic look
		
		private TomeStyle() {
			
		}
		
		public static final String ID_SUFFIX_NOVICE = "novice";
		public static final String ID_SUFFIX_ADVANCED = "advanced";
		public static final String ID_SUFFIX_COMBAT = "combat";
		public static final String ID_SUFFIX_DEATH = "death";
		public static final String ID_SUFFIX_SPOOKY = "spooky";
		public static final String ID_SUFFIX_MUTED = "muted";
		public static final String ID_SUFFIX_LIVING = "living";
	}
	
	private static class LevelCurve {
		public static int getMaxXP(int level) {
			switch (level) {
			case 1:
			case 0:
				return 50;
			case 2:
				return 100;
			case 3:
				return 200;
			case 4:
				return 500;
			case 5:
				return 5000;
			default:
				return 99999999;
			}
		}
		
		public static int getBaseCapacity(int level) {
			// level 1 has 0 capacity
			// First two levels are worth +5
			// Next two are worth +10
			// Last is with +20
			switch (level) {
			case 1:
				return 0;
			case 2:
				return 5;
			case 3:
				return 10;
			case 4:
				return 20;
			case 5:
				return 30;
			case 6:
			default:
				return 50;
			}
		}
		
		public static int getBasePageCount(int level) {
			level = Math.max(1, Math.min(6, level));
			
			// 1 page for lvl 1 and 2, 2 pages at 3 and 4, and 3 at 5 and 6
			// aka a new page every two levels
			return 1 + ((level-1) / 2);
		}
	}

	private static final String NBT_SPELLS = "nostrum_spells";
	private static final String NBT_PAGE_INDEX = "spell_page_index";
	private static final String NBT_ENHANCEMENTS = "tome_enhancements";
	private static final String NBT_ENHANCEMENT_KEY = "enhancement_key";
	private static final String NBT_ENHANCEMENT_LEVEL = "enhancement_level";
	private static final String NBT_LEVEL = "tome_level";
	private static final String NBT_XP = "tome_xp";
	private static final String NBT_MODIFICATIONS = "tome_mods";
	private static final String NBT_CAPACITY_BONUS = "tome_capacity_bonus";
	private static final String NBT_SLOTS = "tome_slots";
	private static final String NBT_PAGE_BONUS = "tome_page_bonus";
	private static final String NBT_ID = "tome_id";
	private static final String NBT_PAGE_LIST = "tome_page_list";
	
	public static final String ID_PREFIX = "spelltome_";
	
	public SpellTome() {
		super(NostrumItems.PropTomeUnstackable().rarity(Rarity.UNCOMMON));
	}
	
	public static SpellTome GetTomeForStyle(TomeStyle style) {
		SpellTome tome = null;
		switch (style) {
		case NOVICE:
			tome = NostrumItems.spellTomeNovice;
			break;
		case ADVANCED:
			tome = NostrumItems.spellTomeAdvanced;
			break;
		case COMBAT:
			tome = NostrumItems.spellTomeCombat;
			break;
		case DEATH:
			tome = NostrumItems.spellTomeDeath;
			break;
		case SPOOKY:
			tome = NostrumItems.spellTomeSpooky;
			break;
		case MUTED:
			tome = NostrumItems.spellTomeMuted;
			break;
		case LIVING:
			tome = NostrumItems.spellTomeLiving;
			break;
		}
		return tome;
	}
	
	public static ItemStack Create(TomeStyle style,
			int level, int bonusCapacity, int slots, int bonusPages,
			SpellTomeEnhancementWrapper ... enhancements) {
		return Create(style, level, bonusCapacity, slots, bonusPages, Lists.newArrayList(enhancements));
	}
	
	public static ItemStack Create(TomeStyle style,
			int level, int bonusCapacity, int slots, int bonusPages,
			List<SpellTomeEnhancementWrapper> enhancements) {
		ItemStack item = new ItemStack(GetTomeForStyle(style), 1);
		setCapacityBonus(item, bonusCapacity);
		setPageBonus(item, bonusPages);
		setSlots(item, slots);
		setLevel(item, level);
		if (enhancements != null && !enhancements.isEmpty()) {
			CompoundNBT tag = item.getTag();
			if (tag == null)
				tag = new CompoundNBT();
			
			writeEnhancements(enhancements, tag);
				
			item.setTag(tag);
			
		}
		
		return item;
	}
	
	public static ItemStack Create(@Nonnull ItemStack plate, List<ItemStack> pages) {
		TomeStyle style = ((SpellPlate) plate.getItem()).getStyle();
		List<SpellTomeEnhancementWrapper> enhancements = SpellPlate.getEnhancements(plate);
		
		if (enhancements == null)
			enhancements = new LinkedList<>();
		
		int capacity = SpellPlate.getCapacity(plate);
		int slots = SpellPlate.getSlots(plate);
		int pageCount = 0;
		
		if (pages != null) {
			for (ItemStack page : pages) {
				if (pages.isEmpty() || !(page.getItem() instanceof SpellTomePage))
					continue;
				
				pageCount++;
				enhancements.add(new SpellTomeEnhancementWrapper(SpellTomePage.getEnhancement(page),
						SpellTomePage.getLevel(page)));
			}
		}
		
		// Get a bonus page if all 4 pages were used when creating
		final int bonusPages = pageCount >= 4 ? 1 : 0; 
		ItemStack stack = Create(style, 1, capacity, slots, bonusPages, enhancements);
		
		return stack;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (worldIn.isRemote)
			NostrumMagica.instance.proxy.openBook(playerIn, this, itemStackIn);
		
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
    }
	
	public static void addSpell(ItemStack itemStack, Spell spell) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;
		
		CompoundNBT nbt = itemStack.getTag();
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		ListNBT tags = nbt.getList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null)
			tags = new ListNBT();
		
		tags.add(IntNBT.valueOf(spell.getRegistryID()));
		nbt.put(NBT_SPELLS, tags);
		
		itemStack.setTag(nbt);
	}
	
	public static boolean removeSpell(ItemStack itemStack, int spellID) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return false;
		
		CompoundNBT nbt = itemStack.getTag();
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		ListNBT tags = nbt.getList(NBT_SPELLS, NBT.TAG_INT);
		boolean found = false;
		
		if (tags == null)
			tags = new ListNBT();
		
		for (int i = 0; i < tags.size(); i++) {
			if (tags.getInt(i) == spellID) {
				tags.remove(i);
				found = true;
				break;
			}
		}
		
		if (found) {
			nbt.put(NBT_SPELLS, tags);
			itemStack.setTag(nbt);
			
			// Clean spell out from any spell pages, too
			for (int i = 0; i < getPageCount(itemStack); i++) {
				found = false;
				int[] slottedSpells = getPageSpellIDs(itemStack, i);
				for (int j = 0; j < slottedSpells.length; j++) {
					if (slottedSpells[j] == spellID) {
						found = true;
						slottedSpells[j] = -1;
					}
				}
				
				if (found) {
					setPageSpellIDs(itemStack, i, slottedSpells);
				}
			}
		}
		
		return found;
	}
	
	public static void clearSpells(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;
		
		CompoundNBT nbt = itemStack.getTag();
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		ListNBT tags = new ListNBT(); // new list to replace old
		nbt.put(NBT_SPELLS, tags);
		
		// Also needs to clear out all pages
		for (int i = 0; i < getPageCount(itemStack); i++) {
			clearPage(itemStack, i);
		}
		
		itemStack.setTag(nbt);
	}
	
	private static int[] getSpellIDs(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return null;
		
		CompoundNBT nbt = itemStack.getTag();
		
		if (nbt == null)
			return null;
		
		ListNBT tags = nbt.getList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null || tags.size() == 0)
			return null;
		
		int ids[] = new int[tags.size()];
		
		for (int i = 0; i < tags.size(); i++) {
			ids[i] = tags.getInt(i);
		}

		return ids;
	}
	
	private static final int[] makeDefaultPage(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome)) {
			return new int[0];
		}
		
		final int slots = getSlots(itemStack);
		final int[] array = new int[slots];
		Arrays.fill(array, -1);
		return array;
	}
	
	/**
	 * Returns the RAW id array, including an empty one if the tome supports it but hasn't used it yet.
	 * @param itemStack
	 * @param pageIdx
	 * @return
	 */
	private static int[] getPageSpellIDs(ItemStack itemStack, int pageIdx) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome) || !itemStack.hasTag())
			return makeDefaultPage(itemStack);
		
		CompoundNBT nbt = itemStack.getTag();
		ListNBT pages = nbt.getList(NBT_PAGE_LIST, NBT.TAG_INT_ARRAY);
		
		if (pages == null || pages.size() == 0 || pages.size() <= pageIdx) {
			return makeDefaultPage(itemStack);
		}
		
		return pages.getIntArray(pageIdx);
	}
	
	private static int getPageSpellID(ItemStack itemStack, int pageIdx, int slot) {
		int[] ids = getPageSpellIDs(itemStack, pageIdx);
		if (ids.length <= slot) {
			return -1;
		}
		
		return ids[slot];
	}
	
	private static void setPageSpellIDs(ItemStack itemStack, int pageIdx, int[] ids) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome)) {
			return;
		}
		
		CompoundNBT nbt = itemStack.getOrCreateTag();
		ListNBT pages = (nbt.contains(NBT_PAGE_LIST, NBT.TAG_LIST)) ? nbt.getList(NBT_PAGE_LIST, NBT.TAG_INT_ARRAY) : new ListNBT();
		
		// Possibly catch up on any pages that aren't in the list yet. Could just go up to pageIdx, but might as well take the time to fill them out all.
		final int pageCount = getPageCount(itemStack);
		while (pages.size() < pageCount) {
			pages.add(new IntArrayNBT(makeDefaultPage(itemStack)));
		}
		
		if (pageIdx > pageCount) {
			// Trying to assign to a page we don't have
			return;
		}
		pages.set(pageIdx, new IntArrayNBT(ids));
		nbt.put(NBT_PAGE_LIST, pages);
		itemStack.setTag(nbt);
	}
	
	private static void setPageSpellID(ItemStack itemStack, int pageIdx, int slot, int spellID) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome) || slot > getSlots(itemStack)) {
			return;
		}
		
		int[] ids = getPageSpellIDs(itemStack, pageIdx);
		//assert(ids.length == getSlots(itemStack) OR upgrade, but we don't expect they'll change
		ids[slot] = spellID;
		
		setPageSpellIDs(itemStack, pageIdx, ids);
	}
	
	private static void clearPage(ItemStack itemStack, int pageIdx) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome)) {
			return;
		}
		
		setPageSpellIDs(itemStack, pageIdx, makeDefaultPage(itemStack));
	}
	
	private static int getPageIndex(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		
		return nbt.getInt(NBT_PAGE_INDEX);
	}
	
	// Returns resultant index, or -1 on no change
	public static int incrementPageIndex(ItemStack itemStack, int amount) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return -1;
		
		CompoundNBT nbt = itemStack.getTag();
		
		int index = nbt.getInt(NBT_PAGE_INDEX);
		int initial = index;
		
		final int pageCount = getPageCount(itemStack);
		if (pageCount == 0)
			return -1;
		
		index = Math.max(0, Math.min(index + amount, pageCount - 1));
		
		nbt.putInt(NBT_PAGE_INDEX, index);
		
		if (initial != index && !NostrumMagica.instance.proxy.isServer()) {
			NostrumMagicaSounds.UI_TICK.play(NostrumMagica.instance.proxy.getPlayer());
			return index;
		} else
			return -1;
		
	}
	
	public static void setPageIndex(ItemStack itemStack, int index) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		final int pageCount = getPageCount(itemStack);
		if (pageCount == 0)
			return;
		
		index = Math.max(0, Math.min(index, pageCount - 1));
		
		CompoundNBT nbt = itemStack.getTag();
		
		nbt.putInt(NBT_PAGE_INDEX, index);
	}
	
	public static int getXP(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 0;
		
			return nbt.getInt(NBT_XP);
	}
	
	public static void setXP(ItemStack itemStack, int xp) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putInt(NBT_XP, xp);
		itemStack.setTag(nbt);
	}
	
	public static int getCapacityBonus(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 0;
		
		return nbt.getInt(NBT_CAPACITY_BONUS);
	}
	
	public static void setCapacityBonus(ItemStack itemStack, int capacity) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putInt(NBT_CAPACITY_BONUS, capacity);
		itemStack.setTag(nbt);
	}
	
	public static int getSlots(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 2;
		
		return nbt.getInt(NBT_SLOTS);
	}
	
	public static void setSlots(ItemStack itemStack, int slots) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putInt(NBT_SLOTS, slots);
		itemStack.setTag(nbt);
	}
	
	public static int getPageBonus(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 0;
		
		return nbt.getInt(NBT_PAGE_BONUS);
	}
	
	public static void setPageBonus(ItemStack itemStack, int pageBonus) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putInt(NBT_PAGE_BONUS, pageBonus);
		itemStack.setTag(nbt);
	}
	
	public static int getLevel(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 1;
		
		int level = nbt.getInt(NBT_LEVEL);
		if (level <= 0) {
			setLevel(itemStack, 1);
			level = 1;
		}
		
		return level;
	}
	
	public static void setLevel(ItemStack itemStack, int level) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putInt(NBT_LEVEL, level);
		itemStack.setTag(nbt);
	}
	
	public static int getModifications(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 0;
		
		return nbt.getInt(NBT_MODIFICATIONS);
	}
	
	public static void setModifications(ItemStack itemStack, int mods) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putInt(NBT_MODIFICATIONS, mods);
		itemStack.setTag(nbt);
	}
	
	private static int genID(ItemStack tome) {
		int id = NostrumMagica.rand.nextInt();
		CompoundNBT nbt = tome.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.putInt(NBT_ID, id);
		return id;
	}
	
	public static int getTomeID(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		int id;
		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			id = genID(itemStack);
		else
			id = nbt.getInt(NBT_ID);
		
		if (id == 0)
			id = genID(itemStack);
		return id;
	}
	
	/**
	 * Retrieves a list of spells stored in the spell tome.
	 * @param itemStack
	 * @return
	 */
	public static List<Spell> getSpellLibrary(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return null;

		List<Spell> list = new LinkedList<>();
		int[] ids = getSpellIDs(itemStack);
		if (ids == null || ids.length == 0)
			return list;
		
		if (!NostrumMagica.instance.proxy.isServer()) {
			sniffIDs(ids);
		}
		
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(id);
			if (spell != null)
				list.add(spell);
		}
		
		return list;
	}
	
	public static @Nonnull Spell[] getSpellsInCurrentPage(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return new Spell[0];
		
		final int slots = getSlots(itemStack);
		final int pageIdx = getPageIndex(itemStack);
		Spell[] spells = new Spell[slots];
		for (int i = 0; i < slots; i++) {
			spells[i] = getSpellInSlot(itemStack, pageIdx, i);
		}
		return spells;
	}
	
	public static @Nullable Spell getSpellInSlot(ItemStack itemStack, int pageIdx, int slot) {
		final int spellID = getPageSpellID(itemStack, pageIdx, slot);
		final @Nullable Spell spell;
		if (spellID == -1) {
			spell = null;
		} else {
			spell = NostrumMagica.instance.getSpellRegistry().lookup(spellID);
		}
		return spell;
	}
	
	public static void setSpellInSlot(ItemStack itemStack, int pageIdx, int slot, @Nullable Spell spell) {
		setPageSpellID(itemStack, pageIdx, slot, (spell == null) ? -1 : spell.getRegistryID());
	}
	
	/**
	 * Scan nested IDs for spells we don't know about and request them
	 * @param tome
	 */
	private static void sniffIDs(int ids[]) {
		
		int id;
		int requests[] = new int[ids.length];
		int requestcount = 0;
		for (int i = 0; i < ids.length; i++) {
			id = ids[i];
			if (NostrumMagica.instance.getSpellRegistry().lookup(id) == null) {
				System.out.println("don't know this one: " + id);
				// Create a temporary spell
				// Request spell from server
				requests[requestcount++] = id;
				getTemp(id);
			}
		}
		
		if (requestcount > 0) {
			NostrumMagica.logger.info("Requesting " + requestcount
				 + " spells from the server...");
			NetworkHandler.sendToServer(
	    			new SpellRequestMessage(requests));
		}
	}
	
	private static Spell getTemp(int id) {
		return Spell.CreateFake("Loading...", id);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BookScreen getScreen(Object userdata) {
		if (userdata == null || !(userdata instanceof ItemStack) || ((ItemStack) userdata).isEmpty())
			return null;
		
		if (!(((ItemStack) userdata).getItem() instanceof SpellTome)) {
			return null;
		}
		
		ItemStack stack = (ItemStack) userdata;
		
		List<IBookPage> pages = new LinkedList<>();
		
		// height 40
		// width 110
		String title = stack.getDisplayName().getString();
		int level = getLevel(stack);
		List<Spell> spells = getSpellLibrary(stack);
		int spellCount = (spells != null && !spells.isEmpty() ? spells.size() : 0);
		int weightCapacity = getCapacity(stack);
		int weightSum = getUsedCapacity(stack);
		int spellPages = getPageCount(stack);
		int xp = getXP(stack);
		int maxxp = LevelCurve.getMaxXP(level);
		int modifications = getModifications(stack);
		pages.add(new TitlePage(title, false));
		pages.add(new LinedTextPage("", "",
				"Level: " + level, "XP: " + xp + "/" + maxxp, "",
				"Spell Pages: " + spellPages,
				"Modifications: " + modifications,
				"",
				spellCount + " Spells",
				weightSum + "/" + weightCapacity + " Weight Capacity"
				)
			);
		
		if (spells != null && spellCount > 0) {
			boolean top = true;
			SpellPreviewPage page = null;
			for (Spell spell : spells) {
				if (top) {
					page = new SpellPreviewPage(stack, spell);
				} else {
					HSplitPage hp = new HSplitPage(page, new SpellPreviewPage(stack, spell));
					pages.add(hp);
				}
				
				top = !top;
			}
			if (!top) {
				// Last one is partial page and didn't get finished.
				HSplitPage hp = new HSplitPage(page, null);
				pages.add(hp);
			}
		}
		
		return new BookScreen("nostrum_tome_" + getTomeID(stack), pages);
	}

	@Override
	public String getLoreKey() {
		return "nostrum_spell_tome";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Tomes";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Spell tomes carry spells to be cast over and over again.", "Casting spells from a spell tome requires reagents and mana.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Spell tomes carry spells to be cast over and over again.", "Casting spells from a spell tome requires reagents and mana.", "Spells must be bound into a Spell Tome in order be to used.", "Bind scrolls into the tome through the Ritual of Binding.");
	}
	
	public static List<SpellTomeEnhancementWrapper> getEnhancements(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTome))
			return null;
		
		return readEnhancements(stack.getTag());
	}
	
	public static void addEnhancement(ItemStack stack, SpellTomeEnhancementWrapper enhancement) {
		List<SpellTomeEnhancementWrapper> enhances = getEnhancements(stack);
		if (enhances == null)
			enhances = new LinkedList<>();
		
		enhances.add(enhancement);
		CompoundNBT tag = stack.getTag();
		if (tag == null)
			tag = new CompoundNBT();
		writeEnhancements(enhances, tag);
		stack.setTag(tag);
	}
	
	public static void applyEnhancements(ItemStack stack, SpellCastSummary summary, LivingEntity caster) {
		List<SpellTomeEnhancementWrapper> list = getEnhancements(stack);
		if (list == null)
			return;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr == null)
			return;
		
		for (SpellTomeEnhancementWrapper wrapper : list) {
			wrapper.getEnhancement().onCast(wrapper.getLevel(),
					summary, caster, attr);
		}
	}
	
	private static void writeEnhancements(List<SpellTomeEnhancementWrapper> enhancements, CompoundNBT nbt) {
		if (enhancements == null || enhancements.isEmpty())
			return;
		
		ListNBT list = new ListNBT();
		for (SpellTomeEnhancementWrapper enhance : enhancements) {
			CompoundNBT tag = new CompoundNBT();
			tag.putString(NBT_ENHANCEMENT_KEY, enhance.getEnhancement().getTitleKey());
			tag.putInt(NBT_ENHANCEMENT_LEVEL, enhance.getLevel());
			list.add(tag);
		}
		
		nbt.put(NBT_ENHANCEMENTS, list);
	}
	
	private static List<SpellTomeEnhancementWrapper> readEnhancements(CompoundNBT nbt) {
		List<SpellTomeEnhancementWrapper> list = new LinkedList<>();
		
		if (nbt != null && nbt.contains(NBT_ENHANCEMENTS, NBT.TAG_LIST)) {
			ListNBT tags = nbt.getList(NBT_ENHANCEMENTS, NBT.TAG_COMPOUND);
			for (int i = 0; i < tags.size(); i++) {
				CompoundNBT tag = tags.getCompound(i);
				String key = tag.getString(NBT_ENHANCEMENT_KEY);
				SpellTomeEnhancement enhance = SpellTomeEnhancement.lookupEnhancement(key);
				if (enhance == null) {
					NostrumMagica.logger.error("Could not find enhancement to match key " + key);
					continue;
				}
				
				list.add(new SpellTomeEnhancementWrapper(enhance, tag.getInt(NBT_ENHANCEMENT_LEVEL)));
			}
		}
		
		return list;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		List<SpellTomeEnhancementWrapper> enhances = getEnhancements(stack);
		if (enhances != null && !enhances.isEmpty()) {
			for (SpellTomeEnhancementWrapper enhance : enhances) {
				tooltip.add(new TranslationTextComponent(enhance.getEnhancement().getNameFormat())
						.append(new StringTextComponent(" " + SpellTomePage.toRoman(enhance.getLevel())))
				);
			}
		}
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (!worldIn.isRemote()) {
			getTomeID(stack); // Prompt ID generation
		}
	}
	
	/**
	 * Does any special effects when casting a spell from a tome
	 * @param tome
	 * @param sp
	 */
	public static void doSpecialCastEffects(ItemStack tome, PlayerEntity sp) {
		if (sp.world.isRemote)
			return;
		
		while (true) {
			int xp = getXP(tome) + 1;
			int level = getLevel(tome);
			int maxxp = LevelCurve.getMaxXP(level);
			if (xp >= maxxp) {
				xp -= maxxp;
				setXP(tome, xp);
				
				doLevelup(tome, sp);
			} else {
				setXP(tome, xp);
				break;
			}
		}
		
		// Could hook up some special effects here
	}
	
	private static void doLevelup(ItemStack tome, PlayerEntity player) {
		player.sendMessage(new TranslationTextComponent("info.tome.levelup", new Object[0]), Util.DUMMY_UUID);
		int mods = getModifications(tome);
		setModifications(tome, ++mods);
		int level = getLevel(tome);
		setLevel(tome, ++level);
	}
	
	public static int getCapacity(ItemStack tome) {
		return LevelCurve.getBaseCapacity(getLevel(tome)) + getCapacityBonus(tome);
	}
	
	public static int getUsedCapacity(ItemStack tome) {
		List<Spell> spells = getSpellLibrary(tome);
		return (spells != null && !spells.isEmpty() ? spells.stream().map(s -> s.getWeight()).collect(Collectors.summingInt(i -> i)) : 0);
	}
	
	public static int getPageCount(ItemStack tome) {
		return LevelCurve.getBasePageCount(getLevel(tome)) + getPageBonus(tome);
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_TOMES;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			ItemStack stack = new ItemStack(this);
			setCapacityBonus(stack, 5);
			setSlots(stack, 1);
			items.add(stack);
			
			stack = new ItemStack(this);
			setCapacityBonus(stack, 10);
			setSlots(stack, 5);
			items.add(stack);
		}
	}
	
	/**
	 * Check whether the tome has room for the provided spell to be bound
	 * @param tome
	 * @return
	 */
	public static boolean hasRoom(ItemStack tome, Spell spell) {
		final int capacity = SpellTome.getCapacity(tome);
		final int used = SpellTome.getUsedCapacity(tome);
		
		return (capacity >= used + spell.getWeight());
	}
	
	public static boolean startBinding(PlayerEntity player, ItemStack tome, ItemStack scroll) {
		if (tome.isEmpty() || scroll.isEmpty())
			return false;
		
		Spell spell = SpellScroll.GetSpell(scroll);
		if (spell == null)
			return false;
		
		if (!hasRoom(tome, spell)) {
			if (!player.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.tome.full"), Util.DUMMY_UUID);
			}
			return false;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		SpellTome.addSpell(tome, spell);
		
		return true;
	}

	@Override
	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack) {
		Spell[] spells = NostrumMagica.getCurrentSpellLoadout(player);
		for (Spell spell : spells) {
			if (spell != null && spell.shouldTrace(player)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public double getTraceRange(World world, PlayerEntity player, ItemStack stack) {
		double largest = 0;
		Spell[] spells = NostrumMagica.getCurrentSpellLoadout(player);
		for (Spell spell : spells) {
			if (spell != null && spell.shouldTrace(player)) {
				largest = Math.max(largest, spell.getTraceRange(player));
			}
		}
		
		return largest;
	}

	@Override
	public void onStartCastFromTool(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		SpellTome.applyEnhancements(stack, summary, caster);
	}

	@Override
	public void onFinishCastFromTool(LivingEntity caster, SpellCastSummary summary, ItemStack stack) {
		if (caster instanceof PlayerEntity) {
			SpellTome.doSpecialCastEffects(stack, (PlayerEntity) caster);
		}
	}

	@Override
	public Spell getSpell(ItemStack stack) {
		// If the last spell a player cast is in this time, return that.
		// Otherwise return first spell on the page they're on.
		@Nullable Spell ret = null;
		@Nullable PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (player != null) {
			@Nullable Spell lastSpell = NostrumMagica.playerListener.getLastSpell(player);
			if (lastSpell != null) {
				List<Spell> library = getSpellLibrary(stack);
				if (library != null && library.stream().anyMatch((s) -> s.getRegistryID() == lastSpell.getRegistryID())) {
					ret = lastSpell;
				}
			}
		}
		
		if (ret == null) {
			Spell[] pageSpells = getSpellsInCurrentPage(stack);
			if (pageSpells != null) {
				for (int i = 0; i < pageSpells.length; i++) {
					if (pageSpells[i] != null) {
						ret = pageSpells[i];
						break;
					}
				}
			}
		}
		
		return ret;
	}
}
