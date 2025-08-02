package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.property.MagicElementProperty;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.tile.ElementalCrystalBlockEntity;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ElementalCrystalBlock extends BaseEntityBlock implements ISpellTargetBlock, ILoreBlock {

	public static final String ID = "elemental_crystal_block";

	private static final double AABB_MARGIN = (5.0);
	protected static final VoxelShape UD_AABB = Block.box(AABB_MARGIN, 0, AABB_MARGIN, 16 - AABB_MARGIN, 16, 16 - AABB_MARGIN);
	protected static final VoxelShape NS_AABB = Block.box(AABB_MARGIN, AABB_MARGIN, 0, 16 - AABB_MARGIN, 16 - AABB_MARGIN, 16);
	protected static final VoxelShape EW_AABB = Block.box(0, AABB_MARGIN, AABB_MARGIN, 16, 16 - AABB_MARGIN, 16 - AABB_MARGIN);
	protected static final VoxelShape INNER = Block.box(AABB_MARGIN - 2, AABB_MARGIN + 2, AABB_MARGIN - 2, 16 - (AABB_MARGIN - 2), 16 - (AABB_MARGIN + 2), 16 - (AABB_MARGIN - 2));
	protected static final VoxelShape AABB = Shapes.or(UD_AABB, NS_AABB, EW_AABB, INNER);
	
	public static final MagicElementProperty ELEMENT = MagicElementProperty.create("element");
	
	public ElementalCrystalBlock() {
		super(Block.Properties.of(Material.AMETHYST)
				.strength(30f, 1200.0f)
				.sound(SoundType.AMETHYST_CLUSTER)
				.noOcclusion()
				.noDrops()
				.lightLevel((state) -> 12)
				);
		
		this.registerDefaultState(this.defaultBlockState().setValue(ELEMENT, EMagicElement.NEUTRAL));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ELEMENT);
	}
	
	public EMagicElement getElement(BlockState state) {
		return state.getValue(ELEMENT);
	}
	
	public BlockState setElement(BlockState state, EMagicElement element) {
		return state.setValue(ELEMENT, element);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AABB;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}
	
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ElementalCrystalBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.ElementalCrystal);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
				10,
				pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .5, 50, 40,
				new Vec3(0, .0125, 0), new Vec3(.025, .025, .025)
				).color(this.getElement(stateIn).getColor()));
	}

	@Override
	public boolean processSpellEffect(Level level, BlockState state, BlockPos pos, LivingEntity caster, SpellLocation hitLocation, SpellEffectPart part, SpellAction action) {
		// Blocks require ENCHANT and take the element
		final EMagicElement element = getElement(state);
		
		// Only neutral can be changed by non-creative players
		if ((!(caster instanceof Player player) || !player.isCreative()) && element != EMagicElement.NEUTRAL) {
			return false;
		}
		
		if (part.getElement() != element
				&& part.getAlteration() == EAlteration.ENCHANT) {
			level.setBlock(pos, state.setValue(ELEMENT, part.getElement()), Block.UPDATE_ALL);
			return true;
		}
		
		return false;
	}
	
	public static final int MakeBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
		if (state.getBlock() instanceof ElementalCrystalBlock block) {
			return RenderFuncs.ARGBFade(block.getElement(state).getColor(), .6f);
		}
		return 0x80FFFFFF;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (!worldIn.isClientSide()) {
			NostrumMagica.awardLore(playerIn, this, NostrumMagica.rand.nextInt(20) == 0);
		}
		return InteractionResult.PASS;
	}
	
}
