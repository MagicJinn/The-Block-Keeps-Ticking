package net.what42.aliveworld.simulator;

import net.what42.aliveworld.AliveWorld;
import net.what42.aliveworld.api.FurnaceAccess;
import net.what42.aliveworld.api.BrewingStandAccess;
import net.what42.aliveworld.api.CampfireAccess;
import net.what42.aliveworld.util.TimeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.ChunkSection;
import java.util.List;
import java.util.Map;

public class ChunkLifeSimulator {
    public static void simulate(WorldChunk chunk, long timePassed) {
        World world = chunk.getWorld();
        if (!world.isClient && timePassed >= TimeUtils.MIN_PROCESS_INTERVAL) {
            
            // Procesar bloques
            if (!chunk.getBlockEntities().isEmpty()) {
                simulateBlockEntities(chunk, (int)timePassed);
            }

            // Procesar entidades
            if ((AliveWorld.CONFIG.isMobGrowingEnabled() || AliveWorld.CONFIG.isChickenEggLayingEnabled())) {
                simulateEntities(chunk, timePassed);
            }

            // Procesar cultivos
            if (AliveWorld.CONFIG.isCropsGrowingEnabled() && world instanceof ServerWorld) {
                simulateCropGrowth((ServerWorld)world, chunk, timePassed);
            }
        }
    }

