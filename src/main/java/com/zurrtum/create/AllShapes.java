package com.zurrtum.create;

import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.logistics.chute.ChuteShapes;
import com.zurrtum.create.content.trains.track.TrackVoxelShapes;

import java.util.function.BiFunction;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.*;

public class AllShapes {
    private static final VoxelShape PISTON_HEAD = Blocks.PISTON_HEAD.defaultBlockState().setValue(DirectionalBlock.FACING, UP)
        .setValue(PistonHeadBlock.SHORT, true).getShape(null, null);
    private static final VoxelShape SMALL_GEAR_SHAPE = cuboid(2, 6, 2, 14, 10, 14);
    private static final VoxelShape LARGE_GEAR_SHAPE = cuboid(0, 6, 0, 16, 10, 16);
    private static final VoxelShape VERTICAL_TABLET_SHAPE = cuboid(3, 1, -1, 13, 15, 3);
    private static final VoxelShape SQUARE_TABLET_SHAPE = cuboid(2, 2, -1, 14, 14, 3);
    private static final VoxelShape LOGISTICS_TABLE_SLOPE = shape(0, 10, 10.667, 16, 14, 15).add(0, 12, 6.333, 16, 16, 10.667)
        .add(0, 14, 2, 16, 18, 6.333).build();
    private static final VoxelShape TANK_BOTTOM_LID = shape(0, 0, 0, 16, 4, 16).build();
    private static final VoxelShape TANK_TOP_LID = shape(0, 12, 0, 16, 16, 16).build();
    private static final VoxelShape WHISTLE_SMALL = shape(4, 3, 4, 12, 16, 12).build();
    private static final VoxelShape WHISTLE_MEDIUM = shape(3, 3, 3, 13, 16, 13).build();
    private static final VoxelShape WHISTLE_LARGE = shape(2, 3, 2, 14, 16, 14).build();
    public static final VoxelShaper CASING_14PX = shape(0, 0, 0, 16, 14, 16).forDirectional();
    public static final VoxelShaper CASING_13PX = shape(0, 0, 0, 16, 13, 16).forDirectional();
    public static final VoxelShaper CASING_12PX = shape(0, 0, 0, 16, 12, 16).forDirectional();
    public static final VoxelShaper CASING_11PX = shape(0, 0, 0, 16, 11, 16).forDirectional();
    public static final VoxelShaper CASING_3PX = shape(0, 0, 0, 16, 3, 16).forDirectional();
    public static final VoxelShaper CASING_2PX = shape(0, 0, 0, 16, 2, 16).forDirectional();
    public static final VoxelShaper MOTOR_BLOCK = shape(3, 0, 3, 13, 14, 13).forDirectional();
    public static final VoxelShaper FOUR_VOXEL_POLE = shape(6, 0, 6, 10, 16, 10).forAxis();
    private static final VoxelShape PISTON_EXTENDED = shape(CASING_12PX.get(UP)).add(FOUR_VOXEL_POLE.get(Axis.Y)).build();
    public static final VoxelShaper MECHANICAL_PISTON_EXTENDED = shape(PISTON_EXTENDED).forDirectional();
    public static final VoxelShaper SIX_VOXEL_POLE = shape(5, 0, 5, 11, 16, 11).forAxis();
    public static final VoxelShaper EIGHT_VOXEL_POLE = shape(4, 0, 4, 12, 16, 12).forAxis();
    public static final VoxelShaper TEN_VOXEL_POLE = shape(3, 0, 3, 13, 16, 13).forAxis();
    public static final VoxelShaper FURNACE_ENGINE = shape(1, 1, 0, 15, 15, 16).add(0, 0, 9, 16, 16, 14).forHorizontal(SOUTH);
    public static final VoxelShaper PORTABLE_STORAGE_INTERFACE = shape(0, 0, 0, 16, 14, 16).forDirectional();
    public static final VoxelShaper ELEVATOR_PULLEY = shape(0, 0, 0, 16, 16, 2).add(0, 0, 14, 16, 16, 16).add(2, 0, 2, 14, 14, 14)
        .forHorizontal(EAST);
    public static final VoxelShaper SAIL_FRAME_COLLISION = shape(0, 5, 0, 16, 9, 16).erase(2, 0, 2, 14, 16, 14).forDirectional();
    public static final VoxelShaper SAIL_FRAME = shape(0, 5, 0, 16, 9, 16).forDirectional();
    public static final VoxelShaper SAIL = shape(0, 5, 0, 16, 10, 16).forDirectional();
    public static final VoxelShaper HARVESTER_BASE = shape(0, 2, 0, 16, 14, 3).forDirectional(SOUTH);
    public static final VoxelShaper ROLLER_BASE = shape(0, 0, 0, 16, 16, 10).forDirectional(SOUTH);
    public static final VoxelShaper NOZZLE = shape(2, 0, 2, 14, 14, 14).add(1, 13, 1, 15, 15, 15).erase(3, 13, 3, 13, 15, 13).forDirectional();
    public static final VoxelShaper CRANK = shape(5, 0, 5, 11, 6, 11).add(1, 3, 1, 15, 8, 15).forDirectional();
    public static final VoxelShaper VALVE_HANDLE = shape(5, 0, 5, 11, 4, 11).add(1, 3, 1, 15, 8, 15).forDirectional();
    public static final VoxelShaper CART_ASSEMBLER = shape(0, 12, 0, 16, 16, 16).add(-2, 0, 1, 18, 14, 15).forHorizontalAxis();
    public static final VoxelShaper CART_ASSEMBLER_PLAYER_COLLISION = shape(0, 0, 1, 16, 16, 15).forHorizontalAxis();
    public static final VoxelShaper STOCKPILE_SWITCH = shape(0, 0, 0, 16, 2, 16).add(1, 0, 1, 15, 16, 15).add(0, 14, 0, 16, 16, 16)
        .add(3, 3, -2, 13, 13, 2).forHorizontal(NORTH);
    public static final VoxelShaper CONTENT_OBSERVER = shape(0, 0, 0, 16, 6, 16).add(1, 0, 1, 15, 16, 15).add(0, 14, 0, 16, 16, 16)
        .add(3, 3, -2, 13, 13, 2).forHorizontal(NORTH);
    public static final VoxelShaper FUNNEL_COLLISION = shape(0, 0, 0, 16, 4, 16).forDirectional(UP);
    public static final VoxelShaper BELT_FUNNEL_RETRACTED = shape(2, -2, 14, 14, 14, 18).add(0, -5, 8, 16, 16, 14).forHorizontal(NORTH);
    public static final VoxelShaper BELT_FUNNEL_EXTENDED = shape(2, -2, 14, 14, 14, 18).add(3, -4, 10, 13, 13, 14).add(2, -4, 6, 14, 14, 10)
        .add(0, -5, 0, 16, 16, 6).forHorizontal(NORTH);
    public static final VoxelShaper BELT_FUNNEL_PERPENDICULAR = shape(2, -2, 14, 14, 14, 18).add(1, 8, 12, 15, 15, 14).add(0.1, 13, 7, 15.9, 15, 11)
        .add(0.1, 9, 8, 15.9, 13, 12).add(0.1, 5, 9, 15.9, 9, 13).add(0.1, 1, 10, 15.9, 5, 14).add(0.1, -3, 11, 15.9, 1, 15).forHorizontal(NORTH);
    public static final VoxelShaper FUNNEL_WALL = shape(2, 2, 14, 14, 14, 18).add(1, 8, 12, 15, 15, 14).add(0.1, 13, 7, 15.9, 15, 11)
        .add(0.1, 9, 8, 15.9, 13, 12).add(0.1, 5, 9, 15.9, 9, 13).add(0.1, 1, 10, 15.9, 5, 14).add(0.1, -1, 11, 15.9, 1, 15).forHorizontal(NORTH);
    public static final VoxelShaper FLUID_VALVE = shape(3, 0, 3, 13, 16, 13).add(2, 2, 2, 14, 14, 14).forAxis();
    public static final VoxelShaper TOOLBOX = shape(1, 0, 4, 15, 9, 12).forHorizontal(NORTH);
    public static final VoxelShaper SMART_FLUID_PIPE_FLOOR = shape(4, 4, 0, 12, 12, 16).add(3, 3, 3, 13, 13, 13).add(5, 13, 3, 11, 14, 11)
        .add(5, 14, 4, 11, 15, 10).add(5, 15, 5, 11, 16, 9).add(5, 16, 6, 11, 17, 8).forHorizontal(SOUTH);
    public static final VoxelShaper SMART_FLUID_PIPE_WALL = shape(4, 0, 4, 12, 16, 12).add(3, 3, 3, 13, 13, 13).add(5, 5, 13, 11, 13, 14)
        .add(5, 6, 14, 11, 12, 15).add(5, 7, 15, 11, 11, 16).add(5, 8, 16, 11, 10, 17).forHorizontal(SOUTH);
    public static final VoxelShaper SMART_FLUID_PIPE_CEILING = shape(4, 4, 0, 12, 12, 16).add(3, 3, 3, 13, 13, 13).add(5, 2, 3, 11, 3, 11)
        .add(5, 1, 4, 11, 2, 10).add(5, 0, 5, 11, 1, 9).add(5, -1, 6, 11, 0, 8).forHorizontal(SOUTH);
    public static final VoxelShaper PUMP = shape(2, 0, 2, 14, 16, 14).forDirectional(Direction.UP);
    public static final VoxelShaper CRUSHING_WHEEL_CONTROLLER_COLLISION = shape(0, 0, 0, 16, 13, 16).forDirectional(Direction.DOWN);
    public static final VoxelShaper BELL_FLOOR = shape(0, 0, 5, 16, 11, 11).add(3, 1, 3, 13, 13, 13).forHorizontal(SOUTH);
    public static final VoxelShaper BELL_WALL = shape(5, 5, 8, 11, 11, 16).add(3, 1, 3, 13, 13, 13).forHorizontal(SOUTH);
    public static final VoxelShaper BELL_DOUBLE_WALL = shape(5, 5, 0, 11, 11, 16).add(3, 1, 3, 13, 13, 13).forHorizontal(SOUTH);
    public static final VoxelShaper BELL_CEILING = shape(0, 5, 5, 16, 16, 11).add(3, 1, 3, 13, 13, 13).forHorizontal(SOUTH);
    public static final VoxelShaper GIRDER_BEAM = shape(4, 2, 0, 12, 14, 16).forHorizontalAxis();
    public static final VoxelShaper GIRDER_BEAM_SHAFT = shape(GIRDER_BEAM.get(Axis.X)).add(SIX_VOXEL_POLE.get(Axis.Z)).forHorizontalAxis();
    public static final VoxelShaper STEP_BOTTOM = shape(0, 0, 8, 16, 8, 16).forHorizontal(SOUTH);
    public static final VoxelShaper STEP_TOP = shape(0, 8, 8, 16, 16, 16).forHorizontal(SOUTH);
    public static final VoxelShaper CONTROLS = shape(0, 0, 6, 16, 16, 16).add(0, 0, 4, 16, 2, 16).forHorizontal(NORTH);
    public static final VoxelShaper CONTROLS_COLLISION = shape(0, 0, 6, 16, 16, 16).forHorizontal(NORTH);
    public static final VoxelShaper CONTRAPTION_CONTROLS = shape(0, 0, 6, 2, 16, 16).add(14, 0, 6, 16, 16, 16).add(0, 0, 14, 16, 16, 16)
        .add(0, 0, 6, 16, 12, 16).add(0, 0, 4, 16, 2, 16).forHorizontal(NORTH);
    public static final VoxelShaper CONTRAPTION_CONTROLS_COLLISION = shape(0, 0, 6, 2, 16, 16).add(14, 0, 6, 16, 16, 16).add(0, 0, 14, 16, 16, 16)
        .add(0, 0, 7, 16, 12, 16).forHorizontal(NORTH);
    public static final VoxelShaper NIXIE_TUBE = shape(9, 0, 5, 15, 12, 11).add(1, 0, 5, 7, 12, 11).forHorizontalAxis();
    public static final VoxelShaper NIXIE_TUBE_CEILING = shape(9, 4, 5, 15, 16, 11).add(1, 4, 5, 7, 16, 11).forHorizontalAxis();
    public static final VoxelShaper NIXIE_TUBE_WALL = shape(5, 9, 0, 11, 15, 12).add(5, 1, 0, 11, 7, 12).forHorizontal(SOUTH);
    public static final VoxelShaper FLAP_DISPLAY = shape(0, 0, 3, 16, 16, 13).forHorizontal(SOUTH);
    public static final VoxelShaper DATA_GATHERER = shape(1, 0, 1, 15, 6, 15).add(3, 5, 3, 13, 9, 13).forDirectional();
    public static final VoxelShaper STOCK_LINK = shape(1, 0, 1, 15, 5, 15).forDirectional();
    public static final VoxelShaper STEAM_ENGINE = shape(1, 0, 1, 15, 3, 15).add(3, 0, 3, 13, 15, 13).add(1, 5, 4, 15, 13, 12).forHorizontalAxis();
    public static final VoxelShaper STEAM_ENGINE_CEILING = shape(1, 13, 1, 15, 16, 15).add(3, 1, 3, 13, 16, 13).add(1, 3, 4, 15, 11, 12)
        .forHorizontalAxis();
    public static final VoxelShaper STEAM_ENGINE_WALL = shape(1, 1, 0, 15, 15, 3).add(3, 3, 0, 13, 13, 15).add(1, 4, 5, 15, 12, 13)
        .forHorizontal(SOUTH);
    public static final VoxelShaper PLACARD = shape(2, 0, 2, 14, 3, 14).forDirectional(UP);
    public static final VoxelShaper FACTORY_PANEL_FALLBACK = shape(0, 0, 0, 16, 2, 16).forDirectional(UP);
    public static final VoxelShaper CLIPBOARD_FLOOR = shape(3, 0, 1, 13, 1, 15).forHorizontal(SOUTH);
    public static final VoxelShaper CLIPBOARD_CEILING = shape(3, 15, 1, 13, 16, 15).forHorizontal(SOUTH);
    public static final VoxelShaper CLIPBOARD_WALL = shape(3, 1, 0, 13, 15, 1).forHorizontal(SOUTH);
    public static final VoxelShaper TRACK_ORTHO = shape(TrackVoxelShapes.orthogonal()).forHorizontal(NORTH);
    public static final VoxelShaper TRACK_ASC = shape(TrackVoxelShapes.ascending()).forHorizontal(SOUTH);
    public static final VoxelShaper TRACK_DIAG = shape(TrackVoxelShapes.diagonal()).forHorizontal(SOUTH);
    public static final VoxelShaper TRACK_ORTHO_LONG = shape(TrackVoxelShapes.longOrthogonalZOffset()).forHorizontal(SOUTH);
    public static final VoxelShaper DEPLOYER_INTERACTION = shape(CASING_12PX.get(UP)).add(SIX_VOXEL_POLE.get(Axis.Y)).forDirectional(UP);
    public static final VoxelShaper WHISTLE_BASE = shape(1, 0, 1, 15, 3, 15).add(5, 0, 5, 11, 8, 11).forDirectional(UP);
    public static final VoxelShaper DESK_BELL = shape(3, 0, 3, 13, 3, 13).add(4, 0, 4, 12, 9, 12).forDirectional(UP);
    public static final VoxelShaper ITEM_HATCH = shape(1, 0, 0, 15, 16, 2).add(2, 2, 0, 14, 13, 3.8).add(2, 4, 0, 14, 11, 5.8)
        .add(2, 6, 0, 14, 9, 7.8).forHorizontal(SOUTH);
    public static final VoxelShaper POSTBOX = shape(2, 0, 0, 14, 14, 16).forHorizontal(SOUTH);
    public static final VoxelShape SCAFFOLD_HALF = shape(0, 8, 0, 16, 16, 16).build();
    public static final VoxelShape SCAFFOLD_FULL = shape(SCAFFOLD_HALF).add(0, 0, 0, 2, 16, 2).add(0, 0, 14, 2, 16, 16).add(14, 0, 0, 16, 16, 2)
        .add(14, 0, 14, 16, 16, 16).build();
    public static final VoxelShape TRACK_CROSS = shape(TRACK_ORTHO.get(SOUTH)).add(TRACK_ORTHO.get(EAST)).build();
    public static final VoxelShape TRACK_CROSS_DIAG = shape(TRACK_DIAG.get(SOUTH)).add(TRACK_DIAG.get(EAST)).build();
    public static final VoxelShape TRACK_COLLISION = shape(0, 0, 0, 16, 2, 16).build();
    public static final VoxelShape PACKAGE_PORT = shape(0, 0, 0, 16, 4, 16).add(2, 2, 2, 14, 14, 14).build();
    public static final VoxelShape TABLE_CLOTH = shape(-1, -9, -1, 17, 1, 17).build();
    public static final VoxelShape TABLE_CLOTH_OCCLUSION = shape(0, 0, 0, 16, 1, 16).build();
    public static final VoxelShape CHAIN_CONVEYOR_INTERACTION = shape(-10, 2, 0, 26, 14, 16).add(0, 2, -10, 16, 14, 26).add(-5, 2, -5, 21, 14, 21)
        .add(Shapes.block()).build();
    public static final VoxelShape TRACK_FALLBACK = shape(0, 0, 0, 16, 4, 16).build();
    public static final VoxelShape BASIN_BLOCK_SHAPE = shape(0, 2, 0, 16, 16, 16).erase(2, 2, 2, 14, 16, 14).add(2, 0, 2, 14, 2, 14).build();
    public static final VoxelShape BASIN_RAYTRACE_SHAPE = shape(0, 2, 0, 16, 16, 16).add(2, 0, 2, 14, 2, 14).build();
    public static final VoxelShape BASIN_COLLISION_SHAPE = shape(0, 2, 0, 16, 13, 16).erase(2, 5, 2, 14, 16, 14).add(2, 0, 2, 14, 2, 14).build();
    public static final VoxelShape GIRDER_CROSS = shape(TEN_VOXEL_POLE.get(Axis.Y)).add(GIRDER_BEAM.get(Axis.X)).add(GIRDER_BEAM.get(Axis.Z)).build();
    public static final VoxelShape BACKTANK = shape(3, 0, 3, 13, 12, 13).add(SIX_VOXEL_POLE.get(Axis.Y)).build();
    public static final VoxelShape SPEED_CONTROLLER = shape(0, 0, 0, 16, 4, 16).add(1, 1, 1, 15, 13, 15).add(0, 8, 0, 16, 14, 16).build();
    public static final VoxelShape HEATER_BLOCK_SHAPE = shape(1, 0, 1, 15, 14, 15).build();
    public static final VoxelShape HEATER_BLOCK_SPECIAL_COLLISION_SHAPE = shape(0, 0, 0, 16, 4, 16).build();
    public static final VoxelShape CRUSHING_WHEEL_COLLISION_SHAPE = cuboid(0, 0, 0, 16, 16, 16);
    public static final VoxelShape SEAT = cuboid(0, 0, 0, 16, 8, 16);
    public static final VoxelShape SEAT_COLLISION = cuboid(0, 0, 0, 16, 6, 16);
    public static final VoxelShape SEAT_COLLISION_PLAYERS = cuboid(0, 0, 0, 16, 3, 16);
    public static final VoxelShape MECHANICAL_PROCESSOR_SHAPE = shape(Shapes.block()).erase(4, 0, 4, 12, 16, 12).build();
    public static final VoxelShape TURNTABLE_SHAPE = shape(1, 4, 1, 15, 8, 15).add(5, 0, 5, 11, 4, 11).build();
    public static final VoxelShape CRATE_BLOCK_SHAPE = cuboid(1, 0, 1, 15, 14, 15);
    public static final VoxelShape TABLE_POLE_SHAPE = shape(4, 0, 4, 12, 2, 12).add(5, 2, 5, 11, 14, 11).build();
    public static final VoxelShape BELT_COLLISION_MASK = cuboid(0, 0, 0, 16, 19, 16);
    public static final VoxelShape SCHEMATICANNON_SHAPE = shape(1, 0, 1, 15, 8, 15).add(0.5, 8, 0.5, 15.5, 11, 15.5).build();
    public static final VoxelShape PULLEY_MAGNET = shape(3, -3, 3, 13, 2, 13).add(FOUR_VOXEL_POLE.get(UP)).build();
    public static final VoxelShape SPOUT = shape(1, 2, 1, 15, 14, 15).add(2, 0, 2, 14, 16, 14).build();
    public static final VoxelShape MILLSTONE = shape(0, 0, 0, 16, 6, 16).add(2, 6, 2, 14, 16, 14).build();
    public static final VoxelShape CUCKOO_CLOCK = shape(1, 0, 1, 15, 19, 15).build();
    public static final VoxelShape GAUGE_SHAPE_UP = shape(1, 0, 0, 15, 2, 16).add(2, 2, 1, 14, 14, 15).build();
    public static final VoxelShape MECHANICAL_ARM = shape(2, 0, 2, 14, 10, 14).add(3, 0, 3, 13, 14, 13).add(0, 0, 0, 16, 6, 16).build();
    public static final VoxelShape MECHANICAL_ARM_CEILING = shape(2, 6, 2, 14, 16, 14).add(3, 2, 3, 13, 16, 13).add(0, 10, 0, 16, 16, 16).build();
    public static final VoxelShape CHUTE = shape(1, 8, 1, 15, 16, 15).add(2, 0, 2, 14, 8, 14).build();
    public static final VoxelShape FUNNEL_FLOOR = shape(2, -2, 2, 14, 8, 14).add(1, 1, 1, 15, 8, 15).add(0, 4, 0, 16, 10, 16).build();
    public static final VoxelShape FUNNEL_CEILING = shape(2, 8, 2, 14, 18, 14).add(1, 8, 1, 15, 15, 15).add(0, 6, 0, 16, 12, 16).build();
    public static final VoxelShape STATION = shape(0, 0, 0, 16, 2, 16).add(1, 0, 1, 15, 13, 15).build();
    public static final VoxelShape STOCK_TICKER = shape(1, 0, 1, 15, 4, 15).add(2, 0, 2, 14, 16, 14).build();
    public static final VoxelShape WHISTLE_EXTENDER_SMALL = shape(4, 0, 4, 12, 10, 12).build();
    public static final VoxelShape WHISTLE_EXTENDER_MEDIUM = shape(3, 0, 3, 13, 10, 13).build();
    public static final VoxelShape WHISTLE_EXTENDER_LARGE = shape(2, 0, 2, 14, 10, 14).build();
    public static final VoxelShape WHISTLE_EXTENDER_SMALL_DOUBLE = shape(4, 0, 4, 12, 18, 12).build();
    public static final VoxelShape WHISTLE_EXTENDER_MEDIUM_DOUBLE = shape(3, 0, 3, 13, 18, 13).build();
    public static final VoxelShape WHISTLE_EXTENDER_LARGE_DOUBLE = shape(2, 0, 2, 14, 18, 14).build();
    public static final VoxelShape WHISTLE_EXTENDER_SMALL_DOUBLE_CONNECTED = shape(4, 0, 4, 12, 16, 12).build();
    public static final VoxelShape WHISTLE_EXTENDER_MEDIUM_DOUBLE_CONNECTED = shape(3, 0, 3, 13, 16, 13).build();
    public static final VoxelShape WHISTLE_EXTENDER_LARGE_DOUBLE_CONNECTED = shape(2, 0, 2, 14, 16, 14).build();
    public static final VoxelShape WHISTLE_MEDIUM_FLOOR = shape(WHISTLE_MEDIUM).add(WHISTLE_BASE.get(UP)).build();
    public static final VoxelShape TANK = shape(1, 0, 1, 15, 16, 15).build();
    public static final VoxelShape TANK_TOP = shape(TANK_TOP_LID).add(TANK).build();
    public static final VoxelShape TANK_TOP_BOTTOM = shape(TANK_BOTTOM_LID).add(TANK_TOP_LID).add(TANK).build();
    public static final VoxelShape TANK_BOTTOM = shape(TANK_BOTTOM_LID).add(TANK).build();
    public static final VoxelShape WHISTLE_SMALL_FLOOR = shape(WHISTLE_SMALL).add(WHISTLE_BASE.get(UP)).build();
    public static final VoxelShape WHISTLE_LARGE_FLOOR = shape(WHISTLE_LARGE).add(WHISTLE_BASE.get(UP)).build();
    public static final VoxelShaper TRACK_CROSS_ORTHO_DIAG = shape(TRACK_DIAG.get(SOUTH)).add(TRACK_ORTHO.get(EAST)).forHorizontal(SOUTH);
    public static final VoxelShaper TRACK_CROSS_DIAG_ORTHO = shape(TRACK_DIAG.get(SOUTH)).add(TRACK_ORTHO.get(SOUTH)).forHorizontal(SOUTH);
    public static final VoxelShaper MECHANICAL_PISTON = CASING_12PX;
    public static final VoxelShaper SCHEMATICS_TABLE = shape(4, 0, 4, 12, 12, 12).add(0, 11, 2, 16, 14, 14).forDirectional(SOUTH);
    public static final VoxelShaper CHUTE_SLOPE = shape(ChuteShapes.createSlope()).forHorizontal(SOUTH);
    public static final VoxelShaper MECHANICAL_PISTON_HEAD = shape(PISTON_HEAD).forDirectional();
    public static final VoxelShaper SMALL_GEAR = shape(SMALL_GEAR_SHAPE).add(SIX_VOXEL_POLE.get(Axis.Y)).forAxis();
    public static final VoxelShaper LARGE_GEAR = shape(LARGE_GEAR_SHAPE).add(SIX_VOXEL_POLE.get(Axis.Y)).forAxis();
    public static final VoxelShaper LOGISTICAL_CONTROLLER = shape(SQUARE_TABLET_SHAPE).forDirectional(SOUTH);
    public static final VoxelShaper REDSTONE_BRIDGE = shape(VERTICAL_TABLET_SHAPE).forDirectional(SOUTH)
        .withVerticalShapes(LOGISTICAL_CONTROLLER.get(UP));
    public static final VoxelShaper LOGISTICS_TABLE = shape(TABLE_POLE_SHAPE).add(LOGISTICS_TABLE_SLOPE).forHorizontal(SOUTH);
    public static final VoxelShaper WHISTLE_SMALL_WALL = shape(WHISTLE_SMALL).add(WHISTLE_BASE.get(NORTH)).forHorizontal(SOUTH);
    public static final VoxelShaper WHISTLE_MEDIUM_WALL = shape(WHISTLE_MEDIUM).add(WHISTLE_BASE.get(NORTH)).forHorizontal(SOUTH);
    public static final VoxelShaper WHISTLE_LARGE_WALL = shape(WHISTLE_LARGE).add(WHISTLE_BASE.get(NORTH)).forHorizontal(SOUTH);

