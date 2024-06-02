package com.smanzana.nostrummagica.progression.quests.objectives;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.progression.quests.NostrumQuest;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ObjectiveKill implements IObjective {
	
	private static class State implements IObjectiveState {

		private static final String KEY = "count";
		private int count;
		
		public State() {
			count = 0;
		}
		
		@Override
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			tag.putInt(KEY, count);
			return tag;
		}

		@Override
		public void fromNBT(CompoundNBT nbt) {
			this.count = nbt.getInt(KEY);
		}
	}
	
	private NostrumQuest quest;
	private Class<? extends LivingEntity> entityClass;
	private String className;
	private int count;
	
	public ObjectiveKill(Class<? extends LivingEntity> clazz, String display, int count) {
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
		LivingEntity dead = event.getEntityLiving();
		if (entityClass.isInstance(dead)) {
			Entity source = event.getSource().getTrueSource();
			PlayerEntity player = null;
			if (source instanceof PlayerEntity) {
				player = (PlayerEntity) source;
			} else if (source instanceof ProjectileEntity) {
				ProjectileEntity proj = (ProjectileEntity) source;
				if (proj.func_234616_v_() instanceof PlayerEntity) {
					player = (PlayerEntity) proj.func_234616_v_();
				}
			}
//			else if (source instanceof EntitySpellProjectile) {
//				EntitySpellProjectile proj = (EntitySpellProjectile) source;
//				if (proj.shootingEntity instanceof PlayerEntity) {
//					player = (PlayerEntity) proj.shootingEntity;
//				}
//			}
			
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
				NetworkHandler.sendTo(
						new StatSyncMessage(attr), (ServerPlayerEntity) player);
			}
		}
	}
}
