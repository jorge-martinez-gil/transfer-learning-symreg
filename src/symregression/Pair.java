/*
 * Jorge Martinez-Gil,  Jose Manuel Chaves-Gonzalez: 
 * Transfer learning for semantic similarity measures based on symbolic regression.
 * J. Intell. Fuzzy Syst. 45(1): 37-49 (2023)
 *
 * @author: Jorge Martinez-Gil
 */
package symregression;

import java.util.Comparator;

/**
 * A class to store a pair of an integer index and a double value.
 */
class Pair {
    final int index;
    private final double value;

    public Pair(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }
}

/**
 * Comparator for Pair objects that compares based on the value field.
 * This comparator defines an epsilon to avoid direct comparison of doubles.
 */
class PairValueComparator implements Comparator<Pair> {
    private static final double EPSILON = 0.0001;

    @Override
    public int compare(Pair p1, Pair p2) {
        if (Math.abs(p1.getValue() - p2.getValue()) < EPSILON) {
            return 0;
        }
        return Double.compare(p1.getValue(), p2.getValue());
    }
}
