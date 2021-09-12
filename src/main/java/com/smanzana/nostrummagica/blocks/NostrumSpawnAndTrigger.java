package com.smanzana.nostrummagica.blocks;

import java.util.Random;
import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.EssenceItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem;
import com.smanzana.nostrummagica.items.NostrumSkillItem.SkillItemType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Like the single spawner, but spawns and then (once the entity is dead) triggers a triggerable block
 * @author Skyler
 *
 */
public class NostrumSpawnAndTrigger extends NostrumSingleSpawner {
	
	public static final String ID = "nostrum_spawner_trigger";
	
	private static NostrumSpawnAndTrigger instance = null;
	public static NostrumSpawnAndTrigger instance() {
		if (instance == null)
			instance = new NostrumSpawnAndTrigger();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(SpawnerTriggerTE.class, new ResourceLocation(NostrumMagica.MODID, "nostrum_mob_spawner_trigger_te"));
	}
	
	public NostrumSpawnAndTrigger() {
		super();
		this.setUnlocalizedName(ID);
		this.setBlockUnbreakable();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (NostrumMagica.proxy.getPlayer().isCreative()) {
			TileEntity te = blockAccess.getTileEntity(pos);
			if (te != null && te instanceof SpawnerTriggerTE) {
				SpawnerTriggerTE ent = ((SpawnerTriggerTE) te);
				return (ent.entity == null && ent.unlinkedEntID == null);
			}
		}
		return false;
	}
	
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		;//super.updateTick(worldIn, pos, state, rand);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SpawnerTriggerTE();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		}
		
		if (hand != EnumHand.MAIN_HAND) {
			return true;
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof SpawnerTriggerTE)) {
			return true;
		}
		
		SpawnerTriggerTE ent = (SpawnerTriggerTE) te;
		
		if (playerIn.isCreative()) {
			ItemStack heldItem = playerIn.getHeldItem(hand);
			if (heldItem.isEmpty()) {
				playerIn.sendMessage(new TextComponentString("Currently set to " + state.getValue(MOB).getName()));
			} else if (heldItem.getItem() instanceof EssenceItem) {
				Type type = null;
				switch (EssenceItem.findType(heldItem)) {
				case EARTH:
					type = Type.GOLEM_EARTH;
					break;
				case ENDER:
					type = Type.GOLEM_ENDER;
					break;
				case FIRE:
					type = Type.GOLEM_FIRE;
					break;
				case ICE:
					type = Type.GOLEM_ICE;
					break;
				case LIGHTNING:
					type = Type.GOLEM_LIGHTNING;
					break;
				case PHYSICAL:
					type = Type.GOLEM_PHYSICAL;
					break;
				case WIND:
					type = Type.GOLEM_WIND;
					break;
				}
				
				worldIn.setBlockState(pos, state.withProperty(MOB, type));
			} else if (heldItem.getItem() instanceof NostrumSkillItem) {
				if (NostrumSkillItem.getTypeFromMeta(heldItem.getMetadata()) == SkillItemType.WING) {
					worldIn.setBlockState(pos, state.withProperty(MOB, Type.DRAGON_RED));
				}
			} if (heldItem.getItem() instanceof PositionCrystal) {
				BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
				if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.provider.getDimension()) {
					ent.setTriggerPosition(heldPos.getX(), heldPos.getY(), heldPos.getZ());
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
				return true;
			} else if (heldItem.getItem() instanceof ItemEnderEye) {
				BlockPos loc = (ent.getTriggerOffset() == null ? null : ent.getTriggerOffset().toImmutable().add(pos));
				if (loc != null) {
					IBlockState atState = worldIn.getBlockState(loc);
					if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
						playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
					} else {
						playerIn.sendMessage(new TextComponentString("Not pointed at valid triggered block!"));
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	public static class SpawnerTriggerTE extends SingleSpawnerTE {
		
		private static final String NBT_ENTITY_ID = "entity_id";
		private static final String NBT_TRIGGER_OFFSET = "trigger_offset";
		
		protected EntityLivingBase entity;
		protected BlockPos triggerOffset;
		
		private UUID unlinkedEntID;
		
		public SpawnerTriggerTE() {
			super();
		}
		
		public void setTriggerOffset(BlockPos offset) {
			triggerOffset = offset;
			this.markDirty();
		}
		
		public void setTriggerPosition(int x, int y, int z) {
			this.setTriggerOffset(new BlockPos(x - pos.getX(), y - pos.getY(), z - pos.getZ()));
		}
		
		public BlockPos getTriggerOffset() {
			return triggerOffset;
		}
		
		protected void trigger(IBlockState state) {
			
			if (triggerOffset != null) {
				state = world.getBlockState(pos.add(this.triggerOffset));
				if (state.getBlock() instanceof ITriggeredBlock) {
					((ITriggeredBlock) state.getBlock()).trigger(world, pos.add(triggerOffset), state, pos);
				}
				triggerOffset = null;
			}
		}
		
		@Override
		protected void majorTick(IBlockState state) {
			if (unlinkedEntID != null) {
				// Need to find our entity!
				for (EntityLivingBase ent : world.getEntities(EntityLivingBase.class, (ent) -> { return ent.getPersistentID().equals(unlinkedEntID);})) {
					this.entity = ent;
					unlinkedEntID = null;
					break;
				}
				
				if (entity == null && ticksExisted > 20 * 15) {
					// Give up
					unlinkedEntID = null;
					this.trigger(state);
					world.setBlockToAir(pos);
				}
			} else if (entity == null) {
				for (EntityPlayer player : world.playerEntities) {
					if (!player.isSpectator() && !player.isCreative() && player.getDistanceSq(pos) < SPAWN_DIST_SQ) {
						entity = instance().spawn(world, pos, state, NostrumMagica.rand);
						world.notifyBlockUpdate(pos, state, state, 2);
						world.addBlockEvent(pos, state.getBlock(), 9, 0);
						this.markDirty();
						return;
					}
				}
			} else {
				if (entity.isDead) {
					this.trigger(state);
					world.setBlockToAir(pos);
				}
			}
		}
		
		@Override
		public void update() {
			super.update();
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (triggerOffset != null) {
				nbt.setLong(NBT_TRIGGER_OFFSET, triggerOffset.toLong());
			}
			if (entity != null) {
				nbt.setUniqueId(NBT_ENTITY_ID, entity.getPersistentID());
			} else if (this.unlinkedEntID != null) {
				nbt.setUniqueId(NBT_ENTITY_ID, this.unlinkedEntID);
			}
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt.hasUniqueId(NBT_ENTITY_ID)) {
				this.unlinkedEntID = nbt.getUniqueId(NBT_ENTITY_ID);
			}
			
			if (nbt.hasKey(NBT_TRIGGER_OFFSET)) {
				this.triggerOffset = BlockPos.fromLong(nbt.getLong(NBT_TRIGGER_OFFSET));
			}
		}
	}
}
