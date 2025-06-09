package io.github.xfacthd.virtualrename.data.storage;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypes;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XMap;
import org.jetbrains.annotations.Nullable;

import java.beans.JavaBean;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@JavaBean
@Tag("class")
public final class VirtualRenameClass
{
    @XMap(entryTagName = "method_entry")
    @Property(surroundWithTag = false)
    @MapAnnotation(surroundWithTag = false, surroundValueWithTag = false, entryTagName = "method_entry")
    private final Map<String, VirtualRenameMethod> methods = new ConcurrentHashMap<>();

    @Nullable
    public VirtualRenameMethod getMethod(PsiMethod psiMth, boolean createIfAbsent)
    {
        String mthDesc = buildMethodDescriptor(psiMth);
        VirtualRenameMethod method = methods.get(mthDesc);
        if (method == null)
        {
            synchronized (methods)
            {
                method = methods.get(mthDesc);
                if (method == null && createIfAbsent)
                {
                    method = new VirtualRenameMethod();
                    methods.put(mthDesc, method);
                }
            }
        }
        return method;
    }

    public void tryRemoveEmptyMethod(PsiMethod psiMth)
    {
        String mthDesc = buildMethodDescriptor(psiMth);
        synchronized (methods)
        {
            VirtualRenameMethod method = methods.get(mthDesc);
            if (method != null && method.isEmpty())
            {
                methods.remove(mthDesc);
            }
        }
    }

    public boolean isEmpty()
    {
        return methods.isEmpty();
    }

    private static String buildMethodDescriptor(PsiMethod mth)
    {
        String mthName = mth.isConstructor() ? "<init>" : mth.getName();
        StringBuilder builder = new StringBuilder(mthName);

        PsiParameterList params = mth.getParameterList();
        builder.append("(");
        for (int i = 0; i < params.getParametersCount(); i++)
        {
            if (i > 0)
            {
                builder.append(",");
            }
            PsiType paramType = Objects.requireNonNull(params.getParameter(i)).getType();
            builder.append(paramType.getCanonicalText());
        }
        builder.append(")");

        PsiType retType = Objects.requireNonNullElseGet(mth.getReturnType(), PsiTypes::voidType);
        builder.append(retType.getCanonicalText());

        return builder.toString();
    }
}
