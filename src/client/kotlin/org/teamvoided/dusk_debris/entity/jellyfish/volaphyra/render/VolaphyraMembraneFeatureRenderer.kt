package org.teamvoided.dusk_debris.entity.jellyfish.volaphyra.render

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModelLoader
import net.minecraft.client.util.math.MatrixStack
import org.teamvoided.dusk_debris.entity.AbstractVolaphyraEntity
import org.teamvoided.dusk_debris.entity.DuskEntityModelLayers
import org.teamvoided.dusk_debris.entity.jellyfish.volaphyra.VolaphyraEntityRenderer.Companion.VOLAPHYRA_MESOGLEA
import org.teamvoided.dusk_debris.entity.jellyfish.volaphyra.model.VolaphyraCoreModel
import org.teamvoided.dusk_debris.entity.jellyfish.volaphyra.model.VolaphyraMesogleaModel

class VolaphyraMembraneFeatureRenderer(
    context: FeatureRendererContext<AbstractVolaphyraEntity, VolaphyraCoreModel>,
    loader: EntityModelLoader
) : FeatureRenderer<AbstractVolaphyraEntity, VolaphyraCoreModel>(context) {
    private val model = VolaphyraMesogleaModel(loader.getModelPart(DuskEntityModelLayers.VOLAPHYRA_MESOGLEA))

    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int, //i
        entity: AbstractVolaphyraEntity,
        limbAngle: Float, //f
        limbDistance: Float, //g
        tickDelta: Float, //h
        age: Float, //j
        headYaw: Float, //k
        headPitch: Float //l
    ) {
        val minecraftClient = MinecraftClient.getInstance()
        val bl = minecraftClient.hasOutline(entity) && entity.isInvisible
        if (!entity.isInvisible || bl) {
            val vertexConsumer = if (bl) {
                vertexConsumers.getBuffer(RenderLayer.getOutline(VOLAPHYRA_MESOGLEA))
            } else {
                vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(VOLAPHYRA_MESOGLEA))
            }

            this.contextModel.copyStateTo(this.model)
            model.animateModel(entity, limbAngle, limbDistance, tickDelta)
            model.setAngles(entity, limbAngle, limbDistance, age, headYaw, headPitch)
            model.method_60879(matrices, vertexConsumer, light, LivingEntityRenderer.getOverlay(entity, 0.0f))
        }
    }
}