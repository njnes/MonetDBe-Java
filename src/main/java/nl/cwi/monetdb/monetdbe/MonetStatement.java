package nl.cwi.monetdb.monetdbe;

import java.nio.ByteBuffer;
import java.sql.*;

public class MonetStatement implements Statement {
    private MonetConnection conn;
    private int updateCount;
    private int lastAffectedRows;
    private MonetResultSet resultSet;

    public MonetStatement(MonetConnection conn) {
        this.conn = conn;
        this.updateCount = -1;
        this.resultSet = null;
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        MonetResultSet resultSet = MonetNative.monetdbe_query(conn.getConnection(),sql,this);
        if(resultSet!=null) {
            System.out.println(resultSet);
            return true;
        }
        else {
            return false;
        }
        /*NativeResult resultValues = MonetNative.monetdbe_query(conn.getConnection(),sql,this);

        ByteBuffer nativeResultSet = resultValues.getResultSet();
        int affectedRows = resultValues.getAffectedRows();

        if (nativeResultSet != null) {
            this.updateCount = -1;
            this.resultSet = new MonetResultSet(this, nativeResultSet, resultValues.getNrows(), resultValues.getNcols());
            System.out.println("Query result set w/ Nrows: " + resultValues.getNrows() + "\nNcols:" + resultValues.getNcols());
            return true;
        }
        else if (affectedRows > 0){
            System.out.println("Update operation with " + affectedRows + " affected rows.");
            this.updateCount = affectedRows;
            return false;
        }
        else {
            System.out.println("Operation had no results or updates.");
            return false;
        }*/
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return this.updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return resultSet;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return 0;
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {

    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        return new long[0];
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {

    }

    @Override
    public int getMaxRows() throws SQLException {
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {

    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {

    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {

    }

    @Override
    public void cancel() throws SQLException {

    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public void setCursorName(String name) throws SQLException {

    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException {
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException {

    }

    @Override
    public void clearBatch() throws SQLException {

    }

    @Override
    public int[] executeBatch() throws SQLException {
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {

    }

    @Override
    public boolean isPoolable() throws SQLException {
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }
}
