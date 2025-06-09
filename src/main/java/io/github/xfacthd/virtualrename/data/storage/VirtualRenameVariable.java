package io.github.xfacthd.virtualrename.data.storage;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiVariable;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import io.github.xfacthd.virtualrename.data.converter.TextRangeConverter;

import java.beans.JavaBean;

@JavaBean
@Tag("variable")
public final class VirtualRenameVariable
{
    @Attribute("newName")
    private String newName;
    @Attribute(value = "origRange", converter = TextRangeConverter.class)
    private TextRange origRange;

    public VirtualRenameVariable(String newName, TextRange origRange)
    {
        this.newName = newName;
        this.origRange = origRange;
    }

    @SuppressWarnings("unused") // Used by serialization to construct a default value
    public VirtualRenameVariable()
    {
        this(null, null);
    }

    public String getNewName()
    {
        return newName;
    }

    public boolean matches(PsiVariable variable)
    {
        return origRange.equals(variable.getTextRange());
    }
}
