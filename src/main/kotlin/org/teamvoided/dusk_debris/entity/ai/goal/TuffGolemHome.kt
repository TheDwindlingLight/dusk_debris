package org.teamvoided.dusk_debris.entity.ai.goal

import net.minecraft.entity.MovementType
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.teamvoided.dusk_debris.entity.TuffGolemEntity
import java.util.*

open class TuffGolemHome(
    private val golem: TuffGolemEntity,
    private val speed: Double
) : Goal() {

    init {
        this.controls = EnumSet.of(Control.MOVE)
    }

    override fun canStart(): Boolean =
        !golem.hasControllingPassenger() &&
                golem.statueTicks > 0 &&
                golem.summonedPos != null

    override fun start() {
        if (golem.summonedPos != null && golem.statueTicks > 0) {
            golem.navigation.stop()
            val target = golem.summonedPos!!.ofBottomCenter()
            golem.navigation.startMovingTo(target.x, target.y, target.z, 0, this.speed)
        }
    }

    override fun tick() {
        if (golem.summonedPos != null && golem.statueTicks > 0) {
            val summonPos = golem.squaredDistanceTo(golem.summonedPos!!.ofBottomCenter())
            if (summonPos < 0.5) {
                if (golem.state != golem.statueState && summonPos < 0.1) {
                    golem.setStateStatue()
                    golem.yaw = (golem.yaw.toInt() / 90) * 90f
                }
                golem.move(MovementType.SELF, moveTo(golem.summonedPos!!))
            }
            super.tick()
        }
    }

    override fun stop() {
        golem.navigation.stop()
        super.stop()
    }

    private fun moveTo(endPos: BlockPos): Vec3d {
        val x: Double = ((endPos.x + 0.5) - golem.x) / 8
        val z: Double = ((endPos.z + 0.5) - golem.z) / 8
        return Vec3d(x, 0.0, z)
    }
}