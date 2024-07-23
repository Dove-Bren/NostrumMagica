package com.smanzana.nostrummagica.item;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.item.IBlueprintHolder;
import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class CopyWandItem extends Item implements IBlueprintHolder, ISelectionItem {

	public static final String ID = "copy_wand";
	
	private @Nullable Blueprint blueprint;
	
	private @Nullable World selectWorld;
	private @Nullable BlockPos select1;
	private @Nullable BlockPos select2;
	
	public CopyWandItem() {
		super(NostrumItems.PropDungeonUnstackable());
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("For Creative Singleplayer Use"));
		if (Screen.hasShiftDown()) {
			tooltip.add(new StringTextComponent("Select bounds to copy with right-click."));
			tooltip.add(new StringTextComponent("Shift-right-click to capture or spawn."));
		}
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		final World world = context.getWorld();
		final BlockPos pos = context.getPos();
		final PlayerEntity player = context.getPlayer();
		
		if (world.isRemote)
			return ActionResultType.SUCCESS;
		
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
			player.sendMessage(new StringTextComponent("This item only works in single player"), Util.DUMMY_UUID);
			return ActionResultType.FAIL;
		}
		
		if (pos == null)
			return ActionResultType.PASS;
		
		if (player == null || !player.isCreative()) {
			if (player != null) {
				player.sendMessage(new StringTextComponent("You must be in creative to use this item"), Util.DUMMY_UUID);
			}
			return ActionResultType.SUCCESS;
		}
		
		// Handle selection and spawning based on shift
		if (player.isSneaking()) {
			Direction face = null;
			// Figure out facing by looking at clicked pos vs our pos
			final BlockPos playerPos = player.getPosition();
			face = Direction.getFacingFromVector((float) (pos.getX() - playerPos.getX()), 0f, (float) (pos.getZ() - playerPos.getZ()));
			BlockPos placePos = pos.offset(context.getFace()); // offset by face clicked on
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
		
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean hasBlueprint(PlayerEntity player, ItemStack stack) {
		return this.blueprint != null;
	}

	@Override
	public boolean shouldDisplayBlueprint(PlayerEntity player, ItemStack stack, BlockPos pos) {
		return player.isSneaking() && player.isCreative();
	}

	@Override
	public IBlueprint getBlueprint(PlayerEntity player, ItemStack stack, BlockPos pos) {
		return blueprint;
	}

	@Override
	public boolean shouldRenderSelection(PlayerEntity player, ItemStack stack) {
		return player.isCreative() && select1 != null && DimensionUtils.SameDimension(selectWorld, player.world);
	}

	@Override
	public BlockPos getAnchor(PlayerEntity player, ItemStack stack) {
		return select1;
	}

	@Override
	public BlockPos getBoundingPos(PlayerEntity player, ItemStack stack) {
		return select2;
	}

	@Override
	public boolean isSelectionValid(ClientPlayerEntity player, ItemStack selectionStack) {
		final int size = Math.abs(select1.getX() - select2.getX())
				* Math.abs(select1.getY() - select2.getY())
				* Math.abs(select1.getZ() - select1.getZ());
		return size < 4096;
	}
}
