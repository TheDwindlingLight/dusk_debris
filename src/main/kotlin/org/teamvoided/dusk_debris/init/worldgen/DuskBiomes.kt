package org.teamvoided.dusk_debris.init.worldgen

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.world.biome.Biome
import org.teamvoided.dusk_debris.DuskDebris.id

object DuskBiomes {
    val FREEZING_WOODS = create("freezing_woods")

    fun init() {}

    private fun create(id: String): RegistryKey<Biome> = RegistryKey.of(RegistryKeys.BIOME, id(id))
}