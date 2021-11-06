package com.smanzana.nostrummagica.transforms;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

// ASM transformations for vanilla classes.
// This is largely inspired by EnderIO
@MCVersion(value = "1.12.2")
public class NostrumTransformer implements IClassTransformer {
	public static final Logger logger = LogManager.getLogger("NostrumTransforms");

	private static final String EntityPlayerClass = "net.minecraft.entity.player.EntityPlayer";
	// EnderIO adds a method to EntityPlayer, sorts itself last, and doesn't have any way to turn it off.
	// It also only checks the chestpiece slot, and doesn't allow any extension.
	// To be compatible with EnderIO, override the clientplayer and playerMP classes instead.
	private static final String EntityPlayerPath = "net/minecraft/entity/player/EntityPlayer";
	private static final String ClientPlayerClass = "net.minecraft.client.entity.AbstractClientPlayer";
	private static final String ClientPlayerClassPath = "net/minecraft/client/entity/AbstractClientPlayer";
	private static final String PlayerMPClass = "net.minecraft.entity.player.EntityPlayerMP";
	private static final String PlayerMPClassPath = "net/minecraft/entity/player/EntityPlayerMP";
	private static final String ElytraMethod = "isElytraFlying";
	private static final String ElytraMethodObf = "func_184613_cA";
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		// Copied from EnderIO:
		// https://github.com/SleepyTrousers/EnderCore/blob/1.10/src/main/java/com/enderio/core/common/transform/EnderCoreMethods.java
		// Basically, adds a new method override to EntityPlayer that calls the EntityLivingBase version and, if it returns false,
		// tries once more with our custom version.
		//if (transformedName.equals(EntityPlayerClass)) {
		if (transformedName.equals(PlayerMPClass)
				|| transformedName.equals(ClientPlayerClass)) {
			
			{
//				ClassNode classNode = new ClassNode();
//				ClassReader classReader = new ClassReader(basicClass);
//				classReader.accept(classNode, 0);
//	
//				boolean deObf = false;
//				Iterator<MethodNode> it = classNode.methods.iterator();
//				while (it.hasNext()) {
//					MethodNode method = it.next();
//					if ("onUpdate".equals(method.name)) {
//						deObf = true;
//					} else if ("hasPlayerInfo".equals(method.name)) { // AbstractClientPlayer has no onUpdate
//						deObf = true;
//					} else if (ElytraMethod.equals(method.name) || ElytraMethodObf.equals(method.name)) {
//						logger.warn("Found existing method in " + transformedName + " class for elytra overrides. Overwriting it. This may cause problems with other mods.");
//						it.remove();
//					}
//				}
//				MethodNode n = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, deObf ? ElytraMethod : ElytraMethodObf, "()Z", null, null);
//	
//				n.instructions = new InsnList();
//				n.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
//				n.instructions
//					.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/entity/EntityLivingBase", deObf ? ElytraMethod : ElytraMethodObf, "()Z", false));
//				LabelNode l1 = new LabelNode(new Label());
//				n.instructions.add(new JumpInsnNode(Opcodes.IFNE, l1));
//				n.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
//				n.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/smanzana/nostrummagica/transforms/NostrumTransforms", ElytraMethod,
//						"(Lnet/minecraft/entity/EntityLivingBase;)Z", false));
//				n.instructions.add(new JumpInsnNode(Opcodes.IFNE, l1));
//				n.instructions.add(new InsnNode(Opcodes.ICONST_0));
//				n.instructions.add(new InsnNode(Opcodes.IRETURN));
//				n.instructions.add(l1);
//				n.instructions.add(new InsnNode(Opcodes.ICONST_1));
//				n.instructions.add(new InsnNode(Opcodes.IRETURN));
//	
//				classNode.methods.add(n);
//	
//				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//				classNode.accept(cw);
//				logger
//					.info("Transforming " + transformedName + " finished, added " + (deObf ? "isElytraFlying()" : "func_184613_cA()") + " overriding EntityLivingBase");
//				return cw.toByteArray();
			}
			
			{
				ClassReader classReader = new ClassReader(basicClass);
				
				boolean deObf = false;
				ClassNode classNode = new ClassNode();
				classReader.accept(classNode, 0);
				Iterator<MethodNode> it = classNode.methods.iterator();
				while (it.hasNext()) {
					MethodNode method = it.next();
					if ("onUpdate".equals(method.name)) {
						deObf = true;
					} else if ("hasPlayerInfo".equals(method.name)) { // AbstractClientPlayer has no onUpdate
						deObf = true;
					} else if (ElytraMethod.equals(method.name) || ElytraMethodObf.equals(method.name)) {
						logger.warn("Found existing method in " + transformedName + " class for elytra overrides. Overwriting it. This may cause problems with other mods.");
						it.remove();
					}
				}

				ClassWriter cw = new ClassWriter(classReader, 0);
				
				MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, deObf ? "isElytraFlying" : "func_184613_cA", "()Z", null, null);
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(42000, l0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/entity/EntityLivingBase", deObf ? "isElytraFlying" : "func_184613_cA", "()Z", false);
				Label l1 = new Label();
				mv.visitJumpInsn(Opcodes.IFNE, l1);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/smanzana/nostrummagica/transforms/NostrumTransforms", "isElytraFlying", "(Lnet/minecraft/entity/EntityLivingBase;)Z",
						false);
				mv.visitJumpInsn(Opcodes.IFNE, l1);
				mv.visitInsn(Opcodes.ICONST_0);
				mv.visitInsn(Opcodes.IRETURN);
				mv.visitLabel(l1);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitInsn(Opcodes.ICONST_1);
				mv.visitInsn(Opcodes.IRETURN);
				Label l2 = new Label();
				mv.visitLabel(l2);
				if (transformedName.equals(PlayerMPClass)) {
					mv.visitLocalVariable("this", "L" + PlayerMPClassPath + ";", null, l0, l2, 0);
				} else if (transformedName.equals(ClientPlayerClass)) {
					mv.visitLocalVariable("this", "L" + ClientPlayerClassPath + ";", null, l0, l2, 0);
				}
				mv.visitMaxs(1, 1);
				mv.visitEnd();
				
				classReader.accept(cw, 0);
				
				logger
					.info("Transforming " + transformedName + " finished, added " + (deObf ? "isElytraFlying()" : "func_184613_cA()") + " overriding EntityLivingBase");
				return cw.toByteArray();
			}
		}
		
		return basicClass;
	}
}
