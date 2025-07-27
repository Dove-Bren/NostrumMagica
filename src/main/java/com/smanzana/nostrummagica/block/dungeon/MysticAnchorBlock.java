package com.smanzana.nostrummagica.block.dungeon;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ISpellTargetBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.Projectiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Block that, when hit with an arrow or spell etc. teleports the shooter to nearby the block
 * @author Skyler
 *
 */
public class MysticAnchorBlock extends Block implements ISpellTargetBlock {
	
	public static final String ID = "mystic_anchor";
	
	protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 20.0D, 11.0D);
	
	public MysticAnchorBlock() {
		super(Block.Properties.of(Material.STONE)
				.sound(SoundType.STONE)
				.strength(1.5f)
				);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
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
					new Vec3(rand.nextFloat() * .05 - .025, rand.nextFloat() * .05, rand.nextFloat() * .05 - .025), null
					).color(color));
		}
	}
	
	protected BlockPos findTeleportSpot(Level world, BlockPos myPos, Direction preferredDirection) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
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
	
	protected void teleportEntity(Level world, BlockPos pos, Entity entity) {
		if (DimensionUtils.InDimension(entity, world)) {
			final Vec3 vecToEnt = entity.position().subtract(new Vec3(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5)).normalize();
			final Direction dirToEnt = Direction.getNearest(vecToEnt.x(), vecToEnt.y(), vecToEnt.z());
			BlockPos toPos = findTeleportSpot(world, pos, dirToEnt);
			
			// Special sauce to allow in sorcery dimension
			{
				entity.xOld = entity.xo = toPos.getX() + .5;
				entity.yOld = entity.yo = toPos.getY() + .005;
				entity.zOld = entity.zo = toPos.getZ() + .5;
			}
			
			entity.teleportTo(toPos.getX() + .5, toPos.getY(), toPos.getZ() + .5);
			((ServerLevel) world).sendParticles(ParticleTypes.PORTAL, toPos.getX() + .5, toPos.getY() + NostrumMagica.rand.nextDouble() * 2.0D, toPos.getZ() + .5, 30, NostrumMagica.rand.nextGaussian(), 0.0D, NostrumMagica.rand.nextGaussian(), .1);
			NostrumMagica.awardLore(entity, LoreRegistry.MysticAnchorLore, true);
		}
	}
	
	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		if (!worldIn.isClientSide()) {
			LivingEntity shooter = Projectiles.getShooter(entityIn);
			if (shooter != null) {
				teleportEntity(worldIn, pos, shooter);
			}
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (!worldIn.isClientSide()) {
			teleportEntity(worldIn, pos, player);
		}
		
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster, SpellLocation hitLocation, SpellEffectPart effect, SpellAction action) {
		if (!level.isClientSide()) {
			teleportEntity(level, pos, caster);
			return true;
		}
		
		return false;
	}
}
