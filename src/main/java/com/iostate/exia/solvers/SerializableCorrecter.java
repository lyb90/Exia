package com.iostate.exia.solvers;

import com.iostate.exia.api.AstFunction;
import com.iostate.exia.ast.AstUtils;
import com.iostate.exia.core.FileWalker;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.Serializable;


public class SerializableCorrecter implements AstFunction {
    private static final Modifier.ModifierKeyword amp = Modifier.ModifierKeyword.PRIVATE_KEYWORD;

    public static void main(String[] args) {
        FileWalker.launch(args, new SerializableCorrecter());
    }

    @Override
    public boolean doAndModify(final CompilationUnit cu, File file) {
        final TypeDeclaration type = AstUtils.tryGetConcreteType(cu);
        if (type == null) return false;
        String packageName = cu.getPackage().getName().toString() + "." + type.getName().toString();
        boolean addMethod = addMethodForType(packageName, type, cu) > 0;
        if (addMethod) {
            AST ast = type.getAST();
            ImportDeclaration inImportDeclaration = ast.newImportDeclaration();
            inImportDeclaration.setName(ast.newName("java.io.ObjectInputStream"));
            ImportDeclaration outImportDeclaration = ast.newImportDeclaration();
            outImportDeclaration.setName(ast.newName("java.io.ObjectOutputStream"));
            ImportDeclaration iOImportDeclaration = ast.newImportDeclaration();
            iOImportDeclaration.setName(ast.newName("java.io.IOException"));
            cu.imports().add(inImportDeclaration);
            cu.imports().add(outImportDeclaration);
            cu.imports().add(iOImportDeclaration);
        }
        return addMethod;
    }

    private long addMethodForType(String className, TypeDeclaration type, CompilationUnit cu) {
        long hits = 0;
        if (!isSerializable(className)) {
            return 0;
        }
        MethodDeclaration[] methods = type.getMethods();
        if (methods == null) {
            return 0;
        }
        boolean hasReadObjectMethod = false;
        boolean hasWriteObjectMethod = false;
        for (MethodDeclaration method : methods) {
            if ("readObject".equals(method.getName().toString())) {
                hasReadObjectMethod = true;
                continue;
            }
            if ("writeObject".equals(method.getName().toString())) {
                hasWriteObjectMethod = true;
            }
        }
        if (!hasReadObjectMethod) {
            writeReadObjectMethod(type, cu);
            hits++;
        }
        if (!hasWriteObjectMethod) {
            writeWriteObjectMethod(type, cu);
            hits++;
        }

        TypeDeclaration[] children = type.getTypes();
        if (children != null) {
            for (TypeDeclaration child : children) {
                if (child == null) {
                    continue;
                }
                hits += addMethodForType(className + "$" + child.getName().toString(), child, cu);
            }
        }
        return hits;
    }

    private void writeReadObjectMethod(TypeDeclaration type, CompilationUnit cu) {
        AST ast = type.getAST();
        MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
        methodDeclaration.setName(ast.newSimpleName("readObject"));
        SingleVariableDeclaration sd = ast.newSingleVariableDeclaration();
        sd.setType(ast.newSimpleType(ast.newSimpleName("ObjectInputStream")));
        sd.setName(ast.newSimpleName("s"));
        methodDeclaration.parameters().add(sd);
        methodDeclaration.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("IOException")));
        methodDeclaration.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("ClassNotFoundException")));
        methodDeclaration.modifiers().add(ast.newModifier(amp));
        Block body = ast.newBlock();
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newSimpleName("s"));
        methodInvocation.setName(ast.newSimpleName("readObject"));
        body.statements().add(ast.newExpressionStatement(methodInvocation));
        methodDeclaration.setBody(body);
        type.bodyDeclarations().add(methodDeclaration);
    }

    private void writeWriteObjectMethod(TypeDeclaration type, CompilationUnit cu) {
        AST ast = type.getAST();
        MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
        methodDeclaration.setName(ast.newSimpleName("writeObject"));
        SingleVariableDeclaration sd = ast.newSingleVariableDeclaration();
        sd.setType(ast.newSimpleType(ast.newSimpleName("ObjectOutputStream")));
        sd.setName(ast.newSimpleName("s"));
        methodDeclaration.parameters().add(sd);
        methodDeclaration.thrownExceptionTypes().add(ast.newSimpleType(ast.newSimpleName("IOException")));
        methodDeclaration.modifiers().add(ast.newModifier(amp));

        Block body = ast.newBlock();
        MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setExpression(ast.newSimpleName("s"));
        methodInvocation.setName(ast.newSimpleName("writeObject"));
        methodInvocation.arguments().add(ast.newThisExpression());
        body.statements().add(ast.newExpressionStatement(methodInvocation));
        methodDeclaration.setBody(body);
        type.bodyDeclarations().add(methodDeclaration);
    }

    private boolean isSerializable(String className) {

        try {
            if (Serializable.class.isAssignableFrom(Class.forName(className))) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("cannot find class " + className + " in classpath");
        }
        return false;
    }

}
