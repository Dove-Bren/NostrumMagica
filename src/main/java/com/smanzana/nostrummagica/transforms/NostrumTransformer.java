package com.smanzana.nostrummagica.transforms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

// ASM transformations for vanilla classes.
// This is largely inspired by EnderIO
@MCVersion(value = "1.10.2")
public class NostrumTransformer implements IClassTransformer {
	
	 public static final Logger logger = LogManager.getLogger("NostrumTransforms");

	private static final String EntityPlayerClass = "net.minecraft.entity.player.EntityPlayer";
	private static final String ElytraMethod = "isElytraFlying";
	private static final String ElytraMethodObf = "func_184613_cA";
	
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		// Copied from EnderIO:
		// https://github.com/SleepyTrousers/EnderCore/blob/1.10/src/main/java/com/enderio/core/common/transform/EnderCoreMethods.java
		// Basically, adds a new method override to EntityPlayer that calls the EntityLivingBase version and, if it returns false,
		// tries once more with our custom version.
		if (transformedName.equals(EntityPlayerClass)) {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(basicClass);
			classReader.accept(classNode, 0);

			boolean deObf = false;
			for (MethodNode method : classNode.methods) {
				if ("onUpdate".equals(method.name)) {
					deObf = true;
				} else if (ElytraMethod.equals(method.name) || ElytraMethodObf.equals(method.name)) {
					logger.fatal("Found existing method in EntityPlayer class for elytra overrides. Cannot proceed with transformations");
					throw new RuntimeException("Elytra method override already in EntityPlayer class. Cannot proceed. Consider turning off elytra overriding in Nostrum config.");
				}
				
			}

			MethodNode n = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, deObf ? ElytraMethod : ElytraMethodObf, "()Z", null, null);

			n.instructions = new InsnList();
			n.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			n.instructions
				.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/entity/EntityLivingBase", deObf ? ElytraMethod : ElytraMethodObf, "()Z", false));
			LabelNode l1 = new LabelNode(new Label());
			n.instructions.add(new JumpInsnNode(Opcodes.IFNE, l1));
			n.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			n.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/smanzana/nostrummagica/transforms/NostrumTransforms", ElytraMethod,
					"(Lnet/minecraft/entity/EntityLivingBase;)Z", false));
			n.instructions.add(new JumpInsnNode(Opcodes.IFNE, l1));
			n.instructions.add(new InsnNode(Opcodes.ICONST_0));
			n.instructions.add(new InsnNode(Opcodes.IRETURN));
			n.instructions.add(l1);
			n.instructions.add(new InsnNode(Opcodes.ICONST_1));
			n.instructions.add(new InsnNode(Opcodes.IRETURN));

			classNode.methods.add(n);

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(cw);
			logger
				.info("Transforming " + EntityPlayerClass + " finished, added " + (deObf ? "isElytraFlying()" : "func_184613_cA()") + " overriding EntityLivingBase");
			return cw.toByteArray();
			}
		
		return basicClass;
	}
}
