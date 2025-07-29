package com.smanzana.nostrummagica.tile;

import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.block.CandleBlock;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

public class CandleTileEntity extends BlockEntity implements TickableBlockEntity, IReagentProviderTile {
	
	private static final String NBT_TYPE = "type";
	private static Random rand = new Random();
	private ReagentType type;
	private int lifeTicks;
	
	public CandleTileEntity(BlockPos pos, BlockState state) {
		super(NostrumBlockEntities.Candle, pos, state);
	}
	
	public ReagentType getReagentType() {
		return type;
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		if (type != null) {
			nbt.put(NBT_TYPE, NetUtils.ToNBT(type));
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt.contains(NBT_TYPE) && !(nbt.contains(NBT_TYPE, Tag.TAG_STRING) && nbt.getString(NBT_TYPE).equalsIgnoreCase("null"))) { // 'null' stuff for old candles
			type = NetUtils.FromNBT(ReagentType.class, nbt.get(NBT_TYPE));
		} else {
			type = null;
		}
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
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	protected boolean isLit() {
		return this.getBlockState().getValue(CandleBlock.LIT);
	}
	
	protected void checkForItems() {
		// Check for a reagent item over the candle
		List<ItemEntity> items = getLevel().getEntitiesOfClass(ItemEntity.class, Shapes.block().bounds().move(getBlockPos()).expandTowards(0, 1, 0));
		if (items != null && !items.isEmpty()) {
			for (ItemEntity item : items) {
				ItemStack stack = item.getItem();
				if (stack.getItem() instanceof ReagentItem reagentItem) {
					if (reagentItem.getType() != null) {
						if (this.tryAddReagent(reagentItem.getType())) {
							stack.split(1);
							if (stack.getCount() <= 0) {
								item.discard();
							}
						}
					}
					
					break;
				}
			}
		}
	}
	
	protected void doReagentAddEfffects(ReagentType newType) {
		((ServerLevel) this.getLevel()).sendParticles(ParticleTypes.LAVA, worldPosition.getX() + .5, worldPosition.getY() + .8, worldPosition.getZ() + .5,
				5, 0, 0, 0, 0);
		this.getLevel().playSound(null, worldPosition, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1f, 2f);
	}
	
	@Override
	public void tick() {
		if (isLit()) {
			
			if (level.isRainingAt(worldPosition) && level.random.nextInt(60) == 0) {
				CandleBlock.extinguish(level, this.worldPosition, this.getBlockState());
				return;
			}
			
			if (this.getReagentType() == null) {
				this.checkForItems();
			} else {
				// If no enhancing block is present, tick down life ticks to eventually consume reagent
				if (!isEnhanced()) {
					if (this.lifeTicks == 0) {
						this.lifeTicks = (20 * 15) + CandleTileEntity.rand.nextInt(20*30);
					}
					
					if (--this.lifeTicks <= 0) {
						this.setReagentType(null);
						
						// Also make candle go out...
						BlockState state = level.getBlockState(this.worldPosition);
						if (state == null)
							return;
						
						CandleBlock.extinguish(level, this.worldPosition, state);
						((ServerLevel) this.getLevel()).sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + .5, worldPosition.getY() + .6, worldPosition.getZ() + .5,
								20, 0, .01, 0, .05);
					}
				}
			}
		} else {
			if (this.getReagentType() != null) {
				this.setReagentType(null);
			}
		}
	}
	
	public void setReagentType(ReagentType type) {
		this.type = type;
		if (this.type == null) {
			this.lifeTicks = 0;
		}
		this.dirty();
	}
	
	public boolean tryAddReagent(ReagentType type) {
		if (this.getReagentType() != null) {
			return false;
		}
		
		this.setReagentType(type);
		this.doReagentAddEfffects(type);
		return true;
	}
	
	protected boolean isEnhanced() {
		return CandleBlock.IsCandleEnhanced(getLevel(), getBlockPos());
	}

	@Override
	public ReagentType getPresentReagentType(BlockEntity provider, Level world, BlockPos pos) {
		return this.getReagentType();
	}

	@Override
	public boolean consumeReagentType(BlockEntity provider, Level world, BlockPos pos, ReagentType type) {
		if (this.getReagentType() == null || (type != null && type != this.getReagentType())) {
			return false;
		}
		
		this.setReagentType(null);
		
		if (!this.isEnhanced()) {
			CandleBlock.extinguish(world, pos, getBlockState());
		}
		return true;
	}
}