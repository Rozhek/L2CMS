package studio.lineage2.cms.repository;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import studio.lineage2.cms.model.Theme;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 Created by iRock
 29.10.2015
 */
@Component
public class ForumRepository
{
	@Value("${jdbc.forum.url}")
	private String url;

	@Value("${jdbc.forum.login}")
	private String login;

	@Value("${jdbc.forum.password}")
	private String password;

	@Value("${jdbc.forum.count}")
	private int count;

	@Value("${forumType}")
	private String forumType;

	private static List<Theme> themes = new ArrayList<>();

	public static List<Theme> getThemes()
	{
		return themes;
	}

	private DataSource dataSource;

	@Scheduled(initialDelay = 1000, fixedRate = 60000)
	public void load()
	{
		if(dataSource == null)
		{
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, login, password);
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
			ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
			poolableConnectionFactory.setPool(connectionPool);
			dataSource = new PoolingDataSource<>(connectionPool);
		}

		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;

		try
		{
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			switch(forumType)
			{
				case "ipb4":
				{
					rset = stmt.executeQuery("SELECT topics.tid, topics.title, topics.last_post, topics.last_poster_id, topics.last_poster_name, 0, 'male' FROM forums_topics as topics ORDER BY last_post DESC LIMIT " + count);
					themes.clear();
				}
				break;
				case "xenforo":
				{
					rset = stmt.executeQuery("SELECT xf_thread.thread_id, xf_thread.title, xf_thread.last_post_date, xf_thread.last_post_user_id, xf_thread.last_post_username, ifnull(xf_user.avatar_date,0), IF(xf_user.gender is NULL or xf_user.gender = '', 'male', xf_user.gender) FROM xf_thread left join xf_user on xf_user.user_id=xf_thread.last_post_user_id where xf_thread.discussion_state not in ('deleted','moderated') and xf_thread.node_id not in (select content_id from xf_permission_entry_content where user_group_id=1 and user_id = 0 and content_type='node' and permission_value in('reset','deny'))ORDER BY `xf_thread`.`last_post_date` DESC LIMIT " + count);
					themes.clear();
				}
				break;
			}
			if(rset != null)
			{
				themes.clear();
				while(rset.next())
				{
					themes.add(new Theme(rset.getInt(1), rset.getString(2), rset.getInt(3), rset.getInt(4), rset.getString(5), rset.getLong(6), rset.getString(7)));
				}
			}
		}
		catch(Exception ignored)
		{
		}
		finally
		{
			try
			{
				if(rset != null)
				{
					rset.close();
				}
			}
			catch(Exception ignored)
			{
			}
			try
			{
				if(stmt != null)
				{
					stmt.close();
				}
			}
			catch(Exception ignored)
			{
			}
			try
			{
				if(conn != null)
				{
					conn.close();
				}
			}
			catch(Exception ignored)
			{
			}
		}
	}
}