package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.integration.curios.items.NostrumCurios;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.item.armor.ElementalArmor;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.TeleportRuneTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TeleportRuneBlock extends Block  {
	
	public static final String ID = "teleport_rune";
	protected static final VoxelShape RUNE_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2D, 16.0D);
	
	public TeleportRuneBlock() {
		super(Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(0.5f, 5.0f)
				.sound(SoundType.STONE)
				.tickRandomly()
				.notSolid()
				);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
		//return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return RUNE_AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	
	public static final int TELEPORT_RANGE = 64;
	
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof TeleportRuneTileEntity)) {
			return ActionResultType.SUCCESS;
		}
		TeleportRuneTileEntity ent = (TeleportRuneTileEntity) te;
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof PositionCrystal)) {
			if (!worldIn.isRemote) {
				ent.doTeleport(playerIn);
			}
			return ActionResultType.SUCCESS;
		}
		
		BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
		if (heldPos == null) {
			return ActionResultType.SUCCESS;
		}
		
		if (!playerIn.isCreative()) {
			// 1) has to be another teleport rune there, and 2) has to be within X blocks
			if (!NostrumMagica.isBlockLoaded(worldIn, heldPos)) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.unloaded"), Util.DUMMY_UUID);
				return ActionResultType.SUCCESS;
			}
			
			BlockState targetState = worldIn.getBlockState(heldPos);
			if (targetState == null || !(targetState.getBlock() instanceof TeleportRuneBlock)) {
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.norune"), Util.DUMMY_UUID);
				return ActionResultType.SUCCESS;
			}
			
			int dist = Math.abs(heldPos.getX() - pos.getX())
					+ Math.abs(heldPos.getY() - pos.getY())
					+ Math.abs(heldPos.getZ() - pos.getZ());
			
			boolean hasEnderBelt = false;
			// Look for lightning belt
			IInventory baubles = NostrumMagica.instance.curios.getCurios(playerIn);
			if (baubles != null) {
				for (int i = 0; i < baubles.getSizeInventory(); i++) {
					ItemStack stack = baubles.getStackInSlot(i);
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
				playerIn.sendMessage(new TranslationTextComponent("info.teleportrune.toofar"), Util.DUMMY_UUID);
				return ActionResultType.SUCCESS;
			}
		}
		
		BlockState targetState = worldIn.getBlockState(heldPos);
		if (targetState != null && targetState.getBlock() instanceof TeleportRuneBlock) {
			;
		} else {
			heldPos = heldPos.up();
		}
		
		ent.setTargetPosition(heldPos);
		playerIn.sendMessage(new TranslationTextComponent("info.generic.block_linked"), Util.DUMMY_UUID);
		
		// If creative, can target tele tiles that are pointing to other ones. But, if it's not pointing anywhere, we'll conveniently hook them up.
		// Non-creative placement forces them to be linked to eachother, though.
		boolean shouldLink = true;
		if (playerIn.isCreative()) {
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				shouldLink = (otherEnt.getOffset() == null);
			}
		}
		
		if (shouldLink) {
			BlockPos oldOffset = null;
			TileEntity otherTE = worldIn.getTileEntity(heldPos);
			if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
				TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
				oldOffset = otherEnt.getOffset();
				otherEnt.setTargetPosition(pos);
			}
			
			if (oldOffset != null && !playerIn.isCreative()) {
				// Unlink old one, too!
				otherTE = worldIn.getTileEntity(heldPos.add(oldOffset));
				if (otherTE != null && otherTE instanceof TeleportRuneTileEntity) {
					TeleportRuneTileEntity otherEnt = (TeleportRuneTileEntity) otherTE;
					otherEnt.setTargetPosition(null);
				}
			}
		}
		
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TeleportRuneTileEntity();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
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
