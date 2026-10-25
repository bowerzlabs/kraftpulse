package com.bowerzlabs.kraftpulse.sqlite.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class SingleFileSqliteDataSource implements DataSource {

    private final String jdbcUrl;

    public SingleFileSqliteDataSource(String filePath) {
        String cleanPath = (filePath != null && !filePath.isBlank()) ? filePath : "./kraftpulse-data.db";

        if (cleanPath.startsWith("jdbc:sqlite:")) {
            cleanPath = cleanPath.substring("jdbc:sqlite:".length());
        }

        this.jdbcUrl = "jdbc:sqlite:" + cleanPath;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found on classpath", e);
        }
        return DriverManager.getConnection(jdbcUrl);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public PrintWriter getLogWriter() { return null; }
    @Override
    public void setLogWriter(PrintWriter out) {}
    @Override
    public void setLoginTimeout(int seconds) {}
    @Override
    public int getLoginTimeout() { return 0; }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}