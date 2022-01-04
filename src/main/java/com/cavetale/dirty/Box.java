package com.cavetale.dirty;

import java.util.List;
import lombok.Value;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;

@Value
public final class Box {
    protected final List<Integer> min;
    protected final List<Integer> max;

    protected static Box of(StructureBoundingBox bb) {
        int ax = bb.g();
        if (ax == Integer.MIN_VALUE || ax == Integer.MAX_VALUE) return null;
        int ay = bb.h();
        if (ay == Integer.MIN_VALUE || ay == Integer.MAX_VALUE) return null;
        int az = bb.i();
        if (az == Integer.MIN_VALUE || az == Integer.MAX_VALUE) return null;
        int bx = bb.j();
        if (bx == Integer.MIN_VALUE || bx == Integer.MAX_VALUE) return null;
        int by = bb.k();
        if (by == Integer.MIN_VALUE || by == Integer.MAX_VALUE) return null;
        int bz = bb.l();
        if (bz == Integer.MIN_VALUE || bz == Integer.MAX_VALUE) return null;
        return new Box(List.of(ax, ay, az), List.of(bx, by, bz));
    }
}
