package com.smanzana.nostrummagica.block;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.fluid.PoisonWaterFluid;
import com.smanzana.nostrummagica.loretag.IBlockLoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class PoisonWaterBlock extends LiquidBlock {

	public static final String ID_BREAKABLE = "poison_water_block";
	public static final String ID_UNBREAKABLE = "poison_water_unbreakable_block";
	
	private final boolean unbreakable;

	public PoisonWaterBlock(Supplier<? extends FlowingFluid> supplier, boolean unbreakable) {
		super(supplier, Block.Properties.of(Material.WATER)
				.noCollission().strength(unbreakable ? 3600000.8F : 100.0F).noDrops()
				);
		
		this.unbreakable = unbreakable;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	@Override
	public Fluid takeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state) {
		// want to do this but we don't know the player
//		final boolean allowed;
//		if (this.unbreakable) {
//			allowed = false;
//		} else {
//			// always allow
//			allowed = true;
//		}
		
		final boolean allowed = !this.unbreakable;
		
		if (allowed) {
			return this.getFluid();
		} else {
			return Fluids.EMPTY;
		}
	}
	
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		return !unbreakable;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
		if (!world.isClientSide
				&& entity instanceof LivingEntity) {
			if (entity.tickCount % 10 == 0) {
				LivingEntity living = (LivingEntity) entity;
				
				@Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(living);
				if (attr != null && attr.isUnlocked()) {
					// Either give full or basic randomly, so that the average experience
					// is that players have to stay in the water long enough to get full
					if (attr.hasLore(PoisonWaterTag.instance()) && world.random.nextInt(50) == 0) {
						attr.giveFullLore(PoisonWaterTag.instance());
					} else {
						attr.giveBasicLore(PoisonWaterTag.instance());
					}
				}
				
				// Mystic air effect prevents poison water damage
				final MobEffectInstance instance = living.getEffect(NostrumEffects.mysticAir);
				if (instance != null && instance.getDuration() > 0) {
					return;
				}
				
				living.hurt(PoisonWaterFluid.PoisonWaterDamageSource, .25f);
			}
		}
		
		super.entityInside(state, world, pos, entity);
	}
	
	public static final class PoisonWaterTag implements IBlockLoreTagged {
		
		private static final String LoreKey = "poison_water";
		
		private static final PoisonWaterTag instance = new PoisonWaterTag();
		public static final PoisonWaterTag instance() {
			return instance;
		}
	
		@Override
		public String getLoreKey() {
			return LoreKey;
		}
	
		@Override
		public String getLoreDisplayName() {
			return "Poison Water";
		}
		
		@Override
		public Lore getBasicLore() {
			return new Lore().add("Dangerous poisonous water that does damage on contact.", "You imagine there is some magical means to prevent the damage...");
					
		}
		
		@Override
		public Lore getDeepLore() {
			return new Lore().add("Dangerous poisonous water that does damage on contact.", "You can prevent the negative effects of poison water with the Mystic Air effect!");
		}

		@Override
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_BLOCKS;
		}

		@Override
		public Block getBlock() {
			return NostrumBlocks.poisonWaterBlock;
		}
	}
}
