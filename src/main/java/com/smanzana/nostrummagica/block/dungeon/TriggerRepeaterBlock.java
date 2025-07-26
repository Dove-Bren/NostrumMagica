package com.smanzana.nostrummagica.block.dungeon;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TriggerRepeaterTileEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.ShapeUtil;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriggerRepeaterBlock extends BaseEntityBlock implements ITriggeredBlock {

	public static final String ID = "trigger_repeater";
	
	public TriggerRepeaterBlock() {
		super(Block.Properties.of(Material.BARRIER)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.noCollission()
				);
	}
	
	// GetHowMuchLightGoesThrough?? Not sure.
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 1.0F;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE; 
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (context != CollisionContext.empty() && context instanceof EntityCollisionContext) {
			final @Nullable Entity entity = ((EntityCollisionContext) context).getEntity();
			if (entity != null && entity instanceof Player && ((Player) entity).isCreative()) {
				return Shapes.block();
			}
		}
		
		return ShapeUtil.EMPTY_NOCRASH;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (worldIn.isClientSide() && NostrumMagica.Proxy.getPlayer() != null && NostrumMagica.Proxy.getPlayer().isCreative()) {
			worldIn.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState()), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggerRepeaterTileEntity(pos, state);
	}

	@Override
	public void trigger(Level world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		BlockEntity te = world.getBlockEntity(blockPos);
		if (te instanceof TriggerRepeaterTileEntity) {
			((TriggerRepeaterTileEntity) te).trigger(triggerPos);
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide() || !playerIn.isCreative()) {
			return playerIn.isCreative() ? InteractionResult.SUCCESS : InteractionResult.FAIL; // in creative, we still want the client to think we ate the interact
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null) {
			return InteractionResult.FAIL;
		}
		
		TriggerRepeaterTileEntity ent = (TriggerRepeaterTileEntity) te;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		if (heldItem.isEmpty()) {
			// Display info
			List<BlockPos> offsets = ent.getOffsets();
			playerIn.sendMessage(new TextComponent("Holding " + offsets.size() + " offsets"), Util.NIL_UUID);
			if (playerIn.isShiftKeyDown()) {
				for (BlockPos offset : offsets) {
					final BlockPos worldPosition = pos.offset(offset);
					playerIn.sendMessage(new TextComponent(" > " + offset), Util.NIL_UUID);
					NostrumParticles.GLOW_TRAIL.spawn(worldIn, new SpawnParams(1, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
							0, 300, 0, new TargetLocation(Vec3.atCenterOf(worldPosition))
							).setTargetBehavior(new ParticleTargetBehavior().joinMode(true)).color(1f, .8f, 1f, .3f));
				}
			}
			if (ent.getTriggerRequirement() > 1) {
				playerIn.sendMessage(new TextComponent("Triggered %d out of %d times ".formatted(ent.getCurrentTriggerCount(), ent.getTriggerRequirement())), Util.NIL_UUID);
			}
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.dimension())) {
				ent.addTriggerPoint(heldPos, false);
				playerIn.sendMessage(new TextComponent("Added offset to " + heldPos), Util.NIL_UUID);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.PAPER) {
			// Clear
			//ent.clearOffsets();
			playerIn.sendMessage(new TextComponent("Cleared offsets"), Util.NIL_UUID);
			NostrumMagicaSounds.DAMAGE_LIGHTNING.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.FEATHER) {
			// Cleanup
			final int count = ent.cleanOffests(playerIn);
			playerIn.sendMessage(new TextComponent("Cleaned " + count + " offsets"), Util.NIL_UUID);
			NostrumMagicaSounds.DAMAGE_WIND.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.CLOCK) {
			// Add requirement count
			final int newReq = ent.getTriggerRequirement() + 1;
			ent.setTriggerRequirement(newReq, false);
			playerIn.sendMessage(new TextComponent("Increased to %d required triggers before repeating".formatted(newReq)), Util.NIL_UUID);
			NostrumMagicaSounds.TOCK.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return InteractionResult.SUCCESS;
	}
}
