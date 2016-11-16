package me.strlght

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method

/**
 * @author Grigoriy Dzhanelidze
 */
private val STATIC_INITIALIZER_METHOD = Method("<clinit>", Type.VOID_TYPE, arrayOf())

class AssertionVisitor(
        delegate: ClassVisitor
) : ClassVisitor(ASM5, delegate) {
    override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?,
                             exceptions: Array<out String>?): MethodVisitor {
        val visitor = super.visitMethod(access, name, desc, signature, exceptions)
        if (name == STATIC_INITIALIZER_METHOD.name) {
            return object : GeneratorAdapter(ASM5, visitor, access, name, desc) {
                override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
                    if (opcode == Opcodes.PUTSTATIC && name == "\$assertionsDisabled") {
                        pop()
                        push(false)
                    }
                    super.visitFieldInsn(opcode, owner, name, desc)
                }
            }
        } else {
            return visitor
        }
    }
}