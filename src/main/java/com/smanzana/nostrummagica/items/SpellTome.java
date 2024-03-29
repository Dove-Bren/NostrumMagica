package com.smanzana.nostrummagica.items;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

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
import com.smanzana.nostrummagica.effects.NostrumEffects;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.SeekingBulletTrigger;
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
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class SpellTome extends Item implements GuiBook, ILoreTagged, IRaytraceOverlay {
	
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
			int level, int capacity,
			SpellTomeEnhancementWrapper ... enhancements) {
		return Create(style, level, capacity, Lists.newArrayList(enhancements));
	}
	
	public static ItemStack Create(TomeStyle style,
			int level, int capacity,
			List<SpellTomeEnhancementWrapper> enhancements) {
		ItemStack item = new ItemStack(GetTomeForStyle(style), 1);
		setCapacity(item, capacity);
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
	
	public static ItemStack Create(@Nonnull ItemStack plate, NonNullList<ItemStack> pages) {
		TomeStyle style = ((SpellPlate) plate.getItem()).getStyle();
		List<SpellTomeEnhancementWrapper> enhancements = SpellPlate.getEnhancements(plate);
		
		if (enhancements == null)
			enhancements = new LinkedList<>();
		
		int capacity = SpellPlate.getCapacity(plate);
		
		if (pages != null) {
			for (ItemStack page : pages) {
				if (pages.isEmpty() || !(page.getItem() instanceof SpellTomePage))
					continue;
				
				enhancements.add(new SpellTomeEnhancementWrapper(SpellTomePage.getEnhancement(page),
						SpellTomePage.getLevel(page)));
			}
		}
		
		ItemStack stack = Create(style, 1, capacity, enhancements);
		
//		if (!enhancements.isEmpty()) {
//			CompoundNBT tag = stack.getTag();
//			if (tag == null)
//				tag = new CompoundNBT();
//			
//			writeEnhancements(enhancements, tag);
//				
//			stack.setTag(tag);
//			
//		}
//		
//		setCapacity(stack, capacity);
//		setLevel(stack, 1);
			
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
		
		tags.add(new IntNBT(spell.getRegistryID()));
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
				if (i >= getIndex(itemStack)) {
					incrementIndex(itemStack, -1);
				}
				break;
			}
		}
		
		if (found) {
			nbt.put(NBT_SPELLS, tags);
			itemStack.setTag(nbt);
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
	
	private static int getIndex(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		
		return nbt.getInt(NBT_INDEX);
	}
	
	// Returns resultant index, or -1 on no change
	public static int incrementIndex(ItemStack itemStack, int amount) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return -1;
		
		CompoundNBT nbt = itemStack.getTag();
		
		int index = nbt.getInt(NBT_INDEX);
		int initial = index;
		
		int indices[] = getSpellIDs(itemStack);
		if (indices == null || indices.length == 0)
			return -1;
		
		index = Math.max(0, Math.min(index + amount, indices.length - 1));
		
		nbt.putInt(NBT_INDEX, index);
		
		if (initial != index && !NostrumMagica.instance.proxy.isServer()) {
			NostrumMagicaSounds.UI_TICK.play(NostrumMagica.instance.proxy.getPlayer());
			return index;
		} else
			return -1;
		
	}
	
	public static void setIndex(ItemStack itemStack, int index) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		int indices[] = getSpellIDs(itemStack);
		if (indices == null || indices.length == 0)
			return;
		
		index = Math.max(0, Math.min(index, indices.length - 1));
		
		CompoundNBT nbt = itemStack.getTag();
		
		nbt.putInt(NBT_INDEX, index);
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
	
	public static int getCapacity(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 0;
		
			return nbt.getInt(NBT_CAPACITY);
	}
	
	public static void setCapacity(ItemStack itemStack, int capacity) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putInt(NBT_CAPACITY, capacity);
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
	
	public static UUID getPlayerID(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return null;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return null;
		
		try {
			return UUID.fromString(nbt.getString(NBT_PLAYER));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getPlayerName(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return null;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return null;
		return nbt.getString(NBT_PLAYER_NAME);
	}
	
	public static void setPlayer(ItemStack itemStack, PlayerEntity player) {
		setPlayer(itemStack, player.getDisplayName().getFormattedText(), player.getUniqueID());
	}
	
	public static void setPlayer(ItemStack itemStack, String name, UUID id) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putString(NBT_PLAYER, id.toString());
		nbt.putString(NBT_PLAYER_NAME, name);
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
	
	public static long getBondTime(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return 0;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			return 0;
		return nbt.getLong(NBT_FINISH_TIME);
	}
	
	public static void setBondTime(ItemStack itemStack, long time) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return;

		CompoundNBT nbt = itemStack.getTag();
		if (nbt == null)
			nbt = new CompoundNBT();
		nbt.putLong(NBT_FINISH_TIME, time);
		itemStack.setTag(nbt);
	}
	
	/**
	 * Retrieves a list of spells stored in the spell tome.
	 * The active 'currently selected' spell is always first in the list.
	 * @param itemStack
	 * @return
	 */
	public static List<Spell> getSpells(ItemStack itemStack) {
		if (itemStack.isEmpty() || !(itemStack.getItem() instanceof SpellTome))
			return null;

		List<Spell> list = new LinkedList<>();
		int[] ids = getSpellIDs(itemStack);
		if (ids == null || ids.length == 0)
			return list;
		
		if (!NostrumMagica.instance.proxy.isServer()) {
			sniffIDs(ids);
		}
		
		int index = getIndex(itemStack);
		if (index < 0) {
			index = 0;
		} else if (index >= ids.length) {
			index = ids.length - 1;
		}
		
		int id = ids[index];
		Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(id);
		
		if (spell != null)
			list.add(spell);
		
		for (int i = 0; i < ids.length; i++) {
			if (i == index)
				continue;
			
			id = ids[i];
			spell = NostrumMagica.instance.getSpellRegistry().lookup(id);
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
		return Spell.CreateInternal("Loading...", id);
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
		String ownerName = getPlayerName(stack);
		if (ownerName == null || ownerName.isEmpty()) {
			// Tome has not been bound yet
			pages.add(new HSplitPage(
					new TitlePage("Unbonded Spelltome", false),
					new PlainTextPage("  This tome is still being bonded.")
					));
		} else {
			String title = stack.getDisplayName().getFormattedText();
			int level = getLevel(stack);
			int maxMana = getMaxMana(stack);
			List<Spell> spells = getSpells(stack);
			int spellCount = (spells != null && !spells.isEmpty() ? spells.size() : 0);
			int capacity = getCapacity(stack);
			int xp = getXP(stack);
			int maxxp = LevelCurve.getMaxXP(level);
			int modifications = getModifications(stack);
			int id = getTomeID(stack);
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.instance.proxy.getPlayer());
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
						.appendSibling(new StringTextComponent(" " + SpellTomePage.toRoman(enhance.getLevel())))
				);
			}
		}
		
		String name = getPlayerName(stack);
		if (name != null && !name.isEmpty()) {
			tooltip.add(new StringTextComponent(""));
			tooltip.add(new StringTextComponent("Bound to " + name).applyTextStyle(TextFormatting.DARK_RED));
		}
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		
		if (worldIn.isRemote)
			return;
		
		UUID owner = getPlayerID(stack);
		if (owner != null)
			return;
		
		if (!(entityIn instanceof PlayerEntity) || ((PlayerEntity) entityIn).isCreative()) {
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
			bond(stack, worldIn, (PlayerEntity) entityIn);
		}
		
		
	}
	
	@Override
	public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
		boolean ret = super.onDroppedByPlayer(item, player);
		if (ret)
			setBondTime(item, 0);
		return ret;
	}
	
	public static long genBondTime(ItemStack tome) {
		// Can be cool later. For now, just do 10 minutes
		return 1000L * 10;//60L * 10L;
	}
	
	public static void bond(ItemStack tome, World world, PlayerEntity player) {
		setPlayer(tome, player);
		NostrumMagicaSounds.SHIELD_APPLY.play(player);
		if (!world.isRemote) {
			player.sendMessage(new TranslationTextComponent("info.tome.bond"));
		}
	}
	
	public static boolean isOwner(ItemStack tome, PlayerEntity player) {
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
	public static void doSpecialCastEffects(ItemStack tome, PlayerEntity sp) {
		if (sp.world.isRemote)
			return;
		
		boolean isOwner = isOwner(tome, sp);
		if (!isOwner) {
			UUID id = getPlayerID(tome);
			if (id == null)
				return;
			PlayerEntity player = 
					sp.world.getServer().getPlayerList().getPlayerByUUID(id);
			if (player != null && player.isAlive()) {
				switch (NostrumMagica.rand.nextInt(4)) {
				case 0:
					// Poison them
					player.addPotionEffect(new EffectInstance(
							Effects.POISON,
							20 * 10,
							0
							));
					player.addPotionEffect(new EffectInstance(
							Effects.NAUSEA,
							20 * 10,
							1
							));
					break;
				case 1:
					// Frostbite
					player.addPotionEffect(new EffectInstance(
							NostrumEffects.frostbite,
							20 * 10,
							0
							));
					player.addPotionEffect(new EffectInstance(
							Effects.NAUSEA,
							20 * 10,
							1
							));
					break;
				case 2:
					// Slow
					player.addPotionEffect(new EffectInstance(
							Effects.SLOWNESS,
							20 * 10,
							1
							));
					player.addPotionEffect(new EffectInstance(
							Effects.NAUSEA,
							20 * 10,
							1
							));
					break;
				case 3:
				default:
					// Blindness
					player.addPotionEffect(new EffectInstance(
							Effects.BLINDNESS,
							20 * 10,
							0
							));
					player.addPotionEffect(new EffectInstance(
							Effects.NAUSEA,
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
	
	private static void doLevelup(ItemStack tome, PlayerEntity player) {
		player.sendMessage(new TranslationTextComponent("info.tome.levelup", new Object[0]));
		int mods = getModifications(tome);
		setModifications(tome, ++mods);
		int level = getLevel(tome);
		setLevel(tome, ++level);
	}
	
	public static int getMaxMana(ItemStack tome) {
		return LevelCurve.getMaxMana(getLevel(tome));
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_TOMES;
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			ItemStack stack = new ItemStack(this);
			setCapacity(stack, 5);
			items.add(stack);
			
			stack = new ItemStack(this);
			setCapacity(stack, 10);
			items.add(stack);
		}
	}
	
	/**
	 * Check whether the tome has room for more spell scrolls to be bound
	 * @param tome
	 * @return
	 */
	public static boolean hasRoom(ItemStack tome) {
		int capacity = SpellTome.getCapacity(tome);
		List<Spell> spells = SpellTome.getSpells(tome);
		int taken = spells == null ? 0 : spells.size();
		return taken < capacity;
	}
	
	public static boolean startBinding(PlayerEntity player, ItemStack tome, ItemStack scroll, boolean quick) {
		if (tome.isEmpty() || scroll.isEmpty())
			return false;
		
		Spell spell = SpellScroll.getSpell(scroll);
		if (spell == null)
			return false;
		
		if (!SpellTome.isOwner(tome, player)) {
			if (!player.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.tome.noowner"));
				
			}
			return false;
		}
		
		if (!hasRoom(tome)) {
			if (!player.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.tome.full"));
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
		} else if (!player.world.isRemote) {
				player.sendMessage(new TranslationTextComponent("info.tome.bind_start", new Object[] {spell.getName(), compName}));
		}
		
		return true;
	}

	@Override
	public boolean shouldTrace(World world, PlayerEntity player, ItemStack stack) {
		Spell spell = NostrumMagica.getCurrentSpell(player);
		if (spell != null && spell.getSpellParts() != null && spell.getSpellParts().get(0).isTrigger()) {
			SpellTrigger trigger = spell.getSpellParts().get(0).getTrigger();
			return trigger instanceof SeekingBulletTrigger;
		}
		// TODO Auto-generated method stub
		return false;
	}
}
