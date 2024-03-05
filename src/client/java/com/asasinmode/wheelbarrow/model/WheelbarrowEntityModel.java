// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class WheelbarrowEntityModel extends EntityModel<Entity> {
	private final ModelPart front;
	private final ModelPart back;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart bottom;
	private final ModelPart bottom_no_water;
	public WheelbarrowEntityModel(ModelPart root) {
		this.front = root.getChild("front");
		this.back = root.getChild("back");
		this.left = root.getChild("left");
		this.right = root.getChild("right");
		this.bottom = root.getChild("bottom");
		this.bottom_no_water = root.getChild("bottom_no_water");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData front = modelPartData.addChild("front", ModelPartBuilder.create().uv(0, 27).cuboid(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 22.0F, -15.0F, 0.0F, -3.1416F, 0.0F));

		ModelPartData back = modelPartData.addChild("back", ModelPartBuilder.create().uv(0, 19).cuboid(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(4.0F, 22.0F, 15.0F));

		ModelPartData left = modelPartData.addChild("left", ModelPartBuilder.create().uv(0, 43).cuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(9.0F, 22.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		ModelPartData right = modelPartData.addChild("right", ModelPartBuilder.create().uv(0, 35).cuboid(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-9.0F, 22.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		ModelPartData bottom = modelPartData.addChild("bottom", ModelPartBuilder.create().uv(0, 0).cuboid(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 21.0F, 0.0F, 0.0F, 1.5708F, -1.5708F));

		ModelPartData bottom_no_water = modelPartData.addChild("bottom_no_water", ModelPartBuilder.create().uv(60, 42).cuboid(-14.0F, -9.0F, -6.0F, 28.0F, 16.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 15.0F, 0.0F, 0.0F, 1.5708F, -1.5708F));
		return TexturedModelData.of(modelData, 128, 64);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		front.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		back.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		left.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		right.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bottom.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bottom_no_water.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}