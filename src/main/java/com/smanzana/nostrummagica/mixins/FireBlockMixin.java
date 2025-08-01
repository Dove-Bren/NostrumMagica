package com.smanzana.nostrummagica.mixins;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.smanzana.nostrummagica.world.FireBlockEvent.FireCheckOddsEvent;
import com.smanzana.nostrummagica.world.FireBlockEvent.FireSpreadAttemptEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;

@Mixin(FireBlock.class)
public class FireBlockMixin extends BaseFireBlock {

	public FireBlockMixin(BlockBehaviour.Properties p_41383_) {
		super(p_41383_, 0);
	}
	
	// Note: Vanilla has "checkBurnOut" with no Direcetion final arg whereas forge patches it to "tryCatchFire".
	// https://github.com/MinecraftForge/MinecraftForge/blob/1.18.x/patches/minecraft/net/minecraft/world/level/block/FireBlock.java.patch
	//Lnet/minecraft/world/level/block/FireBlock;checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILjava/util/Random;I)V
	@Inject(method = "tryCatchFire", at=@At(value="HEAD"), cancellable = true, remap=false)
	public void onTryCatchFire(Level level, BlockPos pos, int chance, Random rand, int fireAge, Direction face, CallbackInfo ci) {
		final BlockPos origPos = pos.relative(face);
		FireSpreadAttemptEvent event = new FireSpreadAttemptEvent(level, origPos, pos);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			ci.cancel();
		}
	}
	
	//Lnet/minecraft/world/level/block/FireBlock;getFireOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I
	@Inject(method = "getFireOdds", at=@At(value="HEAD"), cancellable = true)
	public void onGetFireOdds(LevelReader level, BlockPos pos, CallbackInfoReturnable<Integer> ci) {
		FireCheckOddsEvent event = new FireCheckOddsEvent((Level) level, pos);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.hasOddsOverride()) {
			ci.setReturnValue(event.getOdds());
		}
	}

	@Override
	protected boolean canBurn(BlockState p_49284_) {
		return false;
	}

}
