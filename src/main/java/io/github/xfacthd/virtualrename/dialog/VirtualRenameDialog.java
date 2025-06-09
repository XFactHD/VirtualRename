package io.github.xfacthd.virtualrename.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiVariable;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.util.Objects;

public final class VirtualRenameDialog extends DialogWrapper
{
    private final PsiVariable elem;
    private final JBTextField renameField;

    public VirtualRenameDialog(@Nullable Project project, PsiVariable elem, @Nullable String existing)
    {
        super(project, false);
        this.elem = elem;
        String initial = existing != null ? existing : elem.getName();
        this.renameField = new JBTextField();
        this.renameField.setText(initial);
        setTitle("Virtual Rename");
        init();
    }

    @Override
    protected JComponent createCenterPanel()
    {
        JLabel labelName = new JBLabel("Name:");
        JLabel labelRename = new JBLabel("Rename:");
        JLabel elemName = new JBLabel(Objects.requireNonNull(elem.getName()));

        JComponent panel = JBUI.Panels.simplePanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
        hGroup.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(labelRename));
        hGroup.addGroup(layout.createParallelGroup().addComponent(elemName).addComponent(renameField));
        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelName).addComponent(elemName));
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelRename).addComponent(renameField));
        layout.setVerticalGroup(vGroup);

        return panel;
    }

    @Override
    public JComponent getPreferredFocusedComponent()
    {
        return renameField;
    }

    @Nullable
    public String getRename()
    {
        String text = renameField.getText();
        if (text.isBlank() || text.equals(elem.getName()))
        {
            return null;
        }
        return text;
    }
}