    private static void simulateBlockEntities(WorldChunk chunk, int timePassed) {
        World world = chunk.getWorld();
        int forcedTime = Math.max(timePassed, TimeUtils.FORCED_MIN_PROGRESS);
        
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            BlockPos pos = entry.getKey();
            BlockEntity blockEntity = entry.getValue();
            BlockState state = blockEntity.getCachedState();

            if (blockEntity instanceof AbstractFurnaceBlockEntity && 
                AliveWorld.CONFIG.isFurnacesEnabled()) {
                FurnaceAccess access = (FurnaceAccess) blockEntity;
                FurnaceSimulator simulator = access.createSimulator();
                if (simulator.hasItemsToProcess()) {
                    simulator.simulateFinalResult(forcedTime, world, access);
                    access.apply(world, pos, state, simulator);
                }
            }
            
            if (blockEntity instanceof BrewingStandBlockEntity && 
                AliveWorld.CONFIG.isBrewingStandsEnabled()) {
                BrewingStandAccess access = (BrewingStandAccess) blockEntity;
                BrewingSimulator simulator = access.createSimulator();
                if (simulator.canBrew()) {
                    simulator.simulateFinalResult(forcedTime);
                    access.apply(world, pos, state, simulator);
                }
            }

            if (blockEntity instanceof CampfireBlockEntity && 
                state.getBlock() == Blocks.CAMPFIRE && 
                state.get(net.minecraft.block.CampfireBlock.LIT) && 
                AliveWorld.CONFIG.isCampfiresEnabled()) {
                CampfireAccess access = (CampfireAccess) blockEntity;
                CampfireSimulator simulator = access.createSimulator();
                if (simulator.hasItemsCooking()) {
                    simulator.simulateFinalResult(forcedTime, world.getRecipeManager());
                    access.apply(world, pos, state, simulator);
                }
            }
        }
    }

    private static void simulateEntities(WorldChunk chunk, long timePassed) {
        World world = chunk.getWorld();
        Box box = new Box(
            chunk.getPos().getStartX(),
            world.getBottomY(),
            chunk.getPos().getStartZ(),
            chunk.getPos().getEndX() + 1,
            world.getTopY(),
            chunk.getPos().getEndZ() + 1
        );

        List<Entity> entities = world.getEntitiesByClass(Entity.class, box, entity -> true);
        
        for (Entity entity : entities) {
            if (AliveWorld.CONFIG.isMobGrowingEnabled() && entity instanceof PassiveEntity) {
                PassiveEntity passive = (PassiveEntity) entity;
                int age = passive.getBreedingAge();
                if (age != 0) {
                    passive.setBreedingAge(age < 0 ? Math.max(0, age + (int)timePassed) : Math.max(0, age - (int)timePassed));
                }
            }

            if (AliveWorld.CONFIG.isChickenEggLayingEnabled() && entity instanceof ChickenEntity) {
                ChickenEntity chicken = (ChickenEntity) entity;
                if (!chicken.isBaby() && chicken.getLoveTicks() <= 0) {
                    int eggLayCycle = 6000; 
                    int cycles = (int)(timePassed / eggLayCycle);
                    if (cycles > 0) {
                        chicken.eggLayTime = (int)(timePassed % eggLayCycle);
                    }
                }
            }
        }
    }

    private static void simulateCropGrowth(ServerWorld world, WorldChunk chunk, long timePassed) {
        if (!AliveWorld.CONFIG.isCropsGrowingEnabled()) return;

        int randomTickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        long totalRandomTicks = (timePassed * randomTickSpeed) / TimeUtils.TICK_PER_SECOND;
        
        if (totalRandomTicks > 0) {
            for (int sectionY = 0; sectionY < chunk.getHeight() >> 4; ++sectionY) {
                ChunkSection section = chunk.getSectionArray()[sectionY];
                if (section != null && !section.isEmpty()) {
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = section.getBlockState(x, y, z);
                                if (canRandomTick(state)) {
                                    BlockPos pos = new BlockPos(
                                        chunk.getPos().getStartX() + x,
                                        (sectionY << 4) + y,
                                        chunk.getPos().getStartZ() + z
                                    );

                                    float growthChance = calculateGrowthChance(totalRandomTicks);
                                    if (world.getRandom().nextFloat() < growthChance) {
                                        if (state.getBlock() instanceof CropBlock crop) {
                                            simulateCrop(world, pos, state, crop, totalRandomTicks);
                                        } else if (state.getBlock() instanceof SugarCaneBlock) {
                                            simulateSugarCane(world, pos, state, totalRandomTicks);
                                        } else if (state.getBlock() instanceof CactusBlock) {
                                            simulateCactus(world, pos, state, totalRandomTicks);
                                        } else if (state.getBlock() instanceof BambooBlock) {
                                            simulateBamboo(world, pos, state, totalRandomTicks);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void simulateCrop(ServerWorld world, BlockPos pos, BlockState state, CropBlock crop, long totalRandomTicks) {
        int currentAge = state.get(crop.getAgeProperty());
        if (currentAge < crop.getMaxAge()) {
            int growthStages = calculateGrowthStages(totalRandomTicks, crop.getMaxAge() - currentAge);
            if (canPlantGrow(world, pos)) {
                world.setBlockState(pos, state.with(crop.getAgeProperty(), 
                    Math.min(currentAge + growthStages, crop.getMaxAge())), 2);
            }
        }
    }

    private static void simulateSugarCane(ServerWorld world, BlockPos pos, BlockState state, long totalRandomTicks) {
        int maxHeight = 3;
        int currentHeight = 1;
        BlockPos.Mutable checkPos = pos.mutableCopy();
        
        while (world.getBlockState(checkPos.up()).getBlock() instanceof SugarCaneBlock && currentHeight < maxHeight) {
            currentHeight++;
            checkPos.move(Direction.UP);
        }

        if (currentHeight < maxHeight && 
            world.getBlockState(checkPos.up()).isAir() && 
            canSugarCaneGrow(world, pos)) {
            
            int growthStages = calculateGrowthStages(totalRandomTicks, maxHeight - currentHeight);
            for (int i = 0; i < growthStages && currentHeight < maxHeight; i++) {
                checkPos.move(Direction.UP);
                if (world.getBlockState(checkPos).isAir()) {
                    world.setBlockState(checkPos, state, 2);
                    currentHeight++;
                }
            }
        }
    }

    private static void simulateCactus(ServerWorld world, BlockPos pos, BlockState state, long totalRandomTicks) {
        int maxHeight = 3;
        int currentHeight = 1;
        BlockPos.Mutable checkPos = pos.mutableCopy();
        
        while (world.getBlockState(checkPos.up()).getBlock() instanceof CactusBlock && currentHeight < maxHeight) {
            currentHeight++;
            checkPos.move(Direction.UP);
        }

        if (currentHeight < maxHeight && 
            world.getBlockState(checkPos.up()).isAir() && 
            canCactusGrow(world, pos)) {
            
            int growthStages = calculateGrowthStages(totalRandomTicks, maxHeight - currentHeight);
            for (int i = 0; i < growthStages && currentHeight < maxHeight; i++) {
                checkPos.move(Direction.UP);
                if (world.getBlockState(checkPos).isAir()) {
                    world.setBlockState(checkPos, state, 2);
                    currentHeight++;
                }
            }
        }
    }

    private static void simulateBamboo(ServerWorld world, BlockPos pos, BlockState state, long totalRandomTicks) {
        int maxHeight = 16;
        int currentHeight = 1;
        BlockPos.Mutable checkPos = pos.mutableCopy();
        
        while (world.getBlockState(checkPos.up()).getBlock() instanceof BambooBlock && currentHeight < maxHeight) {
            currentHeight++;
            checkPos.move(Direction.UP);
        }

        if (currentHeight < maxHeight && 
            world.getBlockState(checkPos.up()).isAir() && 
            canBambooGrow(world, pos)) {
            
            int growthStages = calculateGrowthStages(totalRandomTicks, maxHeight - currentHeight);
            for (int i = 0; i < growthStages && currentHeight < maxHeight; i++) {
                checkPos.move(Direction.UP);
                if (world.getBlockState(checkPos).isAir()) {
                    world.setBlockState(checkPos, state, 2);
                    currentHeight++;
                }
            }
        }
    }

    private static boolean canBambooGrow(ServerWorld world, BlockPos pos) {
        BlockState soil = world.getBlockState(pos.down());
        return soil.isOf(Blocks.GRASS_BLOCK) || 
               soil.isOf(Blocks.DIRT) || 
               soil.isOf(Blocks.COARSE_DIRT) || 
               soil.isOf(Blocks.PODZOL) || 
               soil.isOf(Blocks.SAND);
    }

    private static boolean canPlantGrow(ServerWorld world, BlockPos pos) {
        return world.getBaseLightLevel(pos, 0) >= 9 &&
               world.getBlockState(pos.down()).isOf(Blocks.FARMLAND);
    }

    private static boolean canSugarCaneGrow(ServerWorld world, BlockPos pos) {
        BlockState soil = world.getBlockState(pos.down());
        return (soil.isOf(Blocks.GRASS_BLOCK) || 
                soil.isOf(Blocks.DIRT) || 
                soil.isOf(Blocks.COARSE_DIRT) || 
                soil.isOf(Blocks.SAND)) &&
               hasWaterAround(world, pos.down());
    }

    private static boolean canCactusGrow(ServerWorld world, BlockPos pos) {
        return world.getBlockState(pos.down()).isOf(Blocks.SAND) &&
               !hasBlocksAround(world, pos);
    }

    private static boolean hasWaterAround(ServerWorld world, BlockPos pos) {
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockState blockState = world.getBlockState(pos.offset(dir));
            if (blockState.getFluidState().isOf(net.minecraft.fluid.Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBlocksAround(ServerWorld world, BlockPos pos) {
        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (!world.getBlockState(pos.offset(dir)).isAir()) {
                return true;
            }
        }
        return false;
    }

    private static float calculateGrowthChance(long totalRandomTicks) {
        return Math.min(0.99f, (float)Math.log10(1 + totalRandomTicks) * 0.5f);
    }

    private static int calculateGrowthStages(long totalRandomTicks, int maxGrowth) {
        return Math.max(1, Math.min(maxGrowth, (int)(Math.log10(1 + totalRandomTicks) * 2)));
    }

    private static boolean canRandomTick(BlockState state) {
        return state.getBlock() instanceof CropBlock ||
               state.getBlock() instanceof SugarCaneBlock ||
               state.getBlock() instanceof CactusBlock ||
               state.getBlock() instanceof BambooBlock;
    }
}