package io.github.logogaming.brine.client.render.entity.clam;// Made with Blockbench 5.2.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.logogaming.brine.Brine;
import io.github.logogaming.brine.entity.custom.ClamEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ClamModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Brine.id("clam"), "main");
	private final ModelPart bottom;
	private final ModelPart top;
	private final ModelPart bb_main;

	private final ModelPart root;
	private final ModelPart pearl;

	public ClamModel(ModelPart root) {
		this.root = root;
		this.bottom = root.getChild("bottom");
		this.top = root.getChild("top");
		this.bb_main = root.getChild("bb_main");
		this.pearl = root.getChild("pearl");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -1.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 22).addBox(-8.0F, -2.0F, 4.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(22, 22).addBox(-8.0F, -2.0F, -5.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 24.0F, 0.0F));

		PartDefinition bottom_r1 = bottom.addOrReplaceChild("bottom_r1", CubeListBuilder.create().texOffs(18, 26).addBox(-8.0F, -1.0F, 4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -1.0F, -4.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition bottom_r2 = bottom.addOrReplaceChild("bottom_r2", CubeListBuilder.create().texOffs(0, 26).addBox(-8.0F, -1.0F, 4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, -4.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition top = partdefinition.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 11).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 24).addBox(-5.0F, -2.0F, 4.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(22, 24).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, -3.1416F, 0.0F, 0.0F));

		PartDefinition bottom_r3 = top.addOrReplaceChild("bottom_r3", CubeListBuilder.create().texOffs(18, 28).addBox(-8.0F, -1.0F, 4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -1.0F, -4.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition bottom_r4 = top.addOrReplaceChild("bottom_r4", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -1.0F, 4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition pearl = partdefinition.addOrReplaceChild("pearl", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 30).addBox(-2.0F, -3.0F, 5.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}


	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.getAllParts().forEach(ModelPart::resetPose);

		if(!(entity instanceof ClamEntity clamEntity)) return;

		this.pearl.visible = clamEntity.hasPearl();

		this.animate(clamEntity.closeAnimationState, ClamAnimation.close, ageInTicks, 1.0F);
		this.animate(clamEntity.openAnimationState, ClamAnimation.open, ageInTicks, 1.0F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		top.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
		pearl.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public @NotNull ModelPart root() {
		return root;
	}

}