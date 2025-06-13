package com.smanzana.nostrummagica.world.dungeon;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;
import com.smanzana.autodungeons.world.dungeon.RandomPoolDungeon;
import com.smanzana.autodungeons.world.dungeon.room.DungeonStartRoom;
import com.smanzana.autodungeons.world.dungeon.room.IDungeonRoomRef;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

public class NostrumDungeon extends RandomPoolDungeon {
	
	public NostrumDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending, int minPath, int randPath) {
		super(tag, starting, ending, minPath, randPath);
	}
	
	public NostrumDungeon(String tag, DungeonStartRoom starting, IDungeonRoomRef<?> ending) {
		this(tag, starting, ending, 2, 3);
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
	public void spawnLargeKey(DungeonRoomInstance room, LevelAccessor world, BlueprintLocation keyLocation) {
		// Technically, this spawns at two positions and could go out of bounds
		NostrumBlocks.largeDungeonKeyChest.makeDungeonChest(world, keyLocation.getPos(), keyLocation.getFacing(), room.getDungeonInstance());
	}
	
	@Override
	public void spawnLargeDoor(DungeonRoomInstance room, LevelAccessor world, BlueprintLocation doorLocation) {
		// Relying on there already being a door... could make large chest do the same?
		NostrumBlocks.largeDungeonDoor.overrideDungeonKey(world, doorLocation.getPos(), room.getDungeonInstance());
		
		// could if/else and use existing if it's th ere. same with large key?
	}
	
	@Override
	public void spawnSmallKey(DungeonRoomInstance room, LevelAccessor world, BlueprintLocation keyLocation) {
		NostrumBlocks.smallDungeonKeyChest.makeDungeonChest(world, keyLocation.getPos(), keyLocation.getFacing(), room.getDungeonInstance());
	}
	
	@Override
	public void spawnSmallDoor(DungeonRoomInstance room, LevelAccessor world, BlueprintLocation smallDoor, @Nullable BoundingBox bounds) {
		NostrumBlocks.smallDungeonDoor.spawnDungeonDoor(world, smallDoor.getPos(), smallDoor.getFacing(), bounds, room.getDungeonInstance());
	}
}
