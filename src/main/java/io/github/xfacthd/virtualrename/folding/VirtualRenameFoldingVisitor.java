package io.github.xfacthd.virtualrename.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiVariable;
import io.github.xfacthd.virtualrename.data.VirtualRenameResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class VirtualRenameFoldingVisitor extends JavaRecursiveElementVisitor
{
    private final VirtualRenameResolver resolver;
    private final List<FoldingDescriptor> descriptors = new ArrayList<>();

    public VirtualRenameFoldingVisitor(VirtualRenameResolver resolver)
    {
        this.resolver = resolver;
    }

    @Override
    public void visitParameter(PsiParameter param)
    {
        addDescriptor(param.getNode(), param, Objects.requireNonNull(param.getNameIdentifier()).getTextRange());
        super.visitParameter(param);
    }

    @Override
    public void visitLocalVariable(PsiLocalVariable variable)
    {
        addDescriptor(variable.getNode(), variable, Objects.requireNonNull(variable.getNameIdentifier()).getTextRange());
        super.visitLocalVariable(variable);
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression refExpr)
    {
        PsiElement resolved = refExpr.resolve();
        if (resolved instanceof PsiLocalVariable localVar)
        {
            addDescriptor(refExpr.getNode(), localVar, refExpr.getAbsoluteRange());
        }
        else if (resolved instanceof PsiParameter param)
        {
            addDescriptor(refExpr.getNode(), param, refExpr.getAbsoluteRange());
        }
        super.visitReferenceExpression(refExpr);
    }

    private void addDescriptor(ASTNode node, PsiVariable variable, TextRange range)
    {
        String renamed = resolver.getRenameFor(variable);
        if (renamed != null)
        {
            FoldingDescriptor descriptor = new FoldingDescriptor(node, range, null, Set.of(), true, renamed, true);
            descriptor.setCanBeRemovedWhenCollapsed(true);
            descriptors.add(descriptor);
        }
    }

    public FoldingDescriptor[] getDescriptors()
    {
        return descriptors.toArray(FoldingDescriptor[]::new);
    }
}
