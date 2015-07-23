package cn.ffcs.memory;


import java.sql.ResultSet;
import java.sql.SQLException;

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
public class BeanHandler<T> implements ResultSetHandler<T> {
	private final Class<T> type;

	private BeanProcessor convert;
	
	public BeanHandler(Class<T> type) {
		this.type = type;
		this.convert = new BeanProcessor();
	}

	@Override
	public T handle(ResultSet rs) throws SQLException {
		 return rs.next() ? this.convert.toBean(rs, this.type) : null;
	}
}
