package com.github.salvadormg15.rubber_duck.client.curio;

import com.github.salvadormg15.rubber_duck.common.RubberDuckItem;
import com.github.salvadormg15.rubber_duck.common.Registries;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class RubberDuckCurioRenderer implements ICurioRenderer {
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if(!(renderLayerParent.getModel() instanceof HeadedModel parentModel))
            return;

        ItemStack itemstack = new ItemStack(Registries.RUBBER_DUCK_ITEM.get().asItem());
        Item item = itemstack.getItem();
        matrixStack.pushPose();

        parentModel.getHead().translateAndRotate(matrixStack);
        if(item instanceof RubberDuckItem) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            //Places the duck a little bit upper if there is a helmet
            final var playerEntity = slotContext.entity();
            if(playerEntity.getItemBySlot(EquipmentSlot.HEAD).isEmpty())
                matrixStack.translate(0, -0.25D, 0);
            else {
                if(playerEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof RubberDuckItem) {
                    matrixStack.translate(0, -0.425D, .05D);
                    matrixStack.mulPose(Axis.XP.rotationDegrees(20f));
                }else
                    matrixStack.translate(0, -0.313, 0);
            }

            matrixStack.scale(0.62F, -0.62F, -0.62F);

            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(playerEntity, itemstack,
                    ItemDisplayContext.HEAD, false, matrixStack, renderTypeBuffer, light);
        }

        matrixStack.popPose();
    }
}
