package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityCyclerSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile.ISpellProjectileShape;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

/**
 * Disc projectile with a curved trajectory
 * @author Skyler
 *
 */
public class MagicCyclerShape extends SpellShape {
	
	public static class MagicCyclerShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final World world;
		private final Vector3d pos;
		private final boolean onBlocks;
		private final float duration;
		private final SpellCharacteristics characteristics;
		
		public MagicCyclerShapeInstance(ISpellState state, World world, Vector3d pos, boolean onBlocks, float duration, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.onBlocks = onBlocks;
			this.duration = duration;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			EntitySpellSaucer projectile = new EntityCyclerSpellSaucer(getState().getSelf().world, getState().getSelf(),
					MagicCyclerShapeInstance.this,
					5.0f, (int) duration * 20, onBlocks, false);
			
			world.addEntity(projectile);
		}

		@Override
		public void onProjectileHit(SpellLocation location) {
			getState().trigger(null, world, Lists.newArrayList(location), 1f, true);
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(new SpellLocation(entity.getPosition()));
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, null);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
		}

		@Override
		public void onProjectileEnd(Vector3d pos) {
			getState().triggerFail(world, new SpellLocation(pos));
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
	
	protected float getDurationSecs(SpellShapePartProperties properties) {
		//Float param is duration param
		if (properties == null || properties.level == 0) {
			return supportedFloats()[0];
		}
		return properties.level;
	}
	
	protected boolean getHitsBlocks(SpellShapePartProperties properties) {
		// We use param's flip to indicate whether we should interact with blocks
		return properties.flip;
	}
	
	@Override
	public MagicCyclerShapeInstance createInstance(ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		boolean onBlocks = getHitsBlocks(params);
		float duration = this.getDurationSecs(params);
		return new MagicCyclerShapeInstance(state, world, location.shooterPosition, onBlocks, duration, characteristics);
	}
	
	@Override
	public int getManaCost(SpellShapePartProperties properties) {
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
	public int getWeight(SpellShapePartProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return false;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(true, true, params.flip);
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, World world, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		float radius = (float) (EntityCyclerSpellSaucer.CYCLER_RADIUS + .5); // .5 for half the width of the saucer itself
		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radius));
		return super.addToPreview(builder, state, world, location, pitch, yaw, properties, characteristics);
	}
}
