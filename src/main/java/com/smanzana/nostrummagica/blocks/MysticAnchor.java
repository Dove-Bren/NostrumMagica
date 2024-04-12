package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.utils.Projectiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
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
public class MysticAnchor extends Block {
	
	public static final String ID = "mystic_anchor";
	
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 20.0D, 11.0D);
	
	public MysticAnchor() {
		super(Block.Properties.create(Material.ROCK)
				.sound(SoundType.STONE)
				.hardnessAndResistance(1.5f)
				.harvestLevel(1)
				.harvestTool(ToolType.PICKAXE)
				);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
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
		MutableBlockPos cursor = new MutableBlockPos();
		for (int y = -1; y <= 1; y++) {
			int preferredX = preferredDirection.getXOffset();
			int preferredZ = preferredDirection.getZOffset();
			
			for (int x : new int[] {preferredX, 0, 0, 1, -1, 1, 1, -1, -1})
			for (int z : new int[] {preferredZ, 1, -1, 0, 0, 1, -1, 1, -1})
			{
				cursor.setPos(x + myPos.getX(), y + myPos.getY(), z + myPos.getZ());
				if (world.isAirBlock(cursor) && world.isAirBlock(cursor.up())) {
					return cursor.toImmutable();
				}
			}
		}
		
		return myPos;
	}
	
	protected void teleportEntity(World world, BlockPos pos, Entity entity) {
		if (entity.dimension == world.getDimension().getType()) {
			final Vector3d vecToEnt = entity.getPositionVec().subtract(new Vector3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5)).normalize();
			final Direction dirToEnt = Direction.getFacingFromVector(vecToEnt.getX(), vecToEnt.getY(), vecToEnt.getZ());
			BlockPos toPos = findTeleportSpot(world, pos, dirToEnt);
			
			// Special sauce to allow in sorcery dimension
			{
				entity.lastTickPosX = entity.prevPosX = toPos.getX() + .5;
				entity.lastTickPosY = entity.prevPosY = toPos.getY() + .005;
				entity.lastTickPosZ = entity.prevPosZ = toPos.getZ() + .5;
			}
			
			entity.setPositionAndUpdate(toPos.getX() + .5, toPos.getY(), toPos.getZ() + .5);
			((ServerWorld) world).spawnParticle(ParticleTypes.PORTAL, toPos.getX() + .5, toPos.getY() + NostrumMagica.rand.nextDouble() * 2.0D, toPos.getZ() + .5, 30, NostrumMagica.rand.nextGaussian(), 0.0D, NostrumMagica.rand.nextGaussian(), .1);
		}
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!worldIn.isRemote()) {
			LivingEntity shooter = Projectiles.getShooter(entityIn);
			if (shooter != null) {
				teleportEntity(worldIn, pos, shooter);
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!worldIn.isRemote()) {
			teleportEntity(worldIn, pos, player);
		}
		
		return true;
	}
}
