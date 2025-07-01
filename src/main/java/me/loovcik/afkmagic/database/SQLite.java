package me.loovcik.afkmagic.database;

import me.loovcik.afkmagic.AFKMagic;
import me.loovcik.core.ChatHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("CallToPrintStackTrace")
public class SQLite {
	private final AFKMagic plugin;
	private Connection connection;

	public Connection getConnection() {
		if (connection == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder()+"/statistics.db");
				createTable();
			}
			catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}

	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed())
				connection.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createTable() {
		try {
			Connection connection = getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS stats (uuid text PRIMARY KEY, userName VARCHAR(32), totalAfkTime INTEGER, totalKicks INTEGER, altsKicks INTEGER, altsDetections INTEGER)");
			preparedStatement.execute();

		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public SQLite(AFKMagic plugin) {
		this.plugin = plugin;
	}
}