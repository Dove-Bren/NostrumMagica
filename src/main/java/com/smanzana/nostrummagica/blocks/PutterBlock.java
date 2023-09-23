package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.PutterBlockTileEntity;
import com.smanzana.nostrummagica.client.gui.NostrumGui;

import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;

public class PutterBlock extends ContainerBlock {
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	
	public static final String ID = "putter";
	
	private static PutterBlock instance = null;
	public static PutterBlock instance() {
		if (instance == null)
			instance = new PutterBlock();
		
		return instance;
	}
	
		public PutterBlock() {
		super(Material.ROCK, MapColor.STONE);
		this.setUnlocalizedName(ID);
		this.setHardness(3.5f);
		this.setResistance(3.5f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setHarvestLevel("pickaxe", 1);
	}
	
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return this.getDefaultState().with(FACING, Direction.getDirectionFromEntityLiving(pos, placer));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		Direction facing = Direction.VALUES[meta % Direction.VALUES.length];
		return getDefaultState().with(FACING, facing);
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return state.get(FACING).ordinal();
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		
		playerIn.openGui(NostrumMagica.instance,
				NostrumGui.putterBlockID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PutterBlockTileEntity();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(BlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof PutterBlockTileEntity))
			return;
		
		PutterBlockTileEntity putter = (PutterBlockTileEntity) ent;
		IInventory inv = putter.getInventory();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (!item.isEmpty()) {
				double x, y, z;
				x = pos.getX() + .5;
				y = pos.getY() + .5;
				z = pos.getZ() + .5;
				world.addEntity(new ItemEntity(world, x, y, z, item.copy()));
			}
		}
	}
}
