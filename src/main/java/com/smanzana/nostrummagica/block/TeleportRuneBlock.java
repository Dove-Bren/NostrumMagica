package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.TeleportRuneTileEntity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TeleportRuneBlock extends BaseEntityBlock  {
	
	public static final String ID = "teleport_rune";
	protected static final VoxelShape RUNE_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2D, 16.0D);
	
	public TeleportRuneBlock() {
		super(Block.Properties.of(Material.CLOTH_DECORATION)
				.strength(0.5f, 5.0f)
				.sound(SoundType.STONE)
				.randomTicks()
				.noOcclusion()
				);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return true;
    }
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
		//return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return RUNE_AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	public static final int TELEPORT_RANGE = 64;
	
	
	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return InteractionResult.SUCCESS;
		}
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof PositionCrystal)) {
			if (!worldIn.isClientSide) {
				ent.doTeleport(playerIn);
			}
			return InteractionResult.SUCCESS;
		}
		
		BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
		if (heldPos == null) {
			return InteractionResult.SUCCESS;
		}
		
		if (!playerIn.isCreative()) {
			// 1) has to be another teleport rune there, and 2) has to be within X blocks
			if (!NostrumMagica.isBlockLoaded(worldIn, heldPos)) {
				playerIn.sendMessage(new TranslatableComponent("info.teleportrune.unloaded"), Util.NIL_UUID);
				return InteractionResult.SUCCESS;
			}
			
			BlockState targetState = worldIn.getBlockState(heldPos);
			if (targetState == null || !(targetState.getBlock() instanceof TeleportRuneBlock)) {
				playerIn.sendMessage(new TranslatableComponent("info.teleportrune.norune"), Util.NIL_UUID);
				return InteractionResult.SUCCESS;
			}
			
			int dist = Math.abs(heldPos.getX() - pos.getX())
					+ Math.abs(heldPos.getY() - pos.getY())
					+ Math.abs(heldPos.getZ() - pos.getZ());
			
			boolean hasEnderBelt = false;
			// Look for lightning belt
			Container baubles = NostrumMagica.instance.curios.getCurios(playerIn);
			if (baubles != null) {
				for (int i = 0; i < baubles.getContainerSize(); i++) {
					ItemStack stack = baubles.getItem(i);
					if (stack.isEmpty() || stack.getItem() != NostrumCurios.enderBelt) {
						continue;
					}
					
					hasEnderBelt = true;
					break;
				}
			}
			final boolean hasEnderSet = ElementalArmor.GetSetCount(playerIn, EMagicElement.ENDER, ElementalArmor.Type.MASTER) == 4;
			final double range = TELEPORT_RANGE * (hasEnderBelt ? 2 : 1) * (hasEnderSet ? 2 : 1);
			
			if (dist > range) {
				playerIn.sendMessage(new TranslatableComponent("info.teleportrune.toofar"), Util.NIL_UUID);
				return InteractionResult.SUCCESS;
			}
		}
		
		BlockState targetState = worldIn.getBlockState(heldPos);
		if (targetState != null && targetState.getBlock() instanceof TeleportRuneBlock) {
			;
		} else {
			heldPos = heldPos.above();
		}
		
		ent.setTargetPosition(heldPos);
		playerIn.sendMessage(new TranslatableComponent("info.generic.block_linked"), Util.NIL_UUID);
		
		// If creative, can target tele tiles that are pointing to other ones. But, if it's not pointing anywhere, we'll conveniently hook them up.
		// Non-creative placement forces them to be linked to eachother, though.
		boolean shouldLink = true;
		if (playerIn.isCreative()) {
			BlockEntity otherTE = worldIn.getBlockEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				shouldLink = (otherEnt.getOffset() == null);
			}
		}
		
		if (shouldLink) {
			BlockPos oldOffset = null;
			BlockEntity otherTE = worldIn.getBlockEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				oldOffset = otherEnt.getOffset();
				otherEnt.setTargetPosition(pos);
			}
			
			if (oldOffset != null && !playerIn.isCreative()) {
				// Unlink old one, too!
				otherTE = worldIn.getBlockEntity(heldPos.offset(oldOffset));
				if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
					TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
					otherEnt.setTargetPosition(null);
				}
			}
		}
		
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TeleportRuneTileEntity(pos, state);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return;
		}
		
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		if (ent.getOffset() != null) {
			double dx = pos.getX() + .5;
			double dy = pos.getY() + .1;
			double dz = pos.getZ() + .5;
			
			double mx = 1 * (rand.nextFloat() - .5f);
			double mz = 1 * (rand.nextFloat() - .5f);
			
			worldIn.addParticle(ParticleTypes.PORTAL, dx + mx, dy, dz + mz, mx / 3, 0.0D, mz / 3);
		}
	}
}
