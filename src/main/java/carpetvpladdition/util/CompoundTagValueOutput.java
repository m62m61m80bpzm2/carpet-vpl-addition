package carpetvpladdition.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.DataResult.Success;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class CompoundTagValueOutput implements ValueOutput {
    private final ProblemReporter problemReporter;
    private final DynamicOps<Tag> ops;
    private final CompoundTag output;

    public CompoundTagValueOutput(ProblemReporter problemReporter, DynamicOps<Tag> ops, CompoundTag output) {
        this.problemReporter = problemReporter;
        this.ops = ops;
        this.output = output;
    }

    @Override
    public <T> void store(String string, Codec<T> codec, T object) {
        switch (codec.encodeStart(this.ops, object)) {
            case Success<Tag> success -> this.output.put(string, success.value());
            case Error<Tag> error -> {
                this.problemReporter.report(new EncodeToFieldFailedProblem(string, object, error));
                error.partialValue().ifPresent(tag -> this.output.put(string, tag));
            }
        }
    }

    @Override
    public <T> void storeNullable(String string, Codec<T> codec, @Nullable T object) {
        if (object != null) {
            this.store(string, codec, object);
        }
    }

    @Override
    public <T> void store(MapCodec<T> mapCodec, T object) {
        switch (mapCodec.encoder().encodeStart(this.ops, object)) {
            case Success<Tag> success -> this.output.merge((CompoundTag) success.value());
            case Error<Tag> error -> {
                this.problemReporter.report(new EncodeToMapFailedProblem(object, error));
                error.partialValue().ifPresent(tag -> this.output.merge((CompoundTag) tag));
            }
        }
    }

    @Override
    public void putBoolean(String string, boolean bl) {
        this.output.putBoolean(string, bl);
    }

    @Override
    public void putByte(String string, byte b) {
        this.output.putByte(string, b);
    }

    @Override
    public void putShort(String string, short s) {
        this.output.putShort(string, s);
    }

    @Override
    public void putInt(String string, int i) {
        this.output.putInt(string, i);
    }

    @Override
    public void putLong(String string, long l) {
        this.output.putLong(string, l);
    }

    @Override
    public void putFloat(String string, float f) {
        this.output.putFloat(string, f);
    }

    @Override
    public void putDouble(String string, double d) {
        this.output.putDouble(string, d);
    }

    @Override
    public void putString(String string, String string2) {
        this.output.putString(string, string2);
    }

    @Override
    public void putIntArray(String string, int[] is) {
        this.output.putIntArray(string, is);
    }

    @Override
    public ValueOutput child(String string) {
        CompoundTag compoundTag = new CompoundTag();
        this.output.put(string, compoundTag);
        return new CompoundTagValueOutput(
            this.problemReporter.forChild(new ProblemReporter.FieldPathElement(string)),
            this.ops,
            compoundTag
        );
    }

    @Override
    public ValueOutput.ValueOutputList childrenList(String string) {
        ListTag listTag = new ListTag();
        this.output.put(string, listTag);
        return new ListWrapper(string, this.problemReporter, this.ops, listTag);
    }

    @Override
    public <T> ValueOutput.TypedOutputList<T> list(String string, Codec<T> codec) {
        ListTag listTag = new ListTag();
        this.output.put(string, listTag);
        return new TypedListWrapper<>(this.problemReporter, string, this.ops, codec, listTag);
    }

    @Override
    public void discard(String string) {
        this.output.remove(string);
    }

    @Override
    public boolean isEmpty() {
        return this.output.isEmpty();
    }

    public CompoundTag buildResult() {
        return this.output;
    }

    public record EncodeToFieldFailedProblem(String name, Object value, Error<?> error) implements ProblemReporter.Problem {
        @Override
        public String description() {
            return "Failed to encode value '" + this.value + "' to field '" + this.name + "': " + this.error.message();
        }
    }

    public record EncodeToListFailedProblem(String name, Object value, Error<?> error) implements ProblemReporter.Problem {
        @Override
        public String description() {
            return "Failed to append value '" + this.value + "' to list '" + this.name + "': " + this.error.message();
        }
    }

    public record EncodeToMapFailedProblem(Object value, Error<?> error) implements ProblemReporter.Problem {
        @Override
        public String description() {
            return "Failed to merge value '" + this.value + "' to an object: " + this.error.message();
        }
    }

    static class ListWrapper implements ValueOutput.ValueOutputList {
        private final String fieldName;
        private final ProblemReporter problemReporter;
        private final DynamicOps<Tag> ops;
        private final ListTag output;

        ListWrapper(String string, ProblemReporter problemReporter, DynamicOps<Tag> dynamicOps, ListTag listTag) {
            this.fieldName = string;
            this.problemReporter = problemReporter;
            this.ops = dynamicOps;
            this.output = listTag;
        }

        @Override
        public ValueOutput addChild() {
            int i = this.output.size();
            CompoundTag compoundTag = new CompoundTag();
            this.output.add(compoundTag);
            return new CompoundTagValueOutput(
                this.problemReporter.forChild(new ProblemReporter.IndexedFieldPathElement(this.fieldName, i)),
                this.ops,
                compoundTag
            );
        }

        @Override
        public void discardLast() {
            this.output.removeLast();
        }

        @Override
        public boolean isEmpty() {
            return this.output.isEmpty();
        }
    }

    static class TypedListWrapper<T> implements ValueOutput.TypedOutputList<T> {
        private final ProblemReporter problemReporter;
        private final String name;
        private final DynamicOps<Tag> ops;
        private final Codec<T> codec;
        private final ListTag output;

        TypedListWrapper(ProblemReporter problemReporter, String string, DynamicOps<Tag> dynamicOps, Codec<T> codec, ListTag listTag) {
            this.problemReporter = problemReporter;
            this.name = string;
            this.ops = dynamicOps;
            this.codec = codec;
            this.output = listTag;
        }

        @Override
        public void add(T object) {
            switch (this.codec.encodeStart(this.ops, object)) {
                case Success<Tag> success -> this.output.add(success.value());
                case Error<Tag> error -> {
                    this.problemReporter.report(new CompoundTagValueOutput.EncodeToListFailedProblem(this.name, object, error));
                    error.partialValue().ifPresent(this.output::add);
                }
            }
        }

        @Override
        public boolean isEmpty() {
            return this.output.isEmpty();
        }
    }
}
