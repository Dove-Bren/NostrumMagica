package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ISpellTargetBlock;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.tile.PushBlockTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Block that, when hit with an arrow or spell etc. teleports the shooter to nearby the block
 * @author Skyler
 *
 */
public class PushBlock extends BaseEntityBlock implements ISpellTargetBlock {
	
	public static final String ID = "push_block";
	
	private static final double RECESSED_AMT = .25;
	private static final VoxelShape RECESSED_BLOCK = Block.box(RECESSED_AMT, 0.0D, RECESSED_AMT, 16 - RECESSED_AMT, 16, 16 - RECESSED_AMT);
	
	public PushBlock() {
		super(Block.Properties.of(Material.STONE)
				.sound(SoundType.STONE)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				);
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
//		// Doesn't work like I thought :(
		return super.getInteractionShape(state, level, pos);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return RECESSED_BLOCK;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		// Visually looks good but you end up running through it :(
		if (context != null) {
			BlockEntity te = level.getBlockEntity(pos);
			if (te != null && te instanceof PushBlockTileEntity pushEntity && pushEntity.isAnimating()) {
				final float prog = pushEntity.getAnimationProgress(1f);
				if (prog < 1f) {
					final Direction direction = pushEntity.getAnimDirection();
					return RECESSED_BLOCK.move(
							direction.getStepX() * (1f-prog),
							direction.getStepY() * (1f-prog),
							direction.getStepZ() * (1f-prog)
							);
				}
			}
		}
		
		return Shapes.block();
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PushBlockTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return null;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.SUCCESS;
		}
		
		if (worldIn.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof PushBlockTileEntity pushEntity))
			return InteractionResult.FAIL;
		
		ItemStack heldItem = player.getItemInHand(hand);
		return pushEntity.onPlayerUse(player, heldItem, hit) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	@Override
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster, SpellLocation hitLocation, SpellEffectPart effect, SpellAction action) {
		if (!level.isClientSide()) {
			BlockEntity te = level.getBlockEntity(pos);
			if (te == null || !(te instanceof PushBlockTileEntity pushEntity))
				return false;
			
			return pushEntity.onSpell(caster, effect, action, hitLocation);
		}
		
		return false;
	}
	
	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		// require players push directly at the block and be at the its y level and on ground, of this even fires
		if (entity == null || !(entity instanceof Player player)) {
			return;
		}
		
		if (!player.isOnGround() || player.getBlockY() != pos.getY()) {
			return;
		}
		
		// Check direction of motion
		Vec3 diff = Vec3.atBottomCenterOf(pos).subtract(entity.position()).multiply(1f, 0f, 1f);
		Direction closest = Direction.getNearest(diff.x, diff.y, diff.z);
		final Vec3 directionVec = new Vec3(closest.getStepX(), closest.getStepY(), closest.getStepZ());
		final Vec3 motion = NostrumMagica.playerListener.getLastMove(player);
		double dot = motion.normalize().dot(directionVec);
		if (dot < .95) {
			return;
		}
		
		// Redo with look direction too
		final Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
		dot = look.dot(directionVec);
		if (dot < .95) {
			return;
		}
		
		BlockEntity te = level.getBlockEntity(pos);
		if (te == null || !(te instanceof PushBlockTileEntity pushEntity))
			return;
		
		pushEntity.onPlayerPush(player, closest);
	}
}
