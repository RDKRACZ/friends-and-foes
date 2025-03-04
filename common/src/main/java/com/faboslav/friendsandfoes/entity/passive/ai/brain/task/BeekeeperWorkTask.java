package com.faboslav.friendsandfoes.entity.passive.ai.brain.task;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BeekeeperWorkTask extends VillagerWorkTask
{
	public BeekeeperWorkTask() {
		super();
	}

	protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
		super.run(serverWorld, villagerEntity, l);

		GlobalPos beehiveGlobalPos = getBeehiveGlobalPos(serverWorld, villagerEntity);

		if (beehiveGlobalPos == null) {
			return;
		}

		BlockState beehiveBlockState = serverWorld.getBlockState(beehiveGlobalPos.getPos());

		if (isBeehiveReadyForHarvest(beehiveBlockState) == false) {
			return;
		}

		villagerEntity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.SHEARS));
		villagerEntity.setCurrentHand(Hand.MAIN_HAND);
	}

	protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
		villagerEntity.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		super.finishRunning(serverWorld, villagerEntity, l);
	}

	protected void performAdditionalWork(ServerWorld serverWorld, VillagerEntity villagerEntity) {
		GlobalPos beehiveGlobalPos = getBeehiveGlobalPos(serverWorld, villagerEntity);

		if (beehiveGlobalPos == null) {
			return;
		}

		BlockState beehiveBlockState = serverWorld.getBlockState(beehiveGlobalPos.getPos());

		if (beehiveBlockState == null) {
			return;
		}

		if (isBeehiveReadyForHarvest(beehiveBlockState) == false) {
			return;
		}

		this.harvestHoney(serverWorld, beehiveGlobalPos, beehiveBlockState);
	}

	private void harvestHoney(ServerWorld world, GlobalPos globalPos, BlockState beehiveState) {
		BlockPos blockPos = globalPos.getPos();
		world.setBlockState(blockPos, beehiveState.with(BeehiveBlock.HONEY_LEVEL, 0), 3);
		world.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
		BeehiveBlock.dropHoneycomb(world, blockPos);
	}

	@Nullable
	private GlobalPos getBeehiveGlobalPos(ServerWorld world, VillagerEntity entity) {
		Optional<GlobalPos> optional = entity.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE);
		if (optional.isPresent()) {
			GlobalPos globalPos = optional.get();

			return globalPos;
		}

		return null;
	}

	private boolean isBeehiveReadyForHarvest(BlockState beehiveState) {
		return beehiveState.get(BeehiveBlock.HONEY_LEVEL) == BeehiveBlock.FULL_HONEY_LEVEL;
	}
}
