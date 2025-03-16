package com.smanzana.nostrummagica.block;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.tile.ParadoxMirrorTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.nbt.Tag;

/**
 * Magic mirror that links to another and transports items!
 */
public class ParadoxMirrorBlock extends Block implements ILoreTagged {
	
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final double BB_DEPTH = 2.0 / 16.0;
	private static final double BB_MARGIN = 1.0 / 16.0;
	private static final VoxelShape AABB_N = Block.box(16 * BB_MARGIN, 16 * 0, 16 * (1 - BB_DEPTH), 16 * (1 - BB_MARGIN), 16 * 1, 16 * 1);
	private static final VoxelShape AABB_E = Block.box(16 * 0, 16 * 0, 16 * BB_MARGIN, 16 * BB_DEPTH, 16 * 1, 16 * (1-BB_MARGIN));
	private static final VoxelShape AABB_S = Block.box(16 * BB_MARGIN, 16 * 0, 16 * 0, 16 * (1-BB_MARGIN), 16 * 1, 16 * BB_DEPTH);
	private static final VoxelShape AABB_W = Block.box(16 * (1 - BB_DEPTH), 16 * 0, 16 * BB_MARGIN, 16 * 1, 16 * 1, 16 * (1-BB_MARGIN));
	
	private static final String NBT_LINKED_POS = "linked_pos";
	
	public static final String ID = "paradox_mirror";
	
	public ParadoxMirrorBlock() {
		super(Block.Properties.of(Material.METAL)
				.strength(.5f, 0f)
				.sound(SoundType.GLASS)
				);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		//return this.getDefaultState().with(FACING, placer.getHorizontalFacing().getOpposite());
		Direction side = context.getHorizontalDirection().getOpposite();
		if (!this.canPlaceAt(context.getLevel(), context.getClickedPos(), side)) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				side = side.getClockWise();
				if (this.canPlaceAt(context.getLevel(), context.getClickedPos(), side)) {
					break;
				}
			}
		}
		
		return this.defaultBlockState()
				.setValue(FACING, side);
	}
	
	protected boolean canPlaceAt(LevelReader worldIn, BlockPos pos, Direction side) {
		BlockState state = worldIn.getBlockState(pos.relative(side.getOpposite()));
		if (state == null || !(state.isFaceSturdy(worldIn, pos.relative(side.getOpposite()), side.getOpposite()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		for (Direction side : FACING.getPossibleValues()) {
			if (canPlaceAt(world, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		Direction myFacing = state.getValue(FACING);
		if (!this.canPlaceAt(worldIn, currentPos, myFacing)) { // should check passed in facing and only re-check if wall we're on changed but I can't remember if facing is wall we're on or the opposite
			return Blocks.AIR.defaultBlockState();
		}
		
		return state;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		}
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		ParadoxMirrorTileEntity mirror = getTileEntity(worldIn, pos);
		if (mirror != null) {
			
			@Nonnull ItemStack held = playerIn.getItemInHand(hand);
			if (held.isEmpty()) {
				return InteractionResult.FAIL;
			}
			
			// If we have an item, return true only if item has a position we can use
			if (held.getItem() instanceof PositionCrystal) {
				BlockPos heldPos = PositionCrystal.getBlockPosition(held);
				if (heldPos != null && !heldPos.equals(pos)) {
					mirror.setLinkedPosition(heldPos);
					playerIn.sendMessage(new TranslatableComponent("info.generic.block_linked"), Util.NIL_UUID);
				}
				return InteractionResult.SUCCESS; // true even if crystal doesn't have position
			}
			// else try to send whatever item it is through
			else {
				// If we have an item, return true even if mirror is on cooldown
				if (mirror.tryPushItem(held)) {
					// Item was pushed! Remove from hand!
					playerIn.setItemInHand(hand, ItemStack.EMPTY);
					return InteractionResult.SUCCESS;
				}
			}
			return InteractionResult.FAIL;
		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new ParadoxMirrorTileEntity();
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(worldIn, pos, state);
		}
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		world.removeBlockEntity(pos);
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		// ItemStack should carry NBT about linked position
		ItemStack drop = new ItemStack(this);
		BlockPos linkedPos = null;
		
		BlockEntity te = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (te != null && te instanceof ParadoxMirrorTileEntity) {
			linkedPos = ((ParadoxMirrorTileEntity) te).getLinkedPosition();
		}
		
		if (linkedPos != null) {
			CompoundTag tag = drop.getTag();
			if (tag == null) {
				tag = new CompoundTag();
			}
			
			tag.put(NBT_LINKED_POS, NbtUtils.writeBlockPos(linkedPos));
			drop.setTag(tag);
		}
		
		return Lists.newArrayList(drop);
	}
	
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		
		// Read linked position off of item stack, if present
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(NBT_LINKED_POS)) {
			this.setLinkedPosition(worldIn, pos, NbtUtils.readBlockPos(tag.getCompound(NBT_LINKED_POS)));
		}
	}
	
	public @Nullable BlockPos getLinkedPosition(LevelReader world, BlockPos pos) {
		ParadoxMirrorTileEntity mirror = getTileEntity(world, pos);
		if (mirror != null) {
			return mirror.getLinkedPosition();
		}
		
		return null;
	}
	
	public void setLinkedPosition(LevelReader world, @Nullable BlockPos pos, BlockPos linkedPos) {
		ParadoxMirrorTileEntity mirror = getTileEntity(world, pos);
		if (mirror != null) {
			mirror.setLinkedPosition(linkedPos);
		}
	}
	
	protected @Nullable ParadoxMirrorTileEntity getTileEntity(LevelReader world, BlockPos pos) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof ParadoxMirrorTileEntity))
			return null;
		
		return (ParadoxMirrorTileEntity) ent;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains(NBT_LINKED_POS, Tag.TAG_COMPOUND))
			return;
		
		BlockPos pos = NbtUtils.readBlockPos(stack.getTag().getCompound(NBT_LINKED_POS));
		
		if (pos == null)
			return;
		
		tooltip.add(new TextComponent("<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">").withStyle(ChatFormatting.GREEN));
	}

	@Override
	public String getLoreKey() {
		return "paradox_mirror";
	}

	@Override
	public String getLoreDisplayName() {
		return "Paradox Mirrors";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add(
				"Mystic mirrors that accept items in on one side and transport them to another mirror somewhere else!"
				);
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add(
				"Mystic mirrors that accept items in on one side and transport them to another mirror somewhere else!",
				"Link a mirror to another using a geogem holding the other mirror's position.",
				"Mirrors do not have to be linked to one another to function. A mirror can point to a mirror that's pointing to a third mirror.",
				"Mirros will not teleport items they received until the item is picked and up and dropped again, or went more than 2 blocks away."
				);
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
