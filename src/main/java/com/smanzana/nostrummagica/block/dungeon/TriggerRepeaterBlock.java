package com.smanzana.nostrummagica.block.dungeon;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TriggerRepeaterTileEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
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
		if (context != CollisionContext.empty()) {
			if (context.getEntity() != null && context.getEntity() instanceof Player && ((Player) context.getEntity()).isCreative()) {
				return Shapes.block();
			}
		}
		
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (worldIn.isClientSide() && NostrumMagica.instance.proxy.getPlayer() != null && NostrumMagica.instance.proxy.getPlayer().isCreative()) {
			worldIn.addParticle(ParticleTypes.BARRIER, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggerRepeaterTileEntity();
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
					playerIn.sendMessage(new TextComponent(" > " + offset), Util.NIL_UUID);
				}
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
		}
		
		return InteractionResult.SUCCESS;
	}
}
