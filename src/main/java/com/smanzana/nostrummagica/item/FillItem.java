package com.smanzana.nostrummagica.item;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Map-making tool. Fills air with another block, optionally only going horizontally and down.
 * @author Skyler
 *
 */
public class FillItem extends Item {

	public static final String ID_AIR_ALL = "fill_tool_air";
	public static final String ID_WATER_ALL = "fill_tool_water";
	public static final String ID_WATER_DOWN = "fill_tool_water_level";
	
	private static final int MAX_BLOCKS = 4098;
	
	private final boolean onlyDown;
	private final Supplier<BlockState> fillStateSupplier;
	private @Nullable BlockState fillStateCache;
	
	public FillItem(Supplier<BlockState> fillStateSupplier, boolean onlyDown) {
		super(NostrumItems.PropDungeonUnstackable());
		this.fillStateSupplier = fillStateSupplier;
		this.onlyDown = onlyDown;
		this.fillStateCache = null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("For Creative Use"));
	}
	
	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
		return super.use(worldIn, playerIn, hand);
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		final World world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final PlayerEntity player = context.getPlayer();
		
		if (world.isClientSide)
			return ActionResultType.SUCCESS;
		
		if (pos == null)
			return ActionResultType.PASS;
		
		if (player == null || !player.isCreative()) {
			if (player != null) {
				player.sendMessage(new StringTextComponent("You must be in creative to use this item"), Util.NIL_UUID);
			}
			return ActionResultType.SUCCESS;
		}
		
		final BlockPos startPos = pos.relative(context.getClickedFace());
		fill(player, world, startPos);
		
		return ActionResultType.SUCCESS;
	}
	
	protected BlockState getFillState() {
		if (this.fillStateCache == null) {
			this.fillStateCache = this.fillStateSupplier.get();
		}
		
		return this.fillStateCache;
	}
	
	protected void setState(PlayerEntity player, World world, BlockPos pos) {
		world.setBlock(pos, this.getFillState(), 3); // ? Different flags for speed?
	}
	
	protected boolean shouldFill(World world, BlockPos pos) {
		return world.isEmptyBlock(pos);
	}
	
	protected boolean canSpreadTo(BlockPos startPos, BlockPos checkPos) {
		return !this.onlyDown || (checkPos.getY() <= startPos.getY());
	}
	
	protected void fill(PlayerEntity player, World world, BlockPos start) {
		final FillContext context = new FillContext(this, start);
		
		while (context.hasNext()) {
			BlockPos pos = context.getNext();
			fillAndAdd(player, world, pos, context);
		}
		
		player.sendMessage(new StringTextComponent("Filled " + context.count + " blocks"), Util.NIL_UUID);
	}
	
	private static final class FillContext {
		public final FillItem item;
		public final BlockPos start;
		public final Set<BlockPos> visitted = new HashSet<>();
		public final List<BlockPos> queue = new LinkedList<>();
		public int count = 0;
		
		public FillContext(FillItem item, BlockPos start) {
			this.item = item;
			this.start = start;
			queue.add(start);
			count = 1;
		}
		
		public void addNeighbor(BlockPos pos) {
			if (count < MAX_BLOCKS && item.canSpreadTo(this.start, pos) && visitted.add(pos)) {
				queue.add(pos);
				count++;
			}
		}
		
		public BlockPos getNext() {
			return queue.remove(0);
		}
		
		public boolean hasNext() {
			return !queue.isEmpty();
		}
	}
	
	private void fillAndAdd(PlayerEntity player, World world, BlockPos pos, FillContext context) {
		// Check and fill the current block, and then add neighbors to the list
		if (shouldFill(world, pos)) {
			setState(player, world, pos);
			
			context.addNeighbor(pos.north());
			context.addNeighbor(pos.south());
			context.addNeighbor(pos.east());
			context.addNeighbor(pos.west());
			context.addNeighbor(pos.below());
			context.addNeighbor(pos.above()); // Context will enforce onlyDown if it applies
		}
	}
	
}
