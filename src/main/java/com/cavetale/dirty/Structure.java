package com.cavetale.dirty;

import java.util.List;
import lombok.Value;

@Value
public final class Structure {
    protected final String name;
    protected final Box bb;
    protected final List<Box> bbs;
}
