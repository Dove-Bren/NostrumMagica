package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.item.IBlueprintHolder;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class CopyWandItem extends Item implements IBlueprintHolder, ISelectionItem {

	public static final String ID = "copy_wand";
	
	private @Nullable Blueprint blueprint;
	
	private @Nullable Level selectWorld;
	private @Nullable BlockPos select1;
	private @Nullable BlockPos select2;
	
	public CopyWandItem() {
		super(NostrumItems.PropDungeonUnstackable());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent("For Creative Singleplayer Use"));
		if (Screen.hasShiftDown()) {
			tooltip.add(new TextComponent("Select bounds to copy with right-click."));
			tooltip.add(new TextComponent("Shift-right-click to capture or spawn."));
		}
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player player = context.getPlayer();
		
		if (world.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
			player.sendMessage(new TextComponent("This item only works in single player"), Util.NIL_UUID);
			return InteractionResult.FAIL;
		}
		
		if (pos == null)
			return InteractionResult.PASS;
		
		if (player == null || !player.isCreative()) {
			if (player != null) {
				player.sendMessage(new TextComponent("You must be in creative to use this item"), Util.NIL_UUID);
			}
			return InteractionResult.SUCCESS;
		}
		
		// Handle selection and spawning based on shift
		if (player.isShiftKeyDown()) {
			Direction face = null;
			// Figure out facing by looking at clicked pos vs our pos
			final BlockPos playerPos = player.blockPosition();
			face = Direction.getNearest((float) (pos.getX() - playerPos.getX()), 0f, (float) (pos.getZ() - playerPos.getZ()));
			BlockPos placePos = pos.relative(context.getClickedFace()); // offset by face clicked on
			if (this.blueprint != null) {
				// spawn
				blueprint.spawn(world, placePos, face); // spawn rotated based on direction from us
				
			} else if (select1 != null && select2 != null) {
				final BlockPos min = new BlockPos(Math.min(select1.getX(), select2.getX()),
						Math.min(select1.getY(), select2.getY()),
						Math.min(select1.getZ(), select2.getZ()));
				final BlockPos max = new BlockPos(Math.max(select1.getX(), select2.getX()),
						Math.max(select1.getY(), select2.getY()),
						Math.max(select1.getZ(), select2.getZ()));
				final BlockPos offset = placePos.subtract(min);
				this.blueprint = Blueprint.Capture(world, min, max, new BlueprintLocation(offset, face));
				select1 = select2 = null;
				selectWorld = null;
			}
		} else {
			final BlockPos selectPos = pos;
			
			// Adjust selection
			if (select1 == null || selectWorld != world || select2 != null) {
				selectWorld = world;
				select1 = selectPos;
				select2 = null;
			} else {
				select2 = selectPos;
			}
			blueprint = null;
		}
		
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hasBlueprint(Player player, ItemStack stack) {
		return this.blueprint != null;
	}

	@Override
	public boolean shouldDisplayBlueprint(Player player, ItemStack stack, BlockPos pos) {
		return player.isShiftKeyDown() && player.isCreative();
	}

	@Override
	public IBlueprint getBlueprint(Player player, ItemStack stack, BlockPos pos) {
		return blueprint;
	}

	@Override
	public boolean shouldRenderSelection(Player player, ItemStack stack) {
		return player.isCreative() && select1 != null && DimensionUtils.SameDimension(selectWorld, player.level);
	}

	@Override
	public BlockPos getAnchor(Player player, ItemStack stack) {
		return select1;
	}

	@Override
	public BlockPos getBoundingPos(Player player, ItemStack stack) {
		return select2;
	}

	@Override
	public boolean isSelectionValid(Player player, ItemStack selectionStack) {
		final int size = Math.abs(select1.getX() - select2.getX())
				* Math.abs(select1.getY() - select2.getY())
				* Math.abs(select1.getZ() - select1.getZ());
		return size < 4096;
	}
}
