package ca.hackercat.playground;

public interface PGObject {
    default void onDispose() {}
    default boolean isGarbage() {
        return false;
    }
    default int getOrder() {
        return 0;
    }
}
