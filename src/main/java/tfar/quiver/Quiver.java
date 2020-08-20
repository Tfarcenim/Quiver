package tfar.quiver;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfar.quiver.curios.ArrowCurio;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

import static net.minecraft.client.gui.screen.inventory.ContainerScreen.INVENTORY_BACKGROUND;
import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Quiver.MODID)
public class Quiver {
	// Directly reference a log4j logger.

	public static final String MODID = "quiver";

	public static final Item quiver = new QuiverItem(new Item.Properties().group(ItemGroup.COMBAT).maxStackSize(1));

	public static final ITag<Item> arrow_curios = ItemTags.makeWrapperTag(new ResourceLocation("curios", "arrows").toString());

	public static final Predicate<ItemStack> arrow_predicate = stack -> stack.getItem().isIn(arrow_curios);

	public Quiver() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addGenericListener(Item.class, this::item);
		bus.addListener(this::comms);
		EVENT_BUS.addGenericListener(ItemStack.class, this::attachCaps);
		EVENT_BUS.addListener(this::arrowPickup);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			EVENT_BUS.addListener(this::drawSlotBackground);
			bus.addListener(this::stitchTextures);
		}
	}

	private void arrowPickup(final EntityItemPickupEvent e) {
		ItemStack toPickup = e.getItem().getItem();
		PlayerEntity player = e.getPlayer();
		if (player.openContainer instanceof CuriosContainer) {
			return;
		}

		if (!CuriosApi.getCuriosHelper().findEquippedCurio(Quiver.arrow_predicate, player).
						map(stringIntegerItemStackImmutableTriple -> stringIntegerItemStackImmutableTriple.right).orElse(ItemStack.EMPTY).isEmpty()) {
			CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(icurioitemhandler -> {
				ItemStack rem = toPickup.copy();
				ICurioStacksHandler iCurioStacksHandler = icurioitemhandler.getCurios().get("arrows");

				IDynamicStackHandler iDynamicStackHandler = iCurioStacksHandler.getStacks();

				rem = iDynamicStackHandler.insertItem(0, rem, true);
				if (toPickup.getCount() > rem.getCount()) {
					if (rem.isEmpty()) {
						iDynamicStackHandler.insertItem(0, toPickup, false);
						toPickup.setCount(0);
						e.setCanceled(true);
					} else {
						toPickup.setCount(rem.getCount());
					}
				}
			});
		}
	}

	private void comms(final InterModEnqueueEvent event) {
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("quiver")
				.hide()
				.icon(new ResourceLocation(MODID,"item/empty_quiver_slot")).build());
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("arrows")
				.hide()
				.icon(new ResourceLocation(MODID,"item/empty_arrows_slot")).build());
	}

	private void drawSlotBackground(GuiContainerEvent.DrawBackground e) {
		if (e.getGuiContainer() instanceof CuriosScreen) {
			Minecraft.getInstance().getTextureManager().bindTexture(INVENTORY_BACKGROUND);
			CuriosScreen curiosScreen = (CuriosScreen) e.getGuiContainer();
			int i = curiosScreen.getGuiLeft();
			int j = curiosScreen.getGuiTop();
			curiosScreen.blit(e.getMatrixStack(),i + 77, j + 19, 7, 7, 18, 36);
		}
	}

	public void stitchTextures(TextureStitchEvent.Pre evt) {
		if (evt.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
			String[] icons = new String[]{"arrows","quiver"};
			for (String icon : icons) {
				evt.addSprite(new ResourceLocation(MODID, "item/empty_" + icon + "_slot"));
			}
		}
	}

	private void attachCaps(AttachCapabilitiesEvent<ItemStack> e) {
		ItemStack stack = e.getObject();
		if (ItemTags.getCollection().get(new ResourceLocation("curios","arrows")) != null
				&& arrow_curios.contains(stack.getItem())) {
			ArrowCurio arrowCurio = new ArrowCurio();
			e.addCapability(CuriosCapability.ID_ITEM, new ICapabilityProvider() {
				final LazyOptional<ICurio> curio = LazyOptional.of(() -> arrowCurio);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap,
														 @Nullable Direction side) {
					return CuriosCapability.ITEM.orEmpty(cap, curio);

				}
			});
		}
	}

	private void item(RegistryEvent.Register<Item> e) {
		e.getRegistry().register(quiver.setRegistryName("quiver"));
	}

	public static ICapabilityProvider createProvider(ICurio curio) {
		return new Provider(curio);
	}

	public static class Provider implements ICapabilityProvider {
		final LazyOptional<ICurio> capability;

		Provider(ICurio curio) {
			this.capability = LazyOptional.of(() -> curio);
		}

		@Nonnull
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
			return CuriosCapability.ITEM.orEmpty(cap, this.capability);
		}
	}

}
