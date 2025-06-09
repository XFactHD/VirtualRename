package io.github.xfacthd.virtualrename.data;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiVariable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import io.github.xfacthd.virtualrename.data.storage.VirtualRenameFile;
import io.github.xfacthd.virtualrename.data.persistence.VirtualRenamePersistence;
import io.github.xfacthd.virtualrename.data.persistence.VirtualRenameStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VirtualRenameResolver
{
    private static final Key<VirtualRenameResolver> RESOLVER_DATA_KEY = new Key<>("virtual_rename.rename_resolver");
    private static final Object GLOBAL_LOCK = new Object();

    private final VirtualRenameStorage storage;
    private final String fileName;
    private final Map<PsiVariable, VirtualRenameEntry> resolvedRenames = new ConcurrentHashMap<>();
    private VirtualRenameFile data;

    private VirtualRenameResolver(VirtualRenameStorage storage, String fileName)
    {
        this.storage = storage;
        this.fileName = fileName;
    }

    public String getRenameFor(PsiVariable variable)
    {
        VirtualRenameEntry rename = resolvedRenames.get(variable);
        if (rename == null)
        {
            rename = getData(false).getRenameFor(variable);
            resolvedRenames.put(variable, rename);
        }
        return rename.value();
    }

    public void setRenameFor(PsiVariable variable, @Nullable String rename)
    {
        getData(true).setRenameFor(variable, rename);
        resolvedRenames.put(variable, VirtualRenameEntry.of(rename));
    }

    public boolean isEmpty()
    {
        return resolvedRenames.isEmpty() && getData(false).isEmpty();
    }

    private VirtualRenameFile getData(boolean createIfAbsent)
    {
        if (data == null || (createIfAbsent && data == VirtualRenameFile.EMPTY))
        {
            synchronized (storage)
            {
                if (data == null || (createIfAbsent && data == VirtualRenameFile.EMPTY))
                {
                    data = storage.getRenamesInFile(fileName, createIfAbsent);
                }
            }
        }
        return data;
    }

    public static VirtualRenameResolver getSafe(Project project, @Nullable PsiFile file, Document document)
    {
        Computable<VirtualRenameResolver> computable = () -> get(project, file, document);
        return ApplicationManager.getApplication().runReadAction(computable);
    }

    @Contract("_, null, _ -> null")
    public static VirtualRenameResolver get(Project project, @Nullable PsiFile file, Document document)
    {
        VirtualRenameResolver resolver = document.getUserData(RESOLVER_DATA_KEY);
        if (resolver != null) return resolver;

        synchronized (GLOBAL_LOCK)
        {
            resolver = document.getUserData(RESOLVER_DATA_KEY);
            if (resolver != null) return resolver;

            if (file == null)
            {
                file = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (file == null) return null;
            }

            resolver = new VirtualRenameResolver(
                    VirtualRenamePersistence.getStorage(project),
                    file.getVirtualFile().getPresentableUrl()
            );
            document.putUserData(RESOLVER_DATA_KEY, resolver);
        }
        return resolver;
    }
}
