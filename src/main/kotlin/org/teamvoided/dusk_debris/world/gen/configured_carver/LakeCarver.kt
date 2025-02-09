package org.teamvoided.dusk_debris.world.gen.configured_carver

import com.mojang.serialization.Codec
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.registry.Holder
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler
import net.minecraft.util.random.LegacySimpleRandom
import net.minecraft.util.random.RandomGenerator
import net.minecraft.world.biome.Biome
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.ChunkRandom
import net.minecraft.world.gen.DensityFunction
import net.minecraft.world.gen.carver.Carver
import net.minecraft.world.gen.carver.CarverContext
import net.minecraft.world.gen.carver.CarvingMask
import net.minecraft.world.gen.chunk.AquiferSampler
import org.apache.commons.lang3.mutable.MutableBoolean
import org.teamvoided.dusk_debris.world.gen.configured_carver.config.LakeCarverConfig
import org.teamvoided.dusk_debris.world.gen.configured_carver.config.debug.LakeCarverDebugConfig
import java.util.function.Function
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class LakeCarver(codec: Codec<LakeCarverConfig>) : Carver<LakeCarverConfig>(codec) {
    override fun shouldCarve(ravineCarverConfig: LakeCarverConfig, random: RandomGenerator): Boolean {
        return random.nextFloat() <= ravineCarverConfig.probability
    }

    override fun carve(
        carverContext: CarverContext,
        config: LakeCarverConfig,
        chunk: Chunk,
        function: Function<BlockPos, Holder<Biome>>,
        random: RandomGenerator,
        aquiferSampler: AquiferSampler,
        chunkPos: ChunkPos,
        carvingMask: CarvingMask
    ): Boolean {
        val posX: Double = chunkPos.getOffsetX(random.nextInt(16)).toDouble()
        val posY: Double = config.y.get(random, carverContext).toDouble()
        val posZ: Double = chunkPos.getOffsetZ(random.nextInt(16)).toDouble()
        val waterLevel: Double = config.waterLevel.get(random).toDouble()


//        chunk.setBlockState(Vec3d(posX, posY, posZ).toBlockPos(), Blocks.GLOWSTONE.defaultState, false)

        val skipPredicate =
            SkipOrWaterPredicate { context: CarverContext, scaledRelativeX: Double, scaledRelativeY: Double, scaledRelativeZ: Double, y: Int, dps: Double ->
                isPositionExcludedOrWater(
                    scaledRelativeX,
                    scaledRelativeY,
                    scaledRelativeZ,
                    waterLevel,
                    dps
                )
            }

        val height: Double = config.yScale.get(random).toDouble() //radius*this+2
        val radius: Float = config.horizontalRadius.get(random).toFloat() //2*this+2 //1.0f + random.nextFloat() * 6.0f
        this.carveCave(
            carverContext,
            config,
            chunk,
            function,
            aquiferSampler,
            posX,
            posY,
            posZ,
            radius,
            height,
            carvingMask,
            skipPredicate
        )

        return true
    }

    protected fun carveCave(
        context: CarverContext,
        config: LakeCarverConfig,
        chunk: Chunk,
        posToBiome: Function<BlockPos, Holder<Biome>>,
        aquiferSampler: AquiferSampler,
        x: Double,
        y: Double,
        z: Double,
        radius: Float,
        height: Double,
        carvingMask: CarvingMask,
        predicate: SkipOrWaterPredicate
    ) {
        val horizontalScale = 1.5 + radius
        val verticalScale = horizontalScale * height
        this.carveRegion(
            context,
            config,
            chunk,
            posToBiome,
            aquiferSampler,
            x + 1,
            y,
            z,
            horizontalScale,
            verticalScale,
            carvingMask,
            predicate
        )
    }

    //overide but custom predicate
    private fun carveRegion(
        context: CarverContext,
        config: LakeCarverConfig,
        chunk: Chunk,
        posToBiome: Function<BlockPos, Holder<Biome>>,
        sampler: AquiferSampler,
        x: Double,
        y: Double,
        z: Double,
        horizontalScale: Double,
        verticalScale: Double,
        mask: CarvingMask,
        predicate: SkipOrWaterPredicate
    ): Boolean {
        val chunkPos = chunk.pos
        val posX = chunkPos.centerX.toDouble()
        val posZ = chunkPos.centerZ.toDouble()
        val f = 16.0 + horizontalScale * 2.0
        if (!(abs(x - posX) > f) && !(abs(z - posZ) > f)) {
            val startX = chunkPos.startX
            val startZ = chunkPos.startZ
            val xMin = max((MathHelper.floor(x - horizontalScale) - startX - 1.0), 0.0).toInt()
            val xMax = min((MathHelper.floor(x + horizontalScale) - startX).toDouble(), 15.0).toInt()
            val yMin = max((MathHelper.floor(y - verticalScale) - 1.0), (context.minY + 1.0)).toInt()
            val n = if (chunk.hasBelowZeroRetrogen()) 0 else 7
            val yMax =
                min(
                    (MathHelper.floor(y + verticalScale) + 1.0),
                    (context.minY + context.height - 1.0 - n)
                ).toInt()
            val zMin = max((MathHelper.floor(z - horizontalScale) - startZ - 1.0), 0.0).toInt()
            val zMax = min((MathHelper.floor(z + horizontalScale) - startZ).toDouble(), 15.0).toInt()
            var bl = false
            val mutable = BlockPos.Mutable()
            val mutable2 = BlockPos.Mutable()

            val chunkRandom = ChunkRandom(LegacySimpleRandom(0)) ////////HOW TO GET SEED OF WORLD
            val dps = DoublePerlinNoiseSampler.create(chunkRandom, -4, 2.0, 1.0, 0.0)

            for (loopX in xMin..xMax) {
                val chunkX = chunkPos.getOffsetX(loopX)
                val funX = (chunkX + 0.5 - x) / horizontalScale

                for (loopZ in zMin..zMax) {
                    val chunkZ = chunkPos.getOffsetZ(loopZ)
                    val funZ = (chunkZ + 0.5 - z) / horizontalScale
                    if (!(funX * funX + funZ * funZ >= 1.0)) {
                        val mutableBoolean = MutableBoolean(false)

                        for (loopY in yMax downTo yMin + 1) {
                            val funY = (loopY - 0.5 - y) / verticalScale

                            val sample = dps.sample(chunkX.toDouble(), loopY * 0.25, chunkZ.toDouble())
                            val sampleMathed = abs(sample)
                            val output = predicate.shouldSkip(context, funX, funY, funZ, loopY, sampleMathed)
                            if (output != 0 && (!mask[loopX, loopY, loopZ] || isDebug(config))) {
                                mask[loopX, loopY] = loopZ
                                mutable[chunkX, loopY] = chunkZ
                                bl = bl or carveAtPoint(
                                    context,
                                    config,
                                    chunk,
                                    posToBiome,
                                    mask,
                                    mutable,
                                    mutable2,
                                    sampler,
                                    mutableBoolean,
                                    output
                                )
                            }
                        }
                    }
                }
            }
            return bl
        } else {
            return false
        }
    }

    private fun carveAtPoint(
        context: CarverContext,
        config: LakeCarverConfig,
        chunk: Chunk,
        posToBiome: Function<BlockPos, Holder<Biome>>,
        mask: CarvingMask,
        pos: BlockPos.Mutable,
        downPos: BlockPos.Mutable,
        sampler: AquiferSampler,
        foundSurface: MutableBoolean,
        predicateResult: Int,

        ): Boolean {
        val blockState = chunk.getBlockState(pos)
        if (blockState.isOf(Blocks.GRASS_BLOCK) || blockState.isOf(Blocks.MYCELIUM)) {
            foundSurface.setTrue()
        }

        if (!this.canReplaceBlock(config, blockState) && !isDebug(config)) {
            return false
        } else {
            val blockState2 =
                this.getState(context, config, pos, sampler, predicateResult, chunk)
            if (blockState2 == null) {
                return false
            } else {
                chunk.setBlockState(pos, blockState2, false)
                if (sampler.needsFluidTick() && !blockState2.fluidState.isEmpty) {
                    chunk.markBlockForPostProcessing(pos)
                }

                if (foundSurface.isTrue) {
                    downPos[pos] = Direction.DOWN
                    if (chunk.getBlockState(downPos).isOf(Blocks.DIRT)) {
                        context.applyMaterialRule(posToBiome, chunk, downPos, !blockState2.fluidState.isEmpty)
                            .ifPresent { state: BlockState ->
                                chunk.setBlockState(downPos, state, false)
                                if (!state.fluidState.isEmpty) {
                                    chunk.markBlockForPostProcessing(downPos)
                                }
                            }
                    }
                }

                return true
            }
        }
    }


    private fun getState(
        context: CarverContext,
        config: LakeCarverConfig,
        pos: BlockPos,
        sampler: AquiferSampler,
        predicateResult: Int,
        chunk: Chunk,
    ): BlockState? {
        val debug = isDebug(config)
        if (pos.y <= config.lavaLevel.getY(context)) {
            return if (debug) config.debugConfig.lavaState else LAVA.blockState
        } else {
            val blockState = sampler.apply(DensityFunction.SinglePointContext(pos.x, pos.y, pos.z), 0.0)
            if (blockState == null) {
                return if (debug) config.debugConfig.barrierState else null
//            } else if (predicateResult == 2) {
//                return if (debug) Blocks.WHITE_STAINED_GLASS.defaultState else blockState
            } else if (predicateResult >= 2) {
                return if (debug) LakeCarverDebugConfig.default().fluidState else config.fluidState
            } else {
                return if (debug) getDebugState(config, blockState) else blockState
            }
        }
    }

    private fun isPositionExcludedOrWater(
        scaledRelativeX: Double,
        scaledRelativeY: Double,
        scaledRelativeZ: Double,
        waterY: Double,
        dps: Double
    ): Int {
        val value =
            scaledRelativeX * scaledRelativeX + scaledRelativeY * scaledRelativeY + scaledRelativeZ * scaledRelativeZ + dps
        return if (value <= 1.0) if (scaledRelativeY <= waterY) if (value <= 0.8) 3 else 2 else 1 else 0

        //0 means nothing
        //1 means air
        //2 means fluid barrier
        //3 means fluid
    }


    fun interface SkipOrWaterPredicate {
        fun shouldSkip(carverContext: CarverContext, x: Double, y: Double, z: Double, waterY: Int, sample: Double): Int
    }
}