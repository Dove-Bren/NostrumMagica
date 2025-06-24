package com.smanzana.nostrummagica.item.mapmaking;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
	public static final String ID_ROOTING_AIR_ALL = "fill_tool_rooting_air";
	public static final String ID_MECHBLOCK_GHOST_CONNECTED = "fill_tool_mechblock_ghost";
	
	protected static final int MAX_BLOCKS = 4098;
	
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
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent("For Creative Use"));
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		return super.use(worldIn, playerIn, hand);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player player = context.getPlayer();
		
		if (world.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (pos == null)
			return InteractionResult.PASS;
		
		if (player == null || !player.isCreative()) {
			if (player != null) {
				player.sendMessage(new TextComponent("You must be in creative to use this item"), Util.NIL_UUID);
			}
			return InteractionResult.SUCCESS;
		}
		
		final BlockPos startPos = pos.relative(context.getClickedFace());
		fill(player, world, startPos);
		
		return InteractionResult.SUCCESS;
	}
	
	protected BlockState getFillState() {
		if (this.fillStateCache == null) {
			this.fillStateCache = this.fillStateSupplier.get();
		}
		
		return this.fillStateCache;
	}
	
	protected void setState(FillContext context, Level world, BlockPos pos) {
		world.setBlock(pos, this.getFillState(), 3); // ? Different flags for speed?
	}
	
	protected boolean shouldFill(FillContext context, Level world, BlockPos pos) {
		return world.isEmptyBlock(pos);
	}
	
	protected boolean canSpreadTo(FillContext context, BlockPos checkPos) {
		return !this.onlyDown || (checkPos.getY() <= context.start.getY());
	}
	
	protected FillContext makeContext(Player player, Level level, BlockPos start) {
		return new FillContext(this, start);
	}
	
	protected void fill(Player player, Level world, BlockPos start) {
		final FillContext context = this.makeContext(player, world, start);
		
		while (context.hasNext()) {
			BlockPos pos = context.getNext();
			fillAndAdd(player, world, pos, context);
		}
		
		player.sendMessage(new TextComponent("Filled " + context.count + " blocks"), Util.NIL_UUID);
	}
	
	protected static class FillContext {
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
			if (count < MAX_BLOCKS && item.canSpreadTo(this, pos) && visitted.add(pos)) {
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
	
	private void fillAndAdd(Player player, Level world, BlockPos pos, FillContext context) {
		// Check and fill the current block, and then add neighbors to the list
		if (shouldFill(context, world, pos)) {
			setState(context, world, pos);
			
			context.addNeighbor(pos.north());
			context.addNeighbor(pos.south());
			context.addNeighbor(pos.east());
			context.addNeighbor(pos.west());
			context.addNeighbor(pos.below());
			context.addNeighbor(pos.above()); // Context will enforce onlyDown if it applies
		}
	}
	
}
