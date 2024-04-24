package support;

public class Pair<T, U> {
    private T first;
    private U second;

    // Empty constructor
    public Pair() {
        this.first = null;  // Assumes default value is null
        this.second = null; // Assumes default value is null
    }

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair{" +
               "first=" + first +
               ", second=" + second +
               '}';
    }
}
