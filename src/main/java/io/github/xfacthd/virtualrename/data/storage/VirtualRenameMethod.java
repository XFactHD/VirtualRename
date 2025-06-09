package io.github.xfacthd.virtualrename.data.storage;

import com.intellij.psi.PsiVariable;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XMap;
import org.jetbrains.annotations.Nullable;

import java.beans.JavaBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JavaBean
@Tag("method")
public final class VirtualRenameMethod
{
    @XMap(entryTagName = "variable_entry")
    @Property(surroundWithTag = false)
    @MapAnnotation(surroundWithTag = false, surroundValueWithTag = false, entryTagName = "variable_entry")
    private final Map<String, VirtualRenameVariable> variables = new ConcurrentHashMap<>();

    @Nullable
    public VirtualRenameVariable getRename(PsiVariable variable)
    {
        String varName = variable.getName();
        VirtualRenameVariable renamed = variables.get(varName);
        if (renamed != null && !renamed.matches(variable))
        {
            variables.remove(varName);
            return null;
        }
        return renamed;
    }

    @Nullable
    public VirtualRenameVariable setRename(PsiVariable variable, @Nullable String renamed)
    {
        if (renamed != null)
        {
            VirtualRenameVariable value = new VirtualRenameVariable(renamed, variable.getTextRange());
            variables.put(variable.getName(), value);
            return value;
        }
        else
        {
            variables.remove(variable.getName());
            return null;
        }
    }

    public boolean isEmpty()
    {
        return variables.isEmpty();
    }
}
