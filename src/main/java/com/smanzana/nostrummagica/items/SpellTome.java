package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.GuiBook;
import com.smanzana.nostrummagica.client.gui.book.BookScreen;
import com.smanzana.nostrummagica.client.gui.book.HSplitPage;
import com.smanzana.nostrummagica.client.gui.book.IBookPage;
import com.smanzana.nostrummagica.client.gui.book.LinedTextPage;
import com.smanzana.nostrummagica.client.gui.book.PlainTextPage;
import com.smanzana.nostrummagica.client.gui.book.SpellPreviewPage;
import com.smanzana.nostrummagica.client.gui.book.TitlePage;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.potions.FrostbitePotion;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellTome extends Item implements GuiBook, ILoreTagged {
	
	private static class LevelCurve {
		
		
		public static int getMaxMana(int level) {
			switch (level) {
			case 1:
			default:
				return 100;
			case 2:
				return 200;
			case 3:
				return 500;
			case 4:
				return 1000;
			case 5:
				return 5000;
			case 6:
				return 10000;
			}
		}
		
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
	}

	private static final String NBT_SPELLS = "nostrum_spells";
	private static final String NBT_INDEX = "spell_index";
	private static final String NBT_ENHANCEMENTS = "tome_enhancements";
	private static final String NBT_ENHANCEMENT_KEY = "enhancement_key";
	private static final String NBT_ENHANCEMENT_LEVEL = "enhancement_level";
	private static final String NBT_PLAYER = "tome_player";
	private static final String NBT_PLAYER_NAME = "tome_player_name";
	private static final String NBT_FINISH_TIME = "tome_finish_time";
	private static final String NBT_LEVEL = "tome_level";
	private static final String NBT_XP = "tome_xp";
	private static final String NBT_MODIFICATIONS = "tome_mods";
	private static final String NBT_CAPACITY = "tome_capacity";
	private static final String NBT_ID = "tome_id";
	private static SpellTome instance = null;
	
	public static SpellTome instance() {
		if (instance == null)
			instance = new SpellTome();
		
		return instance;
	}
	
	public static final String id = "spelltome";
	public static final String textureName = "tome1";
	public static final int MAX_TOME_COUNT = 7;
	
	private SpellTome() {
		super();
		this.setUnlocalizedName(id);
		// this.setCreativeTab(NostrumMagica.creativeTab);
		// Is icon. Handled special in NostrumMagica
		this.setMaxStackSize(1);
	}
	
	public static ItemStack getItemstack(int type,
			int capacity,
			SpellTomeEnhancementWrapper ... enhancements) {
		return getItemstack(type, capacity, Lists.newArrayList(enhancements));
	}
	
	public static ItemStack getItemstack(int type,
			int capacity,
			List<SpellTomeEnhancementWrapper> enhancements) {
		ItemStack item = new ItemStack(instance(), 1, type) ;
		setCapacity(item, capacity);
		if (enhancements != null && !enhancements.isEmpty()) {
			NBTTagCompound tag = item.getTagCompound();
			if (tag == null)
				tag = new NBTTagCompound();
			
			writeEnhancements(enhancements, tag);
				
			item.setTagCompound(tag);
			
		}
		
		return item;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (worldIn.isRemote)
			NostrumMagica.proxy.openBook(playerIn, this, itemStackIn);
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
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
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		int index = nbt.getInteger(NBT_INDEX);
		int initial = index;
		
		int indices[] = getSpellIDs(itemStack);
		if (indices == null || indices.length == 0)
			return -1;
		
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
		if (indices == null || indices.length == 0)
			return;
		
		index = Math.max(0, Math.min(index, indices.length - 1));
		
		NBTTagCompound nbt = itemStack.getTagCompound();
		
		nbt.setInteger(NBT_INDEX, index);
	}
	
	public static int getXP(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return 0;
		
			return nbt.getInteger(NBT_XP);
	}
	
	public static void setXP(ItemStack itemStack, int xp) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		nbt.setInteger(NBT_XP, xp);
		itemStack.setTagCompound(nbt);
	}
	
	public static int getCapacity(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return 0;
		
			return nbt.getInteger(NBT_CAPACITY);
	}
	
	public static void setCapacity(ItemStack itemStack, int capacity) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		nbt.setInteger(NBT_CAPACITY, capacity);
		itemStack.setTagCompound(nbt);
	}
	
	public static int getLevel(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return 1;
		
		int level = nbt.getInteger(NBT_LEVEL);
		if (level <= 0) {
			setLevel(itemStack, 1);
			level = 1;
		}
		
		return level;
	}
	
	public static void setLevel(ItemStack itemStack, int level) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		nbt.setInteger(NBT_LEVEL, level);
		itemStack.setTagCompound(nbt);
	}
	
	public static int getModifications(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return 0;
		
		return nbt.getInteger(NBT_MODIFICATIONS);
	}
	
	public static void setModifications(ItemStack itemStack, int mods) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		nbt.setInteger(NBT_MODIFICATIONS, mods);
		itemStack.setTagCompound(nbt);
	}
	
	public static UUID getPlayerID(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return null;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return null;
		
		try {
			return UUID.fromString(nbt.getString(NBT_PLAYER));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getPlayerName(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return null;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return null;
		return nbt.getString(NBT_PLAYER_NAME);
	}
	
	public static void setPlayer(ItemStack itemStack, EntityPlayer player) {
		setPlayer(itemStack, player.getDisplayNameString(), player.getUniqueID());
	}
	
	public static void setPlayer(ItemStack itemStack, String name, UUID id) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		nbt.setString(NBT_PLAYER, id.toString());
		nbt.setString(NBT_PLAYER_NAME, name);
		itemStack.setTagCompound(nbt);
	}
	
	private static int genID(ItemStack tome) {
		int id = NostrumMagica.rand.nextInt();
		NBTTagCompound nbt = tome.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setInteger(NBT_ID, id);
		return id;
	}
	
	public static int getTomeID(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		int id;
		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			id = genID(itemStack);
		else
			id = nbt.getInteger(NBT_ID);
		
		if (id == 0)
			id = genID(itemStack);
		return id;
	}
	
	public static long getBondTime(ItemStack itemStack) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			return 0;
		return nbt.getLong(NBT_FINISH_TIME);
	}
	
	public static void setBondTime(ItemStack itemStack, long time) {
		if (itemStack == null || !(itemStack.getItem() instanceof SpellTome))
			return;

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt == null)
			nbt = new NBTTagCompound();
		nbt.setLong(NBT_FINISH_TIME, time);
		itemStack.setTagCompound(nbt);
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
		Spell spell = NostrumMagica.getSpellRegistry().lookup(id);
		
		if (spell != null)
			list.add(spell);
		
		for (int i = 0; i < ids.length; i++) {
			if (i == index)
				continue;
			
			id = ids[i];
			spell = NostrumMagica.getSpellRegistry().lookup(id);
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
			if (NostrumMagica.getSpellRegistry().lookup(id) == null) {
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
		String ownerName = getPlayerName(stack);
		if (ownerName == null || ownerName.isEmpty()) {
			// Tome has not been bound yet
			pages.add(new HSplitPage(
					new TitlePage("Unbonded Spelltome", false),
					new PlainTextPage("  This tome is still being bonded.")
					));
		} else {
			String title = stack.getDisplayName();
			int level = getLevel(stack);
			int maxMana = getMaxMana(stack);
			List<Spell> spells = getSpells(stack);
			int spellCount = (spells != null && !spells.isEmpty() ? spells.size() : 0);
			int capacity = getCapacity(stack);
			int xp = getXP(stack);
			int maxxp = LevelCurve.getMaxXP(level);
			int modifications = getModifications(stack);
			int id = getTomeID(stack);
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.proxy.getPlayer());
			SpellComponentWrapper comp = (attr.isBinding() && id == attr.getBindingID()) ? attr.getBindingComponent() : null;
			String bindingName = (comp == null) ? null : attr.getBindingSpell().getName();
			String compname = (comp == null) ? null :
				(comp.isAlteration() ? comp.getAlteration().getName() :
				(comp.isElement() ? comp.getElement().getName() :
				(comp.isShape() ? comp.getShape().getDisplayName() :
				(comp.getTrigger().getDisplayName()))));
			pages.add(new TitlePage(title, false));
			pages.add(new LinedTextPage("", "",
					"Level: " + level, "XP: " + xp + "/" + maxxp, "",
					"Max Mana: " + maxMana,
					"Modifications: " + modifications,
					"",
					spellCount + "/" + capacity + " Spells",
					"",
					(comp != null ? "Binding spell " + bindingName : ""),
					(comp != null ? "Seek a shrine of " + compname : "")));
			
			if (spells != null && spellCount > 0) {
				boolean top = true;
				SpellPreviewPage page = null;
				for (Spell spell : spells) {
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
		if (stack == null || !(stack.getItem() instanceof SpellTome))
			return null;
		
		return readEnhancements(stack.getTagCompound());
	}
	
	public static void addEnhancement(ItemStack stack, SpellTomeEnhancementWrapper enhancement) {
		List<SpellTomeEnhancementWrapper> enhances = getEnhancements(stack);
		if (enhances == null)
			enhances = new LinkedList<>();
		
		enhances.add(enhancement);
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null)
			tag = new NBTTagCompound();
		writeEnhancements(enhances, tag);
		stack.setTagCompound(tag);
	}
	
	public static void applyEnhancements(ItemStack stack, SpellCastSummary summary, EntityLivingBase caster) {
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
	
	public static ItemStack createTome(ItemStack plate, ItemStack pages[]) {
		ItemStack stack = new ItemStack(instance(), 1, plate.getMetadata());
		List<SpellTomeEnhancementWrapper> enhancements = SpellPlate.getEnhancements(plate);
		
		if (enhancements == null)
			enhancements = new LinkedList<>();
		
		int capacity = SpellPlate.getCapacity(plate);
		
		int len = (pages == null ? 0 : pages.length);
		for (int i = 0; i < len; i++) {
			if (pages == null || pages[i] == null || !(pages[i].getItem() instanceof SpellTomePage))
				continue;
			
			enhancements.add(new SpellTomeEnhancementWrapper(SpellTomePage.getEnhancement(pages[i]),
					SpellTomePage.getLevel(pages[i])));
		}
		
		if (!enhancements.isEmpty()) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag == null)
				tag = new NBTTagCompound();
			
			writeEnhancements(enhancements, tag);
				
			stack.setTagCompound(tag);
			
		}
		
		setCapacity(stack, capacity);
		setLevel(stack, 1);
			
		return stack;
	}
	
	private static void writeEnhancements(List<SpellTomeEnhancementWrapper> enhancements, NBTTagCompound nbt) {
		if (enhancements == null || enhancements.isEmpty())
			return;
		
		NBTTagList list = new NBTTagList();
		for (SpellTomeEnhancementWrapper enhance : enhancements) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString(NBT_ENHANCEMENT_KEY, enhance.getEnhancement().getTitleKey());
			tag.setInteger(NBT_ENHANCEMENT_LEVEL, enhance.getLevel());
			list.appendTag(tag);
		}
		
		nbt.setTag(NBT_ENHANCEMENTS, list);
	}
	
	private static List<SpellTomeEnhancementWrapper> readEnhancements(NBTTagCompound nbt) {
		List<SpellTomeEnhancementWrapper> list = new LinkedList<>();
		
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
				
				list.add(new SpellTomeEnhancementWrapper(enhance, tag.getInteger(NBT_ENHANCEMENT_LEVEL)));
			}
		}
		
		return list;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		List<SpellTomeEnhancementWrapper> enhances = getEnhancements(stack);
		if (enhances != null && !enhances.isEmpty()) {
			for (SpellTomeEnhancementWrapper enhance : enhances) {
				tooltip.add(I18n.format(enhance.getEnhancement().getNameFormat(), new Object[0])
						+ " " + SpellTomePage.toRoman(enhance.getLevel()));
			}
		}
		
		String name = getPlayerName(stack);
		if (name != null && !name.isEmpty()) {
			tooltip.add("");
			tooltip.add(TextFormatting.DARK_RED + "Bound to " + name + TextFormatting.RESET);
		}
	}
	
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		
		if (worldIn.isRemote)
			return;
		
		UUID owner = getPlayerID(stack);
		if (owner != null)
			return;
		
		if (!(entityIn instanceof EntityPlayer) || ((EntityPlayer) entityIn).isCreative()) {
			return;
		}
		
		long time = getBondTime(stack);
		if (time == 0) {
			setBondTime(stack, System.currentTimeMillis() + SpellTome.genBondTime(stack));
			return;
		}
		
		long current = System.currentTimeMillis();
		if (current >= time) {
			// Bond!
			bond(stack, worldIn, (EntityPlayer) entityIn);
		}
		
		
	}
	
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		boolean ret = super.onDroppedByPlayer(item, player);
		if (ret)
			setBondTime(item, 0);
		return ret;
	}
	
	public static long genBondTime(ItemStack tome) {
		// Can be cool later. For now, just do 10 minutes
		return 1000L * 10;//60L * 10L;
	}
	
	public static void bond(ItemStack tome, World world, EntityPlayer player) {
		setPlayer(tome, player);
		NostrumMagicaSounds.SHIELD_APPLY.play(player);
		if (!world.isRemote) {
			player.addChatComponentMessage(new TextComponentTranslation("info.tome.bond"));
		}
	}
	
	public static boolean isOwner(ItemStack tome, EntityPlayer player) {
		UUID owner = getPlayerID(tome);
		if (owner == null)
			return false;
		
		return owner.equals(player.getUniqueID());
	}

	/**
	 * Does any special effects when casting a spell from a tome
	 * @param tome
	 * @param sp
	 */
	public static void doSpecialCastEffects(ItemStack tome, EntityPlayer sp) {
		if (sp.worldObj.isRemote)
			return;
		
		boolean isOwner = isOwner(tome, sp);
		if (!isOwner) {
			UUID id = getPlayerID(tome);
			if (id == null)
				return;
			EntityPlayer player = 
					sp.worldObj.getMinecraftServer().getPlayerList().getPlayerByUUID(id);
			if (player != null && !player.isDead) {
				switch (NostrumMagica.rand.nextInt(4)) {
				case 0:
					// Poison them
					player.addPotionEffect(new PotionEffect(
							Potion.getPotionFromResourceLocation("poison"),
							20 * 10,
							0
							));
					player.addPotionEffect(new PotionEffect(
							Potion.getPotionFromResourceLocation("nausea"),
							20 * 10,
							1
							));
					break;
				case 1:
					// Frostbite
					player.addPotionEffect(new PotionEffect(
							FrostbitePotion.instance(),
							20 * 10,
							0
							));
					player.addPotionEffect(new PotionEffect(
							Potion.getPotionFromResourceLocation("nausea"),
							20 * 10,
							1
							));
					break;
				case 2:
					// Slow
					player.addPotionEffect(new PotionEffect(
							Potion.getPotionFromResourceLocation("slowness"),
							20 * 10,
							1
							));
					player.addPotionEffect(new PotionEffect(
							Potion.getPotionFromResourceLocation("nausea"),
							20 * 10,
							1
							));
					break;
				case 3:
				default:
					// Blindness
					player.addPotionEffect(new PotionEffect(
							Potion.getPotionFromResourceLocation("blindness"),
							20 * 10,
							0
							));
					player.addPotionEffect(new PotionEffect(
							Potion.getPotionFromResourceLocation("nausea"),
							20 * 10,
							1
							));
					break;
				}
			}
		} else {
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
		}
		
		// Could hook up some special effects here
	}
	
	private static void doLevelup(ItemStack tome, EntityPlayer player) {
		player.addChatComponentMessage(new TextComponentTranslation("info.tome.levelup", new Object[0]));
		int mods = getModifications(tome);
		setModifications(tome, ++mods);
		int level = getLevel(tome);
		setLevel(tome, ++level);
	}
	
	public static int getMaxMana(ItemStack tome) {
		return LevelCurve.getMaxMana(getLevel(tome));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (int i = 0; i < SpellTome.MAX_TOME_COUNT; i++) {
			subItems.add(getItemstack(i, 10));
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_TOMES;
	}
	
	public static boolean startBinding(EntityPlayer player, ItemStack tome, ItemStack scroll, boolean quick) {
		if (tome == null || scroll == null)
			return false;
		
		Spell spell = SpellScroll.getSpell(scroll);
		if (spell == null)
			return false;
		
		if (!SpellTome.isOwner(tome, player)) {
			if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new TextComponentTranslation("info.tome.noowner"));
				
			}
			return false;
		}
		
		int capacity = SpellTome.getCapacity(tome);
		List<Spell> spells = SpellTome.getSpells(tome);
		int taken = spells == null ? 0 : spells.size();
		if (taken >= capacity) {
			if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new TextComponentTranslation("info.tome.full"));
			}
			return false;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return false;
		
		SpellComponentWrapper comp = spell.getRandomComponent();
		if (comp == null)
			return false;
		
		while (comp.isAlteration()) {
			comp = spell.getRandomComponent();
		}
		
		String compName;
		if (comp.isElement())
			compName = comp.getElement().getName();
		else if (comp.isTrigger())
			compName = comp.getTrigger().getDisplayName();
		else if (comp.isShape())
			compName = comp.getShape().getDisplayName();
		else
			compName = "Physic";
		
		attr.startBinding(spell, comp, SpellTome.getTomeID(tome));
		if (quick) {
			attr.completeBinding(tome);
		} else if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new TextComponentTranslation("info.tome.bind_start", new Object[] {spell.getName(), compName}));
		}
		
		return true;
	}
}
