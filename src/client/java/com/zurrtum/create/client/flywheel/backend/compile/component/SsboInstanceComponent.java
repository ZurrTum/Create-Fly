package com.zurrtum.create.client.flywheel.backend.compile.component;

import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.layout.Layout;
import com.zurrtum.create.client.flywheel.backend.engine.indirect.BufferBindings;
import com.zurrtum.create.client.flywheel.backend.glsl.generate.*;
import com.zurrtum.create.client.flywheel.lib.math.MoreMath;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;

import java.util.ArrayList;

public class SsboInstanceComponent extends InstanceAssemblerComponent {
    public SsboInstanceComponent(InstanceType<?> type) {
        super(type);
    }

    @Override
    public String name() {
        return ResourceUtil.rl("ssbo_instance_assembler").toString();
    }

    @Override
    protected void generateUnpacking(GlslBuilder builder) {
        var fnBody = new GlslBlock();

        int uintCount = MoreMath.ceilingDiv(layout.byteSize(), 4);

        fnBody.add(GlslStmt.raw("uint base = " + UNPACK_ARG + " * " + uintCount + "u;"));

        for (int i = 0; i < uintCount; i++) {
            // Retrieve all the uints for the given instance ahead of time to simplify the unpacking generators.
            fnBody.add(GlslStmt.raw("uint u" + i + " = _flw_instances[base + " + i + "u];"));
        }

        var unpackArgs = new ArrayList<GlslExpr>();
        for (Layout.Element element : layout.elements()) {
            unpackArgs.add(unpackElement(element));
        }

        fnBody.ret(GlslExpr.call(STRUCT_NAME, unpackArgs));

        builder._raw("layout(std430, binding = " + BufferBindings.INSTANCE + ") restrict readonly buffer InstanceBuffer {\n" + "    uint _flw_instances[];\n" + "};");
        builder.blankLine();
        builder.function().signature(FnSignature.create().returnType(STRUCT_NAME).name(UNPACK_FN_NAME).arg("uint", UNPACK_ARG).build()).body(fnBody);
    }

    @Override
    protected GlslExpr access(int uintOffset) {
        return GlslExpr.variable("u" + uintOffset);
    }
}
