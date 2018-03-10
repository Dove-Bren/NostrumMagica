package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.HSplitPage;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.SpellPreviewPage;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellTome extends Item implements GuiBook, ILoreTagged {
	
	public static final class EnhancementWrapper {
		protected SpellTomeEnhancement enhancement;
		protected int level;
		
		public EnhancementWrapper(SpellTomeEnhancement enhancement, int level) {
			this.enhancement = enhancement;
			this.level = level;
		}

		public SpellTomeEnhancement getEnhancement() {
			return enhancement;
		}

		public int getLevel() {
			return level;
		}
	}

	private static final String NBT_SPELLS = "nostrum_spells";
	private static final String NBT_INDEX = "spell_index";
	private static final String NBT_ENHANCEMENTS = "tome_enhancements";
	private static final String NBT_ENHANCEMENT_KEY = "enhancement_key";
	private static final String NBT_ENHANCEMENT_LEVEL = "enhancement_level";
	private static SpellTome instance = null;
	
	public static SpellTome instance() {
		if (instance == null)
			instance = new SpellTome();
		
		return instance;
	}
	
	public static final String id = "spellTome";
	public static final String textureName = "tome1";
	
	private SpellTome() {
		super();
		this.setUnlocalizedName(id);
		// this.setCreativeTab(NostrumMagica.creativeTab);
		// Is icon. Handled special in NostrumMagica
		this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        // TODO display GUI with stats?
		
		if (worldIn.isRemote)
			NostrumMagica.proxy.openBook(playerIn, this, itemStackIn);
		
		return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }
	
	public static void addSpell(ItemStack itemStack, Spell spell) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		NBTTagList tags = nbt.getTagList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null)
			tags = new NBTTagList();
		
		tags.appendTag(new NBTTagInt(spell.getRegistryID()));
		nbt.setTag(NBT_SPELLS, tags);
		
		itemStack.setTagCompound(nbt);
	}
	
	private static int[] getSpellIDs(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return null;
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		if (nbt == null)
			return null;
		
		NBTTagList tags = nbt.getTagList(NBT_SPELLS, NBT.TAG_INT);
		
		if (tags == null || tags.tagCount() == 0)
			return null;
		
		int ids[] = new int[tags.tagCount()];
		
		for (int i = 0; i < tags.tagCount(); i++) {
			ids[i] = tags.getIntAt(i);
		}

		return ids;
	}
	
	private static int getIndex(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		NBTTagCompound nbt = itemStack.getTagCompound();
		
		return nbt.getInteger(NBT_INDEX);
	}
	
	// Returns resultant index, or -1 on no change
	public static int incrementIndex(ItemStack itemStack, int amount) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return -1;

		// TODO as soon as you get a refresh from the server, you lose your index:
		// The server's version didn't have its index incremented.
		// Should we send a packet each time...? D:
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		int index = nbt.getInteger(NBT_INDEX);
		int initial = index;
		
		int indices[] = getSpellIDs(itemStack);
		index = Math.max(0, Math.min(index + amount, indices.length - 1));
		
		nbt.setInteger(NBT_INDEX, index);
		
		if (initial != index && !NostrumMagica.proxy.isServer()) {
			NostrumMagicaSounds.UI_TICK.play(NostrumMagica.proxy.getPlayer());
			return index;
		} else
			return -1;
		
	}
	
	public static void setIndex(ItemStack itemStack, int index) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;

		int indices[] = getSpellIDs(itemStack);
		index = Math.max(0, Math.min(index, indices.length - 1));
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		nbt.setInteger(NBT_INDEX, index);
	}
	
	/**
	 * Retrieves a list of spells stored in the spell tome.
	 * The active 'currently selected' spell is always first in the list.
	 * @param itemStack
	 * @return
	 */
	public static List<Spell> getSpells(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return null;

		List<Spell> list = new LinkedList<>();
		int[] ids = getSpellIDs(itemStack);
		if (ids == null || ids.length == 0)
			return list;
		
		if (!NostrumMagica.proxy.isServer()) {
			sniffIDs(ids);
		}
		
		int index = getIndex(itemStack);
		if (ids.length < index)
			index = 0;
		
		int id = ids[index];
		Spell spell = NostrumMagica.spellRegistry.lookup(id);
		
		if (spell != null)
			list.add(spell);
		
		for (int i = 0; i < ids.length; i++) {
			if (i == index)
				continue;
			
			id = ids[i];
			spell = NostrumMagica.spellRegistry.lookup(id);
			if (spell != null)
				list.add(spell);
		}
		
		return list;
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
			if (NostrumMagica.spellRegistry.lookup(id) == null) {
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
			NetworkHandler.getSyncChannel().sendToServer(
	    			new SpellRequestMessage(requests));
		}
	}
	
	private static Spell getTemp(int id) {
		return Spell.CreateInternal("Loading...", id);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BookScreen getScreen(Object userdata) {
		if (userdata == null || !(userdata instanceof ItemStack))
			return null;
		
		if (!(((ItemStack) userdata).getItem() instanceof SpellTome)) {
			return null;
		}
		
		ItemStack stack = (ItemStack) userdata;
		
		List<IBookPage> pages = new LinkedList<>();
		
		// height 40
		// width 110
		boolean top = true;
		SpellPreviewPage page = null;
		for (Spell spell : getSpells((stack))) {
			if (top) {
				page = new SpellPreviewPage(spell);
			} else {
				HSplitPage hp = new HSplitPage(page, new SpellPreviewPage(spell));
				pages.add(hp);
			}
			
			top = !top;
		}
		if (!top) {
			// Last one is partial page and didn't get finished.
			HSplitPage hp = new HSplitPage(page, null);
			pages.add(hp);
		}
		
		return new BookScreen(pages);
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
	
	public static List<EnhancementWrapper> getEnhancements(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof SpellTome))
			return null;
		
		return readEnhancements(stack.getTagCompound());
	}
	
	public static void addEnhancement(ItemStack stack, EnhancementWrapper enhancement) {
		List<EnhancementWrapper> enhances = getEnhancements(stack);
		if (enhances == null)
			enhances = new LinkedList<>();
		
		enhances.add(enhancement);
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null)
			tag = new NBTTagCompound();
		writeEnhancements(enhances, tag);
		stack.setTagCompound(tag);
	}
	
	public static void applyEnhancements(ItemStack stack, SpellCastSummary summary, EntityLiving caster) {
		List<EnhancementWrapper> list = getEnhancements(stack);
		if (list == null)
			return;
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		if (attr == null)
			return;
		
		for (EnhancementWrapper wrapper : list) {
			wrapper.enhancement.onCast(wrapper.level,
					summary, caster, attr);
		}
	}
	
	public static ItemStack createTome(ItemStack pages[]) {
		ItemStack stack = new ItemStack(instance());
		List<EnhancementWrapper> enhancements = new LinkedList<>();
		
		int len = (pages == null ? 0 : pages.length);
		for (int i = 0; i < len; i++) {
			if (pages[i] == null || !(pages[i].getItem() instanceof SpellTomePage))
				continue;
			
			enhancements.add(new EnhancementWrapper(SpellTomePage.getEnhancement(pages[i]),
					SpellTomePage.getLevel(pages[i])));
		}
		
		if (!enhancements.isEmpty()) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null)
				tag = new NBTTagCompound();
			
			writeEnhancements(enhancements, tag);
				
			stack.setTagCompound(tag);
			
		}
			
		return stack;
	}
	
	private static void writeEnhancements(List<EnhancementWrapper> enhancements, NBTTagCompound nbt) {
		if (enhancements == null || enhancements.isEmpty())
			return;
		
		NBTTagList list = new NBTTagList();
		for (EnhancementWrapper enhance : enhancements) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString(NBT_ENHANCEMENT_KEY, enhance.enhancement.getTitleKey());
			tag.setInteger(NBT_ENHANCEMENT_LEVEL, enhance.level);
			list.appendTag(tag);
		}
		
		nbt.setTag(NBT_ENHANCEMENTS, list);
	}
	
	private static List<EnhancementWrapper> readEnhancements(NBTTagCompound nbt) {
		List<EnhancementWrapper> list = new LinkedList<>();
		
		if (nbt != null && nbt.hasKey(NBT_ENHANCEMENTS, NBT.TAG_LIST)) {
			NBTTagList tags = nbt.getTagList(NBT_ENHANCEMENTS, NBT.TAG_COMPOUND);
			for (int i = 0; i < tags.tagCount(); i++) {
				NBTTagCompound tag = tags.getCompoundTagAt(i);
				String key = tag.getString(NBT_ENHANCEMENT_KEY);
				SpellTomeEnhancement enhance = SpellTomeEnhancement.lookupEnhancement(key);
				if (enhance == null) {
					NostrumMagica.logger.error("Could not find enhancement to match key " + key);
					continue;
				}
				
				list.add(new EnhancementWrapper(enhance, tag.getInteger(NBT_ENHANCEMENT_LEVEL)));
			}
		}
		
		return list;
	}
}
