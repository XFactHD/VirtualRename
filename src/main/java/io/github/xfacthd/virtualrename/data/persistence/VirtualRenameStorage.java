package io.github.xfacthd.virtualrename.data.persistence;

import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.XMap;
import io.github.xfacthd.virtualrename.data.storage.VirtualRenameFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VirtualRenameStorage
{
    @XMap(entryTagName = "file_entry")
    @MapAnnotation(surroundValueWithTag = false, entryTagName = "file_entry")
    private final Map<String, VirtualRenameFile> files = new ConcurrentHashMap<>();

    public VirtualRenameFile getRenamesInFile(String fileName, boolean createIfAbsent)
    {
        VirtualRenameFile renamesInFile = files.get(fileName);
        if (renamesInFile == null)
        {
            if (!createIfAbsent)
            {
                return VirtualRenameFile.EMPTY;
            }

            synchronized (files)
            {
                renamesInFile = files.get(fileName);
                if (renamesInFile == null)
                {
                    renamesInFile = new VirtualRenameFile();
                    files.put(fileName, renamesInFile);
                }
            }
            return renamesInFile;
        }
        return renamesInFile;
    }
}
