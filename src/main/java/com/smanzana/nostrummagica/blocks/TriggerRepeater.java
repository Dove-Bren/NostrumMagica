package com.smanzana.nostrummagica.blocks;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tiles.TriggerRepeaterEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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

public class TriggerRepeater extends Block implements ITriggeredBlock {

	public static final String ID = "trigger_repeater";
	
	public TriggerRepeater() {
		super(Block.Properties.create(Material.BARRIER)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.doesNotBlockMovement()
				);
	}
	
	// GetHowMuchLightGoesThrough?? Not sure.
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
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
	@Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE; 
	}
	
	@Override
	public boolean canBeConnectedTo(BlockState state, IBlockReader world, BlockPos pos, Direction facing) {
		return false;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		// Render/particle code calls with dummy sometimes and crashes if you return an empty cube
		if (context != ISelectionContext.dummy()) {
			if (context.getEntity() != null && context.getEntity() instanceof PlayerEntity && ((PlayerEntity) context.getEntity()).isCreative()) {
				return VoxelShapes.fullCube();
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
		if (worldIn.isRemote() && NostrumMagica.instance.proxy.getPlayer() != null && NostrumMagica.instance.proxy.getPlayer().isCreative()) {
			worldIn.addParticle(ParticleTypes.BARRIER, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, 0, 0, 0);
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader reader) {
		return new TriggerRepeaterEntity();
	}

	@Override
	public void trigger(World world, BlockPos blockPos, BlockState state, BlockPos triggerPos) {
		TileEntity te = world.getTileEntity(blockPos);
		if (te instanceof TriggerRepeaterEntity) {
			((TriggerRepeaterEntity) te).trigger(triggerPos);
		}
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote() || !playerIn.isCreative()) {
			return playerIn.isCreative(); // in creative, we still want the client to think we ate the interact
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null) {
			return false;
		}
		
		TriggerRepeaterEntity ent = (TriggerRepeaterEntity) te;
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (heldItem.isEmpty()) {
			// Display info
			List<BlockPos> offsets = ent.getOffsets();
			playerIn.sendMessage(new StringTextComponent("Holding " + offsets.size() + " offsets"));
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.getDimension().getType().getId()) {
				ent.addTriggerPoint(heldPos, false);
				playerIn.sendMessage(new StringTextComponent("Added offset to " + heldPos));
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		} else if (!heldItem.isEmpty() && heldItem.getItem() == Items.PAPER) {
			// Clear
			//ent.clearOffsets();
			playerIn.sendMessage(new StringTextComponent("Cleared offsets"));
			NostrumMagicaSounds.DAMAGE_WIND.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return true;
	}
}
