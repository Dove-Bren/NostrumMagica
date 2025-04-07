package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.item.ResourceCrystal;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.SpellRune.AlterationSpellRune;
import com.smanzana.nostrummagica.item.SpellRune.ElementSpellRune;
import com.smanzana.nostrummagica.item.SpellRune.ShapeSpellRune;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.ShrineTileEntity;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ShrineBlock<E extends ShrineTileEntity<?>> extends BaseEntityBlock {
	
	public static final String ID_ELEMENT = "element_shrine";
	public static final String ID_ALTERATION = "alteration_shrine";
	public static final String ID_SHAPE = "shape_shrine";
	public static final String ID_TIER = "tier_shrine";
	
	protected static final VoxelShape BASE_AABB = Block.box(16 * 0.3D, 16 * 0.0D, 16 * 0.3D, 16 * 0.7D, 7, 16 * 0.7D);
	
	protected ShrineBlock() {
		super(Block.Properties.of(Material.BARRIER)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.noOcclusion()
				.lightLevel((state) -> 16)
				);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return BASE_AABB;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		
		if (hand != InteractionHand.MAIN_HAND || !playerIn.isCreative()) {
			return InteractionResult.PASS;
		}
		
		return handleConfigure(worldIn, pos, state, playerIn, playerIn.getItemInHand(hand));
	}
	
	/**
	 * Called when players interact with the block directly.
	 * Should be used to help specify the type in creative mode
	 * @param world
	 * @param pos
	 * @param state
	 * @param player
	 * @param stack
	 * @return
	 */
	protected abstract InteractionResult handleConfigure(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack);
	
	@SuppressWarnings("unchecked")
	protected @Nullable E getTileEntity(Level world, BlockPos pos, BlockState state) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te == null || !(te instanceof ShrineTileEntity))
			return null;
		
		return (E) te;
	}
	
	public static class Element extends ShrineBlock<ShrineTileEntity.Element> {
		
		public Element() {
			super();
		}
		
		@Override
		public ShrineTileEntity.Element newBlockEntity(BlockPos pos, BlockState state) {
			ShrineTileEntity.Element ent = new ShrineTileEntity.Element(pos, state);
			return ent;
		}
		
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
			return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.ElementShrineTileType);
		}

		@Override
		protected InteractionResult handleConfigure(Level world, BlockPos pos, BlockState state, Player player, ItemStack heldItem) {
			ShrineTileEntity.Element tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return InteractionResult.FAIL;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof ElementSpellRune) {
				tile.setElement(SpellRune.getElement(heldItem));
				return InteractionResult.SUCCESS;
			}
			
			return InteractionResult.PASS;
		}
	}
	
	public static class Shape extends ShrineBlock<ShrineTileEntity.Shape> {
		
		public Shape() {
			super();
		}
		
		@Override
		public ShrineTileEntity.Shape newBlockEntity(BlockPos pos, BlockState state) {
			ShrineTileEntity.Shape ent = new ShrineTileEntity.Shape(pos, state);
			return ent;
		}
		
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
			return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.ShapeShrineTileType);
		}

		@Override
		protected InteractionResult handleConfigure(Level world, BlockPos pos, BlockState state, Player player, ItemStack heldItem) {
			ShrineTileEntity.Shape tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return InteractionResult.FAIL;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof ShapeSpellRune) {
				tile.setShape(SpellRune.getShapePart(heldItem).getShape());
				return InteractionResult.SUCCESS;
			}
			
			return InteractionResult.PASS;
		}
	}
	
	public static class Alteration extends ShrineBlock<ShrineTileEntity.Alteration> {
		
		public Alteration() {
			super();
		}
		
		@Override
		public ShrineTileEntity.Alteration newBlockEntity(BlockPos pos, BlockState state) {
			ShrineTileEntity.Alteration ent = new ShrineTileEntity.Alteration(pos, state);
			return ent;
		}
		
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
			return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.AlterationShrineTileType);
		}

		@Override
		protected InteractionResult handleConfigure(Level world, BlockPos pos, BlockState state, Player player, ItemStack heldItem) {
			ShrineTileEntity.Alteration tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return InteractionResult.FAIL;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof AlterationSpellRune) {
				tile.setAlteration(SpellRune.getAlteration(heldItem));
				return InteractionResult.SUCCESS;
			}
			
			return InteractionResult.PASS;
		}
	}
	
	public static class Tier extends ShrineBlock<ShrineTileEntity.Tier> {
		
		public Tier() {
			super();
		}
		
		@Override
		public ShrineTileEntity.Tier newBlockEntity(BlockPos pos, BlockState state) {
			ShrineTileEntity.Tier ent = new ShrineTileEntity.Tier(pos, state);
			return ent;
		}
		
		@Override
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
			return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.TierShrineTileType);
		}

		@Override
		protected InteractionResult handleConfigure(Level world, BlockPos pos, BlockState state, Player player, ItemStack heldItem) {
			ShrineTileEntity.Tier tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return InteractionResult.FAIL;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof ResourceCrystal) {
				tile.setTier(((ResourceCrystal) heldItem.getItem()).getTier());
				return InteractionResult.SUCCESS;
			}
			
			return InteractionResult.PASS;
		}
	}
}
