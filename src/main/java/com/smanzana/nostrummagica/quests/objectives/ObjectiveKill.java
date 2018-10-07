package com.smanzana.nostrummagica.quests.objectives;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.quests.NostrumQuest;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ObjectiveKill implements IObjective {
	
	private static class State implements IObjectiveState {

		private static final String KEY = "count";
		private int count;
		
		public State() {
			count = 0;
		}
		
		@Override
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger(KEY, count);
			return tag;
		}

		@Override
		public void fromNBT(NBTTagCompound nbt) {
			this.count = nbt.getInteger(KEY);
		}
	}
	
	private NostrumQuest quest;
	private Class<? extends EntityLivingBase> entityClass;
	private String className;
	private int count;
	
	public ObjectiveKill(Class<? extends EntityLivingBase> clazz, String display, int count) {
		this.entityClass = clazz;
		this.className = display;
		this.count = count;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void setParentQuest(NostrumQuest quest) {
		this.quest = quest;
	}

	@Override
	public IObjectiveState getBaseState() {
		return new State();
	}

	@Override
	public String getDescription() {
		return I18n.format("objective.kill", new Object[]{count, className});
	}

	@Override
	public boolean isComplete(INostrumMagic attr) {
		Object o = attr.getQuestData(quest.getKey());
		if (o == null || !(o instanceof State))
			return false;
		State s = (State) o;
		return s.count >= this.count;
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		EntityLivingBase dead = event.getEntityLiving();
		if (entityClass.isInstance(dead)) {
			Entity source = event.getSource().getImmediateSource();
			EntityPlayer player = null;
			if (source instanceof EntityPlayer) {
				player = (EntityPlayer) source;
			} else if (source instanceof EntityArrow) {
				EntityArrow arrow = (EntityArrow) source;
				if (arrow.shootingEntity instanceof EntityPlayer) {
					player = (EntityPlayer) arrow.shootingEntity;
				}
			} else if (source instanceof EntitySpellProjectile) {
				EntitySpellProjectile proj = (EntitySpellProjectile) source;
				if (proj.shootingEntity instanceof EntityPlayer) {
					player = (EntityPlayer) proj.shootingEntity;
				}
			} else if (source instanceof EntityFireball) {
				EntityFireball proj = (EntityFireball) source;
				if (proj.shootingEntity instanceof EntityPlayer) {
					player = (EntityPlayer) proj.shootingEntity;
				}
			}
			
			if (player == null)
				return;
			
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			
			if (!attr.getCurrentQuests().contains(quest.getKey()))
				return;
			
			// Increment count
			Object o = attr.getQuestData(quest.getKey());
			State s;
			if (o == null || !(o instanceof State))
				s = new State();
			else
				s = (State) o;
			if (s.count >= this.count)
				return; // Already activated
			s.count++;
			
			attr.setQuestData(quest.getKey(), s);
			if (!player.world.isRemote) {
				// Spells are cast on the server, so sync to client quest state
				NetworkHandler.getSyncChannel().sendTo(
						new StatSyncMessage(attr), (EntityPlayerMP) player);
			}
		}
	}
}
