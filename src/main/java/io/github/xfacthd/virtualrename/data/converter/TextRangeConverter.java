package io.github.xfacthd.virtualrename.data.converter;

import com.intellij.openapi.util.TextRange;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TextRangeConverter extends Converter<TextRange>
{
    @Nullable
    @Override
    public TextRange fromString(@NotNull String s)
    {
        String[] parts = s.split("\\|");
        if (parts.length != 2) return null;

        int start;
        int end;
        try
        {
            start = Integer.parseInt(parts[0]);
            end = Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        if (start < 0 || end < 0)
        {
            return null;
        }
        return new TextRange(start, end);
    }

    @Override
    public String toString(TextRange range)
    {
        return range.getStartOffset() + "|" + range.getEndOffset();
    }
}
