package org.teamvoided.dusk_debris.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.teamvoided.dusk_debris.block.voided.sign.VoidSign;
import org.teamvoided.dusk_debris.init.DuskBlocks;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    @Inject(method = "supports", at = @At("HEAD"), cancellable = true)
    private void supports(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.isOf(DuskBlocks.getSUSPICIOUS_VOLCANIC_SAND())) {
            cir.setReturnValue(true);
        } else if (state.getBlock() instanceof VoidSign) cir.setReturnValue(true);
    }
}
