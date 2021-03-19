package xyz.tanwb.airship.db.converter;

import android.database.Cursor;

import java.sql.Date;

import xyz.tanwb.airship.db.sqlite.ColumnDbType;

public class SqlDateColumnConverter implements ColumnConverter<Date> {

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
