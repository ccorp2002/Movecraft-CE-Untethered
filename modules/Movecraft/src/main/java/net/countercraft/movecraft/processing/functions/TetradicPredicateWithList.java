package net.countercraft.movecraft.processing.functions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface public interface TetradicPredicateWithList<T, U, V, W, Y> {
    @Contract(pure = true) @NotNull Result validate(@NotNull T t, @NotNull U u, @NotNull V v, @NotNull W w, @Nullable Y y);

    @Contract(pure = true)
    default TetradicPredicateWithList<T, U, V, W, Y> or(@NotNull TetradicPredicateWithList<T, U, V, W, Y> other){
        return (t,u,v,w,y) -> {
            var result = this.validate(t,u,v,w,y);
            if(result.isSucess()){
                return result;
            }
            return other.validate(t,u,v,w,y);
        };
    }

    @Contract(pure = true)
    default @NotNull TetradicPredicateWithList<T, U, V, W, Y> and(@NotNull TetradicPredicateWithList<T, U, V, W, Y> other){
        return (t,u,v,w,y) -> {
            var result = this.validate(t,u,v,w,y);
            if(!result.isSucess()){
                return result;
            }
            return other.validate(t,u,v,w,y);
        };
    }

    @Contract(pure = true)
    default @NotNull TriadicPredicate<U, V, W> fixFirst(T t){
        return (u, v, w) -> this.validate(t, u, v, w, null);
    }

    @Contract(pure = true)
    default @NotNull TriadicPredicate<T, V, W> fixSecond(U u){
        return (t, v, w) -> this.validate(t, u, v, w, null);
    }

    @Contract(pure = true)
    default @NotNull TriadicPredicate<T, U, W> fixThird(V v){
        return (t, u, w) -> this.validate(t, u, v, w, null);
    }

    @Contract(pure = true)
    default @NotNull TriadicPredicate<T, U, V> fixFourth(W w){
        return (t, u, v) -> this.validate(t, u, v, w, null);
    }

    @Contract(pure = true)
    default @NotNull TriadicPredicate<T, U, V> fixFifth(W w){
        return (t, u, v) -> this.validate(t, u, v, w, null);
    }
}
