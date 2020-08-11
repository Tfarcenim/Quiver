package tfar.quiver;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfar.quiver.curios.ArrowCurio;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.CuriosCapability;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.inventory.CuriosContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.function.Predicate;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;
import static net.minecraftforge.fml.InterModComms.sendTo;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Quiver.MODID)
public class Quiver {
	// Directly reference a log4j logger.

	public static final String MODID = "quiver";

	public static final Item quiver = new QuiverItem(new Item.Properties().group(ItemGroup.COMBAT).maxStackSize(1));

	public static final Tag<Item> arrow_curios = new ItemTags.Wrapper(new ResourceLocation("curios", "arrows"));

	public static final Predicate<ItemStack> arrow_predicate = stack -> stack.getItem().isIn(arrow_curios);

	public Quiver() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addGenericListener(Item.class, this::item);
		bus.addListener(this::comms);
		EVENT_BUS.addGenericListener(ItemStack.class, this::attachCaps);
		EVENT_BUS.addListener(this::arrowPickup);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			EVENT_BUS.addListener(this::drawSlotBack);
			bus.addListener(this::doClientStuff);
			bus.addListener(this::stitchTextures);
		}
	}

	private void arrowPickup(final EntityItemPickupEvent e) {
		ItemStack toPickup = e.getItem().getItem();
		PlayerEntity player = e.getPlayer();
		if (player.openContainer instanceof CuriosContainer) {
			return;
		}

		if (!CuriosAPI.getCurioEquipped(Quiver.arrow_predicate, player).
						map(stringIntegerItemStackImmutableTriple -> stringIntegerItemStackImmutableTriple.right).orElse(ItemStack.EMPTY).isEmpty()) {
			CuriosAPI.getCuriosHandler(player).ifPresent(iCurioItemHandler -> {
				ItemStack rem = toPickup.copy();
				CurioStackHandler curioStackHandler = iCurioItemHandler.getStackHandler("arrows");
				rem = curioStackHandler.insertItem(0, rem, true);
				if (toPickup.getCount() > rem.getCount()) {
					if (rem.isEmpty()) {
						curioStackHandler.insertItem(0, toPickup, false);
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
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("quiver").setHidden(true));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("arrows").setHidden(true));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_ICON, () -> new Tuple<>("quiver",new ResourceLocation(MODID,"item/empty_quiver_slot")));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_ICON, () -> new Tuple<>("arrows",new ResourceLocation(MODID,"item/empty_arrows_slot")));
	}

	private void drawSlotBack(GuiContainerEvent.DrawBackground e) {
		if (e.getGuiContainer() instanceof CuriosScreen) {
			CuriosScreen curiosScreen = (CuriosScreen) e.getGuiContainer();
			int i = curiosScreen.getGuiLeft();
			int j = curiosScreen.getGuiTop();
			curiosScreen.blit(i + 77, j + 19, 7, 7, 18, 36);
		}
	}

	public void stitchTextures(TextureStitchEvent.Pre evt) {
		if (evt.getMap().getBasePath().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
			String[] icons = new String[]{"arrows","quiver"};
			for (String icon : icons) {
				evt.addSprite(new ResourceLocation(MODID, "item/empty_" + icon + "_slot"));
			}
		}
	}

	private void attachCaps(AttachCapabilitiesEvent<ItemStack> e) {
		ItemStack stack = e.getObject();
		if (stack.getItem().isIn(arrow_curios)) {
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

	private void setup(final FMLCommonSetupEvent event) {
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
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
