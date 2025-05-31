package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.client.particles.ParticleTargetBehavior.TargetBehavior;
import com.smanzana.nostrummagica.item.ICrystalEnchantableItem;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ElementalCrystalBlockEntity extends BlockEntity implements TickableBlockEntity {
	
	protected static record ItemEntry(ItemEntity entity, int ticksStarted) {}
	
	private static final int MAX_ENTITY_RADIUS = 5;
	private static final int WORK_TICKS = 20 * 10;
	
	private final Map<ItemEntity, ItemEntry> activeItems;
	
	protected int ticksExisted;
	
	public ElementalCrystalBlockEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.ElementalCrystal, pos, state);
		this.activeItems = new HashMap<>();
		this.ticksExisted = 0;
	}
	
	public boolean isActive() {
		return !activeItems.isEmpty();
	}
	
	public EMagicElement getElement() {
		return NostrumBlocks.elementalCrystal.getElement(this.getBlockState());
	}
	
	protected void forEachItem(Consumer<ItemEntity> func) {
		for (ItemEntry entry : this.activeItems.values()) {
			func.accept(entry.entity());
		}
	}
	
	protected void cleanItemList() {
		Iterator<ItemEntity> it = this.activeItems.keySet().iterator();
		while (it.hasNext()) {
			final ItemEntity next = it.next();
			if (!next.isAlive() || next.distanceToSqr(Vec3.atCenterOf(worldPosition)) > MAX_ENTITY_RADIUS * MAX_ENTITY_RADIUS) {
				it.remove();
			}
		}
	}
	
	protected ItemEntity transformItem(ItemEntity entity) {
		if (entity.getItem().isEmpty() || !(entity.getItem().getItem() instanceof ICrystalEnchantableItem enchantable)) {
			return entity;
		}
		
		if (!enchantable.canEnchant(entity.getItem())) {
			return entity;
		}
		
		ICrystalEnchantableItem.Result result = enchantable.attemptEnchant(entity.getItem(), getElement());
		if (result.success) {
			entity.setItem(result.resultItem);
			doTransformEffect(entity);
		}
		
		return entity;
	}
	
	protected void doEntityEffect(ItemEntity entity, boolean working) {
		NostrumParticles.GLOW_ORB.spawn(entity.getLevel(), new SpawnParams(
				1,
				entity.getX(), entity.getY() + .25, entity.getZ(), .05, 20, 0,
				new Vec3(0, -.05, 0), new Vec3(0, .015, 0)
				).color(this.getElement().getColor()));
		
		if (working) {
			NostrumParticles.GLOW_ORB.spawn(entity.getLevel(), new SpawnParams(
					1,
					entity.getX(), entity.getY() + .25, entity.getZ(), 1, 40, 10,
					new TargetLocation(entity, true)
					).color(this.getElement().getColor()).setTargetBehavior(TargetBehavior.JOIN));
			
			if (NostrumMagica.rand.nextInt(64) == 0) {
				NostrumParticles.GLOW_TRAIL.spawn(entity.getLevel(), new SpawnParams(
						1,
						worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, 1, 60, 10,
						new TargetLocation(entity, true)
						).color(this.getElement().getColor()).setTargetBehavior(TargetBehavior.ORBIT_LAZY));
			}
		}
	}
	
	protected void doTransformEffect(ItemEntity entity) {
		NostrumParticles.LIGHT_EXPLOSION.spawn(entity.getLevel(), new SpawnParams(
				15,
				entity.getX(), entity.getY() + .5, entity.getZ(), 0, 80, 0,
				new TargetLocation(entity, true)
				).color(this.getElement().getColor()).setTargetBehavior(TargetBehavior.ATTACH));
	}
	
	protected boolean tickEntry(ItemEntry entry, List<ItemEntry> addList) {
		final Vec3 motion = entry.entity().getDeltaMovement();
		if (motion.y < .1 && entry.entity().getY() < this.worldPosition.getY() + 2) {
			entry.entity().setDeltaMovement(motion.x * .8f, Math.min(motion.y + .05, .1), motion.z * .8f);
			entry.entity.hasImpulse = true;
		}
		
		doEntityEffect(entry.entity(), this.ticksExisted - entry.ticksStarted < WORK_TICKS);
		
		if (this.ticksExisted - entry.ticksStarted == WORK_TICKS) {
			// only equals, since items stick around after they transform
			ItemEntity newEnt = transformItem(entry.entity());
			if (newEnt != entry.entity()) {
				// add new ent, and return true to evict old
				addList.add(new ItemEntry(newEnt, entry.ticksStarted()));
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean canWorkItem(ItemEntity ent) {
		return ent.isAlive()
				&& !ent.getItem().isEmpty()
				&& ent.getItem().getItem() instanceof ICrystalEnchantableItem enchantable
				&& enchantable.canEnchant(ent.getItem())
				; 
	}
	
	private void scanForItems(int radius) {
		List<ItemEntity> nearbyEnts = this.getLevel().getEntitiesOfClass(ItemEntity.class,
				AABB.ofSize(Vec3.atCenterOf(this.getBlockPos()), radius, radius, radius),
				this::canWorkItem);
		
		for (ItemEntity item : nearbyEnts) {
			this.activeItems.computeIfAbsent(item, (i) -> new ItemEntry(i, this.ticksExisted));
		}
	}
	
	protected void activeTick() {
		cleanItemList();
		
		List<ItemEntry> addList = new ArrayList<>(2);
		Iterator<ItemEntry> it = this.activeItems.values().iterator();
		while (it.hasNext()) {
			if (tickEntry(it.next(), addList)) {
				it.remove();
			}
		}
		addList.forEach((e) -> this.activeItems.put(e.entity(), e));
	}
	
	@Override
	public void tick() {
		this.ticksExisted++;
		
		if (!level.isClientSide) {
			if (this.isActive()) {
				activeTick();
			}
			this.scanForItems(MAX_ENTITY_RADIUS); // do this AFTER updating current ones so they start at tick 0 (and to not bother checking them)
		}
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		//handleUpdateTag(pkt.getTag());
	}
	
//	private void dirty() {
//		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
//		setChanged();
//	}
	
}