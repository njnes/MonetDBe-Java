package org.monetdb.monetdbe;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonetPreparedStatement extends MonetStatement implements PreparedStatement {
    //Native pointer to C statement
    protected ByteBuffer statementNative;
    private MonetParameterMetaData parameterMetaData;

    protected int nParams;
    protected int[] monetdbeTypes;

    //For executeBatch functions
    private Object[] parameters;
    private List<Object[]> parametersBatch = null;

    public MonetPreparedStatement(MonetConnection conn, String sql) {
        super(conn);

        //nParams, monetdbeTypes and statement Native are set within monetdbe_prepare
        String error_msg = MonetNative.monetdbe_prepare(conn.getDbNative(),sql, this);

        //Failed prepare, destroy statement
        if (this.statementNative == null || error_msg != null) {
            System.out.println("Prepare statement error: " + error_msg);
            try {
                this.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (nParams >= 0) {
            this.parameterMetaData = new MonetParameterMetaData(nParams,monetdbeTypes);
            this.parameters = new Object[nParams];
        }
    }

    //Executes
    @Override
    public boolean execute() throws SQLException {
        checkNotClosed();

        int lastUpdateCount = this.updateCount;
        MonetResultSet lastResultSet = this.resultSet;
        this.resultSet = null;
        this.updateCount = -1;

        //ResultSet and UpdateCount is set within monetdbe_execute
        String error_msg = MonetNative.monetdbe_execute(statementNative,this, false, getMaxRows());
        if (error_msg != null) {
            this.updateCount = lastUpdateCount;
            this.resultSet = lastResultSet;
            throw new SQLException(error_msg);
        }
        else if (this.resultSet!=null) {
            return true;
        }
        else if (this.updateCount >= 0 || this.updateCount == Statement.SUCCESS_NO_INFO){
            return false;
        }
        else {
            throw new SQLException("Error in monetdbe_execute");
        }
    }

    /** override the execute from the Statement to throw an SQLException */
    @Override
    public boolean execute(final String q) throws SQLException {
        throw new SQLException("This method is not available in a PreparedStatement!", "M1M05");
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        if (!execute())
            throw new SQLException("Query did not produce a result set", "M1M19");
        return getResultSet();
    }

    /** override the executeQuery from the Statement to throw an SQLException */
    @Override
    public ResultSet executeQuery(final String q) throws SQLException {
        throw new SQLException("This method is not available in a PreparedStatement!", "M1M05");
    }

    @Override
    public int executeUpdate() throws SQLException {
        if (execute())
            throw new SQLException("Query produced a result set", "M1M17");
        return getUpdateCount();
    }

    /** override the executeUpdate from the Statement to throw an SQLException */
    @Override
    public int executeUpdate(final String q) throws SQLException {
        throw new SQLException("This method is not available in a PreparedStatement!", "M1M05");
    }

    @Override
    public void addBatch() throws SQLException {
        checkNotClosed();
        //This allows us to add multiple "versions" of the same query, using different parameters
        if (parametersBatch == null) {
            parametersBatch = new ArrayList<>();
        }
        parametersBatch.add(parameters);
        parameters = new Object[nParams];
    }

    /** override the addBatch from the Statement to throw an SQLException */
    @Override
    public void addBatch(final String q) throws SQLException {
        throw new SQLException("This method is not available in a PreparedStatement!", "M1M05");
    }

    //Overrides Statement's implementation, which batches different queries instead of different parameters for same query
    @Override
    public int[] executeBatch() throws SQLException {
        if (parametersBatch == null || parametersBatch.isEmpty()) {
            return new int[0];
        }
        int[] counts = new int[parametersBatch.size()];
        int count = -1;
        Object[] cur_batch;

        for (int i = 0; i < parametersBatch.size(); i++) {
            //Get batch of parameters
            cur_batch = parametersBatch.get(i);

            for (int j = 0; j < nParams; j++) {
                //Set each parameter in current batch
                setObject(j+1,cur_batch[j]);
            }

            try {
                count = executeUpdate();
            } catch (SQLException e) {
                //Query returned a resultSet, throw BatchUpdateException
                throw new BatchUpdateException();
            }
            if (count >= 0) {
                counts[i] = count;
            }
            else {
                counts[i] = Statement.SUCCESS_NO_INFO;
            }
        }
        clearBatch();
        return counts;
    }

    //Overrides Statement's implementation, which batches different queries instead of different parameters for same query
    public long[] executeLargeBatch() throws SQLException {
        if (parametersBatch == null || parametersBatch.isEmpty()) {
            return new long[0];
        }
        long[] counts = new long[parametersBatch.size()];
        long count = -1;
        Object[] cur_batch;

        for (int i = 0; i < parametersBatch.size(); i++) {
            //Get batch of parameters
            cur_batch = parametersBatch.get(i);

            for (int j = 0; j < nParams; j++) {
                //Set each parameter in current batch
                setObject(j+1,cur_batch[j]);
            }

            try {
                count = executeLargeUpdate();
            } catch (SQLException e) {
                //Query returned a resultSet, throw BatchUpdateException
                throw new BatchUpdateException();
            }
            if (count >= 0) {
                counts[i] = count;
            }
            else {
                counts[i] = Statement.SUCCESS_NO_INFO;
            }
        }
        clearBatch();
        return counts;
    }

    //Overrides Statement's implementation, which batches different queries instead of different parameters for same query
    @Override
    public void clearBatch() throws SQLException {
        checkNotClosed();
        parametersBatch = null;
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        checkNotClosed();

        long lastUpdateCount = this.largeUpdateCount;
        MonetResultSet lastResultSet = this.resultSet;
        this.resultSet = null;
        this.largeUpdateCount = -1;

        //ResultSet and UpdateCount is set within monetdbe_execute
        String error_msg = MonetNative.monetdbe_execute(statementNative,this, true,getMaxRows());
        if (error_msg != null) {
            this.largeUpdateCount = lastUpdateCount;
            this.resultSet = lastResultSet;
            throw new SQLException(error_msg);
        }
        else if (this.resultSet!=null) {
            throw new SQLException("Query produced a result set", "M1M17");
        }
        else {
            return getLargeUpdateCount();
        }
    }

    //Metadata
    //TODO METADATA
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        //How do I get the result column names and types to construct the ResultSetMetaData object?
        return null;
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return parameterMetaData;
    }

    @Override
    public void clearParameters() throws SQLException {
        checkNotClosed();
        //Verify if I should use the cleanup function or if I should set every parameter to NULL
        //This also cleans up the Prepared Statement
        MonetNative.monetdbe_cleanup_statement(conn.getDbNative(),statementNative);
    }

    //Set objects
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        checkNotClosed();
        if (parameterIndex > nParams) {
            throw new SQLException("parameterIndex is not valid");
        }
        if (x == null) {
            setNull(parameterIndex, targetSqlType);
        }

        if (x instanceof String) {
            setString(parameterIndex,String.valueOf(x));
        }
        else if (x instanceof BigDecimal ||
                x instanceof Byte ||
                x instanceof Short ||
                x instanceof Integer ||
                x instanceof Long ||
                x instanceof Float ||
                x instanceof Double) {
            Number num = (Number) x;
            setObjectNum(parameterIndex,targetSqlType,num,x,scaleOrLength);
        }
        else if (x instanceof Boolean) {
            boolean bool = (Boolean) x;
            setObjectBool(parameterIndex,targetSqlType,bool);
        }
        else if (x instanceof BigInteger) {
            BigInteger num = (BigInteger)x;
            switch (targetSqlType) {
                case Types.BIGINT:
                    setLong(parameterIndex, num.longValue());
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    setString(parameterIndex, num.toString());
                    break;
                default:
                    throw new SQLException("Conversion not allowed", "M1M05");
            }
        }
        else if (x instanceof byte[]) {
            switch (targetSqlType) {
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    setBytes(parameterIndex, (byte[]) x);
                    break;
                default:
                    throw new SQLException("Conversion not allowed", "M1M05");
            }
        }
        else if (x instanceof java.sql.Date ||
                x instanceof Timestamp ||
                x instanceof Time ||
                x instanceof Calendar ||
                x instanceof java.util.Date ||
                x instanceof java.time.LocalDate ||
                x instanceof java.time.LocalTime ||
                x instanceof java.time.LocalDateTime) {
            setObjectDate(parameterIndex,targetSqlType,x);
        }
        else if (x instanceof MonetBlob || x instanceof Blob) {
            setBlob(parameterIndex, (Blob) x);
        }
        else if (x instanceof java.net.URL) {
            setURL(parameterIndex,(URL) x);
        }
    }

    public void setObjectBool (int parameterIndex, int sqlType, Boolean bool) throws SQLException {
        switch (sqlType) {
            case Types.TINYINT:
                setByte(parameterIndex, (byte)(bool ? 1 : 0));
                break;
            case Types.SMALLINT:
                setShort(parameterIndex, (short)(bool ? 1 : 0));
                break;
            case Types.INTEGER:
                setInt(parameterIndex, (bool ? 1 : 0));  // do not cast to (int) as it generates a compiler warning
                break;
            case Types.BIGINT:
                setLong(parameterIndex, (long)(bool ? 1 : 0));
                break;
            case Types.REAL:
            case Types.FLOAT:
                setFloat(parameterIndex, (float)(bool ? 1.0 : 0.0));
                break;
            case Types.DOUBLE:
                setDouble(parameterIndex, (bool ? 1.0 : 0.0));  // do no cast to (double) as it generates a compiler warning
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
            {
                final BigDecimal dec;
                try {
                    dec = new BigDecimal(bool ? 1.0 : 0.0);
                } catch (NumberFormatException e) {
                    throw new SQLException("Internal error: unable to create template BigDecimal: " + e.getMessage(), "M0M03");
                }
                setBigDecimal(parameterIndex, dec);
            } break;
            case Types.BIT:
            case Types.BOOLEAN:
                setBoolean(parameterIndex, bool);
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                setString(parameterIndex, bool.toString());
                break;
            default:
                throw new SQLException("Conversion not allowed", "M1M05");
        }
    }

    public void setObjectNum (int parameterIndex, int sqlType, Number num, Object x, int scale) throws SQLException {
        switch (sqlType) {
            case Types.TINYINT:
                setByte(parameterIndex, num.byteValue());
                break;
            case Types.SMALLINT:
                setShort(parameterIndex, num.shortValue());
                break;
            case Types.INTEGER:
                setInt(parameterIndex, num.intValue());
                break;
            case Types.BIGINT:
                setLong(parameterIndex, num.longValue());
                break;
            case Types.REAL:
            case Types.FLOAT:
                setFloat(parameterIndex, num.floatValue());
                break;
            case Types.DOUBLE:
                setDouble(parameterIndex, num.doubleValue());
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                if (x instanceof BigDecimal) {
                    setBigDecimal(parameterIndex, (BigDecimal) x);
                } else {
                    if (scale == 0) {
                        setBigDecimal(parameterIndex, new BigDecimal(num.doubleValue()));
                    }
                    else {
                        setBigDecimal(parameterIndex, new BigDecimal(num.doubleValue()).setScale(scale,java.math.RoundingMode.HALF_UP));
                    }
                }
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                if (num.doubleValue() != 0.0) {
                    setBoolean(parameterIndex, true);
                } else {
                    setBoolean(parameterIndex, false);
                }
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                setString(parameterIndex, num.toString());
                break;
            default:
                throw new SQLException("Conversion not allowed", "M1M05");
        }
    }

    public void setObjectDate (int parameterIndex, int sqlType, Object x) throws SQLException {
        switch (sqlType) {
            case Types.DATE:
                if (x instanceof java.sql.Date) {
                    setDate(parameterIndex, (java.sql.Date) x);
                } else if (x instanceof Timestamp) {
                    setDate(parameterIndex, new java.sql.Date(((Timestamp)x).getTime()));
                } else if (x instanceof java.util.Date) {
                    setDate(parameterIndex, new java.sql.Date(
                            ((java.util.Date)x).getTime()));
                } else if (x instanceof Calendar) {
                    setDate(parameterIndex, new java.sql.Date(
                            ((Calendar)x).getTimeInMillis()));
                } else if (x instanceof LocalDate) {
                    setDate(parameterIndex, Date.valueOf((LocalDate) x));
                } else {
                    throw new SQLException("Conversion not allowed", "M1M05");
                }
                break;
            case Types.TIME:
                if (x instanceof Time) {
                    setTime(parameterIndex, (Time)x);
                } else if (x instanceof Timestamp) {
                    setTime(parameterIndex, new Time(((Timestamp)x).getTime()));
                } else if (x instanceof java.util.Date) {
                    setTime(parameterIndex, new java.sql.Time(
                            ((java.util.Date)x).getTime()));
                } else if (x instanceof Calendar) {
                    setTime(parameterIndex, new java.sql.Time(
                            ((Calendar)x).getTimeInMillis()));
                } else if (x instanceof LocalTime) {
                    setTime(parameterIndex, Time.valueOf((LocalTime) x));
                } else {
                    throw new SQLException("Conversion not allowed", "M1M05");
                }
                break;
            case Types.TIMESTAMP:
                if (x instanceof Timestamp) {
                    setTimestamp(parameterIndex, (Timestamp)x);
                } else if (x instanceof java.sql.Date) {
                    setTimestamp(parameterIndex, new Timestamp(((java.sql.Date)x).getTime()));
                } else if (x instanceof java.util.Date) {
                    setTimestamp(parameterIndex, new java.sql.Timestamp(
                            ((java.util.Date)x).getTime()));
                } else if (x instanceof Calendar) {
                    setTimestamp(parameterIndex, new java.sql.Timestamp(
                            ((Calendar)x).getTimeInMillis()));
                } else if (x instanceof LocalDateTime) {
                    setTimestamp(parameterIndex, Timestamp.valueOf((LocalDateTime) x));
                } else {
                    throw new SQLException("Conversion not allowed", "M1M05");
                }
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                setString(parameterIndex, x.toString());
                break;
            default:
                throw new SQLException("Conversion not allowed", "M1M05");
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        checkNotClosed();
        int monettype = MonetTypes.getMonetTypeFromSQL(sqlType);
        MonetNative.monetdbe_bind_null(conn.getDbNative(),monettype,statementNative,parameterIndex-1);
        parameters[parameterIndex-1] = null;
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_bool(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_byte(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_short(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_int(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_long(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_float(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_double(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        checkNotClosed();
        Number numberBind;
        //Check unscaled value data type
        BigInteger unscaled = x.unscaledValue();
        int type;
        int bitLenght = unscaled.bitLength();

        if (bitLenght <= 8) {
            numberBind = unscaled.byteValueExact();
            type = 1;
        }
        else if (bitLenght <= 16) {
            numberBind = unscaled.shortValueExact();
            type = 2;
        }
        else if (bitLenght <= 32) {
            numberBind = unscaled.intValueExact();
            type = 3;
        }
        else if (bitLenght <= 64) {
            numberBind = unscaled.longValueExact();
            type = 4;
        }
        else {
            numberBind = unscaled;
            type = 5;
        }
        //TODO Implement the C function
        MonetNative.monetdbe_bind_decimal(statementNative,numberBind,type,x.scale(),parameterIndex-1);
        parameters[parameterIndex-1] = x;
    }

    //TODO Implement the C function
    public void setHugeInteger(int parameterIndex, BigInteger x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_hugeint(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        checkNotClosed();
        MonetNative.monetdbe_bind_string(statementNative,parameterIndex-1,x);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        checkNotClosed();
        LocalDate localDate = x.toLocalDate();
        MonetNative.monetdbe_bind_date(statementNative,parameterIndex-1,(short)localDate.getYear(),(byte)localDate.getMonthValue(),(byte)localDate.getDayOfMonth());
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        checkNotClosed();
        LocalTime localTime = x.toLocalTime();
        MonetNative.monetdbe_bind_time(statementNative,parameterIndex-1,localTime.getHour(),localTime.getMinute(),localTime.getSecond(),localTime.getNano()*1000);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        checkNotClosed();
        LocalDateTime localDateTime = x.toLocalDateTime();
        MonetNative.monetdbe_bind_timestamp(statementNative,parameterIndex-1,localDateTime.getYear(),localDateTime.getMonthValue(),localDateTime.getDayOfMonth(),localDateTime.getHour(),localDateTime.getMinute(),localDateTime.getSecond(),localDateTime.getNano()*1000);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        MonetNative.monetdbe_bind_blob(statementNative,parameterIndex-1,x,x.length);
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        checkNotClosed();
        setString(parameterIndex,x.toString());
        parameters[parameterIndex-1] = x;
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        checkNotClosed();
        long size = x.length();
        if (size > 0) {
            byte[] blob_data = x.getBytes(1,(int) size);
            MonetNative.monetdbe_bind_blob(statementNative,parameterIndex-1,blob_data,x.length());
            parameters[parameterIndex-1] = x;
        }
        else {
            setNull(parameterIndex,Types.BLOB);
        }
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        checkNotClosed();
        long size = x.length();
        if (size > 0) {
            MonetNative.monetdbe_bind_string(statementNative,parameterIndex-1,x.toString());
            parameters[parameterIndex-1] = x;
        }
        else {
            setNull(parameterIndex,Types.BLOB);
        }
    }

    @Override
    //Imported from default driver implementation
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        if (reader == null) {
            setNull(parameterIndex, -1);
            return;
        }

        // Some buffer. Size of 8192 is default for BufferedReader, so...
        final int size = 8192;
        final char[] arr = new char[size];
        final StringBuilder buf = new StringBuilder(size * 32);
        try {
            int numChars;
            while ((numChars = reader.read(arr, 0, size)) > 0) {
                buf.append(arr, 0, numChars);
            }
            setString(parameterIndex, buf.toString());
        } catch (IOException e) {
            throw new SQLException("failed to read from stream: " + e.getMessage(), "M1M25");
        }
    }

    @Override
    //Imported from default driver implementation
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        if (reader == null) {
            setNull(parameterIndex, -1);
            return;
        }
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new SQLException("Invalid length value: " + length, "M1M05");
        }

        // simply serialise the Reader data into a large buffer
        final CharBuffer buf = CharBuffer.allocate((int)length); // have to down cast
        try {
            reader.read(buf);
            // We have to rewind the buffer, because otherwise toString() returns "".
            buf.rewind();
            setString(parameterIndex, buf.toString());
        } catch (IOException e) {
            throw new SQLException("failed to read from stream: " + e.getMessage(), "M1M25");
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        //Ignore typeName parameter, no support for Ref and UDFs in monetdbe
        setNull(parameterIndex,sqlType);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        //Because MonetDBe doesn't support timezones, the Calendar object is ignored
        setDate(parameterIndex,x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        //Because MonetDBe doesn't support timezones, the Calendar object is ignored
        setTime(parameterIndex,x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        //Because MonetDBe doesn't support timezones, the Calendar object is ignored
        setTimestamp(parameterIndex,x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        setObject(parameterIndex,x,MonetTypes.getSQLIntFromSQLName(targetSqlType.getName()),scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        setObject(parameterIndex,x,targetSqlType,0);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setObject(parameterIndex,x,targetSqlType,0);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        int sqltype = MonetTypes.getDefaultSQLTypeForClass(x.getClass());
        setObject(parameterIndex,x,sqltype,0);
    }

    //Set other objects (Ref, Array, NString, NClob, XML)
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob(int parameterIndex, InputStream inputStream)");
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBlob(int parameterIndex, InputStream inputStream, long lenght)");
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setString(parameterIndex,value);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRef");
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("setSQLXML");
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setArray");
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setRowId");
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNClob");
    }

    //Set stream object
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        setClob(parameterIndex, reader, (long)length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        setClob(parameterIndex, reader, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setClob(parameterIndex, reader);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream");
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setAsciiStream");
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setUnicodeStream");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream");
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("setBinaryStream");
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setCharacterStream(parameterIndex,value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        setCharacterStream(parameterIndex,value,length);
    }
}
