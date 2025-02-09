package org.teamvoided.dusk_debris.block.throwable_bomb.bonecaller

import net.minecraft.util.math.BlockPos
import net.minecraft.world.*
import net.minecraft.world.explosion.ExplosionBehavior
import org.teamvoided.dusk_debris.block.throwable_bomb.AbstractThrwowableBombBlock
import org.teamvoided.dusk_debris.entity.throwable_bomb.BlunderbombEntity
import org.teamvoided.dusk_debris.entity.throwable_bomb.BonecallerEntity
import org.teamvoided.dusk_debris.entity.throwable_bomb.bonecaller.BonechillerEntity
import org.teamvoided.dusk_debris.init.DuskEntities

open class BonechillerBlock(settings: Settings) : AbstractThrwowableBombBlock(settings) {
    override fun explode(world: World, pos: BlockPos, explosionBehavior: ExplosionBehavior) {
        world.breakBlock(pos, false)
        val bombEntity = BonechillerEntity(
            pos.x.toDouble() + 0.5,
            pos.y.toDouble() + Math.random() * 0.8,
            pos.z.toDouble() + 0.5,
            world
        )
        world.spawnEntity(bombEntity)
    }
}