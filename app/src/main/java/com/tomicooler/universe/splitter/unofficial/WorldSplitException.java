package com.tomicooler.universe.splitter.unofficial;

public class WorldSplitException extends Exception {
    public WorldSplitException(String message) {
        super(message);
    }

    public WorldSplitException(Throwable cause) {
        super (cause);
    }

    public WorldSplitException(String message, Throwable cause) {
        super(message, cause);
    }
}
