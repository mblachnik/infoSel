package org.prules.operator.learner.prototype;

import org.prules.tools.math.BasicMath;

public class PrototypeTuple {
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
    public PrototypeTuple(int prototypeId1, int prototypeId2) {
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
    public PrototypeTuple(PrototypeTuple tuple) {
        super();
        this.set(tuple);
    }

    public PrototypeTuple() {
        this.prototypeId1 = -1;
        this.prototypeId2 = -1;
        this.pairId = -1;
    }

    /**
     * Method to set data from other tuple
     *
     * @param tuple from which to copy fields
     */
    public final void set(PrototypeTuple tuple) {
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
    public long getPairId() {
        return pairId;
    }

    /**
     * Getter for smaller prototype
     *
     * @return int
     */
    public int getPrototypeId1() {
        return prototypeId1;
    }

    /**
     * Getter for bigger prototype
     *
     * @return int
     */
    public int getPrototypeId2() {
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
