package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.item.MasteryOrb;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.tile.TrialBlockTileEntity;
import com.smanzana.nostrummagica.trial.WorldTrial;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TrialBlock extends Block {
	
	public static final String ID = "trial_block";
	protected static final VoxelShape ALTAR_AABB = Block.box(16 * 0.3D, 16 * 0.0D, 16 * 0.3D, 16 * 0.7D, 16 * 0.8D, 16 * 0.7D);
	
	public TrialBlock() {
		super(Block.Properties.of(Material.BARRIER)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.lightLevel((state) -> 16)
				);
	}
	
//	@Override
//	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
//		return true;
//	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TrialBlockTileEntity();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return ALTAR_AABB;
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		if (hand != Hand.MAIN_HAND) {
			return ActionResultType.SUCCESS;
		}
		
		if (worldIn.isClientSide()) {
			return ActionResultType.SUCCESS;
		}
		
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof TrialBlockTileEntity))
			return ActionResultType.FAIL;
		
		TrialBlockTileEntity trialEntity = ((TrialBlockTileEntity) te);
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		// code for map building
		if (playerIn.isCreative() && !heldItem.isEmpty() && heldItem.getItem() instanceof InfusedGemItem) {
			trialEntity.setElement(InfusedGemItem.GetElement(heldItem));
			return ActionResultType.SUCCESS;
		}
		
		// Make sure we have an orb first
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof MasteryOrb)) {
			return ActionResultType.FAIL;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr == null)
			return ActionResultType.FAIL;
		
		// If the player is a novice, they can do a world trial to become adept.
		// Otherwise they do a combat trial
		final EMagicElement element = trialEntity.getElement();
		final EElementalMastery mastery = attr.getElementalMastery(element);
		
		if (mastery == EElementalMastery.UNKNOWN) {
			playerIn.sendMessage(new TranslationTextComponent("info.trial.weak"), Util.NIL_UUID);
		} else if (mastery == EElementalMastery.NOVICE) {
			WorldTrial trial = WorldTrial.getTrial(element);
			if (trial == null) {
				NostrumMagica.logger.error("No trial found for element " + element.name());
				return ActionResultType.FAIL;
			} else {
				if (trial.canTake(playerIn, attr)) {
					trial.start(playerIn, attr);
					if (!playerIn.isCreative()) {
						heldItem.split(1);
					}
				} else {
					playerIn.sendMessage(new TranslationTextComponent("info.trial.already_have"), Util.NIL_UUID);
				}
				
				return ActionResultType.SUCCESS;
			}
		} else {
			if (mastery.isGreaterOrEqual(EElementalMastery.MASTER)) {
				playerIn.sendMessage(new TranslationTextComponent("info.trial.none"), Util.NIL_UUID);
			} else {
				trialEntity.startTrial(playerIn);
				if (!playerIn.isCreative()) {
					heldItem.split(1);
				}
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	public static void DoEffect(BlockPos shrinePos, LivingEntity entity, int color) {
		if (entity.level.isClientSide) {
			return;
		}
		
		NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
			50,
			shrinePos.getX() + .5, shrinePos.getY() + 1.75, shrinePos.getZ() + .5, 1, 40, 10,
			entity.getId()
			).color(color));
	}
}
