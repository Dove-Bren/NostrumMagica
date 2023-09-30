package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DungeonAir extends BreakableBlock {

	public static final String ID = "dungeon_air";
	
	public DungeonAir() {
		super(Block.Properties.create(Material.BARRIER)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				);
	}
	
	// GetHowMuchLightGoesThrough?? Not sure.
	@Override
	@OnlyIn(Dist.CLIENT)
	public float func_220080_a(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return 1.0F;
	}
	
	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return true;
	}

	@Override
	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return false;
	}

	@Override
	public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type) {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);
		
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
	
//	@Override
//	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
//		final Item item = stack.getItem();
//		if (item == fillItem) {
//			this.spawnDoor(worldIn, pos);
//		} else {
//			super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
//		}
//	}
	
//	private void spawnDoor(World world, BlockPos center) {
//		// Fill all air blocks around the start up to a maximum. Flood!
//		Set<BlockPos> visited = new HashSet<>();
//		List<BlockPos> next = new LinkedList<>();
//		int blocksLeft = 256;
//		
//		// Center already placed so ignore it
//		next.add(center.up());
//		next.add(center.down());
//		next.add(center.north());
//		next.add(center.south());
//		next.add(center.east());
//		next.add(center.west());
//		
//		while (!next.isEmpty() && blocksLeft > 0) {
//			BlockPos cur = next.remove(0);
//			
//			if (visited.contains(cur))
//				continue;
//			
//			if (!world.isAirBlock(cur))
//				continue;
//			
//			blocksLeft--;
//			
//			visited.add(cur);
//			world.setBlockState(cur, this.getDefaultState());
//			
//			next.add(cur.up());
//			next.add(cur.down());
//			next.add(cur.north());
//			next.add(cur.south());
//			next.add(cur.east());
//			next.add(cur.west());
//		}
//	}
	
	@Override
	public boolean canBeConnectedTo(BlockState state, IBlockReader world, BlockPos pos, Direction facing) {
		return false;
	}
	
//	@Override
//	public boolean canCollideCheck(BlockState state, boolean hitIfLiquid) {
//		final PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
//		if (player == null || player.world == null || !player.isCreative()) {
//			return false;
//		}
//		return true;
//	}
//	
//	@Override
//	public boolean isCollidable()
//    {
//        return false;
//    }
}
