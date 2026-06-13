package com.zurrtum.create.client.foundation.block.connected;

public class AllCTTypes {
    public static final HorizontalCTType HORIZONTAL = new HorizontalCTType();
    public static final HorizontalKryppersCTType HORIZONTAL_KRYPPERS = new HorizontalKryppersCTType();
    public static final CTType VERTICAL = new CTType("vertical", CTType.VERTICAL);
    public static final OmnidirectionalCTType OMNIDIRECTIONAL = new OmnidirectionalCTType();
    public static final RoofCTType ROOF = new RoofCTType();
    public static final CTType CROSS = new CTType("cross", CTType.AXIS_ALIGNED);
    public static final RectangleCTType RECTANGLE = new RectangleCTType();
    public static final RectangleWithOriginalCTType RECTANGLE_WITH_ORIGINAL = new RectangleWithOriginalCTType();
    public static final RectangleWithoutSingleCTType RECTANGLE_WITHOUT_SINGLE = new RectangleWithoutSingleCTType();
    public static final RectangleWithoutHorizontalSingleCTType RECTANGLE_WITHOUT_HORIZONTAL_SINGLE = new RectangleWithoutHorizontalSingleCTType();
    public static final RectangleWithoutVerticalMiddleCTType RECTANGLE_WITHOUT_VERTICAL_MIDDLE = new RectangleWithoutVerticalMiddleCTType();
    public static final SquareCTType SQUARE = new SquareCTType();
}
