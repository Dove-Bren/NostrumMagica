package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.ElementalMastery;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spells.components.shapes.ChainShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.tiles.SymbolTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShrineBlock extends SymbolBlock {
	
	public static final String ID = "shrine_block";
	private static final BooleanProperty EXHAUSTED = BooleanProperty.create("exhausted");
	protected static final VoxelShape ALTAR_AABB = Block.makeCuboidShape(16 * 0.3D, 16 * 0.0D, 16 * 0.3D, 16 * 0.7D, 16 * 0.8D, 16 * 0.7D);
	
	public ShrineBlock() {
		super();
		this.setDefaultState(this.stateContainer.getBaseState().with(EXHAUSTED, false));
	}
	
	public boolean isExhausted(BlockState state) {
		return state.get(EXHAUSTED);
	}
	
	public BlockState getExhaustedState(boolean exhausted) {
		return this.getDefaultState().with(EXHAUSTED, true);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(EXHAUSTED);
	}
	
//	@Override
//	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
//		return true;
//	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return super.createTileEntity(state, world);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return ALTAR_AABB;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		if (hand != Hand.MAIN_HAND) {
			return true;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SymbolTileEntity))
			return false;
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		// code for map building
		if (playerIn.isCreative() && !heldItem.isEmpty() && heldItem.getItem() instanceof SpellRune) {
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(heldItem);
			if (comp != null) {
				((SymbolTileEntity) te).setComponent(comp);
				return true;
			}
		}
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
		if (attr == null)
			return false;
		
		
		
		SymbolTileEntity tile = (SymbolTileEntity) te;
		SpellComponentWrapper component = tile.getComponent();
		
//		// Check for binding first
//		if (attr.isBinding()) {
//			if (attr.getBindingComponent().equals(component)) {
//				attr.completeBinding(null);
//				return true;
//			}
//		}
		
		if (component.isElement()) {
			// Shrine blocks grant novice mastery of their elements
			final EMagicElement element = component.getElement();
			
			if (attr.getElementalMastery(element) == ElementalMastery.UNKNOWN
					&& attr.setElementalMastery(element, ElementalMastery.NOVICE)) {
				// Just learned!
				final int color = 0x80000000 | (0x00FFFFFF & element.getColor());
				DoEffect(pos, playerIn, color);
			}
			
			return true;
		}
		
		if (component.isTrigger()) {
			if (attr.getTriggers().contains(component.getTrigger()))
				return false;
			
			if (isExhausted(state)) {
				if (playerIn.world.isRemote) {
					playerIn.sendMessage(new TranslationTextComponent("info.shrine.exhausted", new Object[0]));
				}
				return true;
			}
			
			attr.addTrigger(component.getTrigger());
			
			DoEffect(pos, playerIn, 0x8080A0C0);
			worldIn.setBlockState(pos, getExhaustedState(true));
			SymbolTileEntity ent = (SymbolTileEntity) worldIn.getTileEntity(pos);
			ent.setComponent(component);
			
			if (playerIn.world.isRemote) {
				playerIn.sendMessage(new TranslationTextComponent("info.shrine.trigger", new Object[] {component.getTrigger().getDisplayName()}));
			}
		}
		
		if (component.isShape()) {
			boolean pass = false;
			if (component.getShape() instanceof SingleShape) {
				pass = true;
			}
			
			if (!heldItem.isEmpty() && heldItem.getItem() instanceof SpellScroll) {
				Spell spell = SpellScroll.getSpell(heldItem);
				if (spell != null) {
					// What we require depends on the shape
					if (component.getShape() instanceof AoEShape) {
						boolean speed, leap;
						speed = leap = false;
						for (SpellPart part : spell.getSpellParts()) {
							if (part.isTrigger())
								continue;
							if (!(part.getShape() instanceof SingleShape))
								continue;
							
							if (part.getAlteration() == null)
								continue;
							
							if (!speed
									&& part.getElement() == EMagicElement.WIND
									&& part.getAlteration() == EAlteration.SUPPORT) {
								speed = true;
								continue;
							}
							
							if (!leap
									&& part.getElement() == EMagicElement.LIGHTNING
									&& part.getAlteration() == EAlteration.GROWTH) {
								leap = true;
								continue;
							}
						}
						
						if (speed && leap)
							pass = true;
					} else if (component.getShape() instanceof ChainShape) {
						boolean ice, weak;
						ice = weak = false;
						for (SpellPart part : spell.getSpellParts()) {
							if (part.isTrigger())
								continue;
							if (!(part.getShape() instanceof SingleShape))
								continue;
							
							if (!ice
									&& part.getElement() == EMagicElement.ICE
									&& part.getAlteration() == null
									&& part.getElementCount() >= 2) {
								ice = true;
								continue;
							}
							
							if (!weak
									&& part.getElement() == EMagicElement.PHYSICAL
									&& part.getAlteration() == EAlteration.INFLICT) {
								weak = true;
								continue;
							}
						}
						
						if (ice && weak)
							pass = true;
					}
				}
			}
			
			if (pass && !attr.getShapes().contains(component.getShape())) {
				attr.addShape(component.getShape());
				DoEffect(pos, playerIn, 0x8080C0A0);
				if (playerIn.world.isRemote) {
					playerIn.sendMessage(new TranslationTextComponent("info.shrine.shape", new Object[] {component.getShape().getDisplayName()}));
				}
				
				if (!(component.getShape() instanceof SingleShape)) {
					playerIn.setHeldItem(hand, ItemStack.EMPTY);
				}
			} else if (!pass) {
				// Shape that we haven't correctly unlocked yet
				if (playerIn.world.isRemote) {
					String suffix = "";
					if (component.getShape() instanceof AoEShape) {
						suffix = "aoe";
					} else if (component.getShape() instanceof ChainShape) {
						suffix = "chain";
					}
					
					TranslationTextComponent trans = new TranslationTextComponent("info.shapehint.preamble", new Object[0]);
					trans.getStyle().setColor(TextFormatting.DARK_GRAY);
					playerIn.sendMessage(trans);
					
					trans = new TranslationTextComponent("info.shapehint." + suffix, new Object[0]);
					trans.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
					playerIn.sendMessage(trans);
				}
			}
		}
		
		return false;
	}
	
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
}
