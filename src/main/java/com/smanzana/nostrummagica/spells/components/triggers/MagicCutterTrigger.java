package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.entity.EntityChakramSpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer;
import com.smanzana.nostrummagica.entity.EntitySpellSaucer.ISpellSaucerTrigger;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class MagicCutterTrigger extends SpellTrigger {
	
	public class MagicCutterTriggerInstance extends SpellTrigger.SpellTriggerInstance implements ISpellSaucerTrigger {

		private World world;
		private Vec3d pos;
		private float pitch;
		private float yaw;
		private boolean piercing;
		
		public MagicCutterTriggerInstance(SpellState state, World world, Vec3d pos, float pitch, float yaw, boolean piercing) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.piercing = piercing;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vec3d dir;
			if (caster instanceof EntityLiving && ((EntityLiving) caster).getAttackTarget() != null) {
				EntityLiving ent = (EntityLiving) caster  ;
				dir = ent.getAttackTarget().getPositionVector().addVector(0.0, ent.height / 2.0, 0.0)
						.subtract(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			} else {
				dir = MagicCutterTrigger.getVectorForRotation(pitch, yaw);
			}
			
			final MagicCutterTriggerInstance self = this;
			
			caster.getServer().addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntitySpellSaucer projectile = new EntityChakramSpellSaucer(self, 
							getState().getSelf(),
							world,
							pos.xCoord, pos.yCoord, pos.zCoord,
							dir,
							5.0f, piercing ? PROJECTILE_RANGE/2 : PROJECTILE_RANGE, piercing);
					
//					EntitySpellSaucer projectile = new EntityCyclerSpellSaucer(self,
//							getState().getSelf(),
//							5.0f, 500);
					
					world.spawnEntityInWorld(projectile);
			
				}
			
			});
		}
		
		@Override
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, Lists.newArrayList(getState().getOther()), world, Lists.newArrayList(pos), piercing); /// TODO only force split if piercing
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (!(entity instanceof EntityLivingBase)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList((EntityLivingBase) entity), Lists.newArrayList(getState().getOther()), null, null, piercing);
			}
		}
	}

	private static final String TRIGGER_KEY = "trigger_cutter";
	private static MagicCutterTrigger instance = null;
	
	public static MagicCutterTrigger instance() {
		if (instance == null)
			instance = new MagicCutterTrigger();
		
		return instance;
	}
	
	private MagicCutterTrigger() {
		super(TRIGGER_KEY);
	}

	private static final double PROJECTILE_RANGE = 50.0;
	
	@Override
	public int getManaCost() {
		return 20;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		// We use param's flip to indicate whether we should be piercing or not
		boolean piercing = false;
		if (params != null)
			piercing = params.flip;
		
		// Add direction
		pos = new Vec3d(pos.xCoord, pos.yCoord + state.getSelf().getEyeHeight(), pos.zCoord);
		return new MagicCutterTriggerInstance(state, world, pos, pitch, yaw, piercing);
	}

	// Copied from vanilla entity class
	public static final Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(1);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Mana Cutter";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.SNOWBALL, 1, OreDictionary.WILDCARD_VALUE);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public ItemStack[] supportedFloatCosts() {
		return null;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.cutter.name", (Object[]) null);
	}

	@Override
	public String supportedFloatName() {
		return null;
	}
	
}
