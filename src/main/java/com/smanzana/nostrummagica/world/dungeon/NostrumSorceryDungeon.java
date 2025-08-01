package com.smanzana.nostrummagica.world.dungeon;

import java.util.Random;

import com.smanzana.autodungeons.world.dungeon.StaticDungeon;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoomRef;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.phys.Vec3;

public class NostrumSorceryDungeon extends StaticDungeon {
	
	public NostrumSorceryDungeon(IDungeonRoomRef<?> start) {
		super(start);
		this.setColor(0xFF602080);
	}
	
	@Override
	protected void spawnDungeonParticles(Level world, Player player) {
		Random rand = player.level.random;
		final float range = 15;
		for (int i = 0; i < 15; i++) {
			NostrumParticles.GLOW_ORB.spawn(player.level, new SpawnParams(
				1, player.getX() + (rand.nextGaussian() * range), player.getY() + (rand.nextGaussian() * 4), player.getZ() + (rand.nextGaussian() * range), .5,
				80, 30,
				new Vec3(0, .025, 0), new Vec3(.01, .0125, .01)
				).color(color));
		}
	}
	
	@Override
	public HeightProvider getSpawnHeight(WorldGenerationContext genContext) {
		// Pick random Y between 45 and 75 from bottom of world by default
		return ConstantHeight.of(VerticalAnchor.absolute(NostrumSorceryDimension.SPAWN_Y));
	}
	
	@Override
	public BlockPos getSpawnChunkPos(WorldGenerationContext genContext) {
		return BlockPos.ZERO; // right at spot
	}
	
	@Override
	public Direction getSpawnRotation(WorldGenerationContext genContext, Random rand) {
		// Hardcode north, so all are facing the same direction and offsets are right.
		// Whats most important is that all dungeons that are part of the sorcery dimension dungeon set are the same direction.
		// Capture code puts things as north by default, so north will mean no rotation.
		return Direction.NORTH;
	}
}
