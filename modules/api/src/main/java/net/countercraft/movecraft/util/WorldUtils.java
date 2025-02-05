package net.countercraft.movecraft.util;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldUtils {
    private static Integer minecraftVersion = null;

    private static boolean is1_17OrNewer() {
        if(minecraftVersion == null) {
            var packageName = Bukkit.getServer().getClass().getPackage().getName();
            var packageVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            var parts = packageVersion.split("_");
            minecraftVersion = Integer.valueOf(parts[1]);
        }
        return minecraftVersion >= 17;
    }

    public static int getWorldMinHeightLimit(World w) {
        try {
            return (int) w.getMinHeight();
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
