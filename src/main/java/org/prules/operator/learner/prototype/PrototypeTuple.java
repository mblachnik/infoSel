package org.prules.operator.learner.prototype;

import org.prules.tools.math.BasicMath;

class PrototypeTuple {
    /**
     * Id of pair
     */
    private long pairId;
    /**
     * Smaller id of pair
     */
    private int prototypeId1;
    /**
     * Seconds Id of pair(bigger)
     */
    private int prototypeId2;

    /**
     * Base Constructor
     * Class will self decide which number is smaller
     *
     * @param prototypeId1 id of first prototype
     * @param prototypeId2 id of second prototype
     */
    PrototypeTuple(int prototypeId1, int prototypeId2) {
        if (prototypeId1 < prototypeId2) {
            this.prototypeId1 = prototypeId1;
            this.prototypeId2 = prototypeId2;
        } else {
            this.prototypeId1 = prototypeId2;
            this.prototypeId2 = prototypeId1;
        }
        this.pairId = BasicMath.pair(this.prototypeId1, this.prototypeId2);
    }

    /**
     * Copy constructor
     *
     * @param tuple other {@link PrototypeTuple}
     */
    PrototypeTuple(PrototypeTuple tuple) {
        this.prototypeId1 = tuple.prototypeId1;
        this.prototypeId2 = tuple.prototypeId2;
        this.pairId = tuple.pairId;
    }

    PrototypeTuple() {
        this.prototypeId1 = -1;
        this.prototypeId2 = -1;
        this.pairId = -1;
    }

    /**
     * Method to set data from other tuple
     *
     * @param tuple from which to copy fields
     */
    final void set(PrototypeTuple tuple) {
        this.set(tuple.getPrototypeId1(), tuple.getPrototypeId2());
    }

    /**
     * Method to set data from specific Ids,
     * will self decide which number is smaller and generate pairId
     *
     * @param prototypeId1 first id
     * @param prototypeId2 second id
     */
    public void set(int prototypeId1, int prototypeId2) {
        if (prototypeId1 < prototypeId2) {
            this.prototypeId1 = prototypeId1;
            this.prototypeId2 = prototypeId2;
        } else {
            this.prototypeId1 = prototypeId2;
            this.prototypeId2 = prototypeId1;
        }
        this.pairId = BasicMath.pair(this.prototypeId1, this.prototypeId2);
    }

    /**
     * Getter for pair Id
     *
     * @return long
     */
    long getPairId() {
        return pairId;
    }

    /**
     * Getter for smaller prototype
     *
     * @return int
     */
    int getPrototypeId1() {
        return prototypeId1;
    }

    /**
     * Getter for bigger prototype
     *
     * @return int
     */
    int getPrototypeId2() {
        return prototypeId2;
    }

    /**
     * Method for faster printing Class data
     *
     * @return string with object summary
     */
    @Override
    public String toString() {
        return "PrototypeTuple{" +
                "pairId=" + pairId +
                ", protoId1=" + prototypeId1 +
                ", protoId2=" + prototypeId2 +
                '}';
    }
}
