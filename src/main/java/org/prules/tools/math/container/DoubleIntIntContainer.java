package org.prules.tools.math.container;

import java.util.Objects;

public class DoubleIntIntContainer {
    public double first;
    public int second;
    public int third;

    public DoubleIntIntContainer(double first, int second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getThird() {
        return third;
    }

    public void setThird(int third) {
        this.third = third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleIntIntContainer that = (DoubleIntIntContainer) o;
        return Double.compare(that.first, first) == 0 &&
                second == that.second &&
                third == that.third;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
