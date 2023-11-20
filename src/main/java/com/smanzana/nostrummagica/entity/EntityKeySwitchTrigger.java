package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.tiles.KeySwitchBlockTileEntity;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class EntityKeySwitchTrigger extends EntitySwitchTrigger {
	
	public static final String ID = "entity_key_switch_trigger";
	
	public EntityKeySwitchTrigger(EntityType<? extends EntityKeySwitchTrigger> type, World worldIn) {
		super(type, worldIn);
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		
		if (world != null && world.isRemote() && this.ticksExisted % 5 == 0) {
			SwitchBlockTileEntity te = getLinkedTileEntity();
			if (te != null && te instanceof KeySwitchBlockTileEntity) {
				KeySwitchBlockTileEntity keyEnt = (KeySwitchBlockTileEntity) te;
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						4, posX, posY + 1.25, posZ, 1.25,
						20, 0,
						this.getPositionVec().add(0, 1.45, 0)
						).gravity(-.025f).color(keyEnt.getColor().getColorValue() | 0xAA000000));
			}
		}
	}
}
