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

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Magic mirror that links to another and transports items!
 */
public class ParadoxMirrorBlock extends Block implements ILoreTagged {
	
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final double BB_DEPTH = 2.0 / 16.0;
	private static final double BB_MARGIN = 1.0 / 16.0;
	private static final VoxelShape AABB_N = Block.makeCuboidShape(16 * BB_MARGIN, 16 * 0, 16 * (1 - BB_DEPTH), 16 * (1 - BB_MARGIN), 16 * 1, 16 * 1);
	private static final VoxelShape AABB_E = Block.makeCuboidShape(16 * 0, 16 * 0, 16 * BB_MARGIN, 16 * BB_DEPTH, 16 * 1, 16 * (1-BB_MARGIN));
	private static final VoxelShape AABB_S = Block.makeCuboidShape(16 * BB_MARGIN, 16 * 0, 16 * 0, 16 * (1-BB_MARGIN), 16 * 1, 16 * BB_DEPTH);
	private static final VoxelShape AABB_W = Block.makeCuboidShape(16 * (1 - BB_DEPTH), 16 * 0, 16 * BB_MARGIN, 16 * 1, 16 * 1, 16 * (1-BB_MARGIN));
	
	private static final String NBT_LINKED_POS = "linked_pos";
	
	public static final String ID = "paradox_mirror";
	
	public ParadoxMirrorBlock() {
		super(Block.Properties.create(Material.IRON)
				.hardnessAndResistance(.5f, 0f)
				.sound(SoundType.GLASS)
				);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		//return this.getDefaultState().with(FACING, placer.getHorizontalFacing().getOpposite());
		Direction side = context.getPlacementHorizontalFacing().getOpposite();
		if (!this.canPlaceAt(context.getWorld(), context.getPos(), side)) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				side = side.rotateY();
				if (this.canPlaceAt(context.getWorld(), context.getPos(), side)) {
					break;
				}
			}
		}
		
		return this.getDefaultState()
				.with(FACING, side);
	}
	
	protected boolean canPlaceAt(IWorldReader worldIn, BlockPos pos, Direction side) {
		BlockState state = worldIn.getBlockState(pos.offset(side.getOpposite()));
		if (state == null || !(state.isSolidSide(worldIn, pos.offset(side.getOpposite()), side.getOpposite()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		for (Direction side : FACING.getAllowedValues()) {
			if (canPlaceAt(world, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		Direction myFacing = state.get(FACING);
		if (!this.canPlaceAt(worldIn, currentPos, myFacing)) { // should check passed in facing and only re-check if wall we're on changed but I can't remember if facing is wall we're on or the opposite
			return Blocks.AIR.getDefaultState();
		}
		
		return state;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(FACING)) {
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
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		ParadoxMirrorTileEntity mirror = getTileEntity(worldIn, pos);
		if (mirror != null) {
			
			@Nonnull ItemStack held = playerIn.getHeldItem(hand);
			if (held.isEmpty()) {
				return ActionResultType.FAIL;
			}
			
			// If we have an item, return true only if item has a position we can use
			if (held.getItem() instanceof PositionCrystal) {
				BlockPos heldPos = PositionCrystal.getBlockPosition(held);
				if (heldPos != null && !heldPos.equals(pos)) {
					mirror.setLinkedPosition(heldPos);
					playerIn.sendMessage(new TranslationTextComponent("info.generic.block_linked"), Util.DUMMY_UUID);
				}
				return ActionResultType.SUCCESS; // true even if crystal doesn't have position
			}
			// else try to send whatever item it is through
			else {
				// If we have an item, return true even if mirror is on cooldown
				if (mirror.tryPushItem(held)) {
					// Item was pushed! Remove from hand!
					playerIn.setHeldItem(hand, ItemStack.EMPTY);
					return ActionResultType.SUCCESS;
				}
			}
			return ActionResultType.FAIL;
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ParadoxMirrorTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(worldIn, pos, state);
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		world.removeTileEntity(pos);
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		// ItemStack should carry NBT about linked position
		ItemStack drop = new ItemStack(this);
		BlockPos linkedPos = null;
		
		TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);
		if (te != null && te instanceof ParadoxMirrorTileEntity) {
			linkedPos = ((ParadoxMirrorTileEntity) te).getLinkedPosition();
		}
		
		if (linkedPos != null) {
			CompoundNBT tag = drop.getTag();
			if (tag == null) {
				tag = new CompoundNBT();
			}
			
			tag.put(NBT_LINKED_POS, NBTUtil.writeBlockPos(linkedPos));
			drop.setTag(tag);
		}
		
		return Lists.newArrayList(drop);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		
		// Read linked position off of item stack, if present
		CompoundNBT tag = stack.getTag();
		if (tag != null && tag.contains(NBT_LINKED_POS)) {
			this.setLinkedPosition(worldIn, pos, NBTUtil.readBlockPos(tag.getCompound(NBT_LINKED_POS)));
		}
	}
	
	public @Nullable BlockPos getLinkedPosition(IWorldReader world, BlockPos pos) {
		ParadoxMirrorTileEntity mirror = getTileEntity(world, pos);
		if (mirror != null) {
			return mirror.getLinkedPosition();
		}
		
		return null;
	}
	
	public void setLinkedPosition(IWorldReader world, @Nullable BlockPos pos, BlockPos linkedPos) {
		ParadoxMirrorTileEntity mirror = getTileEntity(world, pos);
		if (mirror != null) {
			mirror.setLinkedPosition(linkedPos);
		}
	}
	
	protected @Nullable ParadoxMirrorTileEntity getTileEntity(IWorldReader world, BlockPos pos) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof ParadoxMirrorTileEntity))
			return null;
		
		return (ParadoxMirrorTileEntity) ent;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.isEmpty() || !stack.hasTag() || !stack.getTag().contains(NBT_LINKED_POS, NBT.TAG_COMPOUND))
			return;
		
		BlockPos pos = NBTUtil.readBlockPos(stack.getTag().getCompound(NBT_LINKED_POS));
		
		if (pos == null)
			return;
		
		tooltip.add(new StringTextComponent("<" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ">").mergeStyle(TextFormatting.GREEN));
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
