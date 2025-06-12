package com.smanzana.nostrummagica.tile;

import java.util.Optional;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.boss.plantboss.PlantBossEntity;
import com.smanzana.nostrummagica.entity.boss.reddragon.RedDragonEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEarthGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicEnderGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicFireGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicIceGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicLightningGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicPhysicalGolemEntity;
import com.smanzana.nostrummagica.entity.golem.MagicWindGolemEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SingleSpawnerTileEntity extends BlockEntity implements TickableBlockEntity {
	
	public static final int SPAWN_DIST_SQ = 900; // 30^2 
	
	protected int ticksExisted;
	
	protected ResourceLocation spawnType;
	
	public SingleSpawnerTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.SingleSpawner, pos, state);
	}
	
	protected SingleSpawnerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		ticksExisted = 0;
		spawnType = null;
	}
	
	public void setSpawnType(EntityType<?> type) {
		setSpawnType(type.getRegistryName());
	}
	
	public void setSpawnType(ResourceLocation type) {
		this.spawnType = type;
		this.setChanged();
	}
	
	public ResourceLocation getSpawnType() {
		if (this.spawnType != null) {
			return this.spawnType;
		} else {
			SingleSpawnerBlock.Type type = NostrumBlocks.singleSpawner.getLegacySpawnType(this.getBlockState());
			if (type == null)
				return null;
			
			EntityType<?> entType = NostrumEntityTypes.golemPhysical;
			
			switch (type) {
			case GOLEM_EARTH:
				entType = NostrumEntityTypes.golemEarth;
				break;
			case GOLEM_ENDER:
				entType = NostrumEntityTypes.golemEnder;
				break;
			case GOLEM_FIRE:
				entType = NostrumEntityTypes.golemFire;
				break;
			case GOLEM_ICE:
				entType = NostrumEntityTypes.golemIce;
				break;
			case GOLEM_LIGHTNING:
				entType = NostrumEntityTypes.golemLightning;
				break;
			case GOLEM_PHYSICAL:
				entType = NostrumEntityTypes.golemPhysical;
				break;
			case GOLEM_WIND:
				entType = NostrumEntityTypes.golemWind;
				break;
			case DRAGON_RED:
				entType = NostrumEntityTypes.dragonRed;
				break;
			case PLANT_BOSS:
				entType = NostrumEntityTypes.plantBoss;
				break;
			}
			
			return entType.getRegistryName();
		}
	}
	
	// Only call on server
	protected void majorTick(BlockState state) {
		if (!level.isClientSide())
		{
			for (Player player : level.players()) {
				if (!player.isSpectator() && !player.isCreative() && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY(), worldPosition.getZ() + .5) < SPAWN_DIST_SQ) {
					this.spawn(worldPosition, NostrumMagica.rand);
					level.removeBlock(worldPosition, false);
					return;
				}
			}
		}
		
		//NostrumBlocks.singleSpawner.tick(state, (ServerLevel) level, worldPosition, NostrumMagica.rand);
	}
	
	protected Mob spawn(BlockPos pos, Random rand) {
		Mob entity = createEntity(getLevel(), this.getBlockState());
		
		entity.setPersistenceRequired();
		entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + .5);
		
		level.addFreshEntity(entity);
		return entity;
	}
	
	protected Mob createEntity(Level world, BlockState state) {
		if (spawnType != null) {
			Optional<EntityType<?>> type = EntityType.byString(spawnType.toString());
			if (type.isPresent()) {
				return (Mob) type.get().create(world);
			}
		}
		
		return createFallbackMob(world, state);
	}
	
	protected final Mob createFallbackMob(Level world, BlockState state) {
		SingleSpawnerBlock.Type type = NostrumBlocks.singleSpawner.getLegacySpawnType(state);
		if (type == null)
			return null;
		
		Mob entity = null;
		
		switch (type) {
		case GOLEM_EARTH:
			entity = new MagicEarthGolemEntity(NostrumEntityTypes.golemEarth, world);
			break;
		case GOLEM_ENDER:
			entity = new MagicEnderGolemEntity(NostrumEntityTypes.golemEnder, world);
			break;
		case GOLEM_FIRE:
			entity = new MagicFireGolemEntity(NostrumEntityTypes.golemFire, world);
			break;
		case GOLEM_ICE:
			entity = new MagicIceGolemEntity(NostrumEntityTypes.golemIce, world);
			break;
		case GOLEM_LIGHTNING:
			entity = new MagicLightningGolemEntity(NostrumEntityTypes.golemLightning, world);
			break;
		case GOLEM_PHYSICAL:
			entity = new MagicPhysicalGolemEntity(NostrumEntityTypes.golemPhysical, world);
			break;
		case GOLEM_WIND:
			entity = new MagicWindGolemEntity(NostrumEntityTypes.golemWind, world);
			break;
		case DRAGON_RED:
			entity = new RedDragonEntity(NostrumEntityTypes.dragonRed, world);
			break;
		case PLANT_BOSS:
			entity = new PlantBossEntity(NostrumEntityTypes.plantBoss, world);
			break;
		}
		
		return entity;
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (spawnType != null) {
			nbt.putString("SpawnType", spawnType.toString());
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
			
		if (nbt.contains("SpawnType")) {
			this.spawnType = ResourceLocation.parse(nbt.getString("SpawnType"));
		}
	}
	
	@Override
	public void tick() {
		if (!level.isClientSide && ++ticksExisted % 16 == 0) {
			BlockState state = this.level.getBlockState(this.worldPosition);
			if (state == null || !(state.getBlock() instanceof SingleSpawnerBlock)) {
				level.removeBlockEntity(worldPosition);
				return;
			}
			
			majorTick(state);
		}
	}
}