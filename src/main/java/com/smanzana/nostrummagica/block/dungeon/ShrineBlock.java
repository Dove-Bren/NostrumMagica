package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.item.SpellRune.AlterationSpellRune;
import com.smanzana.nostrummagica.item.SpellRune.ElementSpellRune;
import com.smanzana.nostrummagica.item.SpellRune.ShapeSpellRune;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.tile.ShrineTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

public abstract class ShrineBlock<E extends ShrineTileEntity<?>> extends Block {
	
	public static final String ID_ELEMENT = "element_shrine";
	public static final String ID_ALTERATION = "alteration_shrine";
	public static final String ID_SHAPE = "shape_shrine";
	
	protected static final VoxelShape BASE_AABB = Block.makeCuboidShape(16 * 0.3D, 16 * 0.0D, 16 * 0.3D, 16 * 0.7D, 7, 16 * 0.7D);
	
	protected ShrineBlock() {
		super(Block.Properties.create(Material.BARRIER)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.notSolid()
				.setLightLevel((state) -> 16)
				);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return BASE_AABB;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		if (hand != Hand.MAIN_HAND || !playerIn.isCreative()) {
			return ActionResultType.PASS;
		}
		
		return handleConfigure(worldIn, pos, state, playerIn, playerIn.getHeldItem(hand));
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
	protected abstract ActionResultType handleConfigure(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack);
	
	public abstract void handleRelease(World world, BlockPos pos, BlockState state, PlayerEntity player);
	
	
	public static void DoEffect(BlockPos shrinePos, LivingEntity entity, int color) {
		if (entity.world.isRemote) {
			return;
		}
		
		NostrumParticles.FILLED_ORB.spawn(entity.world, new SpawnParams(
			50,
			shrinePos.getX() + .5, shrinePos.getY() + 1.75, shrinePos.getZ() + .5, 1, 40, 10,
			entity.getEntityId()
			).color(color));
	}
	
	@SuppressWarnings("unchecked")
	protected @Nullable E getTileEntity(World world, BlockPos pos, BlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te == null || !(te instanceof ShrineTileEntity))
			return null;
		
		return (E) te;
	}
	
	public static class Element extends ShrineBlock<ShrineTileEntity.Element> {
		
		public Element() {
			super();
		}
		
		@Override
		public boolean hasTileEntity(BlockState state) {
			return true;
		}
		
		@Override
		public ShrineTileEntity.Element createTileEntity(BlockState state, IBlockReader world) {
			ShrineTileEntity.Element ent = new ShrineTileEntity.Element();
			return ent;
		}

		@Override
		protected ActionResultType handleConfigure(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack heldItem) {
			ShrineTileEntity.Element tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return ActionResultType.FAIL;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof ElementSpellRune) {
				tile.setElement(SpellRune.getElement(heldItem));
				return ActionResultType.SUCCESS;
			}
			
			return ActionResultType.PASS;
		}

		@Override
		public void handleRelease(World world, BlockPos pos, BlockState state, PlayerEntity player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			ShrineTileEntity.Element tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return;
			}
			
			// Shrine blocks grant novice mastery of their elements
			final EMagicElement element = tile.getElement();
			
			if (attr.getElementalMastery(element) == EElementalMastery.UNKNOWN
					&& attr.setElementalMastery(element, EElementalMastery.NOVICE)) {
				// Just learned!
				final int color = 0x80000000 | (0x00FFFFFF & element.getColor());
				DoEffect(pos, player, color);
			} else {
				if (player.world.isRemote) {
					player.sendMessage(new TranslationTextComponent("info.shrine.seektrial"), Util.DUMMY_UUID);
				}
			}
		}
	}
	
	public static class Shape extends ShrineBlock<ShrineTileEntity.Shape> {
		
		public Shape() {
			super();
		}
		
		@Override
		public boolean hasTileEntity(BlockState state) {
			return true;
		}
		
		@Override
		public ShrineTileEntity.Shape createTileEntity(BlockState state, IBlockReader world) {
			ShrineTileEntity.Shape ent = new ShrineTileEntity.Shape();
			return ent;
		}

		@Override
		protected ActionResultType handleConfigure(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack heldItem) {
			ShrineTileEntity.Shape tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return ActionResultType.FAIL;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof ShapeSpellRune) {
				tile.setShape(SpellRune.getShapePart(heldItem).getShape());
				return ActionResultType.SUCCESS;
			}
			
			return ActionResultType.PASS;
		}

		@Override
		public void handleRelease(World world, BlockPos pos, BlockState state, PlayerEntity player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			ShrineTileEntity.Shape tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return;
			}
			
			final SpellShape shape = tile.getShape();
			
			if (!attr.getShapes().contains(shape)) {
				attr.addShape(shape);
				DoEffect(pos, player, 0x8080C0A0);
				if (player.world.isRemote) {
					player.sendMessage(new TranslationTextComponent("info.shrine.shape", new Object[] {shape.getDisplayName()}), Util.DUMMY_UUID);
				}
			}
		}
	}
	
	public static class Alteration extends ShrineBlock<ShrineTileEntity.Alteration> {
		
		public Alteration() {
			super();
		}
		
		@Override
		public boolean hasTileEntity(BlockState state) {
			return true;
		}
		
		@Override
		public ShrineTileEntity.Alteration createTileEntity(BlockState state, IBlockReader world) {
			ShrineTileEntity.Alteration ent = new ShrineTileEntity.Alteration();
			return ent;
		}

		@Override
		protected ActionResultType handleConfigure(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack heldItem) {
			ShrineTileEntity.Alteration tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return ActionResultType.FAIL;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof AlterationSpellRune) {
				tile.setAlteration(SpellRune.getAlteration(heldItem));
				return ActionResultType.SUCCESS;
			}
			
			return ActionResultType.PASS;
		}

		@Override
		public void handleRelease(World world, BlockPos pos, BlockState state, PlayerEntity player) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			ShrineTileEntity.Alteration tile = getTileEntity(world, pos, state);
			if (tile == null) {
				return;
			}
			
			final EAlteration alteration = tile.getAlteration();
			
			if (!attr.getAlterations().getOrDefault(alteration, false)) {
				attr.unlockAlteration(alteration);
				DoEffect(pos, player, 0x80808ABF);
				if (player.world.isRemote) {
					player.sendMessage(new TranslationTextComponent("info.shrine.alteration", alteration.getName()), Util.DUMMY_UUID);
				}
			}
		}
	}
}
