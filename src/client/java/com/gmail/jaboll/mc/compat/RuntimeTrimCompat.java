package com.gmail.jaboll.mc.compat;

//import com.bawnorton.runtimetrims.client.RuntimeTrimsClient;
//import com.bawnorton.runtimetrims.client.palette.TrimPalettes;
import net.minecraft.world.item.Item;


public class RuntimeTrimCompat {

//    private static final TrimPalettes palettes = RuntimeTrimsClient.getTrimPalettes();

    public static int[] getPallete(Item item){
//        return palettes.getOrGeneratePalette(item).getColourArr();
        return new int[0];
    }
}
