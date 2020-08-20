package tfar.quiver.curios;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import tfar.quiver.Quiver;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Set;

public class QuiverCurio implements ICurio {

	@Override
	public boolean canRightClickEquip() {
		return true;
	}

	@Override
	public void onUnequip(String identifier, int index, LivingEntity livingEntity) {
		ItemStack stack = CuriosApi.getCuriosHelper().findEquippedCurio(Quiver.arrow_predicate,livingEntity).map(stringIntegerItemStackImmutableTriple -> stringIntegerItemStackImmutableTriple.right).orElse(ItemStack.EMPTY);
		if (livingEntity instanceof PlayerEntity) {
			if (!((PlayerEntity) livingEntity).addItemStackToInventory(stack)) {
				InventoryHelper.spawnItemStack(livingEntity.world,livingEntity.getPosX(),livingEntity.getPosY(),livingEntity.getPosZ(),stack);
			}
		}
		CuriosApi.getCuriosHelper().getCuriosHandler(livingEntity).map(ICuriosItemHandler::getCurios)
				.map(stringICurioStacksHandlerMap -> stringICurioStacksHandlerMap.get("arrows"))
				.map(ICurioStacksHandler::getStacks)
				.ifPresent(curioStackHandler -> curioStackHandler.setStackInSlot(0, ItemStack.EMPTY));
	}
}
