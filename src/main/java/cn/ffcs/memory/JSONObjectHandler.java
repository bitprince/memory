package cn.ffcs.memory;

import com.alibaba.fastjson.JSONObject;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 *
 * @Description:
 * @Copyright: Copyright (c) 2013 FFCS All Rights Reserved
 * @Company: 北京福富软件有限公司
 * @author 黄君毅 2013-08-05
 * @version 1.00.00
 * @history:
 *
 */
public class JSONObjectHandler implements ResultSetHandler<JSONObject> {

	private SimpleDateFormat dateFormat;

	public JSONObjectHandler() {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * 结果集不包含数据时，返回空的JSON对象
	 */
	@Override
	public JSONObject handle(ResultSet rs) {
		try {
			JSONObject object = new JSONObject();

			if (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					String columnName = rsmd.getColumnLabel(i);
					Object value = rs.getObject(columnName);
					if (value == null)
						value = "";

					if (value instanceof Date) {
						value = rs.getTimestamp(columnName);
						value = dateFormat.format((Date) value);
					}

					if (value instanceof Clob) {
						Clob clob = (Clob) value;
						value = clob
								.getSubString((long) 1, (int) clob.length());
					}
					object.put(underscore2Camel(columnName), value);
				}
			}

			return object;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public String underscore2Camel(String underscore) {
		StringBuffer buf = new StringBuffer();
		underscore = underscore.toLowerCase();
		Matcher m = Pattern.compile("_([a-z])").matcher(underscore);
		while (m.find()) {
			m.appendReplacement(buf, m.group(1).toUpperCase());
		}
		return m.appendTail(buf).toString();
	}
}
