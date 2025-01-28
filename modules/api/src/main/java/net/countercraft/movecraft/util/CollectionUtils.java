package net.countercraft.movecraft.util;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.util.hitboxes.BitmapHitBox;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class CollectionUtils {
    @NotNull
    @Contract(pure=true)
    public static <E> Collection<E> oldFilter(@NotNull final Collection<E> collection, @NotNull final Collection<E> filter){
        final Collection<E> returnList = new HashSet<>();
        final HashSet<E> filterSet = Sets.newHashSet(filter);
        for(E object : collection){
            if(!filterSet.contains(object)){
                returnList.add(object);
            }
        }
        return returnList;
    }

    @NotNull
    @Contract(pure=true)
    public static <E> List<E> oldFilter(@NotNull final List<E> collection, @NotNull final Collection<E> filter){
        final List<E> returnList = new ArrayList<>();
        final HashSet<E> filterSet = Sets.newHashSet(filter);
        for(int i = 0; i < collection.size(); i++){
            if(!filterSet.contains(collection.get(i))){
                returnList.add(collection.get(i));
            }
        }
        return returnList;
    }

    @NotNull
    @Contract(pure=true)
    @Deprecated
    public static Collection<MovecraftLocation> oldFilter(@NotNull final HitBox collection, @NotNull final Collection<MovecraftLocation> filter){
        final Collection<MovecraftLocation> returnList = new HashSet<>();
        final HashSet<MovecraftLocation> filterSet = Sets.newHashSet(filter);
        for(MovecraftLocation object : collection){
            if(!filterSet.contains(object)){
                returnList.add(object);
            }
        }
        return returnList;
    }

    @NotNull
    @Contract(pure=true)
    @Deprecated
    public static BitmapHitBox oldFilter(@NotNull final HitBox collection, @NotNull final HitBox filter){
        final BitmapHitBox returnList = new BitmapHitBox();
        int counter = filter.size();
        for(MovecraftLocation object : collection){
            if(counter <= 0 || !filter.contains(object)){
                returnList.add(object);
            } else {
                counter--;
            }
        }
        return returnList;
    }
    /**
     * Removes the elements from <code>collection</code> that also exist in <code>filter</code> without modifying either.
     * @param <E> the element type
     * @return a <code>Collection</code> containing all the elements of <code>collection</code> except those in <code>filter</code>
     */
    @NotNull
    @Contract(pure=true)
    public static <E> Collection<E> filter(@NotNull final Collection<E> collection, @NotNull final Collection<E> filter){
        //final Collection<E> returnList = new HashSet<>();
        /*for(E object : collection){
            if(!filterSet.contains(object)){
                returnList.add(object);
            }
        }*/
        //final List<E> returnList = collection.stream().filter(item -> filter.contains(item)).collect(Collectors.toList());
        final List<E> returnList = collection.stream().filter(((Predicate<E>) filter::contains).negate()).collect(Collectors.toUnmodifiableList());
        return returnList;
    }

    @NotNull
    @Contract(pure=true)
    public static <E> List<E> filter(@NotNull final List<E> collection, @NotNull final Collection<E> filter){
        //final List<E> returnList = new ArrayList<>();
        /*for(int i = 0; i < collection.size(); i++){
            if(!filterSet.contains(collection.get(i))){
                returnList.add(collection.get(i));
            }
        }*/
        //final List<E> returnList = collection.stream().filter(item -> filter.contains(item)).collect(Collectors.toList());
        final List<E> returnList = collection.stream().filter(((Predicate<E>) filter::contains).negate()).collect(Collectors.toUnmodifiableList());
        return returnList;
    }

    @NotNull
    @Contract(pure=true)
    @Deprecated
    public static Collection<MovecraftLocation> filter(@NotNull final HitBox collection, @NotNull final Collection<MovecraftLocation> filter){
        //final Collection<MovecraftLocation> returnList = new HashSet<>();
        /*for(MovecraftLocation object : collection){
            if(!filterSet.contains(object)){
                returnList.add(object);
            }
        }*/

        //final Collection<MovecraftLocation> returnList = collection.asSet().stream().filter(item -> filter.contains(item)).collect(Collectors.toList());
        final Collection<MovecraftLocation> returnList = (collection.asSet()).stream().filter(((Predicate<MovecraftLocation>) filter::contains).negate()).collect(Collectors.toUnmodifiableSet());
        
        return returnList;
    }

    @NotNull
    @Contract(pure=true)
    @Deprecated
    public static BitmapHitBox filter(@NotNull final HitBox collection, @NotNull final HitBox filter){
        final BitmapHitBox returnList = new BitmapHitBox();
        int counter = filter.size();
        for(MovecraftLocation object : collection){
            if(counter <= 0 || !filter.contains(object)){
                returnList.add(object);
            } else {
                counter--;
            }
        }
        return returnList;
    }

    public final static MovecraftLocation[] SHIFTS = {
            new MovecraftLocation(0, 0, 1),
//            new MovecraftLocation(0, 1, 0),
            new MovecraftLocation(1, 0 ,0),
            new MovecraftLocation(0, 0, -1),
            new MovecraftLocation(0, -1, 0),
            new MovecraftLocation(-1, 0, 0)};
    /**
     * finds the axial neighbors to a location. Neighbors are defined as locations that exist within one meter of a given
     * location
     * @param location the location to search for neighbors
     * @return an iterable set of neighbors to the given location
     */
    @NotNull
    @Contract(pure = true)
    public static Iterable<MovecraftLocation> neighbors(@NotNull HitBox hitbox, @NotNull MovecraftLocation location){
        return neighbors(hitbox.asSet(),location);
    }
    /**
     * finds the axial neighbors to a location. Neighbors are defined as locations that exist within one meter of a given
     * location
     * @param location the location to search for neighbors
     * @return an iterable set of neighbors to the given location
     */
    @NotNull
    @Contract(pure = true)
    public static Iterable<MovecraftLocation> neighbors(@NotNull Set<MovecraftLocation> hitbox, @NotNull MovecraftLocation location){
        if(hitbox.isEmpty()){
            return Collections.emptyList();
        }
        final List<MovecraftLocation> neighbors = new ArrayList<>(6);
        for(MovecraftLocation test : SHIFTS){
            if(hitbox.contains(location.add(test))){
                neighbors.add(location.add(test));
            }
        }
        return neighbors;
    }
}
