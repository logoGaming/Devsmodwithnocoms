package io.github.logogaming.brine.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.client.render.BrineRenderTypes;
import io.github.logogaming.brine.item.BrineItems;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private PlayerModel brine$playerModel;

    @Shadow
    protected abstract void createNormalMenuOptions(int y, int rowHeight);

    @Shadow
    @Final
    private static Component COPYRIGHT_TEXT;

    @Shadow
    @Nullable
    protected abstract Component getMultiplayerDisabledReason();

    @Shadow
    private long fadeInStart;

    @Shadow
    private boolean fading;

    @Shadow
    private float panoramaFade;

    @Shadow
    protected abstract void fadeWidgets(float alpha);

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void brine$customTitle(CallbackInfo ci) {
        int i = this.font.width(COPYRIGHT_TEXT);
        int j = this.width - i - 2;
        int l = this.height / 4 + 48;
        int rowHeight = 24;
        this.createNormalMenuOptions(l, rowHeight);

        SpriteIconButton language = (SpriteIconButton)this.addRenderableWidget(CommonButtons.language(20, (button) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true));

        //this.addRenderableWidget(Button.builder(Component.translatable("menu.options"), (button) -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).bounds(this.width / 2 - 100, l + 72 + 12, 98, 20).build());
        SpriteIconButton options = this.addRenderableWidget(SpriteIconButton.builder(Component.translatable("menu.options"), (button) -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)), true).width(20).sprite(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "icon/options"), 15, 15).build());

        this.addRenderableWidget(Button.builder(Component.literal("Quit"), (button) -> this.minecraft.stop()).bounds(this.width / 8, l + rowHeight * 3, 82, 20).build());

        SpriteIconButton accessibility = (SpriteIconButton)this.addRenderableWidget(CommonButtons.accessibility(20, (button) -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), true));
        accessibility.setPosition(this.width / 8, l + rowHeight*2);
        language.setPosition(this.width / 8+31, l  + rowHeight*2);
        options.setPosition(this.width / 8+62, l  + rowHeight*2);

        this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, (button) -> this.minecraft.setScreen(new CreditsAndAttributionScreen(this)), this.font));

        ci.cancel();
    }

    @Inject(method = "createNormalMenuOptions", at = @At("HEAD"), cancellable = true)
    private void brine$createNormalMenuOptions(int y, int rowHeight, CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("Solo"), (button) -> this.minecraft.setScreen(new SelectWorldScreen(this))).bounds(this.width / 8, y + rowHeight, 40, 20).build());
        Component component = this.getMultiplayerDisabledReason();
        boolean bl = component == null;
        Tooltip tooltip = component != null ? Tooltip.create(component) : null;
        ((Button)this.addRenderableWidget(Button.builder(Component.literal("Team"), (button) -> {
            Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
            this.minecraft.setScreen(screen);
        }).bounds(this.width / 8 + 42, y + rowHeight, 40, 20).tooltip(tooltip).build())).active = bl;

        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void brine$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float f = 1.0F;
        if (this.fading) {
            float g = (float)(Util.getMillis() - this.fadeInStart) / 2000.0F;
            if (g > 1.0F) {
                this.fading = false;
                this.panoramaFade = 1.0F;
            } else {
                g = Mth.clamp(g, 0.0F, 1.0F);
                f = Mth.clampedMap(g, 0.5F, 1.0F, 0.0F, 1.0F);
                this.panoramaFade = Mth.clampedMap(g, 0.0F, 0.5F, 0.0F, 1.0F);
            }

            this.fadeWidgets(f);
        }

        this.renderPanorama(guiGraphics, 0);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "textures/gui/title/background.png"), 0, 0, 0, 0, width, height, width, height);
        int i = Mth.ceil(f * 255.0F) << 24;
        if ((i & -67108864) != 0) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            //guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "textures/gui/title/brine.png"), 0, 0, 0, 0, 673, 241);
            guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "textures/gui/title/brine.png"), width/8, this.height / 4 - 44, 0, 0, 235, 84, 235, 84);

            String string = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                string = string + " Demo";
            } else {
                string = string + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            if (Minecraft.checkModStatus().shouldReportAsModified()) {
                string = string + I18n.get("menu.modded", new Object[0]);
            }

            guiGraphics.drawString(this.font, string, 2, this.height - 10, 16777215 | i);

            renderPlayer(guiGraphics, partialTick);

        }

        ci.cancel();
    }

    @Unique
    private float age = 0.0f;

    @Unique
    private void renderPlayer(GuiGraphics guiGraphics, float partialTick) {
        if (this.brine$playerModel == null && minecraft.getEntityModels() != null) {
            this.brine$playerModel = new PlayerModel(
                    minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), false
            );
        }

        if (this.brine$playerModel != null && minecraft.getGameProfile() != null) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();

            float offset = 10.0F * Mth.sin(age/30f);
            age+=partialTick;

            poseStack.translate(width/1.8f, height/1.85f+offset, 50.0F);
            poseStack.scale(45.0F, -45.0F, 45.0F);

            poseStack.mulPose(Axis.YP.rotationDegrees(20.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

            ResourceLocation skinTexture = minecraft.getSkinManager()
                    .getInsecureSkin(minecraft.getGameProfile())
                    .texture();

            var vertexConsumer = guiGraphics.bufferSource().getBuffer(BrineRenderTypes.titleScreenPlayer(skinTexture));

            this.brine$playerModel.head.setRotation(-20 * Mth.PI/180, -30 * Mth.PI/180, 0);
            this.brine$playerModel.hat.setRotation(this.brine$playerModel.head.xRot, this.brine$playerModel.head.yRot, this.brine$playerModel.head.zRot);
            this.brine$playerModel.leftArm.setRotation(-(65+5*Mth.sin((age+15f)/30f))*Mth.PI/180, -20*Mth.PI/180, 0);
            this.brine$playerModel.leftSleeve.setRotation(this.brine$playerModel.leftArm.xRot, this.brine$playerModel.leftArm.yRot, this.brine$playerModel.leftArm.zRot);
            this.brine$playerModel.rightArm.setRotation((10)*Mth.PI/180, 0, (10+5*Mth.sin((age+15f)/30f))*Mth.PI/180);
            this.brine$playerModel.rightSleeve.setRotation(this.brine$playerModel.rightArm.xRot, this.brine$playerModel.rightArm.yRot, this.brine$playerModel.rightArm.zRot);
            this.brine$playerModel.leftLeg.setRotation(-(20+10*Mth.sin((age+15f)/30f))*Mth.PI/180, 0, -5*Mth.PI/180);
            this.brine$playerModel.leftPants.setRotation(this.brine$playerModel.leftLeg.xRot, this.brine$playerModel.leftLeg.yRot, this.brine$playerModel.leftLeg.zRot);
            this.brine$playerModel.rightLeg.setRotation(-(20+10*Mth.sin((age+15f)/30f))*Mth.PI/180, 0, 10*Mth.PI/180);
            this.brine$playerModel.rightPants.setRotation(this.brine$playerModel.rightLeg.xRot, this.brine$playerModel.rightLeg.yRot, this.brine$playerModel.rightLeg.zRot);

            this.brine$playerModel.young = false;
            this.brine$playerModel.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    -1
            );

            poseStack.popPose();

            poseStack.pushPose();

            poseStack.translate(width/1.8f, height/1.85f + offset, 150.0F);
            poseStack.scale(45.0F, -45.0F, 45.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(20.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

            this.brine$playerModel.leftArm.translateAndRotate(poseStack);

            poseStack.translate(0.15F, 0.65F, 0.05F);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F));

            ItemStack flashlightStack = new ItemStack(BrineItems.FLASHLIGHT);

            CompoundTag tag = new CompoundTag();
            tag.putBoolean("On", true);

            flashlightStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            minecraft.getItemRenderer().renderStatic(
                    flashlightStack,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    guiGraphics.bufferSource(),
                    null,
                    0
            );

            poseStack.popPose();

            guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "textures/gui/title/serpent.png"), width-(width/2),0, 0, 0, (width/2), (width/2), (width/2), (width/2));

            poseStack.pushPose();
            poseStack.translate(width/1.8f, height/1.85f + offset, width/1.8f);
            poseStack.translate(180f, -128-5*Mth.sin((age+15f)/30f), 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(45));
            //this.brine$playerModel.leftArm.translateAndRotate(poseStack);

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1,1);
            guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Brine.MOD_ID, "textures/gui/title/light.png"), 0,0, 0, 0, 200, 200, 200, 200);
            RenderSystem.disableBlend();

            poseStack.popPose();

        }
    }
}
