package com.tomicooler.universe.splitter.unofficial;

public interface WorldSplitter {

    enum World {
        A, B
    }

    World split() throws WorldSplitException;
    boolean available();
    boolean throttled();
}
