package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.item.MasteryOrb;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.tile.TrialBlockTileEntity;
import com.smanzana.nostrummagica.trial.WorldTrial;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TrialBlock extends BaseEntityBlock {
	
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
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TrialBlockTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.TrialBlock);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ALTAR_AABB;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.SUCCESS;
		}
		
		if (worldIn.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof TrialBlockTileEntity))
			return InteractionResult.FAIL;
		
		TrialBlockTileEntity trialEntity = ((TrialBlockTileEntity) te);
		
		ItemStack heldItem = playerIn.getItemInHand(hand);
		
		// code for map building
		if (playerIn.isCreative() && !heldItem.isEmpty() && heldItem.getItem() instanceof InfusedGemItem) {
			trialEntity.setElement(InfusedGemItem.GetElement(heldItem));
			return InteractionResult.SUCCESS;
		}
		
		// Make sure we have an orb first
		if (heldItem.isEmpty() || !(heldItem.getItem() instanceof MasteryOrb)) {
			return InteractionResult.FAIL;
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr == null)
			return InteractionResult.FAIL;
		
		// If the player is a novice, they can do a world trial to become adept.
		// Otherwise they do a combat trial
		final EMagicElement element = trialEntity.getElement();
		final EElementalMastery mastery = attr.getElementalMastery(element);
		
		if (mastery == EElementalMastery.UNKNOWN) {
			playerIn.sendMessage(new TranslatableComponent("info.trial.weak"), Util.NIL_UUID);
		} else if (mastery == EElementalMastery.NOVICE) {
			WorldTrial trial = WorldTrial.getTrial(element);
			if (trial == null) {
				NostrumMagica.logger.error("No trial found for element " + element.name());
				return InteractionResult.FAIL;
			} else {
				if (trial.canTake(playerIn, attr)) {
					trial.start(playerIn, attr);
					if (!playerIn.isCreative()) {
						heldItem.split(1);
					}
				} else {
					playerIn.sendMessage(new TranslatableComponent("info.trial.already_have"), Util.NIL_UUID);
				}
				
				return InteractionResult.SUCCESS;
			}
		} else {
			if (mastery.isGreaterOrEqual(EElementalMastery.MASTER)) {
				playerIn.sendMessage(new TranslatableComponent("info.trial.none"), Util.NIL_UUID);
			} else {
				trialEntity.startTrial(playerIn);
				if (!playerIn.isCreative()) {
					heldItem.split(1);
				}
			}
		}
		
		return InteractionResult.SUCCESS;
	}
	
	public static void DoEffect(BlockPos shrinePos, LivingEntity entity, int color) {
		if (entity.level.isClientSide) {
			return;
		}
		
		NostrumParticles.FILLED_ORB.spawn(entity.level, new SpawnParams(
			50,
			shrinePos.getX() + .5, shrinePos.getY() + 1.75, shrinePos.getZ() + .5, 1, 40, 10,
			new TargetLocation(entity, true)
			).setTargetBehavior(TargetBehavior.ORBIT_LAZY).color(color));
	}
}
