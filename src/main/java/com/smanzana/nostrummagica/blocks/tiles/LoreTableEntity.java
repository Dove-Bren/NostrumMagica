package com.smanzana.nostrummagica.blocks.tiles;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class LoreTableEntity extends TileEntity implements ITickableTileEntity {

	private @Nonnull ItemStack item;
	private float progress;
	private String lorekey;
	private int ticksExisted;
	
	public LoreTableEntity() {
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
	
	public void onTakeItem(PlayerEntity player) {
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
		if (world != null && !world.isRemote) {
			world.addBlockEvent(pos, blockType, 0, PROGRESS_TO_INT(progress));
		}
	}
	
	private void dirty() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
		markDirty();
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		if (!item.isEmpty())
			nbt.put("item", item.serializeNBT());
		
		if (lorekey != null)
			nbt.putString("lore", lorekey);
		
		nbt.putFloat("progress", progress);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		super.readFromNBT(nbt);
		
		if (nbt == null)
			return;
		
		this.progress = nbt.getFloat("progress");
		if (nbt.contains("item", NBT.TAG_COMPOUND))
			this.item = new ItemStack(nbt.getCompound("item"));
		else
			this.item = ItemStack.EMPTY;
		
		if (nbt.contains("lore", NBT.TAG_STRING))
			this.lorekey = nbt.getString("lore");
			
		
	}

	@Override
	public void tick() {
		ticksExisted++;
		
		if (world != null && !world.isRemote) {
			if (!item.isEmpty() && lorekey == null) {
				progress += 1f / (30f * 20f); // 30 seconds
				if (ticksExisted % 5 == 0) {
					setProgress(progress);
				}
				
				if (progress >= 1f) {
					if (item.getItem() instanceof ILoreTagged) {
						this.lorekey = ((ILoreTagged) item.getItem()).getLoreKey();
						NostrumMagicaSounds.DAMAGE_ICE.play(world,
								pos.getX(), pos.getY(), pos.getZ());
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
	public boolean receiveClientEvent(int id, int type) {
		if (world != null && world.isRemote) {
			if (id == 0) {
				this.progress = INT_TO_PROGRESS(type);
				return true;
			}
		}
		
		return true;
	}
	
}