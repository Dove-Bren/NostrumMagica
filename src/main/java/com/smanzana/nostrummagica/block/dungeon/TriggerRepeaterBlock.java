package com.smanzana.nostrummagica.block.dungeon;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.TriggerRepeaterTileEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriggerRepeaterBlock extends Block implements ITriggeredBlock {

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
	public float getShadeBrightness(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return 1.0F;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return true;
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.INVISIBLE; 
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (context != ISelectionContext.empty()) {
			if (context.getEntity() != null && context.getEntity() instanceof PlayerEntity && ((PlayerEntity) context.getEntity()).isCreative()) {
				return VoxelShapes.block();
			}
		}
		
		return VoxelShapes.empty();
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (worldIn.isClientSide() && NostrumMagica.instance.proxy.getPlayer() != null && NostrumMagica.instance.proxy.getPlayer().isCreative()) {
			worldIn.addParticle(ParticleTypes.BARRIER, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader reader) {
		return new TriggerRepeaterTileEntity();
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		TileEntity te = world.getBlockEntity(blockPos);
		if (te instanceof TriggerRepeaterTileEntity) {
			((TriggerRepeaterTileEntity) te).trigger(triggerPos);
		}
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isClientSide() || !playerIn.isCreative()) {
			return playerIn.isCreative() ? ActionResultType.SUCCESS : ActionResultType.FAIL; // in creative, we still want the client to think we ate the interact
		}
		
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te == null) {
			return ActionResultType.FAIL;
		}
		
		TriggerRepeaterTileEntity ent = (TriggerRepeaterTileEntity) te;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		if (heldItem.isEmpty()) {
			// Display info
			List<BlockPos> offsets = ent.getOffsets();
			playerIn.sendMessage(new StringTextComponent("Holding " + offsets.size() + " offsets"), Util.NIL_UUID);
			if (playerIn.isShiftKeyDown()) {
				for (BlockPos offset : offsets) {
					playerIn.sendMessage(new StringTextComponent(" > " + offset), Util.NIL_UUID);
				}
			}
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && DimensionUtils.DimEquals(PositionCrystal.getDimension(heldItem), worldIn.dimension())) {
				ent.addTriggerPoint(heldPos, false);
				playerIn.sendMessage(new StringTextComponent("Added offset to " + heldPos), Util.NIL_UUID);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.PAPER) {
			// Clear
			//ent.clearOffsets();
			playerIn.sendMessage(new StringTextComponent("Cleared offsets"), Util.NIL_UUID);
			NostrumMagicaSounds.DAMAGE_LIGHTNING.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.FEATHER) {
			// Cleanup
			final int count = ent.cleanOffests(playerIn);
			playerIn.sendMessage(new StringTextComponent("Cleaned " + count + " offsets"), Util.NIL_UUID);
			NostrumMagicaSounds.DAMAGE_WIND.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return ActionResultType.SUCCESS;
	}
}
