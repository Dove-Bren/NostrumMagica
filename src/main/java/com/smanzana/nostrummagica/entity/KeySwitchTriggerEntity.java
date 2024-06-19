package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.tile.KeySwitchBlockTileEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap.MutableAttribute;
import net.minecraft.world.World;

public class KeySwitchTriggerEntity extends TileProxyTriggerEntity<KeySwitchBlockTileEntity> {
	
	public static final String ID = "entity_key_switch_trigger";
	
	public KeySwitchTriggerEntity(EntityType<? extends KeySwitchTriggerEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		
		if (world != null && world.isRemote() && this.ticksExisted % 5 == 0) {
			KeySwitchBlockTileEntity keyEnt = getLinkedTileEntity();
			if (keyEnt != null) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						4, this.getPosX(), getPosY() + 1.25, getPosZ(), 1.25,
						20, 0,
						this.getPositionVec().add(0, 1, 0)
						).gravity(-.025f).color(keyEnt.getColor().getColorValue() | 0xAA000000));
			}
		}
	}
	
	public static final MutableAttribute BuildKeySwitchAttributes() {
		return SwitchTriggerEntity.BuildAttributes();
	}
}
