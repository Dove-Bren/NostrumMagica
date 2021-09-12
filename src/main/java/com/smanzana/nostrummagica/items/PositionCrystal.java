package com.smanzana.nostrummagica.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Crystal that stores the location of a position in a world
 * @author Skyler
 *
 */
public class PositionCrystal extends Item implements ILoreTagged {

	public static void init() {
		;
	}
	
	public static final String ID = "nostrum_pos_crystal";
	private static final String NBT_DIMENSION = "dimension";
	private static final String NBT_X = "x";
	private static final String NBT_Y = "y";
	private static final String NBT_Z = "z";

	private static PositionCrystal instance = null;
	private static Map<Integer, String> DimensionNames = new HashMap<>();

	public static PositionCrystal instance() {
		if (instance == null)
			instance = new PositionCrystal();
	
		return instance;

	}

	public PositionCrystal() {
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setCreativeTab(NostrumMagica.creativeTab);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		int dim = getDimension(stack);
		BlockPos pos = getBlockPosition(stack);
		
		if (pos == null)
			return;
		
		String dimName = getDimensionName(dim);
		if (dimName == null)
			dimName = "An Unknown Dimension";
		
		tooltip.add(TextFormatting.GREEN + "<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">" + TextFormatting.RESET);
		tooltip.add(TextFormatting.DARK_GREEN + dimName + TextFormatting.RESET);
	}
	
	public static BlockPos getBlockPosition(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return null;
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
			return null;
		
		if (!nbt.hasKey(NBT_X)
				|| !nbt.hasKey(NBT_Y)
				|| !nbt.hasKey(NBT_Z))
			return null;
		
		return new BlockPos(
				nbt.getInteger(NBT_X),
				nbt.getInteger(NBT_Y),
				nbt.getInteger(NBT_Z)
				);
	}
	
	/**
	 * Returns 0 on error
	 * @param stack
	 * @return
	 */
	public static int getDimension(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return 0;
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
			return 0;
		
		return nbt.getInteger(NBT_DIMENSION);
	}
	
	public static void setPosition(ItemStack stack, int dimension, BlockPos pos) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return;
		
		if (pos == null)
			return;
		
		NBTTagCompound tag;
		if (!stack.hasTagCompound())
			tag = new NBTTagCompound();
		else
			tag = stack.getTagCompound();
		
		tag.setInteger(NBT_DIMENSION, dimension);
		tag.setInteger(NBT_X, pos.getX());
		tag.setInteger(NBT_Y, pos.getY());
		tag.setInteger(NBT_Z, pos.getZ());
		
		stack.setTagCompound(tag);
	}
	
	public static void clearPosition(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return;
		
		NBTTagCompound tag;
		if (!stack.hasTagCompound())
			return;
		
		tag = stack.getTagCompound();
		tag.removeTag(NBT_DIMENSION);
		tag.removeTag(NBT_X);
		tag.removeTag(NBT_Y);
		tag.removeTag(NBT_Z);
		
		stack.setTagCompound(tag);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		final @Nonnull ItemStack stack = playerIn.getHeldItem(hand);
		
		if (worldIn.isRemote)
			return EnumActionResult.SUCCESS;
		
		if (pos == null)
			return EnumActionResult.PASS;
		
		setPosition(stack, playerIn.dimension, pos);
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
		
		if (playerIn.isSneaking()) {
			clearPosition(itemStackIn);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
	}

	@Override
	public String getLoreKey() {
		return "nostrum_pos_crystal";
	}

	@Override
	public String getLoreDisplayName() {
		return "Geogem";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("These gems have an incredible affinity to earth.", "They seem to form strong bonds to the ground beneath them.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Geogems form strong bonds with the ground, making them valuable for storing location information.", "One common use for a Geogem is to mark the location of an obelisk for use in a teleportation ritual.");
	}
	
	public static String getDimensionName(int id) {
		initDimensions();
		
		return DimensionNames.get(id);
	}
	
	private static void initDimensions() {
		if (!DimensionNames.isEmpty())
			return;
		
		for (Integer id : DimensionManager.getStaticDimensionIDs()) {
			WorldProvider provider = DimensionManager.createProviderFor(id);
			if (provider == null)
				continue;
			
			DimensionNames.put(id, provider.getDimensionType().getName());
		}
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
