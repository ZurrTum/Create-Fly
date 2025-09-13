package com.zurrtum.create.client.flywheel.backend.glsl;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.client.flywheel.backend.glsl.error.ErrorBuilder;
import com.zurrtum.create.client.flywheel.backend.glsl.span.Span;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

sealed public interface LoadError {
    ErrorBuilder generateMessage();

    record CircularDependency(Identifier offender, List<Identifier> stack) implements LoadError {
        public String format() {
            return stack.stream().dropWhile(l -> !l.equals(offender)).map(Identifier::toString).collect(Collectors.joining(" -> "));
        }

        @Override
        public ErrorBuilder generateMessage() {
            return ErrorBuilder.create().error("files are circularly dependent").note(format());
        }
    }

    record IncludeError(Identifier location, List<Pair<Span, LoadError>> innerErrors) implements LoadError {
        @Override
        public ErrorBuilder generateMessage() {
            var out = ErrorBuilder.create().error("could not load \"" + location + "\"").pointAtFile(location);

            for (var innerError : innerErrors) {
                var err = innerError.getSecond().generateMessage();
                out.pointAt(innerError.getFirst()).nested(err);
            }

            return out;
        }
    }

    record IOError(Identifier location, IOException exception) implements LoadError {
        @Override
        public ErrorBuilder generateMessage() {
            if (exception instanceof FileNotFoundException) {
                return ErrorBuilder.create().error("\"" + location + "\" was not found");
            } else {
                return ErrorBuilder.create().error("could not load \"" + location + "\" due to an IO error").note(exception.toString());
            }
        }
    }

    record ResourceError(Identifier location) implements LoadError {
        @Override
        public ErrorBuilder generateMessage() {
            return ErrorBuilder.create().error("\"" + location + "\" was not found");
        }
    }

    record MalformedInclude(InvalidIdentifierException exception) implements LoadError {
        @Override
        public ErrorBuilder generateMessage() {
            return ErrorBuilder.create().error(exception.toString());
        }
    }
}