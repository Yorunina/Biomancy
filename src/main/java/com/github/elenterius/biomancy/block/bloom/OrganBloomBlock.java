package com.github.elenterius.biomancy.block.bloom;

import com.github.elenterius.biomancy.integration.kubejs.BiomancyKJSEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;

public class OrganBloomBlock extends BloomBlock {

	public OrganBloomBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		int age = AGE.getValue(state);
		if (age > 5 && player.getItemInHand(hand).isEmpty()) {
			if (!level.isClientSide) {
				BiomancyKJSEvents.onOrganBloomHarvest(state, level, pos, player, hand, hit);
				level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1f, 0.5f + level.random.nextFloat() * 0.4f);
				BlockState blockState = AGE.setValue(state, AGE.getMin());
				level.setBlock(pos, blockState, Block.UPDATE_CLIENTS);
				level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockState));
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.use(state, level, pos, player, hand, hit);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!level.isAreaLoaded(pos, 1)) return;

		Direction direction = getFacing(state);
		BlockPos relativePos = pos.relative(direction);
		BlockState relativeState = level.getBlockState(relativePos);
		if (relativeState.isSolid() || !relativeState.getCollisionShape(level, relativePos).isEmpty()) return;

		int age = AGE.getValue(state);
		if (age < AGE.getMax()) {
			int growthSpeed = age < AGE.getMax() - 1 ? getGrowthSpeed(level, pos) : 1;

			if (ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(growthSpeed) == 0)) {
				level.setBlock(pos, AGE.addValue(state, 1), Block.UPDATE_CLIENTS);
				ForgeHooks.onCropsGrowPost(level, pos, state);
			}
		}
	}
}
