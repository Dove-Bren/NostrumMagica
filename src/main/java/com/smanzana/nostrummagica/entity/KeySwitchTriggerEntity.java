package com.smanzana.nostrummagica.entity;


import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.tile.KeySwitchBlockTileEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;

public class KeySwitchTriggerEntity extends TileProxyTriggerEntity<KeySwitchBlockTileEntity> {
	
	public static final String ID = "entity_key_switch_trigger";
	
	public KeySwitchTriggerEntity(EntityType<? extends KeySwitchTriggerEntity> type, Level worldIn) {
		super(type, worldIn);
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		
		if (level != null && level.isClientSide() && this.tickCount % 5 == 0) {
			KeySwitchBlockTileEntity keyEnt = getLinkedTileEntity();
			if (keyEnt != null) {
				NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
						4, this.getX(), getY() + 1.25, getZ(), 1.25,
						20, 0,
						this.position().add(0, 1, 0)
						).gravity(-.025f).color(keyEnt.getColor().getColorValue() | 0xAA000000));
			}
		}
	}
	
	public static final Builder BuildKeySwitchAttributes() {
		return SwitchTriggerEntity.BuildAttributes();
	}
}
