package tfar.quiver;

import net.minecraft.entity.player.PlayerEntity;
import tfar.quiver.mixin.ContainerAccessor;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

import java.util.Map;


public class MixinEvents {

    public static void onCurioContainerCreated(CuriosContainer curiosContainer, PlayerEntity player) {
        curiosContainer.curiosHandler.ifPresent(iCuriosItemHandler -> {
            Map<String, ICurioStacksHandler> curioMap = iCuriosItemHandler.getCurios();

            for (String identifier : curioMap.keySet()) {
                if (identifier.equals("quiver")) {
                    ICurioStacksHandler stackHandler = curioMap.get(identifier);
                    IDynamicStackHandler iDynamicStackHandler = stackHandler.getStacks();
                    ((ContainerAccessor) curiosContainer).$addSlot(new CurioSlot(player, iDynamicStackHandler, 0, identifier, 78, 20, stackHandler.getRenders()));
                }
                if (identifier.equals("arrows")) {
                    ICurioStacksHandler stackHandler = curioMap.get(identifier);
                    IDynamicStackHandler iDynamicStackHandler = stackHandler.getStacks();
                    ((ContainerAccessor) curiosContainer).$addSlot(new CurioSlot(player, iDynamicStackHandler, 0, identifier, 78, 38, stackHandler.getRenders()));
                }
            }
        });
    }
}
