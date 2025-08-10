package com.smanzana.nostrummagica.item.equipment;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.entity.ArrowShardProjectile;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.loretag.IItemLoreTagged;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MageBow extends BowItem implements IItemLoreTagged {
	
	public static final String ID = "mage_bow";
	
	public static final ResourceLocation PROPERTY_PULL = new ResourceLocation("minecraft", "pull");
	public static final ResourceLocation PROPERTY_PULLING = new ResourceLocation("minecraft", "pulling");

	public MageBow(Properties props) {
		super(props);
	}
	
	@Override
	public AbstractArrow customArrow(AbstractArrow arrow) {
		// Use method to know when an arrow is about to be loosed
		final @Nullable EMagicElement elem;
		if (arrow.getOwner() instanceof Player player) {
			elem = checkElementAmmo(player, true);
		} else {
			elem = null;
		}
		
		if (elem != null) {
			for (int i = 0; i < 3; i++) {
				final float prog = ((float) i / 3f);
				
				ArrowShardProjectile shard = new ArrowShardProjectile(NostrumEntityTypes.arrowShard, arrow.level);
				shard.setParentArrow(arrow);
				shard.setOwner(arrow.getOwner());
				shard.setPos(arrow.position());
				shard.setElement(elem);
				shard.setRotationOffset(prog);
				arrow.level.addFreshEntity(shard);
			}
		}
		
		return arrow;
	}

	@Override
	public int getDefaultProjectileRange() {
		return 17;
	}
	
	protected @Nullable EMagicElement checkElementAmmo(Player shooter, boolean consume) {
		final Predicate<ItemStack> ESSENCE_ITEM = (s) -> !s.isEmpty() && s.getItem() instanceof EssenceItem;
		
		ItemStack ammo = ProjectileWeaponItem.getHeldProjectile(shooter, ESSENCE_ITEM);
		if (ammo.isEmpty()) {
			for(int i = 0; i < shooter.getInventory().getContainerSize(); ++i) {
				ammo = shooter.getInventory().getItem(i);
				if (ESSENCE_ITEM.test(ammo)) {
					break;
				}
			}
		}
		
		if (!ammo.isEmpty()) {
			EMagicElement elem = EssenceItem.findType(ammo);
			if (consume) {
				ammo.shrink(1);
			}
			return elem;
		}
		
		return null;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelPull(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		if (entityIn == null) {
			return 0.0F;
		} else {
			return entityIn.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - entityIn.getUseItemRemainingTicks()) / 20.0F;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelPulling(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		return entityIn != null && entityIn.isUsingItem() && entityIn.getUseItem() == stack ? 1.0F : 0.0F;
	}

	@Override
	public Item getItem() {
		return this;
	}

	@Override
	public ResourceLocation getItemRegistryName() {
		return this.getRegistryName();
	}
}
