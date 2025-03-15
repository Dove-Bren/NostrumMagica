package com.smanzana.nostrummagica.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Crystal that stores the location of a position in a world
 * @author Skyler
 *
 */
public class PositionCrystal extends Item implements ILoreTagged, ISelectionItem {

	public static final String ID = "nostrum_pos_crystal";
	private static final String NBT_DIMENSION = "dimension";
	private static final String NBT_X = "x";
	private static final String NBT_Y = "y";
	private static final String NBT_Z = "z";

	private static Map<RegistryKey<World>, String> DimensionNames = new HashMap<>();

	public PositionCrystal() {
		super(NostrumItems.PropUnstackable());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		RegistryKey<World> dim = getDimension(stack);
		BlockPos pos = getBlockPosition(stack);
		
		if (pos == null)
			return;
		
		String dimName = getDimensionName(dim);
		if (dimName == null)
			dimName = "An Unknown Dimension";
		
		tooltip.add(new StringTextComponent("<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">").withStyle(TextFormatting.GREEN));
		tooltip.add(new StringTextComponent(dimName).withStyle(TextFormatting.DARK_GREEN));
	}
	
	public static BlockPos getBlockPosition(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return null;
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null)
			return null;
		
		if (!nbt.contains(NBT_X)
				|| !nbt.contains(NBT_Y)
				|| !nbt.contains(NBT_Z))
			return null;
		
		return new BlockPos(
				nbt.getInt(NBT_X),
				nbt.getInt(NBT_Y),
				nbt.getInt(NBT_Z)
				);
	}
	
	/**
	 * Returns 0 on error
	 * @param stack
	 * @return
	 */
	public static RegistryKey<World> getDimension(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return World.OVERWORLD;
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null)
			return World.OVERWORLD;
		
		return DimensionUtils.GetDimKey(nbt.getString(NBT_DIMENSION));
	}
	
	public static void setPosition(ItemStack stack, RegistryKey<World> dimension, BlockPos pos) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return;
		
		if (pos == null)
			return;
		
		CompoundNBT tag;
		if (!stack.hasTag())
			tag = new CompoundNBT();
		else
			tag = stack.getTag();
		
		tag.putString(NBT_DIMENSION, dimension.location().toString());
		tag.putInt(NBT_X, pos.getX());
		tag.putInt(NBT_Y, pos.getY());
		tag.putInt(NBT_Z, pos.getZ());
		
		stack.setTag(tag);
	}
	
	public static void clearPosition(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return;
		
		CompoundNBT tag;
		if (!stack.hasTag())
			return;
		
		tag = stack.getTag();
		tag.remove(NBT_DIMENSION);
		tag.remove(NBT_X);
		tag.remove(NBT_Y);
		tag.remove(NBT_Z);
		
		stack.setTag(tag);
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		final World worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final PlayerEntity playerIn = context.getPlayer();
		final @Nonnull ItemStack stack = context.getItemInHand();
		
		if (worldIn.isClientSide)
			return ActionResultType.SUCCESS;
		
		if (pos == null)
			return ActionResultType.PASS;
		
		setPosition(stack, DimensionUtils.GetDimension(playerIn), pos);
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getItemInHand(hand);
		
		if (playerIn.isShiftKeyDown()) {
			clearPosition(itemStackIn);
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemStackIn);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, itemStackIn);
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
	
	@OnlyIn(Dist.CLIENT)
	public static String getDimensionName(RegistryKey<World> dim) {
		if (DimensionNames.containsKey(dim)) {
			return DimensionNames.get(dim);
		}
		
		final String name;
		if (DimensionUtils.IsOverworld(dim)) {
			name = "Overworld";
		} else if (DimensionUtils.IsNether(dim)) {
			name = "Nether";
		} else if (DimensionUtils.IsEnd(dim)) {
			name = "The End";
		} else if (I18n.exists(dim.location().toString())) {
			name = I18n.get(dim.location().toString());
		} else if (I18n.exists(dim.location().toString().replace(':', '.'))) {
			name = I18n.get(dim.location().toString().replace(':', '.'));
		} else {
			String raw = dim.location().getPath();
			name = raw.substring(0, 1).toUpperCase() + raw.substring(1);
		}
		
		DimensionNames.put(dim, name);
		return name;
	}
	
	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

	@Override
	public boolean shouldRenderSelection(PlayerEntity player, ItemStack stack) {
		return player.isCreative() && player.isShiftKeyDown()
				&& !player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof PositionCrystal
				&& !player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() instanceof PositionCrystal
				&& getBlockPosition(player.getMainHandItem()) != null
				&& getBlockPosition(player.getOffhandItem()) != null
				&& DimensionUtils.InDimension(player, getDimension(player.getMainHandItem()))
				&& DimensionUtils.InDimension(player, getDimension(player.getOffhandItem()))
				;
	}

	@Override
	public BlockPos getAnchor(PlayerEntity player, ItemStack stack) {
		return getBlockPosition(player.getMainHandItem());
	}

	@Override
	public BlockPos getBoundingPos(PlayerEntity player, ItemStack stack) {
		return getBlockPosition(player.getOffhandItem());
	}

	@Override
	public boolean isSelectionValid(PlayerEntity player, ItemStack selectionStack) {
		return true;
	}
}
