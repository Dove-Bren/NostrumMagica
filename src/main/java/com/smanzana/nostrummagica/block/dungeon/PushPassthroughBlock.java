package com.smanzana.nostrummagica.block.dungeon;

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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Block that, when hit with an arrow or spell etc. teleports the shooter to nearby the block
 * @author Skyler
 *
 */
public class PushPassthroughBlock extends PushBlock {
	
	public static final String ID = "push_passthrough_block";
	
	public PushPassthroughBlock() {
		super();
	}
	
	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		return super.use(state, worldIn, pos, player, hand, hit);
	}

	@Override
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster, SpellLocation hitLocation, SpellEffectPart effect, SpellAction action) {
		return super.processSpellEffect(level, state, pos, caster, hitLocation, effect, action);
	}
	
	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		; // don't call super, because we don't want things to push these by walkingthrough them
	}
}
