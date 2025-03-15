package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Projectiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

/**
 * Block that, when hit with an arrow or spell etc. teleports the shooter to nearby the block
 * @author Skyler
 *
 */
public class MysticAnchorBlock extends Block {
	
	public static final String ID = "mystic_anchor";
	
	protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 20.0D, 11.0D);
	
	public MysticAnchorBlock() {
		super(Block.Properties.of(Material.STONE)
				.sound(SoundType.STONE)
				.strength(1.5f)
				.harvestLevel(1)
				.harvestTool(ToolType.PICKAXE)
				);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);
		
		if (rand.nextBoolean()) {
			final int color;
				if (rand.nextBoolean()) {
					color = 0x4D5e34eb;
				} else {
					color = 0x4D200870;
				}
			
			NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
					1,
					pos.getX() + .5, pos.getY() + .75, pos.getZ() + .5, .5, 40, 0,
					new Vector3d(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	protected BlockPos findTeleportSpot(World world, BlockPos myPos, Direction preferredDirection) {
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int y = -1; y <= 1; y++) {
			int preferredX = preferredDirection.getStepX();
			int preferredZ = preferredDirection.getStepZ();
			
			for (int x : new int[] {preferredX, 0, 0, 1, -1, 1, 1, -1, -1})
			for (int z : new int[] {preferredZ, 1, -1, 0, 0, 1, -1, 1, -1})
			{
				cursor.set(x + myPos.getX(), y + myPos.getY(), z + myPos.getZ());
				if (world.isEmptyBlock(cursor) && world.isEmptyBlock(cursor.above())) {
					return cursor.immutable();
				}
			}
		}
		
		return myPos;
	}
	
	protected void teleportEntity(World world, BlockPos pos, Entity entity) {
		if (DimensionUtils.InDimension(entity, world)) {
			final Vector3d vecToEnt = entity.position().subtract(new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5)).normalize();
			final Direction dirToEnt = Direction.getNearest(vecToEnt.x(), vecToEnt.y(), vecToEnt.z());
			BlockPos toPos = findTeleportSpot(world, pos, dirToEnt);
			
			// Special sauce to allow in sorcery dimension
			{
				entity.xOld = entity.xo = toPos.getX() + .5;
				entity.yOld = entity.yo = toPos.getY() + .005;
				entity.zOld = entity.zo = toPos.getZ() + .5;
			}
			
			entity.teleportTo(toPos.getX() + .5, toPos.getY(), toPos.getZ() + .5);
			((ServerWorld) world).sendParticles(ParticleTypes.PORTAL, toPos.getX() + .5, toPos.getY() + NostrumMagica.rand.nextDouble() * 2.0D, toPos.getZ() + .5, 30, NostrumMagica.rand.nextGaussian(), 0.0D, NostrumMagica.rand.nextGaussian(), .1);
		}
	}
	
	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!worldIn.isClientSide()) {
			LivingEntity shooter = Projectiles.getShooter(entityIn);
			if (shooter != null) {
				teleportEntity(worldIn, pos, shooter);
			}
		}
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide()) {
			teleportEntity(worldIn, pos, player);
		}
		
		return ActionResultType.SUCCESS;
	}
}
