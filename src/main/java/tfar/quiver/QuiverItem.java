package tfar.quiver;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import tfar.quiver.curios.QuiverCurio;

import javax.annotation.Nullable;

public class QuiverItem extends Item {
	public QuiverItem(Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return Quiver.createProvider(new QuiverCurio());
	}
}