    private static Builder shape(VoxelShape shape) {
        return new Builder(shape);
    }

    private static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    public static class Builder {
        private VoxelShape shape;

        public Builder(VoxelShape shape) {
            this.shape = shape;
        }

        public Builder add(VoxelShape shape) {
            this.shape = Shapes.or(this.shape, shape);
            return this;
        }

        public Builder add(double x1, double y1, double z1, double x2, double y2, double z2) {
            return add(cuboid(x1, y1, z1, x2, y2, z2));
        }

        public Builder erase(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.shape = Shapes.join(shape, cuboid(x1, y1, z1, x2, y2, z2), BooleanOp.ONLY_FIRST);
            return this;
        }

        public VoxelShape build() {
            return shape;
        }

        public VoxelShaper build(BiFunction<VoxelShape, Direction, VoxelShaper> factory, Direction direction) {
            return factory.apply(shape, direction);
        }

        public VoxelShaper build(BiFunction<VoxelShape, Axis, VoxelShaper> factory, Axis axis) {
            return factory.apply(shape, axis);
        }

        public VoxelShaper forDirectional(Direction direction) {
            return build(VoxelShaper::forDirectional, direction);
        }

        public VoxelShaper forAxis() {
            return build(VoxelShaper::forAxis, Axis.Y);
        }

        public VoxelShaper forHorizontalAxis() {
            return build(VoxelShaper::forHorizontalAxis, Axis.Z);
        }

        public VoxelShaper forHorizontal(Direction direction) {
            return build(VoxelShaper::forHorizontal, direction);
        }

        public VoxelShaper forDirectional() {
            return forDirectional(UP);
        }
    }
}