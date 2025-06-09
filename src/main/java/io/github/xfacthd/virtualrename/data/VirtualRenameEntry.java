package io.github.xfacthd.virtualrename.data;

import org.jetbrains.annotations.Nullable;

public record VirtualRenameEntry(String value)
{
    public static final VirtualRenameEntry SENTINEL = new VirtualRenameEntry(null);

    public static VirtualRenameEntry of(@Nullable String rename)
    {
        return rename != null ? new VirtualRenameEntry(rename) : SENTINEL;
    }
}
