package com.example.streaming.library.db;

import android.database.Cursor;

import com.example.streaming.library.db.sqlite.WhereBuilder;
import com.example.streaming.library.db.table.DbModel;
import com.example.streaming.library.db.table.TableEntity;

import java.util.ArrayList;
import java.util.List;

public final class Selector<T> {

    private final TableEntity<T> table;

    private WhereBuilder whereBuilder;
    private List<OrderBy> orderByList;
    private int limit;
    private int offset;

    private Selector(TableEntity<T> table) {
        this.table = table;
    }

    public static <T> Selector<T> from(TableEntity<T> table) {
        return new Selector<T>(table);
    }

    public Selector<T> where(WhereBuilder whereBuilder) {
        this.whereBuilder = whereBuilder;
        return this;
    }

    public Selector<T> where(String columnName, String op, Object value) {
        this.whereBuilder = WhereBuilder.b(columnName, op, value);
        return this;
    }

    public Selector<T> and(String columnName, String op, Object value) {
        this.whereBuilder.and(columnName, op, value);
        return this;
    }

    public Selector<T> and(WhereBuilder where) {
        this.whereBuilder.and(where);
        return this;
    }

    public Selector<T> or(String columnName, String op, Object value) {
        this.whereBuilder.or(columnName, op, value);
        return this;
    }

    public Selector or(WhereBuilder where) {
        this.whereBuilder.or(where);
        return this;
    }

    public Selector<T> expr(String expr) {
        if (this.whereBuilder == null) {
            this.whereBuilder = WhereBuilder.b();
        }
        this.whereBuilder.expr(expr);
        return this;
    }

    public DbModelSelector groupBy(String columnName) {
        return new DbModelSelector(this, columnName);
    }

    public DbModelSelector select(String... columnExpressions) {
        return new DbModelSelector(this, columnExpressions);
    }

    public Selector<T> orderBy(String columnName) {
        if (orderByList == null) {
            orderByList = new ArrayList<OrderBy>(5);
        }
        orderByList.add(new OrderBy(columnName));
        return this;
    }

    public Selector<T> orderBy(String columnName, boolean desc) {
        if (orderByList == null) {
            orderByList = new ArrayList<OrderBy>(5);
        }
        orderByList.add(new OrderBy(columnName, desc));
        return this;
    }

    public Selector<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Selector<T> offset(int offset) {
        this.offset = offset;
        return this;
    }

    public TableEntity<T> getTable() {
        return table;
    }

    public WhereBuilder getWhereBuilder() {
        return whereBuilder;
    }

    public List<OrderBy> getOrderByList() {
        return orderByList;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public T findFirst() throws DbException {
        if (!table.tableIsExist()) return null;

        this.limit(1);
        Cursor cursor = table.getDb().execQuery(this.toString());
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    return CursorUtils.getEntity(table, cursor);
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public List<T> findAll() throws DbException {
        if (!table.tableIsExist()) return null;

        List<T> result = null;
        Cursor cursor = table.getDb().execQuery(this.toString());
        if (cursor != null) {
            try {
                result = new ArrayList<T>();
                while (cursor.moveToNext()) {
                    T entity = CursorUtils.getEntity(table, cursor);
                    result.add(entity);
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    public long count() throws DbException {
        if (!table.tableIsExist()) return 0;

        DbModelSelector dmSelector = this.select("count(\"" + table.getId().getName() + "\") as count");
        DbModel firstModel = dmSelector.findFirst();
        if (firstModel != null) {
            return firstModel.getLong("count");
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(DBConstants.SELECT).append(DBConstants.START).append(DBConstants.FROM).
                append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES);
        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            result.append(DBConstants.WHERE).append(whereBuilder.toString());
        }
        if (orderByList != null && orderByList.size() > 0) {
            result.append(DBConstants.ORDER_BY);
            for (OrderBy orderBy : orderByList) {
                result.append(orderBy.toString()).append(DBConstants.COMMA);
            }
            result.deleteCharAt(result.length() - 1);
        }
        if (limit > 0) {
            result.append(DBConstants.LIMIT).append(limit);
            result.append(DBConstants.OFFSET).append(offset);
        }
        return result.toString();
    }

    public static class OrderBy {
        private String columnName;
        private boolean desc;

        public OrderBy(String columnName) {
            this.columnName = columnName;
        }

        public OrderBy(String columnName, boolean desc) {
            this.columnName = columnName;
            this.desc = desc;
        }

        @Override
        public String toString() {
            return DBConstants.DOUBLE_QUOTES + columnName + DBConstants.DOUBLE_QUOTES + DBConstants.SPACE + (desc ? DBConstants.DESC : DBConstants.ASC);
        }
    }
}
