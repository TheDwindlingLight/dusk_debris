package org.teamvoided.dusk_debris.world.gen.density_functions

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.dynamic.CodecHolder
import net.minecraft.world.gen.DensityFunction
import net.minecraft.world.gen.DensityFunction.ContextProvider
import net.minecraft.world.gen.DensityFunctions
import org.teamvoided.dusk_debris.util.world_helper.makeCodec

class ShiftedNoiseRange(
    val shiftX: DensityFunction,
    val shiftY: DensityFunction,
    val shiftZ: DensityFunction,
    val horizontalScale: Double,
    val verticalScale: Double,
    val minYRange: Double,
    val maxYRange: Double,
    val noise: DensityFunction.NoiseHolder
) : DensityFunction {

    override fun compute(c: DensityFunction.FunctionContext): Double {
        val y2: Int = c.blockY()
        val y3: Int =
            if (y2 <= minYRange) minYRange.toInt()
            else if (y2 >= maxYRange) maxYRange.toInt()
            else y2

        val x: Double = c.blockX() * this.horizontalScale + shiftX.compute(c)
        val y: Double = y3 * this.horizontalScale + shiftY.compute(c)
        val z: Double = c.blockZ() * this.horizontalScale + shiftZ.compute(c)

        return noise.sample(x, y, z)
    }

    override fun fillArray(array: DoubleArray, context: ContextProvider) = context.fillAllDirectly(array, this)

    override fun mapAll(visitor: DensityFunction.Visitor): DensityFunction {
        return visitor.apply(
            ShiftedNoiseRange(
                shiftX.mapAll(visitor),
                shiftY.mapAll(visitor),
                shiftZ.mapAll(visitor),
                horizontalScale,
                verticalScale,
                minYRange,
                maxYRange,
                visitor.visitNoise(this.noise)
            )
        )
    }

    override fun minValue(): Double = -this.maxValue()

    override fun maxValue(): Double = noise.maxValue

    override fun codec(): CodecHolder<ShiftedNoiseRange> = CODEC

    companion object {
        private val DATA_CODEC: MapCodec<ShiftedNoiseRange> =
            RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter { it.shiftX },
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter { it.shiftY },
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter { it.shiftZ },
                    Codec.DOUBLE.fieldOf("xz_scale").forGetter { it.horizontalScale },
                    Codec.DOUBLE.fieldOf("y_scale").forGetter { it.verticalScale },
                    Codec.DOUBLE.fieldOf("min_y_range").forGetter { it.minYRange },
                    Codec.DOUBLE.fieldOf("max_y_range").forGetter { it.maxYRange },
                    DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter { it.noise })
                    .apply(instance, ::ShiftedNoiseRange)
            }

        val CODEC: CodecHolder<ShiftedNoiseRange> = makeCodec(DATA_CODEC)
    }
}