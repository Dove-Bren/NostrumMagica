package com.smanzana.nostrummagica.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Houses a switch that has to be interacted iwth in order to proc other mechanisms
 * @author Skyler
 *
 */
public class SwitchBlock extends Block {
	
	protected static final AxisAlignedBB SWITCH_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1D, 0.2D, 1D);

	public static String ID = "switch_block";
	
	private static SwitchBlock instance = null;
	public static SwitchBlock instance() {
			if (instance == null)
				instance = new SwitchBlock();
			
			return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(SwitchBlockTileEntity.class, "switch_block_tile_entity");
	}
	
	public SwitchBlock() {
		super(Material.BARRIER, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 8;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return SWITCH_BLOCK_AABB;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new SwitchBlockTileEntity();
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote || !playerIn.isCreative()) {
			return false;
		}
		
		if (heldItem != null && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.provider.getDimension()) {
				TileEntity te = worldIn.getTileEntity(pos);
				if (te != null) {
					SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
					ent.offsetTo(heldPos);
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return true;
		} else if (heldItem != null && heldItem.getItem() instanceof ItemEnderEye) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				BlockPos loc = ent.getOffset().toImmutable().add(pos);
				IBlockState atState = worldIn.getBlockState(loc);
				if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
					playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
				} else {
					playerIn.addChatComponentMessage(new TextComponentString("Not pointed at valid triggered block!"));
				}
			}
		} else if (heldItem == null && hand == EnumHand.MAIN_HAND) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setType(ent.getType() == SwitchBlockTileEntity.SwitchType.ANY ? SwitchBlockTileEntity.SwitchType.MAGIC : SwitchBlockTileEntity.SwitchType.ANY);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
		
		return false;
	}
	
	public static class SwitchBlockTileEntity extends TileEntity implements ITickable {
		
		public static enum SwitchType {
			ANY,
			MAGIC,
		}
		
		private SwitchType type;
		private BlockPos triggerOffset;
		private boolean triggered;
		private EntityLivingBase triggerEntity;
		
		public SwitchBlockTileEntity() {
			super();
			
			type = SwitchType.ANY;
			triggerOffset = new BlockPos(0, -2, 0);
			triggerEntity = null;
			triggered = false;
		}
		
		public SwitchBlockTileEntity(SwitchType type, BlockPos pos) {
			this();
			
			this.type = type;
			this.triggerOffset = pos;
		}
		
		private static final String NBT_TYPE = "switch_type";
		private static final String NBT_OFFSET = "switch_offset";
		private static final String NBT_TRIGGERED = "triggered";
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setInteger(NBT_TYPE, this.type.ordinal());
			nbt.setLong(NBT_OFFSET, this.triggerOffset.toLong());
			nbt.setBoolean(NBT_TRIGGERED, this.triggered);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			int ord = nbt.getInteger(NBT_TYPE);
			for (SwitchType type : SwitchType.values()) {
				if (type.ordinal() == ord) {
					this.type = type;
					break;
				}
			}
			this.triggerOffset = BlockPos.fromLong(nbt.getLong(NBT_OFFSET));
			this.triggered = nbt.getBoolean(NBT_TRIGGERED);
		}
		
		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
		}

		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}
		
		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			super.onDataPacket(net, pkt);
			handleUpdateTag(pkt.getNbtCompound());
		}
		
		private void dirty() {
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
			worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
			worldObj.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
			markDirty();
		}
		
		public void setType(SwitchType type) {
			this.type = type;
			dirty();
		}
		
		public void setOffset(BlockPos newOffset) {
			this.triggerOffset = newOffset.toImmutable();
			dirty();
		}
		
		public void offsetTo(BlockPos targ) {
			this.setOffset(targ.subtract(this.getPos()));
		}
		
		public SwitchType getType() {
			return this.type;
		}
		
		public BlockPos getOffset() {
			return this.triggerOffset;
		}
		
		@Nullable
		public EntityLivingBase getTriggerEntity() {
			return this.triggerEntity;
		}
		
		public boolean isTriggered() {
			return this.triggered;
		}
		
		@Override
		public void update() {
			if (worldObj.isRemote) {
				return;
			}
			
			// Create entity here if it doesn't exist
			if (triggerEntity == null || triggerEntity.isDead || triggerEntity.worldObj != this.worldObj
					|| triggerEntity.getDistanceSq(this.pos.up()) > 1.5) {
				// Entity is dead OR is too far away
				if (triggerEntity != null && !triggerEntity.isDead) {
					triggerEntity.setDead();
				}
				
				triggerEntity = new EntitySwitchTrigger(this.worldObj);
				triggerEntity.setPosition(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				worldObj.spawnEntityInWorld(triggerEntity);
			}
		}
		
		public void trigger(boolean isMagic) {
			if (!this.triggered) {
				if (type == SwitchType.ANY || isMagic) {
					this.triggered = true;
					NostrumMagicaSounds.DAMAGE_ICE.play(worldObj, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
					this.dirty();
					
					BlockPos triggerPos = this.getPos().add(this.getOffset());
					IBlockState state = worldObj.getBlockState(triggerPos);
					if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
						return;
					}
					
					((ITriggeredBlock) state.getBlock()).trigger(worldObj, triggerPos, state, this.getPos());
				} else {
					// Wrong input type
					NostrumMagicaSounds.CAST_FAIL.play(worldObj, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
				}
			}
		}
	}
	
}
