package com.smanzana.nostrummagica.blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DungeonAir extends Block {

	public static final String ID = "dungeon_air";
	
	private static DungeonAir instance = null;
	public static DungeonAir instance() {
		if (instance == null)
			instance = new DungeonAir();
		
		return instance;
	}
	
	public DungeonAir() {
		super(Material.BARRIER, MapColor.AIR);
		this.setUnlocalizedName(ID);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		this.setLightOpacity(0);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return Block.FULL_BLOCK_AABB;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
		
		if (rand.nextBoolean() && rand.nextBoolean()
				&& rand.nextBoolean() && rand.nextBoolean()
				 && rand.nextBoolean() && rand.nextBoolean()) {
			final float brightness = rand.nextFloat();
			final float alpha = rand.nextFloat();
			final int color = 0x40200020
					+ (((int) (alpha * 40f) & 0xFF) << 24)
					+ (((int) (brightness * 60f) & 0xFF) << 16)
					+ (((int) (brightness * 60f) & 0xFF) << 0);
			
			NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
					1,
					pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .5, 40, 20,
					new Vec3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
		IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
		Block block = iblockstate.getBlock();
		
		return !(block == instance());
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, LivingEntity placer, ItemStack stack) {
		if (stack.getMetadata() == 1) {
			this.spawnDoor(worldIn, pos);
		} else {
			super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		}
	}
	
	private void spawnDoor(World world, BlockPos center) {
		// Fill all air blocks around the start up to a maximum. Flood!
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		int blocksLeft = 256;
		
		// Center already placed so ignore it
		next.add(center.up());
		next.add(center.down());
		next.add(center.north());
		next.add(center.south());
		next.add(center.east());
		next.add(center.west());
		
		while (!next.isEmpty() && blocksLeft > 0) {
			BlockPos cur = next.remove(0);
			
			if (visited.contains(cur))
				continue;
			
			if (!world.isAirBlock(cur))
				continue;
			
			blocksLeft--;
			
			visited.add(cur);
			world.setBlockState(cur, this.getDefaultState());
			
			next.add(cur.up());
			next.add(cur.down());
			next.add(cur.north());
			next.add(cur.south());
			next.add(cur.east());
			next.add(cur.west());
		}
	}
	
	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		items.add(new ItemStack(this, 1, 0));
		items.add(new ItemStack(this, 1, 1));
	}
	
	@Override
	public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, Direction facing) {
		return false;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, Direction face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
		final PlayerEntity player = NostrumMagica.proxy.getPlayer();
		if (player == null || player.world == null || !player.isCreative()) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isCollidable()
    {
        return false;
    }
}
