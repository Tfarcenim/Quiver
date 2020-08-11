package tfar.quiver.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfar.quiver.Quiver;
import top.theillusivec4.curios.api.CuriosAPI;


@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

	@Inject(method = "findAmmo",at = @At("HEAD"),cancellable = true)
	private void checkQuiver(ItemStack shootable, CallbackInfoReturnable<ItemStack> cir) {
		if (!(shootable.getItem() instanceof ShootableItem)) {
			return;
		}
		ItemStack stack = CuriosAPI.getCurioEquipped(Quiver.arrow_predicate,(PlayerEntity)(Object)this).map(stringIntegerItemStackImmutableTriple -> stringIntegerItemStackImmutableTriple.right).orElse(ItemStack.EMPTY);
		if (!stack.isEmpty())cir.setReturnValue(stack);
	}
}
