package com.smanzana.nostrummagica.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
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

	private static Map<ResourceKey<Level>, String> DimensionNames = new HashMap<>();

	public PositionCrystal() {
		super(NostrumItems.PropUnstackable());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		ResourceKey<Level> dim = getDimension(stack);
		BlockPos pos = getBlockPosition(stack);
		
		if (pos == null)
			return;
		
		String dimName = getDimensionName(dim);
		if (dimName == null)
			dimName = "An Unknown Dimension";
		
		tooltip.add(new TextComponent("<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">").withStyle(ChatFormatting.GREEN));
		tooltip.add(new TextComponent(dimName).withStyle(ChatFormatting.DARK_GREEN));
	}
	
	public static BlockPos getBlockPosition(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return null;
		
		CompoundTag nbt = stack.getTag();
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
	public static ResourceKey<Level> getDimension(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return Level.OVERWORLD;
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null)
			return Level.OVERWORLD;
		
		return DimensionUtils.GetDimKey(nbt.getString(NBT_DIMENSION));
	}
	
	public static void setPosition(ItemStack stack, ResourceKey<Level> dimension, BlockPos pos) {
		if (stack.isEmpty() || !(stack.getItem() instanceof PositionCrystal))
			return;
		
		if (pos == null)
			return;
		
		CompoundTag tag;
		if (!stack.hasTag())
			tag = new CompoundTag();
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
		
		CompoundTag tag;
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
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player playerIn = context.getPlayer();
		final @Nonnull ItemStack stack = context.getItemInHand();
		
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (pos == null)
			return InteractionResult.PASS;
		
		setPosition(stack, DimensionUtils.GetDimension(playerIn), pos);
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final @Nonnull ItemStack itemStackIn = playerIn.getItemInHand(hand);
		
		if (playerIn.isShiftKeyDown()) {
			clearPosition(itemStackIn);
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, itemStackIn);
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
	public static String getDimensionName(ResourceKey<Level> dim) {
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
	public boolean shouldRenderSelection(Player player, ItemStack stack) {
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
	public BlockPos getAnchor(Player player, ItemStack stack) {
		return getBlockPosition(player.getMainHandItem());
	}

	@Override
	public BlockPos getBoundingPos(Player player, ItemStack stack) {
		return getBlockPosition(player.getOffhandItem());
	}

	@Override
	public boolean isSelectionValid(Player player, ItemStack selectionStack) {
		return true;
	}
}
