package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator.DungeonGen;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

// Queued the next shrine to be a certain type
public class ShrineSeekingGem extends Item implements ILoreTagged {

	private static final String NBT_TYPE = "gen_type";
	private static ShrineSeekingGem instance = null;
	
	public static ShrineSeekingGem instance() {
		if (instance == null)
			instance = new ShrineSeekingGem();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.addRecipe(new SeekingGemRecipe(DungeonGen.PORTAL, new ItemStack(Items.ENDER_PEARL)));
		GameRegistry.addRecipe(new SeekingGemRecipe(DungeonGen.DRAGON, NostrumResourceItem.getItem(ResourceType.SLAB_FIERCE, 1)));
	}
	
	public static final String id = "seeking_gem";
	
	private ShrineSeekingGem() {
		super();
		this.setUnlocalizedName(id);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setMaxStackSize(8);
	}
	
	public static DungeonGen getType(ItemStack stack) {
		DungeonGen type = DungeonGen.PORTAL;
		
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			
			if (nbt.hasKey(NBT_TYPE, NBT.TAG_STRING)) {
				try {
					type = DungeonGen.valueOf(nbt.getString(NBT_TYPE).toUpperCase());
				} catch (Exception e) {
					System.out.println("=== Exception caught: " + nbt.getString(NBT_TYPE));
					;
				}
			} else {
				System.out.println("=== No key");
			}
		} else {
			System.out.println("=== No tag");
		}
		
		return type;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		if (stack.isEmpty()) {
			return;
		}
		
		DungeonGen type = getType(stack);
		
		tooltip.add(TextFormatting.DARK_BLUE + I18n.format("info.shrinegem." + type.name().toLowerCase()) + TextFormatting.RESET);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (DungeonGen type : DungeonGen.values()) {
			subItems.add(getItemstack(type));
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		if (worldIn.isRemote) {
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
		
		if (!itemStackIn.hasTagCompound()) {
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		}
		
		DungeonGen type = getType(itemStackIn);
		if (type == null) {
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		}
		
		if (NostrumDungeonGenerator.boostOdds(type)) {
			itemStackIn.shrink(1);
			playerIn.sendMessage(new TextComponentTranslation("info.shrinegem.success"));
		} else {
			playerIn.sendMessage(new TextComponentTranslation("info.shrinegem.failure"));
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		
    }
	
	public static void setType(ItemStack stack, DungeonGen type) {
		if (stack.isEmpty())
			return;
		
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		NBTTagCompound nbt = stack.getTagCompound();
		nbt.setString(NBT_TYPE, type.name());
	}
	
	public static ItemStack getItemstack(DungeonGen type) {
		return getItemstack(type, 1);
	}
	
	public static ItemStack getItemstack(DungeonGen type, int count) {
		ItemStack stack = new ItemStack(instance(), count);
		setType(stack, type);
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
		return new Lore().add("Seeking gems contain strong energies attuned to the different types of shrines.", "On use, they release this energy and help influence the shrines that spawn.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Seeking gems contain strong energies attuned to the different types of shrines.", "Releasing this energy makes it more likely that a shrine will spawn.", "There's a maximum number of boosts that can be in affect per shrine type.", "Each additional boost increases the odds.");
	}
	
	private static class SeekingGemRecipe extends ShapedRecipes {
		
		public SeekingGemRecipe(DungeonGen type, ItemStack item) {
			super(3, 3, new ItemStack[] {
				ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1),
				ReagentItem.instance().getReagent(ReagentType.GINSENG, 1),
				ReagentItem.instance().getReagent(ReagentType.MANDRAKE_ROOT, 1),
				ReagentItem.instance().getReagent(ReagentType.BLACK_PEARL, 1),
				item,
				ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1),
				new ItemStack(Items.GOLD_INGOT),
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1),
				new ItemStack(Items.GOLD_INGOT),	
			}, ShrineSeekingGem.getItemstack(type));
			
			RecipeSorter.register(NostrumMagica.MODID + ":SeekingGemRecipe",
					this.getClass(), Category.SHAPED, "after:minecraft:shaped");
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
