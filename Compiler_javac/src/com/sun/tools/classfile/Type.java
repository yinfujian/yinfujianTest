/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.classfile;

import java.util.List;

/*
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class Type {
    protected Type() { }

    public boolean isObject() {
        return false;
    }

    protected static void append(StringBuilder sb, String prefix, List<? extends Type> types, String suffix) {
        sb.append(prefix);
        String sep = "";
        for (Type t: types) {
            sb.append(sep);
            sb.append(t);
            sep = ", ";
        }
        sb.append(suffix);
    }

    protected static void appendIfNotEmpty(StringBuilder sb, String prefix, List<? extends Type> types, String suffix) {
        if (types != null && types.size() > 0)
            append(sb, prefix, types, suffix);
    }

    public static class SimpleType extends Type {
        public SimpleType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean isObject() {
            return name.equals("java.lang.Object");
        }

        public final String name;
    }

    public static class ArrayType extends Type {
        public ArrayType(Type elemType) {
            this.elemType = elemType;
        }

        @Override
        public String toString() {
            return elemType + "[]";
        }

        public final Type elemType;
    }

    public static class MethodType extends Type {
        public MethodType(List<? extends Type> argTypes, Type resultType) {
            this(null, argTypes, resultType, null);
        }

        public MethodType(List<? extends Type> typeArgTypes,
                List<? extends Type> argTypes,
                Type returnType,
                List<? extends Type> throwsTypes) {
            this.typeArgTypes = typeArgTypes;
            this.argTypes = argTypes;
            this.returnType = returnType;
            this.throwsTypes = throwsTypes;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            appendIfNotEmpty(sb, "<", typeArgTypes, "> ");
            sb.append(returnType);
            append(sb, " (", argTypes, ")");
            appendIfNotEmpty(sb, " throws ", throwsTypes, "");
            return sb.toString();
        }

        public final List<? extends Type> typeArgTypes;
        public final List<? extends Type> argTypes;
        public final Type returnType;
        public final List<? extends Type> throwsTypes;
    }

    public static class ClassSigType extends Type {
        public ClassSigType(List<Type> typeArgTypes, Type superclassType, List<Type> superinterfaceTypes) {
            this.typeArgTypes = typeArgTypes;
            this.superclassType = superclassType;
            this.superinterfaceTypes = superinterfaceTypes;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            appendIfNotEmpty(sb, "<", typeArgTypes, ">");
            if (superclassType != null && !superclassType.isObject()) {
                sb.append(" extends ");
                sb.append(superclassType);
            }
            appendIfNotEmpty(sb, " implements ", superinterfaceTypes, "");
            return sb.toString();
        }

        public final List<Type> typeArgTypes;
        public final Type superclassType;
        public final List<Type> superinterfaceTypes;
    }

    public static class ClassType extends Type {
        public ClassType(String name, List<Type> typeArgs) {
            this.name = name;
            this.typeArgs = typeArgs;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            appendIfNotEmpty(sb, "<", typeArgs, ">");
            return sb.toString();
        }

        public final String name;
        public final List<Type> typeArgs;
    }


    public static class InnerClassType extends Type {
        public InnerClassType(Type outerType, Type innerType) {
            this.outerType = outerType;
            this.innerType = innerType;
        }

        @Override
        public String toString() {
            return outerType + "." + innerType;
        }

        public final Type outerType;
        public final Type innerType;
    }

    public static class TypeArgType extends Type {
        public TypeArgType(String name, Type classBound, List<Type> interfaceBounds) {
            this.name = name;
            this.classBound = classBound;
            this.interfaceBounds = interfaceBounds;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            String sep = " extends ";
            if (classBound != null && !classBound.isObject()) {
                sb.append(sep);
                sb.append(classBound);
                sep = " & ";
            }
            if (interfaceBounds != null) {
                for (Type bound: interfaceBounds) {
                    sb.append(sep);
                    sb.append(bound);
                    sep = " & ";
                }
            }
            return sb.toString();
        }

        public final String name;
        public final Type classBound;
        public final List<Type> interfaceBounds;
    }

    public static class WildcardType extends Type {
        public WildcardType() {
            this(null, null);
        }

        public WildcardType(String kind, Type boundType) {
            this.kind = kind;
            this.boundType = boundType;
        }

        @Override
        public String toString() {
            if (kind == null)
                return "?";
            else
                return "? " + kind + " " + boundType;
        }

        public final String kind;
        public final Type boundType;
    }
}
