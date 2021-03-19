package com.example.streaming.library.db.converter;

import android.database.Cursor;

import com.example.streaming.library.db.sqlite.ColumnDbType;

public class IntegerColumnConverter implements ColumnConverter<Integer> {

    @Override
    public Integer getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getInt(index);
    }

    @Override
    public Object fieldValue2DbValue(Integer fieldValue) {
        return fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.INTEGER;
    }
}
