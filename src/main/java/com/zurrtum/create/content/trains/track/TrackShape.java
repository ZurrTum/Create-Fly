package com.zurrtum.create.content.trains.track;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

public enum TrackShape implements StringIdentifiable {
    NONE("", Vec3d.ZERO),
    ZO("z_ortho", new Vec3d(0, 0, 1)),
    XO("x_ortho", new Vec3d(1, 0, 0)),
    PD("diag", new Vec3d(1, 0, 1)),
    ND("diag_2", new Vec3d(-1, 0, 1)),
    AN("ascending", 180, new Vec3d(0, 1, -1), new Vec3d(0, 1, 1)),
    AS("ascending", 0, new Vec3d(0, 1, 1), new Vec3d(0, 1, -1)),
    AE("ascending", 270, new Vec3d(1, 1, 0), new Vec3d(-1, 1, 0)),
    AW("ascending", 90, new Vec3d(-1, 1, 0), new Vec3d(1, 1, 0)),

    TN("teleport", 180, new Vec3d(0, 0, -1), new Vec3d(0, 1, 0)),
    TS("teleport", 0, new Vec3d(0, 0, 1), new Vec3d(0, 1, 0)),
    TE("teleport", 270, new Vec3d(1, 0, 0), new Vec3d(0, 1, 0)),
    TW("teleport", 90, new Vec3d(-1, 0, 0), new Vec3d(0, 1, 0)),

    CR_O("cross_ortho", new Vec3d(0, 0, 1), new Vec3d(1, 0, 0)),
    CR_D("cross_diag", new Vec3d(1, 0, 1), new Vec3d(-1, 0, 1)),
    CR_PDX("cross_d1_xo", new Vec3d(1, 0, 0), new Vec3d(1, 0, 1)),
    CR_PDZ("cross_d1_zo", new Vec3d(0, 0, 1), new Vec3d(1, 0, 1)),
    CR_NDX("cross_d2_xo", new Vec3d(1, 0, 0), new Vec3d(-1, 0, 1)),
    CR_NDZ("cross_d2_zo", new Vec3d(0, 0, 1), new Vec3d(-1, 0, 1));

    private String model;
    private List<Vec3d> axes;
    private int modelRotation;
    private Vec3d normal;

    static EnumMap<TrackShape, TrackShape> zMirror = new EnumMap<>(TrackShape.class), xMirror = new EnumMap<>(TrackShape.class), clockwise = new EnumMap<>(
        TrackShape.class);

    static {
        zMirror.putAll(ImmutableMap.<TrackShape, TrackShape>builder().put(PD, ND).put(ND, PD).put(AN, AS).put(AS, AN).put(CR_PDX, CR_NDX)
            .put(CR_NDX, CR_PDX).put(CR_PDZ, CR_NDZ).put(CR_NDZ, CR_PDZ).build());

        xMirror.putAll(ImmutableMap.<TrackShape, TrackShape>builder().put(PD, ND).put(ND, PD).put(AE, AW).put(AW, AE).put(CR_PDX, CR_NDX)
            .put(CR_NDX, CR_PDX).put(CR_PDZ, CR_NDZ).put(CR_NDZ, CR_PDZ).build());

        clockwise.putAll(ImmutableMap.<TrackShape, TrackShape>builder().put(PD, ND).put(ND, PD).put(XO, ZO).put(ZO, XO).put(AE, AS).put(AS, AW)
            .put(AW, AN).put(AN, AE).put(CR_PDX, CR_NDZ).put(CR_NDX, CR_PDZ).put(CR_PDZ, CR_NDX).put(CR_NDZ, CR_PDX).build());
    }

    TrackShape(String model, Vec3d axis) {
        this(model, 0, axis, new Vec3d(0, 1, 0));
    }

    TrackShape(String model, Vec3d axis, Vec3d secondAxis) {
        this.model = model;
        this.modelRotation = 0;
        this.normal = new Vec3d(0, 1, 0);
        this.axes = ImmutableList.of(axis, secondAxis);
    }

    TrackShape(String model, int modelRotation, Vec3d axis, Vec3d normal) {
        this.model = model;
        this.modelRotation = modelRotation;
        this.normal = normal.normalize();
        this.axes = ImmutableList.of(axis);
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getModel() {
        return model;
    }

    public List<Vec3d> getAxes() {
        return axes;
    }

    public boolean isJunction() {
        return axes.size() > 1;
    }

    public boolean isPortal() {
        return switch (this) {
            case TE, TN, TS, TW -> true;
            default -> false;
        };
    }

    public static TrackShape asPortal(Direction horizontalFacing) {
        return switch (horizontalFacing) {
            case EAST -> TE;
            case NORTH -> TN;
            case SOUTH -> TS;
            default -> TW;
        };
    }

    public Vec3d getNormal() {
        return normal;
    }

    public int getModelRotation() {
        return modelRotation;
    }

    public TrackShape mirror(BlockMirror mirror) {
        return mirror == BlockMirror.NONE ? this : mirror == BlockMirror.FRONT_BACK ? xMirror.getOrDefault(this, this) : zMirror.getOrDefault(
            this,
            this
        );
    }

    public TrackShape rotate(BlockRotation rotation) {
        TrackShape shape = this;
        for (int i = 0; i < rotation.ordinal(); i++)
            shape = clockwise.getOrDefault(shape, shape);
        return shape;
    }

}
