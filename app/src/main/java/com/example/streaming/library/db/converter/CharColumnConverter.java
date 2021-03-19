package com.example.streaming.library.db.converter;

import android.database.Cursor;

import com.example.streaming.library.db.sqlite.ColumnDbType;

public class CharColumnConverter implements ColumnConverter<Character> {

    @Override
    public Character getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : (char) cursor.getInt(index);
    }

    @Override
    public Object fieldValue2DbValue(Character fieldValue) {
        if (fieldValue == null) return null;
        return (int) fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.INTEGER;
    }
}
