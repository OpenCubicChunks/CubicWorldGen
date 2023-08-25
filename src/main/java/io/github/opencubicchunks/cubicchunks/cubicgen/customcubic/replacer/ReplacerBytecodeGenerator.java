package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.replacer;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.LAND;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.V1_8;

import jdk.nashorn.internal.codegen.types.Type;
import mcp.MethodsReturnNonnullByDefault;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReplacerBytecodeGenerator {

    public static IMultiBiomeBlockReplacer generateFromArray(IBiomeBlockReplacer[] replacers) {
        //TODO: maybe we could re-use an existing replacer class if this is called multiple times with an identical array?

        String className = Type.getInternalName(IMultiBiomeBlockReplacer.class) + "$GeneratedImpl";

        ClassWriterWithConstants cw = new ClassWriterWithConstants(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, className, null, Type.getInternalName(IMultiBiomeBlockReplacer.class), null);

        { //getReplacedBlock
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getReplacedBlock", "(Lnet/minecraft/block/state/IBlockState;"
                    + "Lnet/minecraft/world/biome/Biome;IIIDDDD[J)Lnet/minecraft/block/state/IBlockState;", null, null);

            /*
             * LVT map:
             *  0: this
             *  1: state
             *  2: biome
             *  3: x
             *  4: y
             *  5: z
             *  6: dx
             *  7: -
             *  8: dy
             *  9: -
             * 10: dz
             * 11: -
             * 12: density
             * 13: -
             * 14: replacerFlags
             *   </method arguments>
             * 15: flagsElement
             * 16: -
             */

            for (int replacerIndex = 0; replacerIndex < replacers.length; replacerIndex++) {
                //flagsElement = replacerFlags[replacerIndex / 64];
                mv.visitVarInsn(ALOAD, 14);
                mv.visitLdcInsn(replacerIndex / Long.SIZE);
                mv.visitInsn(LALOAD);
                mv.visitVarInsn(LSTORE, 15);

                for (int bitIndex = 0; bitIndex < Long.SIZE && replacerIndex < replacers.length; bitIndex++, replacerIndex++) {
                    IBiomeBlockReplacer replacer = replacers[replacerIndex];
                    if (replacer.rangeChecksAlwaysFail()) {
                        // if this replacer will never pass its range checks, we'll skip processing it entirely and not bother generating any code
                        continue;
                    }

                    Label tailLabel = new Label();

                    //if ((flagsElement & (1L << bitIndex)) != 0L) {
                    mv.visitVarInsn(LLOAD, 15);
                    mv.visitLdcInsn(1L << bitIndex);
                    mv.visitInsn(LAND);
                    mv.visitLdcInsn(0L);
                    mv.visitInsn(LCMP);
                    mv.visitJumpInsn(IFEQ, tailLabel);

                    // if this replacer's range checks won't always pass, we'll hoist them out of IBiomeBlockReplacer#getReplacedBlock() to allow
                    // JIT to treat the min/max values as constants so they can be optimized more aggressively
                    if (replacer.minY == replacer.maxY) {
                        //  if (y == replacers[replacerIndex].minY)
                        mv.visitVarInsn(ILOAD, 4);
                        mv.visitLdcInsn(replacer.minY);
                        mv.visitJumpInsn(IF_ICMPNE, tailLabel);
                    } else {
                        if (replacer.minY != Integer.MIN_VALUE) {
                            //  if (y >= replacers[replacerIndex].minY)
                            mv.visitVarInsn(ILOAD, 4);
                            mv.visitLdcInsn(replacer.minY);
                            mv.visitJumpInsn(IF_ICMPLT, tailLabel);
                        }
                        if (replacer.maxY != Integer.MAX_VALUE) {
                            //  if (y <= replacers[replacerIndex].maxY)
                            mv.visitVarInsn(ILOAD, 4);
                            mv.visitLdcInsn(replacer.maxY);
                            mv.visitJumpInsn(IF_ICMPGT, tailLabel);
                        }
                        // if minY/maxY are Integer.MIN_VALUE/MAX_VALUE respectively, no range checking code will be generated
                    }

                    //    state = replacers[replacerIndex].getReplacedBlockImpl(state, biome, x, y, z, dx, dy, dz, density);
                    mv.visitLdcInsn(cw.constantPlaceholder(replacer));
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName(IBiomeBlockReplacer.class));
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitVarInsn(ILOAD, 3);
                    mv.visitVarInsn(ILOAD, 4);
                    mv.visitVarInsn(ILOAD, 5);
                    mv.visitVarInsn(DLOAD, 6);
                    mv.visitVarInsn(DLOAD, 8);
                    mv.visitVarInsn(DLOAD, 10);
                    mv.visitVarInsn(DLOAD, 12);
                    mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(IBiomeBlockReplacer.class), "getReplacedBlockImpl",
                            "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/biome/Biome;IIIDDDD)"
                                    + "Lnet/minecraft/block/state/IBlockState;", false);
                    mv.visitVarInsn(ASTORE, 1);

                    //} //end if
                    mv.visitLabel(tailLabel);
                }
            }

            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();

        if (Boolean.getBoolean("cwg.dumpClasses")) {
            try {
                Files.write(Paths.get((className + ".class").replace('/', '_')), cw.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);

            Class<?> generatedClass = unsafe.defineAnonymousClass(IMultiBiomeBlockReplacer.class, cw.toByteArray(), cw.getCPPatches());
            return (IMultiBiomeBlockReplacer) unsafe.allocateInstance(generatedClass); //i am too lazy to write a constructor
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ClassWriterWithConstants extends ClassWriter {

        /*
         * This makes use of a neat feature in sun.misc.Unsafe#defineAnonymousClass, which allows us to replace entries in the class' string
         * constant pool with arbitrary object instances. The end result is that we can use the LDC instruction to load any object as a constant,
         * which avoids the extra overhead of adding a static final field for every constant and then somehow getting access to the constant
         * values in the generated class' static initializer.
         *
         * We achieve this by using a placeholder string value for every unique object instance we want to use in the bytecode, and issue an LDC to
         * the placeholder string followed by a CHECKCAST to the actual type. We can then replace the placeholder values with their actual values
         * by putting the values in the corresponding elements of the 'cpPatches' array passed to Unsafe#defineAnonymousClass, and voilà! We can
         * now LDC arbitrary objects.
         *
         * This same technique is used by the OpenJDK runtime when generating bytecode for MethodHandles which return a constant value.
         */

        private final Map<Object, ConstantPoolPatch> patchesByInstance = new IdentityHashMap<>();

        public ClassWriterWithConstants(int flags) {
            super(flags);
        }

        public Object constantPlaceholder(Object instanceIn) {
            return this.patchesByInstance.computeIfAbsent(instanceIn, instance -> {
                Object placeholder = "DUMMY CONSTANT POOL VALUE #" + this.patchesByInstance.size();
                int cpIndex = this.newConst(placeholder);
                return new ConstantPoolPatch(instance, placeholder, cpIndex);
            }).placeholder;
        }

        @Nullable public Object[] getCPPatches() {
            if (this.patchesByInstance.isEmpty()) {
                return null;
            }

            Object[] cpPatches = new Object[this.patchesByInstance.values().stream().max(Comparator.naturalOrder()).get().cpIndex + 1];
            for (ConstantPoolPatch patch : this.patchesByInstance.values()) {
                cpPatches[patch.cpIndex] = patch.instance;
            }
            return cpPatches;
        }

        private static class ConstantPoolPatch implements Comparable<ConstantPoolPatch> {

            public final Object instance;
            public final Object placeholder;
            public final int cpIndex;

            private ConstantPoolPatch(Object instance, Object placeholder, int cpIndex) {
                this.instance = instance;
                this.placeholder = placeholder;
                this.cpIndex = cpIndex;
            }

            @Override public int compareTo(ConstantPoolPatch o) {
                return Integer.compare(this.cpIndex, o.cpIndex);
            }
        }
    }
}
