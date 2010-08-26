/*
 * Copyright 2006-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.osgi.test.internal.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * ASM based class for determining a class imports. The code is heavily based on
 * an Eugene Kuleshov's ASM <a
 * href="http://asm.objectweb.org/doc/tutorial-asm-2.0.html">tutorial</a>.
 * 
 * <p/>The main differences from the original source in the article are the 1.4
 * compatibility, the handling of class objects not instantiated
 * (MyClass.class.getName()) as these are specially handled by the compiler and
 * analysis of inner classes.
 * 
 * @author Eugene Kuleshov
 * @author Costin Leau
 * 
 */
public class DependencyVisitor implements AnnotationVisitor, SignatureVisitor, ClassVisitor, FieldVisitor,
		MethodVisitor {

	private Set packages = new HashSet();

	private Map groups = new HashMap();

	private Map current;

	private String tempLdc;

	private static final String CLASS_NAME = Class.class.getName();

	public Map getGlobals() {
		return groups;
	}

	public Set getPackages() {
		return packages;
	}

	// ClassVisitor
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		tempLdc = null;
		String p = getGroupKey(name);
		current = (Map) groups.get(p);
		if (current == null) {
			current = new HashMap();
			groups.put(p, current);
		}

		if (signature == null) {
			addName(superName);
			addNames(interfaces);
		}
		else {
			addSignature(signature);
		}
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		tempLdc = null;
		addDesc(desc);
		return this;
	}

	public void visitAttribute(Attribute attr) {
		tempLdc = null;
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		tempLdc = null;
		if (signature == null) {
			addDesc(desc);
		}
		else {
			addTypeSignature(signature);
		}
		if (value instanceof Type)
			addType((Type) value);
		return this;
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		tempLdc = null;
		if (signature == null) {
			addMethodDesc(desc);
		}
		else {
			addSignature(signature);
		}
		addNames(exceptions);
		return this;
	}

	public void visitSource(String source, String debug) {
		tempLdc = null;
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		tempLdc = null;
		
		// addName( outerName);
		// addName( innerName);
	}

	public void visitOuterClass(String owner, String name, String desc) {
		tempLdc = null;
		// addName(owner);
		// addMethodDesc(desc);
	}

	// MethodVisitor

	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		tempLdc = null;
		addDesc(desc);
		return this;
	}

	public void visitTypeInsn(int opcode, String desc) {
		tempLdc = null;
		if (desc.charAt(0) == '[')
			addDesc(desc);
		else
			addName(desc);
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		tempLdc = null;
		addName(owner);
		addDesc(desc);
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		String returnType = Type.getReturnType(desc).getClassName();
		if (opcode == Opcodes.INVOKESTATIC && CLASS_NAME.equals(returnType)) {
			if (tempLdc != null)
				addName(tempLdc.replace('.', '/'));
		}

		tempLdc = null;

		addName(owner);
		addMethodDesc(desc);

	}

	public void visitLdcInsn(Object cst) {
		tempLdc = null;
		if (cst instanceof Type)
			addType((Type) cst);
		else if (cst instanceof String) {
			tempLdc = (String) cst;
		}

	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		tempLdc = null;
		addDesc(desc);
	}

	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		tempLdc = null;
		addTypeSignature(signature);
	}

	public AnnotationVisitor visitAnnotationDefault() {
		tempLdc = null;
		return this;
	}

	public void visitCode() {
		tempLdc = null;
	}

	public void visitInsn(int opcode) {
		tempLdc = null;
	}

	public void visitIntInsn(int opcode, int operand) {
		tempLdc = null;
	}

	public void visitVarInsn(int opcode, int var) {
		tempLdc = null;
	}

	public void visitJumpInsn(int opcode, Label label) {
		tempLdc = null;
	}

	public void visitLabel(Label label) {
		tempLdc = null;
	}

	public void visitIincInsn(int var, int increment) {
		tempLdc = null;
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		tempLdc = null;
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		tempLdc = null;
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		tempLdc = null;
		addName(type);
	}

	public void visitLineNumber(int line, Label start) {
		tempLdc = null;
	}

	public void visitMaxs(int maxStack, int maxLocals) {
		tempLdc = null;
	}

	// AnnotationVisitor

	public void visit(String name, Object value) {
		tempLdc = null;
		if (value instanceof Type)
			addType((Type) value);
	}

	public void visitEnum(String name, String desc, String value) {
		tempLdc = null;
		addDesc(desc);
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		tempLdc = null;
		addDesc(desc);
		return this;
	}

	public AnnotationVisitor visitArray(String name) {
		tempLdc = null;
		return this;
	}

	// SignatureVisitor

	public void visitFormalTypeParameter(String name) {
		tempLdc = null;
	}

	public SignatureVisitor visitClassBound() {
		tempLdc = null;
		return this;
	}

	public SignatureVisitor visitInterfaceBound() {
		tempLdc = null;
		return this;
	}

	public SignatureVisitor visitSuperclass() {
		tempLdc = null;
		return this;
	}

	public SignatureVisitor visitInterface() {
		tempLdc = null;
		return this;
	}

	public SignatureVisitor visitParameterType() {
		tempLdc = null;
		return this;
	}

	public SignatureVisitor visitReturnType() {
		tempLdc = null;
		return this;
	}

	public SignatureVisitor visitExceptionType() {
		tempLdc = null;
		return this;
	}

	public void visitBaseType(char descriptor) {
		tempLdc = null;
	}

	public void visitTypeVariable(String name) {
		tempLdc = null;
	}

	public SignatureVisitor visitArrayType() {
		tempLdc = null;
		return this;
	}

	public void visitClassType(String name) {
		tempLdc = null;
		addName(name);
	}

	public void visitInnerClassType(String name) {
		tempLdc = null;
		addName(name);
	}

	public void visitTypeArgument() {
		tempLdc = null;
	}

	public SignatureVisitor visitTypeArgument(char wildcard) {
		tempLdc = null;
		return this;
	}

	// common

	public void visitEnd() {
		tempLdc = null;
	}

	// ---------------------------------------------

	private String getGroupKey(String name) {
		int n = name.lastIndexOf('/');
		if (n > -1)
			name = name.substring(0, n);
		packages.add(name);
		return name;
	}

	private void addName(String name) {
		if (name == null)
			return;
		String p = getGroupKey(name);
		if (current.containsKey(p)) {
			current.put(p, new Integer(((Integer) current.get(p)).intValue() + 1));
		}
		else {
			current.put(p, new Integer(1));
		}
	}

	private void addNames(String[] names) {
		for (int i = 0; names != null && i < names.length; i++)
			addName(names[i]);
	}

	private void addDesc(String desc) {
		addType(Type.getType(desc));
	}

	private void addMethodDesc(String desc) {
		addType(Type.getReturnType(desc));
		Type[] types = Type.getArgumentTypes(desc);
		for (int i = 0; i < types.length; i++)
			addType(types[i]);
	}

	private void addType(Type t) {
		switch (t.getSort()) {
		case Type.ARRAY:
			addType(t.getElementType());
			break;
		case Type.OBJECT:
			addName(t.getClassName().replace('.', '/'));
			break;
		}
	}

	private void addSignature(String signature) {
		if (signature != null)
			new SignatureReader(signature).accept(this);
	}

	private void addTypeSignature(String signature) {
		if (signature != null)
			new SignatureReader(signature).acceptType(this);
	}

}
