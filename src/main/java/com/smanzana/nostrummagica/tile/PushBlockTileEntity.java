package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.dungeon.PushBlock;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.InfusedGemItem;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PushBlockTileEntity extends BlockEntity {
	
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_GRAVITY = "gravity";
	private static final int ANIM_DURATION = 20;
	
	private @Nullable EMagicElement element;
	private boolean gravity;
	
	// Used to lerp between old position and current position
	private long animStartTicks;
	private @Nullable Direction oldDirection;
	
	public PushBlockTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.PushBlock, pos, state);
		element = null;
	}
	
	public void setElement(EMagicElement element) {
		this.element = element;
		dirty();
	}
	
	// Ease of use
	public EMagicElement getElement() {
		return element;
	}
	
	public void setGravity(boolean gravity) {
		this.gravity = gravity;
		dirty();
	}
	
	public boolean hasGravity() {
		return this.gravity;
	}
	
	protected void animateFrom(Direction oldDirection) {
		this.animStartTicks = this.hasLevel() ? this.level.getGameTime() : 0;
		this.oldDirection = oldDirection;
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		
		if (this.getElement() != null) {
			nbt.put(NBT_ELEMENT, this.getElement().toNBT());
		}
		if (this.oldDirection != null) {
			nbt.putString("FROM_DIRECTION", this.oldDirection.getName());
		}
		nbt.putBoolean(NBT_GRAVITY, gravity);
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt.contains(NBT_ELEMENT)) {
			this.element = EMagicElement.FromNBT(nbt.get(NBT_ELEMENT));
		} else {
			this.element = null;
		}
		
		if (nbt.contains("FROM_DIRECTION")) {
			this.animateFrom(Direction.byName(nbt.getString("FROM_DIRECTION")));
		} else {
			this.oldDirection = null;
		}
		this.gravity = nbt.getBoolean(NBT_GRAVITY);
	}
	
	public float getAnimTicksElapsed(float partialTicks) {
		// Not sure how it happens, but make sure we don't go mega negative for no reason
		if (level.getGameTime() - this.animStartTicks < 0) {
			this.animStartTicks = level.getGameTime();
		}
		return ((int)(level.getGameTime() - this.animStartTicks) + partialTicks);
	}
	
	public float getAnimationProgress(float partialTicks) {
		return getAnimTicksElapsed(partialTicks) / (float) ANIM_DURATION;
	}
	
	public boolean isAnimating() {
		return this.oldDirection != null && this.level != null && getAnimTicksElapsed(0f) < ANIM_DURATION; 
	}
	
	public @Nullable Direction getAnimDirection() {
		return this.oldDirection;
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithId();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
	}
	
	protected void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), Block.UPDATE_ALL_IMMEDIATE);
		setChanged();
	}
	
	protected boolean canPush(Direction direction) {
		if (direction.getAxis().isVertical()) {
			return false;
		}
		
		BlockPos to = this.getBlockPos().relative(direction);
		if (!this.level.isLoaded(to)) {
			return false;
		}
		
		if (level.isEmptyBlock(to)) {
			return true;
		}
		
		BlockState toState = level.getBlockState(to);
		if (!(toState.getBlock() instanceof PushBlock)) {
			return false;
		}
		
		final BlockEntity toTE = level.getBlockEntity(to);
		if (toTE == null || !(toTE instanceof PushBlockTileEntity pushEntity)) {
			return false;
		}
		
		return pushEntity.canPush(direction);
	}
	
	public boolean canPushDirectly(Direction direction) {
		return canPush(direction) && !isStacked() && isOnGround() && !anyEntsOn() && !this.isAnimating();
	}
	
	protected boolean isStacked() {
		return level.getBlockState(getBlockPos().below()).getBlock() instanceof PushBlock;
	}
	
	protected void propogatePush(Direction direction) {
		// Only propogate up
		final BlockEntity te = level.getBlockEntity(getBlockPos().above());
		if (te instanceof PushBlockTileEntity push) {
			if (push.canPush(direction)) {
				push.push(direction);
			}
		}
	}
	
	protected void propogateFall() {
		// Propogate up
		final BlockEntity te = level.getBlockEntity(getBlockPos().above());
		if (te instanceof PushBlockTileEntity push) {
			push.fall();
		}
	}
	
	protected void setNewBlock(Direction direction) {
		BlockPos toPos = this.getBlockPos().relative(direction);
		level.setBlock(toPos, getBlockState(), Block.UPDATE_ALL);
		
		// And update tile entity
		BlockEntity te = level.getBlockEntity(toPos);
		if (te instanceof PushBlockTileEntity push) {
			push.setElement(this.getElement());
			push.setGravity(this.hasGravity());
			push.animateFrom(direction.getOpposite());
			level.scheduleTick(toPos, getBlockState().getBlock(), ANIM_DURATION);
		}
		
	}
	
	protected void push(Direction direction) {
		// First propogate forward, if applicable
		{
			if (!isStacked()) {
				final BlockEntity te = level.getBlockEntity(getBlockPos().relative(direction));
				if (te instanceof PushBlockTileEntity push) {
					if (push.canPush(direction)) {
						push.push(direction);
					}
				}
			}
		}
		
		// Then set up new block
		setNewBlock(direction);
		
		// Then propogate up
		propogatePush(direction);
		
		// Do effects (for bottom only)
		if (!this.isStacked()) {
			level.playSound(null, getBlockPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1f, 1f);
		}
		
		// Then remeove self
		this.level.removeBlock(getBlockPos(), false);
	}
	
	protected void fall() {
		// Set up new block
		setNewBlock(Direction.DOWN);
		
		// Then remeove self (since ent will e moving into space)
		this.level.removeBlock(getBlockPos(), false);
		
		// Then propogate up
		propogateFall();
		
		// Do effects (for bottom only)
		if (!this.isStacked()) {
			level.playSound(null, getBlockPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1f, 1f);
		}
	}
	
	protected boolean entityIsOn(Entity ent, int topY) {
		// Makes assumptions that ent is at least in the block BB above top layer
		return ent.isOnGround() && ent.getBlockY() >= topY;
		
//		return Math.abs(ent.getX() - (this.worldPosition.getX() + .5)) < 1
//				&& Math.abs(ent.getZ() - (this.worldPosition.getZ() + .5)) < 1
//				&& ent.getY() > this.worldPosition.getY() + 1;
	}
	
	protected boolean anyEntsOn() {
		// Find top of any stacked blocks
		MutableBlockPos pos = this.getBlockPos().mutable();
		
		while (pos.getY() + 1 < level.getMaxBuildHeight() && level.getBlockState(pos.move(Direction.UP)).getBlock() instanceof PushBlock) {
			;
		}
		
		final int topY = pos.getY();
		return !this.level.getEntities((Entity)null, new AABB(pos), (e) -> entityIsOn(e, topY)).isEmpty();
	}
	
	protected boolean isOnGround() {
		return !level.isEmptyBlock(getBlockPos().below());
	}
	
	protected Direction directionFromHit(BlockHitResult hit) {
		return hit.getDirection().getOpposite();
	}
	
	protected Direction directionFromHit(Vec3 hit) {
		Vec3 origin = Vec3.atCenterOf(this.getBlockPos());
		Vec3 diff = origin.subtract(hit);
		
		return Direction.getNearest(diff.x, diff.y, diff.z);
	}

	public boolean onPlayerUse(Player player, ItemStack heldItem, BlockHitResult hit) {
		// Map making: allow setting element
		if (player.isCreative() && !heldItem.isEmpty() && heldItem.getItem() instanceof InfusedGemItem gem) {
			this.oldDirection = null;
			this.setElement(gem.getElement());
			return true;
		}
		
		if (player.isCreative() && !heldItem.isEmpty() && heldItem.getItem() == Items.ARROW) {
			this.oldDirection = null;
			this.setGravity(!this.hasGravity());
			return true;
		}
		
		if (player.isCreative() && heldItem.isEmpty() && player.isCrouching()) {
			this.oldDirection = null;
			this.setElement(null);
			return true;
		}
		
		final Direction direction = directionFromHit(hit);
		return this.onPlayerPush(player, direction);
	}

	public boolean onSpell(LivingEntity caster, SpellEffectPart effect, SpellAction action, SpellLocation hitLocation) {
		// could try to figure out based on spell action what element it is but no need
		if (effect.getElement() == this.element) {
			// Originally I had you push with the element
			// but that ends up being a lot of casts which is a lot of reagents
			// which doesn't make sense for puzzle elements
			// so dispell element if they match it
//			final Direction direction = directionFromHit(hitLocation.hitPosition);
//			if (canPushDirectly(direction)) {
//				push(direction);
//				return true;
//			}
			NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
					10, hitLocation.hitPosition.x, hitLocation.hitPosition.y, hitLocation.hitPosition.z, 0,
					60, 20,
					Vec3.ZERO, new Vec3(.2, .1, .2)
					).color(element.getColor()));
			level.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, .8f, .8f);
			
			this.oldDirection = null;
			this.setElement(null);
			this.dirty();
		}
		
		return false;
	}
	
	public boolean onPlayerPush(Player player, Direction direction) {
		// Can only 'push manually' if no element is present
		if (this.element == null) {
			
			if (canPushDirectly(direction)) {
				push(direction);
				return true;
			}
		}
		
		// else
		return false;
	}
	
	public void onWorldCheckTick() {
		if (this.level.isClientSide() || !this.hasGravity() || this.isStacked()) {
			return;
		}
		
		// Bottom-most block with gravity. Should we fall?
		if (!isOnGround()) {
			fall();
		}
	}
	
}