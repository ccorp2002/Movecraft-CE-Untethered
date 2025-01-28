package net.countercraft.movecraft.processing.functions;

import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.processing.MovecraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a data carrying predicate, which can return a message depending on the success state of its evaluation
 * @param <T>
 */
@FunctionalInterface public interface IgnoreDetectionPredicate<T> extends TetradicPredicateWithList<T, CraftType, MovecraftWorld, Player, List>{

    @Override
    @Contract(pure = true)
    @NotNull Result validate(@NotNull T t, @NotNull CraftType type, @NotNull MovecraftWorld world, @Nullable Player player, @Nullable List list);

    @Contract(pure = true)
    default @NotNull IgnoreDetectionPredicate<T> or(@NotNull IgnoreDetectionPredicate<T> other){
        return (t, type, world, player, list) -> {
            var result = this.validate(t, type, world, player,list);
            if(result.isSucess()){
                return result;
            }
            return other.validate(t,type,world,player,list);
        };
    }

    @Contract(pure = true)
    default @NotNull IgnoreDetectionPredicate<T> and(@NotNull IgnoreDetectionPredicate<T> other){
        return (t, type, world, player, list) -> {
            var result = this.validate(t, type, world, player, list);
            if(!result.isSucess()){
                return result;
            }
            return other.validate(t,type,world,player,list);
        };
    }

}
