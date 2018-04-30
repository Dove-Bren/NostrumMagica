package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.world.NostrumShrineGenerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

// Queued the next shrine to be a certain type
public class ShrineSeekingGem extends Item implements ILoreTagged {

	private static final String NBT_COMP = "type";
	private static ShrineSeekingGem instance = null;
	
	public static ShrineSeekingGem instance() {
		if (instance == null)
			instance = new ShrineSeekingGem();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addRecipe(new SeekingGemRecipe());
	}
	
	public static final String id = "seeking_gem";
	
	private ShrineSeekingGem() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(8);
	}
	
	public static SpellComponentWrapper getComponent(ItemStack stack) {
		if (!stack.hasTagCompound())
			return null;
		
		NBTTagCompound nbt = stack.getTagCompound();
		
		if (!nbt.hasKey(NBT_COMP, NBT.TAG_STRING))
			return null;
		
		return SpellComponentWrapper.fromKeyString(nbt.getString(NBT_COMP));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

		if (stack == null)
			return;
		
		SpellComponentWrapper component = getComponent(stack);
		
		if (component == null)
			return;
		
		if (component.isElement()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getElement().getName() + TextFormatting.RESET);
		} else if (component.isAlteration()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getAlteration().getName() + TextFormatting.RESET);
		} else if (component.isTrigger()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getTrigger().getDisplayName() + TextFormatting.RESET);
		} else if (component.isShape()) {
			tooltip.add(TextFormatting.DARK_BLUE + component.getShape().getDisplayName() + TextFormatting.RESET);
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!itemStackIn.hasTagCompound())
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		SpellComponentWrapper wrapper = getComponent(itemStackIn);
		if (wrapper == null)
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		
		NostrumShrineGenerator.enqueueShrineRequest(wrapper);
		itemStackIn.stackSize--;
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		
    }
	
	public static void setComponent(ItemStack stack, SpellComponentWrapper component) {
		if (stack == null)
			return;
		
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		NBTTagCompound nbt = stack.getTagCompound();
		nbt.setString(NBT_COMP, component.getKeyString());
	}
	
	public static ItemStack getItemstack(SpellComponentWrapper wrapper) {
		return getItemstack(wrapper, 1);
	}
	
	public static ItemStack getItemstack(SpellComponentWrapper wrapper, int count) {
		ItemStack stack = new ItemStack(instance(), count);
		setComponent(stack, wrapper);
		return stack;
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_seeking_gem";
	}

	@Override
	public String getLoreDisplayName() {
		return "Seeking Gems";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Seeking gems contain raw essence of spell components.", "On use, they release this energy and help influence the shrines that spawn.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Seeking gems contain raw essence of a spell component.", "Releasing this energy makes the next shrine that spawns of the provided type.", "Multiple alterations can be queued and will affect shrine generation as they are encountered.");
	}
	
	private static class SeekingGemRecipe extends ShapedRecipes {

		public SeekingGemRecipe() {
			super(3, 3, new ItemStack[] {
				ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1),
				ReagentItem.instance().getReagent(ReagentType.GINSENG, 1),
				ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1),
				ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
				new ItemStack(SpellRune.instance(), 1, OreDictionary.WILDCARD_VALUE),
				ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
				new ItemStack(Items.GOLD_INGOT),
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1),
				new ItemStack(Items.GOLD_INGOT),	
			}, ShrineSeekingGem.getItemstack(new SpellComponentWrapper(EMagicElement.PHYSICAL)));
			
			RecipeSorter.register(NostrumMagica.MODID + ":SeekingGemRecipe",
					this.getClass(), Category.SHAPED, "after:minecraft:shaped");
		}
		
		@Override
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			// Just care about the rune in the center
			ItemStack rune = inv.getStackInSlot(4);
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(rune);
			if (comp == null) {
				return null;
			}
			
			return ShrineSeekingGem.getItemstack(comp);
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
