package com.asasinmode.wheelbarrow.model;

import com.asasinmode.wheelbarrow.entity.custom.WheelbarrowEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class WheelbarrowEntityModel extends EntityModel<WheelbarrowEntity> {
	private final ModelPart root;
	private final ModelPart front;
	private final ModelPart back;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart bottom;
	private final ModelPart wheel;

	public WheelbarrowEntityModel(ModelPart root) {
		this.root = root.getChild("root");
		this.front = this.root.getChild("front");
		this.back = this.root.getChild("back");
		this.left = this.root.getChild("left");
		this.right = this.root.getChild("right");
		this.bottom = this.root.getChild("bottom");
		this.wheel = this.bottom.getChild("wheel");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create(),
				ModelTransform.pivot(0.0F, 24.0F, -4.0F));

		ModelPartData front = root.addChild("front",
				ModelPartBuilder.create().uv(4, 43).cuboid(6.0F, -4.0F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(2, 37).cuboid(6.0F, -6.0F, -4.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
						.uv(0, 29).cuboid(6.0F, -8.0F, -6.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F))
						.uv(2, 37).cuboid(-8.0F, -6.0F, -4.0F, 2.0F, 2.0F, 4.0F, new Dilation(0.0F))
						.uv(4, 43).cuboid(-8.0F, -4.0F, -2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 29).cuboid(-8.0F, -8.0F, -6.0F, 2.0F, 2.0F, 6.0F, new Dilation(0.0F))
						.uv(0, 60).cuboid(-8.0F, -2.0F, -2.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 56).cuboid(-8.0F, -4.0F, -4.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 52).cuboid(-8.0F, -6.0F, -6.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 48).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 2.0F, 2.0F, new Dilation(0.0F)),
				ModelTransform.pivot(0.0F, -6.0F, -8.0F));

		ModelPartData back = root.addChild("back",
				ModelPartBuilder.create().uv(0, 19).cuboid(-8.0F, -8.0F, -1.0F, 16.0F, 8.0F, 2.0F, new Dilation(0.0F))
						.uv(44, 40).cuboid(-8.0F, -8.0F, 1.0F, 2.0F, 2.0F, 8.0F, new Dilation(0.0F))
						.uv(44, 40).cuboid(6.0F, -8.0F, 1.0F, 2.0F, 2.0F, 8.0F, new Dilation(0.0F)),
				ModelTransform.pivot(0.0F, -6.0F, 7.0F));

		ModelPartData left = root.addChild("left",
				ModelPartBuilder.create().uv(32, 18).cuboid(-1.0F, -8.0F, -7.0F, 2.0F, 8.0F, 14.0F, new Dilation(0.0F)),
				ModelTransform.pivot(7.0F, -6.0F, -1.0F));

		ModelPartData right = root.addChild("right",
				ModelPartBuilder.create().uv(32, 18).cuboid(-1.0F, -8.0F, -7.0F, 2.0F, 8.0F, 14.0F, new Dilation(0.0F)),
				ModelTransform.pivot(-7.0F, -6.0F, -1.0F));

		ModelPartData bottom = root.addChild("bottom",
				ModelPartBuilder.create().uv(0, 0).cuboid(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new Dilation(0.0F))
						.uv(0, 8).cuboid(5.0F, 0.0F, 5.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F))
						.uv(0, 8).cuboid(-6.0F, 0.0F, 5.0F, 1.0F, 4.0F, 1.0F, new Dilation(0.0F)),
				ModelTransform.pivot(0.0F, -4.0F, 0.0F));

		ModelPartData wheel = bottom.addChild("wheel",
				ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)),
				ModelTransform.pivot(0.0F, 2.0F, -4.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}

	@Override
	public void setAngles(WheelbarrowEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// pitch it slightly forward when controlled
		if (entity.getControllingPassenger() instanceof PlayerEntity) {
			// todo try 2.5f
			this.root.pitch = 220.0f;
		} else {
			this.root.pitch = 0.0f;
		}

		this.wheel.pitch = limbSwing;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red,
			float green, float blue, float alpha) {
		root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}
