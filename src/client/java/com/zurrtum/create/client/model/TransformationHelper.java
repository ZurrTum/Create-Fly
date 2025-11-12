/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.zurrtum.create.client.model;

import com.google.gson.*;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.StringRepresentable;

public class TransformationHelper {
    public static Quaternionf quatFromXYZ(float[] xyz, boolean degrees) {
        return quatFromXYZ(xyz[0], xyz[1], xyz[2], degrees);
    }

    public static Quaternionf quatFromXYZ(float x, float y, float z, boolean degrees) {
        float conversionFactor = degrees ? (float) Math.PI / 180 : 1;
        return new Quaternionf().rotationXYZ(x * conversionFactor, y * conversionFactor, z * conversionFactor);
    }

    public static Quaternionf makeQuaternion(float[] values) {
        return new Quaternionf(values[0], values[1], values[2], values[3]);
    }

    public static class Deserializer implements JsonDeserializer<Transformation> {
        public Transformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                String transform = json.getAsString();
                if (transform.equals("identity")) {
                    return Transformation.identity();
                } else {
                    throw new JsonParseException("TRSR: unknown default string: " + transform);
                }
            }
            if (json.isJsonArray()) {
                // direct matrix array
                return new Transformation(parseMatrix(json));
            }
            if (!json.isJsonObject())
                throw new JsonParseException("TRSR: expected array or object, got: " + json);
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("matrix")) {
                // matrix as a sole key
                Transformation ret = new Transformation(parseMatrix(obj.get("matrix")));
                if (obj.entrySet().size() > 1) {
                    throw new JsonParseException("TRSR: can't combine matrix and other keys");
                }
                return ret;
            }
            Vector3f translation = null;
            Quaternionf leftRot = null;
            Vector3f scale = null;
            Quaternionf rightRot = null;
            // TODO: Default origin is opposing corner, due to a mistake.
            // This should probably be replaced with center in future versions.
            Vector3f origin = TransformOrigin.OPPOSING_CORNER.getVector(); // TODO: Changing this to ORIGIN_CENTER breaks models, function content needs changing too -C
            Set<String> elements = new HashSet<>(obj.keySet());
            if (obj.has("translation")) {
                translation = new Vector3f(parseFloatArray(obj.get("translation"), 3, "Translation"));
                elements.remove("translation");
            }
            if (obj.has("rotation")) {
                leftRot = parseRotation(obj.get("rotation"));
                elements.remove("rotation");
            } else if (obj.has("left_rotation")) {
                leftRot = parseRotation(obj.get("left_rotation"));
                elements.remove("left_rotation");
            }
            if (obj.has("scale")) {
                if (!obj.get("scale").isJsonArray()) {
                    try {
                        float s = obj.get("scale").getAsNumber().floatValue();
                        scale = new Vector3f(s, s, s);
                    } catch (ClassCastException ex) {
                        throw new JsonParseException("TRSR scale: expected number or array, got: " + obj.get("scale"));
                    }
                } else {
                    scale = new Vector3f(parseFloatArray(obj.get("scale"), 3, "Scale"));
                }
                elements.remove("scale");
            }
            if (obj.has("right_rotation")) {
                rightRot = parseRotation(obj.get("right_rotation"));
                elements.remove("right_rotation");
            } else if (obj.has("post-rotation")) {
                rightRot = parseRotation(obj.get("post-rotation"));
                elements.remove("post-rotation");
            }
            if (obj.has("origin")) {
                origin = parseOrigin(obj);
                elements.remove("origin");
            }
            if (!elements.isEmpty())
                throw new JsonParseException(
                    "TRSR: can either have single 'matrix' key, or a combination of 'translation', 'rotation' OR 'left_rotation', 'scale', 'post-rotation' (legacy) OR 'right_rotation', 'origin'. Found: " + String.join(
                        ", ",
                        elements
                    ));

            Transformation matrix = new Transformation(translation, leftRot, scale, rightRot);
            if (matrix.equals(Transformation.identity()))
                return Transformation.identity();

            Matrix4f ret = new Matrix4f(matrix.getMatrix());
            Matrix4f tmp = new Matrix4f().translation(origin.x(), origin.y(), origin.z());
            tmp.mul(ret, ret);
            tmp.translation(-origin.x(), -origin.y(), -origin.z());
            ret.mul(tmp);
            return new Transformation(ret);
        }

        private static Vector3f parseOrigin(JsonObject obj) {
            Vector3f origin = null;

            // Two types supported: string ("center", "corner", "opposing-corner") and array ([x, y, z])
            JsonElement originElement = obj.get("origin");
            if (originElement.isJsonArray()) {
                origin = new Vector3f(parseFloatArray(originElement, 3, "Origin"));
            } else if (originElement.isJsonPrimitive()) {
                String originString = originElement.getAsString();
                TransformOrigin originEnum = TransformOrigin.fromString(originString);
                if (originEnum == null) {
                    throw new JsonParseException("Origin: expected one of 'center', 'corner', 'opposing-corner'");
                }
                origin = originEnum.getVector();
            } else {
                throw new JsonParseException("Origin: expected an array or one of 'center', 'corner', 'opposing-corner'");
            }
            return origin;
        }

        public static Matrix4f parseMatrix(JsonElement e) {
            if (!e.isJsonArray())
                throw new JsonParseException("Matrix: expected an array, got: " + e);
            JsonArray m = e.getAsJsonArray();
            if (m.size() != 3)
                throw new JsonParseException("Matrix: expected an array of length 3, got: " + m.size());
            Matrix4f matrix = new Matrix4f();
            for (int rowIdx = 0; rowIdx < 3; rowIdx++) {
                if (!m.get(rowIdx).isJsonArray())
                    throw new JsonParseException("Matrix row: expected an array, got: " + m.get(rowIdx));
                JsonArray r = m.get(rowIdx).getAsJsonArray();
                if (r.size() != 4)
                    throw new JsonParseException("Matrix row: expected an array of length 4, got: " + r.size());
                for (int columnIdx = 0; columnIdx < 4; columnIdx++) {
                    try {
                        matrix.set(columnIdx, rowIdx, r.get(columnIdx).getAsNumber().floatValue());
                    } catch (ClassCastException ex) {
                        throw new JsonParseException("Matrix element: expected number, got: " + r.get(columnIdx));
                    }
                }
            }
            // JOML's unsafe matrix component setter does not recalculate these properties, so the matrix would stay marked as identity
            matrix.determineProperties();
            return matrix;
        }

        public static float[] parseFloatArray(JsonElement e, int length, String prefix) {
            if (!e.isJsonArray())
                throw new JsonParseException(prefix + ": expected an array, got: " + e);
            JsonArray t = e.getAsJsonArray();
            if (t.size() != length)
                throw new JsonParseException(prefix + ": expected an array of length " + length + ", got: " + t.size());
            float[] ret = new float[length];
            for (int i = 0; i < length; i++) {
                try {
                    ret[i] = t.get(i).getAsNumber().floatValue();
                } catch (ClassCastException ex) {
                    throw new JsonParseException(prefix + " element: expected number, got: " + t.get(i));
                }
            }
            return ret;
        }

        public static Quaternionf parseAxisRotation(JsonElement e) {
            if (!e.isJsonObject())
                throw new JsonParseException("Axis rotation: object expected, got: " + e);
            JsonObject obj = e.getAsJsonObject();
            if (obj.entrySet().size() != 1)
                throw new JsonParseException("Axis rotation: expected single axis object, got: " + e);
            Map.Entry<String, JsonElement> entry = obj.entrySet().iterator().next();
            Quaternionf ret;
            try {
                if (entry.getKey().equals("x")) {
                    ret = Axis.XP.rotationDegrees(entry.getValue().getAsNumber().floatValue());
                } else if (entry.getKey().equals("y")) {
                    ret = Axis.YP.rotationDegrees(entry.getValue().getAsNumber().floatValue());
                } else if (entry.getKey().equals("z")) {
                    ret = Axis.ZP.rotationDegrees(entry.getValue().getAsNumber().floatValue());
                } else
                    throw new JsonParseException("Axis rotation: expected single axis key, got: " + entry.getKey());
            } catch (ClassCastException ex) {
                throw new JsonParseException("Axis rotation value: expected number, got: " + entry.getValue());
            }
            return ret;
        }

        public static Quaternionf parseRotation(JsonElement e) {
            if (e.isJsonArray()) {
                if (e.getAsJsonArray().get(0).isJsonObject()) {
                    Quaternionf ret = new Quaternionf();
                    for (JsonElement a : e.getAsJsonArray()) {
                        ret.mul(parseAxisRotation(a));
                    }
                    return ret;
                } else if (e.isJsonArray()) {
                    JsonArray array = e.getAsJsonArray();
                    if (array.size() == 3) //Vanilla rotation
                        return quatFromXYZ(parseFloatArray(e, 3, "Rotation"), true);
                    else // quaternion
                        return makeQuaternion(parseFloatArray(e, 4, "Rotation"));
                } else
                    throw new JsonParseException("Rotation: expected array or object, got: " + e);
            } else if (e.isJsonObject()) {
                return parseAxisRotation(e);
            } else
                throw new JsonParseException("Rotation: expected array or object, got: " + e);
        }
    }

    public enum TransformOrigin implements StringRepresentable {
        CENTER(new Vector3f(.5f, .5f, .5f), "center"),
        CORNER(new Vector3f(), "corner"),
        OPPOSING_CORNER(new Vector3f(1, 1, 1), "opposing-corner");

        private final Vector3f vec;
        private final String name;

        TransformOrigin(Vector3f vec, String name) {
            this.vec = vec;
            this.name = name;
        }

        public Vector3f getVector() {
            return vec;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public static @Nullable TransformOrigin fromString(String originName) {
            if (CENTER.getSerializedName().equals(originName)) {
                return CENTER;
            }
            if (CORNER.getSerializedName().equals(originName)) {
                return CORNER;
            }
            if (OPPOSING_CORNER.getSerializedName().equals(originName)) {
                return OPPOSING_CORNER;
            }
            return null;
        }
    }
}
