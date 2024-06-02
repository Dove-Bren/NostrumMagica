package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer.ISpellSaucerShape;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.Spell.SpellState;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Disc projectile with a curved trajectory
 * @author Skyler
 *
 */
public class MagicCyclerShape extends SpellShape {
	
	public static class MagicCyclerShapeInstance extends SpellShapeInstance implements ISpellSaucerShape {

		private final World world;
		private final Vector3d pos;
		private final boolean onBlocks;
		private final float duration;
		private final SpellCharacteristics characteristics;
		
		public MagicCyclerShapeInstance(SpellState state, World world, Vector3d pos, boolean onBlocks, float duration, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.onBlocks = onBlocks;
			this.duration = duration;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			caster.getServer().runAsync(new Runnable() {

				@Override
				public void run() {
					EntitySpellSaucer projectile = new EntityCyclerSpellSaucer(NostrumEntityTypes.cyclerSpellSaucer, MagicCyclerShapeInstance.this,
							getState().getSelf(),
							5.0f, (int) duration * 20, onBlocks, false);
					
					world.addEntity(projectile);
			
				}
			
			});
		}

		@Override
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, world, Lists.newArrayList(pos)); /// TODO only force split if piercing
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, null);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
		}
	}
	
	private static final String ID = "vortex_blade";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.from(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.GINSENG, 1),
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	
	protected MagicCyclerShape(String key) {
		super(key);
	}
	
	public MagicCyclerShape() {
		this(ID);
	}
	
	@Override
	public MagicCyclerShapeInstance createInstance(SpellState state, World world, Vector3d pos, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		// We use param's flip to indicate whether we should interact with blocks
		boolean onBlocks = false;
		if (params != null)
			onBlocks = params.flip;
		
		// Float param is duration param
		float duration = this.supportedFloats()[0];
		if (params != null && params.level != 0f)
			duration = params.level;
			
		
		// Add direction
		pos = new Vector3d(pos.x, pos.y + state.getSelf().getEyeHeight(), pos.z);
		return new MagicCyclerShapeInstance(state, world, pos, onBlocks, duration, characteristics);
	}
	
	@Override
	public int getManaCost() {
		return 25;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public String getDisplayName() {
		return "Mana Cycle";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.COMPASS, 1);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return new float[]{
				10,
				20,
				50,
		};
	}

	public static NonNullList<ItemStack> costs = null;
	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		if (costs == null) {
			costs = NonNullList.from(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.COAL),
				new ItemStack(Blocks.COAL_BLOCK)
			);
		}
		return costs;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.vortex_blade.bool.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return I18n.format("modification.vortex_blade.float.name", (Object[]) null);
	}
	
	@Override
	public int getWeight() {
		return 1;
	}

	@Override
	public boolean shouldTrace(SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, params.flip);
	}
}
