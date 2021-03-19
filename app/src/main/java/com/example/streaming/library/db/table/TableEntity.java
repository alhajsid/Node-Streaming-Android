package com.example.streaming.library.db.table;

import android.database.Cursor;

import com.example.streaming.library.db.DBConstants;
import com.example.streaming.library.db.DbException;
import com.example.streaming.library.db.DbManager;
import com.example.streaming.library.db.annotation.Table;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

public final class TableEntity<T> {

    private final DbManager db;
    private final String name;
    private final String onCreated;
    private ColumnEntity id;
    private Class<T> entityType;
    private Constructor<T> constructor;
    private volatile boolean checkedDatabase;

    private final LinkedHashMap<String, ColumnEntity> columnMap;

    public TableEntity(DbManager db, Class<T> entityType) throws Exception {
        this.db = db;
        this.entityType = entityType;
        this.constructor = entityType.getConstructor();
        this.constructor.setAccessible(true);

        Table table = entityType.getAnnotation(Table.class);
        this.name = table.name();
        this.onCreated = table.onCreated();
        this.columnMap = TableUtils.findColumnMap(entityType);

        for (ColumnEntity column : columnMap.values()) {
            if (column.isId()) {
                this.id = column;
                break;
            }
        }
    }

    public T createEntity() throws Exception {
        return this.constructor.newInstance();
    }

    public boolean tableIsExist() throws DbException {
        if (this.isCheckedDatabase()) {
            return true;
        }

        Cursor cursor = db.execQuery(String.format(DBConstants.SQL_QUERY_COUNT, name));
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    int count = cursor.getInt(0);
                    if (count > 0) {
                        this.setCheckedDatabase(true);
                        return true;
                    }
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }

        return false;
    }

    public DbManager getDb() {
        return db;
    }

    public String getName() {
        return name;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public String getOnCreated() {
        return onCreated;
    }

    public ColumnEntity getId() {
        return id;
    }

    public LinkedHashMap<String, ColumnEntity> getColumnMap() {
        return columnMap;
    }

    public boolean isCheckedDatabase() {
        return checkedDatabase;
    }

    public void setCheckedDatabase(boolean checkedDatabase) {
        this.checkedDatabase = checkedDatabase;
    }

    @Override
    public String toString() {
        return name;
    }
}
