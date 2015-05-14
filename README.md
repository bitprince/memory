##持久化工具：Memory介绍

###	1. 概述
#### 1.1 连接、语句和结果集
![](https://github.com/bitprince/memory/blob/master/docs/jdbc.png)  

　　从[JDBC的规范](http://download.oracle.com/otndocs/jcp/jdbc-4_1-mrel-spec/index.html)上看，其对数据访问层有相当简洁的抽象：1、连接(connection) 2、语句(statement)、3结果集(result set)。我们对数据库做的事情无非：**连接数据库，执行语句，拿到结果**。

　　因此，持久化的工具的目的就不言自明了：进一步简化连接的管理、语句的执行、结果集提取等操作。下面从获取结果集、管理连接、语句预处理等3方面逐一阐述工具做了哪些事情。

　　这里提一句，Memory在设计与实现上，都借鉴了Dbutils，其相对于hibernate,mybatis这些庞然大物，已经是一个极其小巧的工具。
但是Memory的类和接口更少（不超过10个），体积更小（只有二十几K），数目和体积都约为dbutils的1/3，却添加了非常实用的功能：

  - 将简单的POJO对象直接持久化到数据库中；
  - 打印运行时出错的SQL语句，其可以直接拷贝到数据库客户端上进行调试；
  - 直截了当的分页查询。 

### 1.2 获取结果集
　　获取结果集，就是把ResultSet转换为目标数据结构，这里使用T（泛型）泛指各种数据结构。我们定义一个接口类来表示这件事情：  
  ``` java
  public interface ResultSetHandler<T> {
  	T handle(ResultSet rs) throws SQLException;
  }
  ```
　　在实际应用中，结果集是某张表的一行或多行数据时，常使用BeanHandler、BeanListHandler或JSONObjectHandler、JSONArrayHandler进行处理，结果集是某一列的一行或多行数据时，使用ColumnHandler、ColumnListHandler进行处理。 
　　
### 1.3 连接的管理

　　将连接的交给外部的数据源(DataSource)进行统一管理。比如使用Tomcat容器自带的数据源。    
　　在Tomcat的context.xml文件配置数据源xxxxx：  
	
``` xml
<?xml version="1.0" encoding="UTF-8"?> 
<Context>
<Resource name="jdbc/test" 
		auth="Container" 
		type="javax.sql.DataSource"
		driverClassName="com.mysql.jdbc.Driver"
        url="jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8"
        username="root" 
		password="root" 
        maxActive="300" 
		maxIdle="30" 
		maxWait="3000"
		validationQuery = "SELECT 1"  
        testWhileIdle = "true"   
        testOnBorrow = "true" 
        timeBetweenEvictionRunsMillis = "3600000"  
        minEvictableIdleTimeMillis = "18000000"  
	 />
</Context>
```
　　在代码中实例化,采用[懒加载单例模式](http://en.wikipedia.org/wiki/Singleton_pattern#Lazy_initialization)该数据源：
``` java
public class MemoryFactory {

	private MemoryFactory() {

	}

	private static class SingletonHolder {
		public static final Memory MEMORY = new Memory(getDataSource());
		//public static final Memory MEMORY = new Memory(new SimpleDataSource());
	}

	public static Memory getInstance() {
		return SingletonHolder.MEMORY;
	}
	
	public static final DataSource getDataSource() {
		try {
			Context context = new InitialContext();
			return (DataSource) context.lookup("java:comp/env/jdbc/test");
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}

```

### 1.4 语句预处理

　　与ResultSetHanlder相互呼应，提供了PreparedStatementHanlder类，这个类提供语句（PreparedStatment）一些辅助性的方法，比如生成运行时的SQL语句、调整日期格式、简化分页语句写法等。这个类在应用中不会直接用到。其作用将隐藏在最重要的一个类Memory之中（与这个工具命名相同）。
  
## 2.	使用 
　　上章从结果集提取、连接管理、语句处理等3个角度介绍了这个工具，本章介绍的Memory类就是对3者的集成，分3节描述Memory开放的API。
### 2.1	命令与查询
　　对数据库所有的操作，可分为两类：命令与查询。命令即更新数据，可进一步分为新增、删除与编辑。
　　
#### 2.1.1 查询(query)
``` java
public <T> T query(StringBuffer sql, ResultSetHandler<T> rsh, List<Object> params);
public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params);
public <T> T query(Connection conn, StringBuffer sql,ResultSetHandler<T> rsh, List<Object> params);
public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params);
```
　　从接口定义可以看出，查询(query)方法，返回结果集，参数名也相似，只是数据结构不同而已：StringBuffer和List一组，String和Array（变长参数）一组，没有传递Connection参数，则表明连接在memory内部管理；有传递Connection参数，则表明连接交给外部程序管理。  

　　在这个层面使用API，就是写SQL语句，几乎没有任何限制，唯一的限制就是在使用BeanHandler与BeanListHandler时，Bean的字段与Table的字段要存在相互匹配，Bean的字段命名风格是驼峰式，Table的字段命名是下划线连接。
　　
#### 2.1.2 命令(update)
 ``` java
 public int update(StringBuffer sql, List<Object> params);
 public int update(String sql, Object... params);
 public int update(Connection conn, StringBuffer sql, List<Object> params);
 public int update(Connection conn, String sql, Object... params);
 
 public int[] batch(String sql, Object[][] params);
 public int[] batch(Connection conn, String sql, Object[][] params);
 ```
　　相对于查询(query)方法，更新(update)方法，没有结果集处理器(ResultSetHandler)的参数以及结果集转化为的对象。但更新有批量更新(batch)的方法，提供批量执行sql语句的功能。


### 2.2 增删改查(CRUD)
　　增删改查，英文缩写为CRUD，这个大家都非常熟悉，使用Create, read, update, delete来做作为接口名称，这样记忆和理解成本最低。
　　  
　　Lifesinger在[《jQuery 为什么优秀兼谈库与框架的设计》](https://github.com/lifesinger/lifesinger.github.com/issues/114)一文中，提到：**在类库界，解决了What，解决了定位问题后，基本上已经决定了生死存亡。 至于 How，也重要但往往不是关键。**
　　　　  
　　本人对此深以为然，所以Memory工具在接口方法名称、类名等的使用上相当节制（数量尽量少），这点也不同于别的持久化工具。


##	3.	多余的废话

##	4. 参考文献



