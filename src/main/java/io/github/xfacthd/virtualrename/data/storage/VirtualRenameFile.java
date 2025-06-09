package io.github.xfacthd.virtualrename.data.storage;

import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XMap;
import org.jetbrains.annotations.Nullable;
import io.github.xfacthd.virtualrename.data.VirtualRenameEntry;

import java.beans.JavaBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JavaBean
@Tag("file")
public final class VirtualRenameFile
{
    public static final VirtualRenameFile EMPTY = new VirtualRenameFile();

    @XMap(entryTagName = "class_entry")
    @Property(surroundWithTag = false)
    @MapAnnotation(surroundWithTag = false, surroundValueWithTag = false, entryTagName = "class_entry")
    private final Map<String, VirtualRenameClass> classes = new ConcurrentHashMap<>();

    public VirtualRenameEntry getRenameFor(PsiVariable variable)
    {
        VirtualRenameVariable rename = runActionFor(variable, false, VirtualRenameMethod::getRename);
        return rename == null ? VirtualRenameEntry.SENTINEL : VirtualRenameEntry.of(rename.getNewName());
    }

    public void setRenameFor(PsiVariable variable, @Nullable String renamed)
    {
        runActionFor(variable, renamed != null, (mth, var) -> mth.setRename(var, renamed));
    }

    public boolean isEmpty()
    {
        return classes.isEmpty();
    }

    @Nullable
    private <T> T runActionFor(PsiVariable variable, boolean createIfAbsent, RenameAction<T> action)
    {
        if (classes.isEmpty() && !createIfAbsent) return null;

        String clazzName = getClassName(PsiTreeUtil.getParentOfType(variable, PsiClass.class));
        VirtualRenameClass clazz = classes.get(clazzName);
        if (clazz == null)
        {
            if (!createIfAbsent) return null;

            synchronized (classes)
            {
                clazz = classes.get(clazzName);
                if (clazz == null)
                {
                    clazz = new VirtualRenameClass();
                    classes.put(clazzName, clazz);
                }
            }
        }

        PsiMethod psiMth = PsiTreeUtil.getParentOfType(variable, PsiMethod.class);
        if (psiMth == null) return null;

        VirtualRenameMethod method = clazz.getMethod(psiMth, createIfAbsent);
        if (method == null) return null;

        T result = action.run(method, variable);

        // Either a read action "failed" and cleaned itself up or a write action removed an entry
        if (result == null)
        {
            if (method.isEmpty())
            {
                clazz.tryRemoveEmptyMethod(psiMth);
            }
            if (clazz.isEmpty())
            {
                synchronized (classes)
                {
                    if (clazz.isEmpty())
                    {
                        classes.remove(clazzName);
                    }
                }
            }
        }

        return result;
    }

    private static String getClassName(@Nullable PsiClass clazz)
    {
        if (clazz instanceof PsiAnonymousClass anonClass)
        {
            String ownerName = getClassName(PsiTreeUtil.getParentOfType(anonClass, PsiClass.class));
            return ownerName + ".anonclass";
        }

        String fqn;
        if (clazz == null || (fqn = clazz.getQualifiedName()) == null)
        {
            return "unknowncontainingclass";
        }
        return fqn;
    }

    @FunctionalInterface
    private interface RenameAction<T>
    {
        T run(VirtualRenameMethod method, PsiVariable variable);
    }
}
