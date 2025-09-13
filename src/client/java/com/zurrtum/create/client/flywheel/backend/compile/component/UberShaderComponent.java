package com.zurrtum.create.client.flywheel.backend.compile.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zurrtum.create.client.flywheel.backend.glsl.ShaderSources;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceComponent;
import com.zurrtum.create.client.flywheel.backend.glsl.SourceFile;
import com.zurrtum.create.client.flywheel.backend.glsl.generate.*;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class UberShaderComponent implements SourceComponent {
    private final Identifier name;
    private final GlslExpr switchArg;
    private final List<AdaptedFn> functionsToAdapt;
    private final List<StringSubstitutionComponent> adaptedComponents;

    private UberShaderComponent(
        Identifier name,
        GlslExpr switchArg,
        List<AdaptedFn> functionsToAdapt,
        List<StringSubstitutionComponent> adaptedComponents
    ) {
        this.name = name;
        this.switchArg = switchArg;
        this.functionsToAdapt = functionsToAdapt;
        this.adaptedComponents = adaptedComponents;
    }

    public static Builder builder(Identifier name) {
        return new Builder(name);
    }

    @Override
    public String name() {
        return ResourceUtil.rl("uber_shader").toString() + " / " + name;
    }

    @Override
    public Collection<? extends SourceComponent> included() {
        return adaptedComponents;
    }

    @Override
    public String source() {
        var builder = new GlslBuilder();

        for (var adaptedFunction : functionsToAdapt) {
            builder.function().signature(adaptedFunction.signature()).body(body -> generateAdapter(body, adaptedFunction));

            builder.blankLine();
        }

        return builder.build();
    }

    private void generateAdapter(GlslBlock body, AdaptedFn adaptedFunction) {
        var sw = GlslSwitch.on(switchArg);
        var fnSignature = adaptedFunction.signature();
        var fnName = fnSignature.name();
        var isVoid = fnSignature.isVoid();
        var fnArgs = fnSignature.createArgExpressions();

        for (int i = 0; i < adaptedComponents.size(); i++) {
            var component = adaptedComponents.get(i);

            if (!component.replaces(fnName)) {
                continue;
            }

            var adaptedCall = GlslExpr.call(component.remapFnName(fnName), fnArgs);

            var block = GlslBlock.create();
            if (isVoid) {
                block.eval(adaptedCall).breakStmt();
            } else {
                block.ret(adaptedCall);
            }

            sw.uintCase(i, block);
        }

        if (!isVoid) {
            var defaultReturn = adaptedFunction.defaultReturn;
            if (defaultReturn == null) {
                throw new IllegalStateException("Function " + fnName + " is not void, but no default return value was provided");
            }
            sw.defaultCase(GlslBlock.create().ret(defaultReturn));
        }

        body.add(sw);
    }

    private record AdaptedFn(FnSignature signature, @Nullable GlslExpr defaultReturn) {
    }

    public static class Builder {
        private final Identifier name;
        private final List<Identifier> materialSources = new ArrayList<>();
        private final List<AdaptedFn> adaptedFunctions = new ArrayList<>();
        @Nullable
        private GlslExpr switchArg;

        public Builder(Identifier name) {
            this.name = name;
        }

        public Builder materialSources(List<Identifier> sources) {
            this.materialSources.addAll(sources);
            return this;
        }

        public Builder adapt(FnSignature function) {
            adaptedFunctions.add(new AdaptedFn(function, null));
            return this;
        }

        public Builder adapt(FnSignature function, GlslExpr defaultReturn) {
            adaptedFunctions.add(new AdaptedFn(function, defaultReturn));
            return this;
        }

        public Builder switchOn(GlslExpr expr) {
            this.switchArg = expr;
            return this;
        }

        public UberShaderComponent build(ShaderSources sources) {
            if (switchArg == null) {
                throw new NullPointerException("Switch argument must be set");
            }

            var transformed = ImmutableList.<StringSubstitutionComponent>builder();

            int index = 0;
            for (var rl : materialSources) {
                SourceFile sourceFile = sources.get(rl);
                final int finalIndex = index;
                var adapterMap = createAdapterMap(adaptedFunctions, fnName -> "_" + fnName + "_" + finalIndex);
                transformed.add(new StringSubstitutionComponent(sourceFile, adapterMap));
                index++;
            }

            return new UberShaderComponent(name, switchArg, adaptedFunctions, transformed.build());
        }

        private static ImmutableMap<String, String> createAdapterMap(List<AdaptedFn> adaptedFunctions, UnaryOperator<String> nameAdapter) {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

            for (var adapted : adaptedFunctions) {
                var fnName = adapted.signature().name();
                builder.put(fnName, nameAdapter.apply(fnName));
            }

            return builder.build();
        }
    }
}
