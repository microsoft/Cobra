package com.nuance.cobra.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JavaCastUtils
{
    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> c) {
        List<T> r = new ArrayList<>(c.size());
        for(Object o: c)
            r.add(clazz.cast(o));
        return r;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T uncheckedCast(Object obj) {
        return (T) obj;
    }
}
