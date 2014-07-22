package com.demigodsrpg.stoa.data;

import java.util.concurrent.Callable;

public class MetaCallable implements Callable<Object> {
    private Object data;

    public MetaCallable(Object data) {
        this.data = data;
    }

    @Override
    public Object call() {
        return data;
    }
}