package org.prules.operator.learner.classifiers.data_splitter;

public class PiredTriple {
    long pired = -1; //An id of paired prototypes
    int protoId1 = -1; //First prototype id
    int protoId2 = -1; //Second prototype id
    boolean isPure = false; //?

    public PiredTriple(long pired, int protoId1, int protoId2) {
        set(pired, protoId1, protoId2, false);
    }

    public PiredTriple(long pired, int protoId1, int protoId2, boolean isPure) {
        set(pired, protoId1, protoId2, isPure);
    }

    public PiredTriple(PiredTriple pair) {
        this.set(pair);
    }

    public PiredTriple() {
    }

    final void set(PiredTriple pair) {
        this.pired = pair.pired;
        this.protoId1 = pair.protoId1;
        this.protoId2 = pair.protoId2;
        this.isPure = pair.isPure;
    }

    final void set(long pired, int protoId1, int protoId2) {
        this.pired = pired;
        this.protoId1 = protoId1;
        this.protoId2 = protoId2;
        this.isPure = false;
    }

    final void set(long pired, int protoId1, int protoId2, boolean isPure) {
        this.pired = pired;
        this.protoId1 = protoId1;
        this.protoId2 = protoId2;
        this.isPure = isPure;
    }
}