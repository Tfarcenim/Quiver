package tfar.quiver.curios;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import tfar.quiver.Quiver;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.ICurio;

public class QuiverCurio implements ICurio {

	@Override
	public boolean canRightClickEquip() {
		return true;
	}

	@Override
	public void onUnequipped(String identifier, LivingEntity livingEntity) {
		ItemStack stack = CuriosAPI.getCurioEquipped(Quiver.arrow_predicate,livingEntity).map(stringIntegerItemStackImmutableTriple -> stringIntegerItemStackImmutableTriple.right).orElse(ItemStack.EMPTY);
		if (livingEntity instanceof PlayerEntity) {
			((PlayerEntity) livingEntity).addItemStackToInventory(stack);
		}
		CuriosAPI.getCuriosHandler(livingEntity).map(iCurioItemHandler -> iCurioItemHandler.getStackHandler("arrows"))
						.ifPresent(curioStackHandler -> curioStackHandler.setStackInSlot(0, ItemStack.EMPTY));
	}
}
