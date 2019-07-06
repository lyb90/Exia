package com.iostate.exia.solvers;

import com.iostate.exia.api.AstFunction;
import com.iostate.exia.ast.AstUtils;
import com.iostate.exia.core.FileWalker;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.util.function.Predicate;


public class FieldFinalCorrecter implements AstFunction {
    private static final Modifier.ModifierKeyword amp = Modifier.ModifierKeyword.FINAL_KEYWORD;

    public static void main(String[] args) {
        FileWalker.launch(args, new FieldFinalCorrecter());
    }

    @Override
    public boolean doAndModify(final CompilationUnit cu, File file) {
        final TypeDeclaration type = AstUtils.tryGetConcreteType(cu);
        return addFinalForType(type) > 0;
    }

    private long addFinalForType(TypeDeclaration type) {
        long hits = 0;
        if (type == null) return 0;
        if (type.getFields().length == 0) {
            return 0;
        }
        for (FieldDeclaration fieldDeclaration : type.getFields()) {
            Modifier finalModifier = fieldDeclaration.getAST().newModifier(amp);
            boolean isStatic = fieldDeclaration.modifiers().stream().anyMatch((Predicate<Modifier>) Modifier::isStatic);
            boolean isFinal = fieldDeclaration.modifiers().stream().anyMatch((Predicate<Modifier>) Modifier::isFinal);
            if (isStatic && !isFinal) {
                fieldDeclaration.modifiers().add(finalModifier);
                hits++;
            }
        }
        TypeDeclaration[] children = type.getTypes();
        if (children != null) {
            for (TypeDeclaration child : children) {
                hits += addFinalForType(child);
            }
        }
        return hits;
    }

}
