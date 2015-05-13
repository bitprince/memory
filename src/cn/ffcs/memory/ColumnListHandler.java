package cn.ffcs.memory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * 
 * @Description:
 * @Copyright: Copyright (c) 2013 FFCS All Rights Reserved
 * @Company: 北京福富软件有限公司
 * @author 黄君毅 2013-4-12
 * @version 1.00.00
 * @history:
 * 
 */
public class ColumnListHandler<T> implements ResultSetHandler<List<T>> {
	private final int columnIndex;
	private final String columnName;
	private final Class<T> type;

	public ColumnListHandler(Class<T> type) {
		this(1, null, type);
	}

	public ColumnListHandler(int columnIndex, Class<T> type) {
		this(columnIndex, null, type);
	}

	public ColumnListHandler(String columnName, Class<T> type) {
		this(1, columnName, type);
	}

	private ColumnListHandler(int columnIndex, String columnName, Class<T> type) {
		this.columnIndex = columnIndex;
		this.columnName = columnName;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> handle(ResultSet rs) throws SQLException {
		List<T> rows = new ArrayList<T>();
		while (rs.next()) {
			rows.add((T) processColumn(rs));
		}
		return rows;
	}

	private Object processColumn(ResultSet rs) throws SQLException {
		int index;
		if (this.columnName == null) {
			index = columnIndex;
		} else {
			index = rs.findColumn(columnName);
		}

		if (!type.isPrimitive() && rs.getObject(index) == null) {
			return null;
		}

		if (type.equals(String.class)) {
			return rs.getString(index);

		} else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
			return Integer.valueOf(rs.getInt(index));

		} else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
			return Boolean.valueOf(rs.getBoolean(index));

		} else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
			return Long.valueOf(rs.getLong(index));

		} else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
			return Double.valueOf(rs.getDouble(index));

		} else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
			return Float.valueOf(rs.getFloat(index));

		} else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
			return Short.valueOf(rs.getShort(index));

		} else if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
			return Byte.valueOf(rs.getByte(index));
		} else if (type.equals(Timestamp.class)) {
			return rs.getTimestamp(index);
		} else if (type.equals(Date.class)) {
			return rs.getTimestamp(index);
		} else if (type.equals(SQLXML.class)) {
			return rs.getSQLXML(index);
		} else {
			return rs.getObject(index);
		}
	}
}
