package io.github.xfacthd.virtualrename.action;

import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiVariable;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.Nullable;
import io.github.xfacthd.virtualrename.data.VirtualRenameResolver;
import io.github.xfacthd.virtualrename.dialog.VirtualRenameDialog;

public final class VirtualRenameAction extends AnAction implements DumbAware
{
    @Override
    public void actionPerformed(AnActionEvent event)
    {
        Project project = event.getProject();
        if (project == null) return;

        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;

        Language lang = event.getData(LangDataKeys.LANGUAGE);
        if (lang == null || !lang.getID().equals("JAVA")) return;

        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        if (file == null || file.getVirtualFile() == null) return;

        // TODO: this doesn't handle modifiable files manually marked as read-only
        if (file.isWritable())
        {
            HintManager.getInstance().showInformationHint(editor, "File is not read-only");
            return;
        }

        CaretModel caretModel = editor.getCaretModel();
        if (caretModel.getCaretCount() != 1) return;

        PsiVariable variable = getTargetElement(editor, file, event.getDataContext());
        if (variable == null) return;

        caretModel.getPrimaryCaret().moveToOffset(variable.getTextOffset());

        VirtualRenameResolver resolver = VirtualRenameResolver.get(project, file, editor.getDocument());
        String existing = resolver.getRenameFor(variable);
        VirtualRenameDialog dialog = new VirtualRenameDialog(file.getProject(), variable, existing);
        if (dialog.showAndGet())
        {
            resolver.setRenameFor(variable, dialog.getRename());
            CodeFoldingManager.getInstance(file.getProject()).scheduleAsyncFoldingUpdate(editor);
        }
    }

    @Nullable
    private static PsiVariable getTargetElement(Editor editor, PsiFile file, DataContext dataContext)
    {
        PsiElement element = PsiElementRenameHandler.getElement(dataContext);
        if (element == null)
        {
            element = CommonRefactoringUtil.getElementAtCaret(editor, file);
        }
        return unwrapFiltered(element);
    }

    @Nullable
    private static PsiVariable unwrapFiltered(@Nullable PsiElement elem)
    {
        return switch (elem)
        {
            case PsiIdentifier identifier -> unwrapFiltered(identifier.getParent());
            case PsiReferenceExpression refExp -> unwrapFiltered(refExp.resolve());
            case PsiParameter param -> param;
            case PsiLocalVariable localVar -> localVar;
            case null, default -> null;
        };
    }
}
