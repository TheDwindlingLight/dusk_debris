package org.teamvoided.dusk_debris.particle

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType

@Environment(EnvType.CLIENT)
open class GunpowderExplosionSmokeParticle(
    world: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    velocityX: Double,
    velocityY: Double,
    velocityZ: Double,
    spriteProvider: SpriteProvider,
//    colorRed: Float,
//    colorBlue: Float,
//    colorGreen: Float
) :
    SpriteBillboardParticle(world, x, y, z) {
    private val spriteProvider: SpriteProvider

    init {
//        this.gravityStrength = -0.1f
        this.velocityMultiplier = 0.9f
        this.spriteProvider = spriteProvider
//        this.velocityX = velocityX + (Math.random() * 2.0 - 1.0) * 0.05
//        this.velocityY = velocityY + (Math.random() * 2.0 - 1.0) * 0.05
//        this.velocityZ = velocityZ + (Math.random() * 2.0 - 1.0) * 0.05
        val grayscaleColor = random.nextFloat() * 0.3f + 0.7f
        this.colorRed = grayscaleColor
        this.colorBlue = grayscaleColor
        this.colorGreen = grayscaleColor
        this.scale = 0.1f * (random.nextFloat() * random.nextFloat() * 6.0f + 1.0f)
        this.maxAge = ((random.nextFloat() * 80).toInt() + 60)
        this.setSpriteForAge(spriteProvider)
    }

    override fun getType(): ParticleTextureSheet {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE
    }

    override fun tick() {
        super.tick()
        this.setSpriteForAge(this.spriteProvider)
    }

    @Environment(EnvType.CLIENT)
    class Factory(private val spriteProvider: SpriteProvider) : ParticleFactory<DefaultParticleType> {
        override fun createParticle(
            defaultParticleType: DefaultParticleType,
            world: ClientWorld,
            posX: Double,
            posY: Double,
            posZ: Double,
            velX: Double,
            velY: Double,
            velZ: Double,
        ): Particle {
            return GunpowderExplosionSmokeParticle(world, posX, posY, posZ, velX, velY, velZ, this.spriteProvider)
        }
    }
}