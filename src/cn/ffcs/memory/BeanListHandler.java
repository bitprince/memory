package cn.ffcs.memory;


import java.sql.ResultSet;
import java.sql.SQLException;
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
public class BeanListHandler<T> implements ResultSetHandler<List<T>> {

	private final Class<T> type;

	private BeanProcessor convert = new BeanProcessor();
   
	public BeanListHandler(Class<T> type) {
		this.type = type;
		this.convert = new BeanProcessor();
    }

    @Override
    public List<T> handle(ResultSet rs) throws SQLException {
        return this.convert.toBeanList(rs, type);
    }
}