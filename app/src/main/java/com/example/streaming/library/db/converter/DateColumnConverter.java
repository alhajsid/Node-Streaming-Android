package com.example.streaming.library.db.converter;

import android.database.Cursor;

import com.example.streaming.library.db.sqlite.ColumnDbType;

import java.util.Date;

public class DateColumnConverter implements ColumnConverter<Date> {

    @Override
    public Date getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : new Date(cursor.getLong(index));
    }

    @Override
    public Object fieldValue2DbValue(Date fieldValue) {
        if (fieldValue == null) return null;
        return fieldValue.getTime();
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.INTEGER;
    }
}
