package com.faboslav.friendsandfoes.mixin;

import com.faboslav.friendsandfoes.FriendsAndFoes;
import com.faboslav.friendsandfoes.entity.passive.CopperGolemEntity;
import com.faboslav.friendsandfoes.init.ModEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(LightningRodBlock.class)
public abstract class LightningRodBlockMixin extends RodBlock
{
	@Nullable
	private BlockPattern copperGolemPattern;

	private static final Predicate<BlockState> IS_GOLEM_LIGHTNING_ROD_PREDICATE = state -> state != null && (state == Blocks.LIGHTNING_ROD.getDefaultState().with(LightningRodBlock.FACING, Direction.UP));
	private static final Predicate<BlockState> IS_GOLEM_HEAD_PREDICATE = state -> state != null && (state.isOf(Blocks.CARVED_PUMPKIN) || state.isOf(Blocks.JACK_O_LANTERN));
	private static final Predicate<BlockState> IS_GOLEM_BODY_PREDICATE = state -> state != null && (
		state.isOf(Blocks.COPPER_BLOCK)
		|| state.isOf(Blocks.WEATHERED_COPPER)
		|| state.isOf(Blocks.EXPOSED_COPPER)
		|| state.isOf(Blocks.OXIDIZED_COPPER)
		|| state.isOf(Blocks.WAXED_COPPER_BLOCK)
		|| state.isOf(Blocks.WAXED_WEATHERED_COPPER)
		|| state.isOf(Blocks.WAXED_EXPOSED_COPPER)
		|| state.isOf(Blocks.WAXED_OXIDIZED_COPPER)
	);

	protected LightningRodBlockMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "onBlockAdded", at = @At("HEAD"))
	private void customOnBlockAdded(
		BlockState state,
		World world,
		BlockPos pos,
		BlockState oldState,
		boolean notify,
		CallbackInfo ci
	) {
		if (!oldState.isOf(state.getBlock())) {
			this.tryToSpawnCopperGolem(
				world,
				pos
			);
		}
	}

	private void tryToSpawnCopperGolem(
		World world,
		BlockPos pos
	) {
		if (FriendsAndFoes.getConfig().enableCopperGolem == false) {
			return;
		}

		BlockPattern.Result patternSearchResult = this.getCopperGolemPattern().searchAround(world, pos);

		if (patternSearchResult == null) {
			return;
		}

		BlockState headBlockState = patternSearchResult.translate(0, 1, 0).getBlockState();
		BlockState bodyBlockState = patternSearchResult.translate(0, 2, 0).getBlockState();

		for (int i = 0; i < this.getCopperGolemPattern().getHeight(); ++i) {
			CachedBlockPosition cachedBlockPosition = patternSearchResult.translate(0, i, 0);
			world.setBlockState(
				cachedBlockPosition.getBlockPos(),
				Blocks.AIR.getDefaultState(),
				Block.NOTIFY_LISTENERS
			);
			world.syncWorldEvent(
				WorldEvents.BLOCK_BROKEN,
				cachedBlockPosition.getBlockPos(),
				Block.getRawIdFromState(cachedBlockPosition.getBlockState())
			);
		}

		BlockPos cachedBlockPosition = patternSearchResult.translate(0, 2, 0).getBlockPos();
		float copperGolemYaw = headBlockState.get(CarvedPumpkinBlock.FACING).asRotation();

		CopperGolemEntity copperGolemEntity = ModEntity.COPPER_GOLEM.get().create(world);

		copperGolemEntity.setPosition(
			(double) cachedBlockPosition.getX() + 0.5D,
			(double) cachedBlockPosition.getY() + 0.05D,
			(double) cachedBlockPosition.getZ() + 0.5D
		);
		copperGolemEntity.setSpawnYaw(copperGolemYaw);
		world.spawnEntity(copperGolemEntity);

		Oxidizable.OxidationLevel oxidationLevel;

		if (bodyBlockState.isOf(Blocks.WAXED_COPPER_BLOCK)) {
			oxidationLevel = Oxidizable.OxidationLevel.UNAFFECTED;
		} else if (bodyBlockState.isOf(Blocks.WAXED_WEATHERED_COPPER)) {
			oxidationLevel = Oxidizable.OxidationLevel.WEATHERED;
		} else if (bodyBlockState.isOf(Blocks.WAXED_EXPOSED_COPPER)) {
			oxidationLevel = Oxidizable.OxidationLevel.EXPOSED;
		} else if (bodyBlockState.isOf(Blocks.WAXED_OXIDIZED_COPPER)) {
			oxidationLevel = Oxidizable.OxidationLevel.OXIDIZED;
		} else {
			oxidationLevel = ((OxidizableBlock) bodyBlockState.getBlock()).getDegradationLevel();
		}

		copperGolemEntity.setOxidationLevel(oxidationLevel);
		copperGolemEntity.setIsWaxed(this.isCopperBlockWaxed(bodyBlockState));

		for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(
			ServerPlayerEntity.class,
			copperGolemEntity.getBoundingBox().expand(5.0D)
		)) {
			Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, copperGolemEntity);
		}

		for (int j = 0; j < this.getCopperGolemPattern().getHeight(); ++j) {
			CachedBlockPosition cachedBlockPosition2 = patternSearchResult.translate(0, j, 0);
			world.updateNeighbors(cachedBlockPosition2.getBlockPos(), Blocks.AIR);
		}
	}

	private BlockPattern getCopperGolemPattern() {
		if (this.copperGolemPattern == null) {
			this.copperGolemPattern = BlockPatternBuilder.start()
				.aisle("|", "^", "#")
				.where('|', CachedBlockPosition.matchesBlockState(IS_GOLEM_LIGHTNING_ROD_PREDICATE))
				.where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE))
				.where('#', CachedBlockPosition.matchesBlockState(IS_GOLEM_BODY_PREDICATE))
				.build();
		}

		return this.copperGolemPattern;
	}

	private boolean isCopperBlockWaxed(
		BlockState blockState
	) {
		return blockState.isOf(Blocks.WAXED_COPPER_BLOCK)
			   || blockState.isOf(Blocks.WAXED_WEATHERED_COPPER)
			   || blockState.isOf(Blocks.WAXED_EXPOSED_COPPER)
			   || blockState.isOf(Blocks.WAXED_OXIDIZED_COPPER);
	}
}
