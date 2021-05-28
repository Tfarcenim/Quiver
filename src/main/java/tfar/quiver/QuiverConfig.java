package tfar.quiver;

import net.minecraftforge.common.ForgeConfigSpec;

public class QuiverConfig {

    static ForgeConfigSpec.IntValue x;
    static ForgeConfigSpec.IntValue y;

    public static QuiverConfig buildServer(ForgeConfigSpec.Builder builder) {
        x = builder.defineInRange("xPos",76,-1000,1000);
        y = builder.defineInRange("yPos",23,-1000,1000);
        return null;
    }
}
