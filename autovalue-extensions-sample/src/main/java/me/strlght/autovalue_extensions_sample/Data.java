package me.strlght.autovalue_extensions_sample;

import com.google.auto.value.AutoValue;

/**
 * @author Grigoriy Dzhanelidze
 */
@SQLTable
@AutoValue
public abstract class Data {
    public static Data create(long id, String name, String userPic) {
        return new AutoValue_Data(id, name, userPic);
    }

    public abstract long id();

    public abstract String name();

    public abstract String userPic();
}
