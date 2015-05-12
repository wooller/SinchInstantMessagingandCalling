package com.sinch.messagingtutorialskeleton;

/**
 * Created by Andy on 11/05/2015.
 */
public class Triplet<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    /**
     * Constructor for a Pair.
     *
     * @param first the first object in the Pair
     * @param second the second object in the pair
     */
    public Triplet(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }



    /**
     * Convenience method for creating an appropriately typed pair.
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B, C> Triplet <A, B, C> create(A a, B b, C c) {
        return new Triplet<A, B, C>(a, b, c);
    }
}
