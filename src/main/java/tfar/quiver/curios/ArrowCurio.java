package tfar.quiver.curios;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import tfar.quiver.Quiver;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;

public class ArrowCurio implements ICurio {

	@Override
	public boolean canEquip(String identifier, LivingEntity livingEntity) {
		return CuriosAPI.getCurioEquipped(Quiver.quiver,livingEntity)
						.map(stringIntegerItemStackImmutableTriple -> stringIntegerItemStackImmutableTriple.right)
						.map(ItemStack::getItem).map(item -> item == Quiver.quiver).orElse(false);
	}

	@Override
	public boolean canRightClickEquip() {
		return true;
	}
}
