package dev.lazurite.fpvracing;

import dev.lazurite.fpvracing.client.input.keybinds.EMPKeybind;
import dev.lazurite.fpvracing.client.input.keybinds.GodModeKeybind;
import dev.lazurite.fpvracing.client.input.keybinds.GogglePowerKeybind;
import dev.lazurite.fpvracing.client.input.keybinds.NoClipKeybind;
import dev.lazurite.fpvracing.client.packet.GodModeC2S;
import dev.lazurite.fpvracing.client.packet.NoClipC2S;
import dev.lazurite.fpvracing.client.packet.PowerGogglesC2S;
import dev.lazurite.fpvracing.common.item.container.GogglesContainer;
import dev.lazurite.fpvracing.common.entity.VoxelRacerOne;
import dev.lazurite.fpvracing.common.item.container.QuadcopterContainer;
import dev.lazurite.fpvracing.common.item.container.TransmitterContainer;
import dev.lazurite.fpvracing.common.packet.ElectromagneticPulseS2C;
import dev.lazurite.fpvracing.common.packet.SelectedSlotS2C;
import dev.lazurite.fpvracing.common.packet.ShouldRenderPlayerS2C;
import dev.lazurite.fpvracing.common.item.ChannelWandItem;
import dev.lazurite.fpvracing.common.item.GogglesItem;
import dev.lazurite.fpvracing.common.item.QuadcopterItem;
import dev.lazurite.fpvracing.common.item.TransmitterItem;
import dev.lazurite.fpvracing.common.entity.QuadcopterEntity;
import dev.lazurite.fpvracing.client.render.QuadcopterRenderer;
import dev.lazurite.rayon.api.event.EntityBodyStepEvents;
import dev.lazurite.rayon.api.registry.DynamicEntityRegistry;
import dev.lazurite.rayon.api.shape.provider.BoundingBoxShapeProvider;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

public class FPVRacing implements ModInitializer, ClientModInitializer, ItemComponentInitializer, ModMenuApi {
	public static final String MODID = "fpvracing";

	/* Items */
	public static QuadcopterItem VOXEL_RACER_ONE_SPAWNER_ITEM;
	public static GogglesItem GOGGLES_ITEM;
	public static TransmitterItem TRANSMITTER_ITEM;
	public static ChannelWandItem CHANNEL_WAND_ITEM;
	public static ItemGroup ITEM_GROUP;

	/* Component */
	public static final ComponentKey<GogglesContainer> GOGGLES_CONTAINER = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "goggles_container"), GogglesContainer.class);
	public static final ComponentKey<TransmitterContainer> TRANSMITTER_CONTAINER = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "transmitter_container"), TransmitterContainer.class);
	public static final ComponentKey<QuadcopterContainer> QUADCOPTER_CONTAINER = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "quadcopter_container"), QuadcopterContainer.class);

	/* Entity */
	public static EntityType<VoxelRacerOne> VOXEL_RACER_ONE;

	@Override
	public void onInitialize() {
		NoClipC2S.register();
		GodModeC2S.register();
		PowerGogglesC2S.register();
		ElectromagneticPulseS2C.register();

		VOXEL_RACER_ONE_SPAWNER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "voxel_racer_one_spawner"), new QuadcopterItem(new Item.Settings().maxCount(1)));
		GOGGLES_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "goggles_item"), new GogglesItem(new Item.Settings().maxCount(1)));
		TRANSMITTER_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "transmitter_item"), new TransmitterItem(new Item.Settings().maxCount(1)));
		CHANNEL_WAND_ITEM = Registry.register(Registry.ITEM, new Identifier(MODID, "channel_wand_item"), new ChannelWandItem(new Item.Settings().maxCount(1)));

		ITEM_GROUP = FabricItemGroupBuilder
				.create(new Identifier(MODID, "items"))
				.icon(() -> new ItemStack(GOGGLES_ITEM))
				.appendItems(stack -> {
					stack.add(new ItemStack(VOXEL_RACER_ONE_SPAWNER_ITEM));
					stack.add(new ItemStack(GOGGLES_ITEM));
					stack.add(new ItemStack(TRANSMITTER_ITEM));
					stack.add(new ItemStack(CHANNEL_WAND_ITEM));
				})
				.build();

		VOXEL_RACER_ONE = Registry.register(
				Registry.ENTITY_TYPE,
				new Identifier(MODID, "voxel_racer_one"),
				FabricEntityTypeBuilder.create(SpawnGroup.MISC, VoxelRacerOne::new)
						.dimensions(EntityDimensions.fixed(0.5F, 0.125F))
						.trackedUpdateRate(3)
						.trackRangeBlocks(80)
						.forceTrackedVelocityUpdates(true)
						.build());

		DynamicEntityRegistry.INSTANCE.register(QuadcopterEntity.class, BoundingBoxShapeProvider::get, 1.0f, 0.05f);
		EntityBodyStepEvents.START_ENTITY_STEP.register((entity, delta) -> {
			if (entity.getEntity() instanceof QuadcopterEntity) {
				((QuadcopterEntity) entity.getEntity()).step(delta);
			}
		});
	}

	@Override
	public void onInitializeClient() {
		GLFW.glfwInit(); // forcefully initializes GLFW

		GogglePowerKeybind.register();
		GodModeKeybind.register();
		NoClipKeybind.register();
		EMPKeybind.register();
		SelectedSlotS2C.register();
		ShouldRenderPlayerS2C.register();

		QuadcopterRenderer.register();
	}

	@Override
	public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
		registry.registerFor(new Identifier(MODID, "goggles_item"), GOGGLES_CONTAINER, GogglesContainer::new);
		registry.registerFor(new Identifier(MODID, "transmitter_item"), TRANSMITTER_CONTAINER, TransmitterContainer::new);
		registry.registerFor(new Identifier(MODID, "quadcopter_item"), QUADCOPTER_CONTAINER, QuadcopterContainer::new);
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return null;
	}
}