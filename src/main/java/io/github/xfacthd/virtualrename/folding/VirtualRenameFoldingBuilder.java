package io.github.xfacthd.virtualrename.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import io.github.xfacthd.virtualrename.data.VirtualRenameResolver;

public final class VirtualRenameFoldingBuilder extends FoldingBuilderEx
{
    @Override
    public FoldingDescriptor[] buildFoldRegions(PsiElement root, Document doc, boolean quick)
    {
        // Avoid expensive resolves on initial file load, this will be called again shortly after with quick=false
        if (quick || doc.isWritable()) return FoldingDescriptor.EMPTY_ARRAY;

        VirtualRenameResolver resolver = VirtualRenameResolver.getSafe(root.getProject(), null, doc);
        if (resolver == null || resolver.isEmpty()) return FoldingDescriptor.EMPTY_ARRAY;

        VirtualRenameFoldingVisitor visitor = new VirtualRenameFoldingVisitor(resolver);
        root.accept(visitor);
        return visitor.getDescriptors();
    }

    @Override
    public String getPlaceholderText(ASTNode node)
    {
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(ASTNode node)
    {
        return true;
    }
}
