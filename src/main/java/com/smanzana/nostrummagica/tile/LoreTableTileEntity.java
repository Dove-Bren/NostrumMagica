package com.smanzana.nostrummagica.tile;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LoreTableTileEntity extends BlockEntity implements TickableBlockEntity {

	private @Nonnull ItemStack item;
	private float progress;
	private String lorekey;
	private int ticksExisted;
	
	public LoreTableTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.LoreTableEntityType, pos, state);
		progress = 0f;
		lorekey = null;
		item = ItemStack.EMPTY;
		ticksExisted = 0;
	}
	
	/**
	 * Returns the current (if any) lore available for taking
	 * and clears it
	 * @return
	 */
	private String takeLore() {
		String key = this.lorekey;
		this.lorekey = null;
		return key;
	}
	
	public boolean hasLore() {
		return (this.lorekey != null);
	}
	
	public void onTakeItem(Player player) {
		if (hasLore()) {
			String lore = takeLore();
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr != null) {
				ILoreTagged tag = LoreRegistry.instance().lookup(lore);
				if (tag != null) {
					attr.giveFullLore(tag);
				}
			}
		}
	}
	
	public @Nonnull ItemStack getItem() {
		return item;
	}
	
	public boolean setItem(@Nonnull ItemStack item) {
		if (!item.isEmpty()) {
			// Make sure it has lore
			if (!(item.getItem() instanceof ILoreTagged)) {
				return false;
			}
		}
		
		this.item = item;
		progress = 0f;
		this.dirty();
		return true;
	}
	
	public float getProgress() {
		return progress;
	}
	
	public void setProgress(float progress) {
		this.progress = progress;
		if (level != null && !level.isClientSide) {
			level.blockEvent(worldPosition, this.getBlockState().getBlock(), 0, PROGRESS_TO_INT(progress));
		}
	}
	
	private void dirty() {
		level.sendBlockUpdated(worldPosition, this.level.getBlockState(worldPosition), this.level.getBlockState(worldPosition), 3);
		setChanged();
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		if (nbt == null)
			nbt = new CompoundTag();
		
		if (!item.isEmpty())
			nbt.put("item", item.serializeNBT());
		
		if (lorekey != null)
			nbt.putString("lore", lorekey);
		
		nbt.putFloat("progress", progress);
		
		return nbt;
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null)
			return;
		
		this.progress = nbt.getFloat("progress");
		if (nbt.contains("item", Tag.TAG_COMPOUND))
			this.item = ItemStack.of(nbt.getCompound("item"));
		else
			this.item = ItemStack.EMPTY;
		
		if (nbt.contains("lore", Tag.TAG_STRING))
			this.lorekey = nbt.getString("lore");
			
		
	}

	@Override
	public void tick() {
		ticksExisted++;
		
		if (level != null && !level.isClientSide) {
			if (!item.isEmpty() && lorekey == null) {
				progress += 1f / (30f * 20f); // 30 seconds
				if (ticksExisted % 5 == 0) {
					setProgress(progress);
				}
				
				if (progress >= 1f) {
					if (item.getItem() instanceof ILoreTagged) {
						this.lorekey = ((ILoreTagged) item.getItem()).getLoreKey();
						NostrumMagicaSounds.DAMAGE_ICE.play(level,
								worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
					}
				}
			}
		}
	}
	
	private static final int PROGRESS_TO_INT(float progress) {
		return (int) (progress * 100);
	}
	
	private static final float INT_TO_PROGRESS(int raw) {
		return Math.min(1f, (float)raw / 100f);
	}
	
	@Override
	public boolean triggerEvent(int id, int type) {
		if (level != null && level.isClientSide) {
			if (id == 0) {
				this.progress = INT_TO_PROGRESS(type);
				return true;
			}
		}
		
		return true;
	}
	
}