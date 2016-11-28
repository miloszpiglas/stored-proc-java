/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.mpiglas.dbproc.postgres.jdbc;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Calling PostgreSql stored procedures with plain JDBC.
 *
 * @author Milosz Piglas
 */
public class PostgresJdbcTest
{

    @BeforeClass
    public static void loadClass() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }
    private Connection pgCon;

    @Before
    public void setup() throws IOException, SQLException
    {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("jdbc-connection.properties"));
        pgCon = DriverManager.getConnection(
                props.getProperty("url"), props.getProperty("user"), props.getProperty("password"));
    }

    /**
     * Executes procedure with SELECT statement. Procedure returns sum of values
     * to JDBC result set.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReturnSingleValue() throws SQLException
    {
        try (PreparedStatement ps = pgCon.prepareStatement("SELECT * from num_sum(?, ?)"))
        {
            ps.setInt(1, 100);
            ps.setInt(2, 11);
            try (ResultSet resultSet = ps.executeQuery())
            {
                Assertions.assertThat(resultSet.next()).isTrue();
                int sum = resultSet.getInt(1);
                Assertions.assertThat(sum).isEqualTo(111);
            }
        }
    }

    /**
     * Procedure called with {@link CallableStatement} syntax. Sum of values is
     * registered as out parameter
     *
     * @throws SQLException
     */
    @Test
    public void shouldCallProcedureReturnSingleValue() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{? = call num_sum(?, ?)}"))
        {
            proc.registerOutParameter(1, Types.INTEGER);
            proc.setInt(2, 100);
            proc.setInt(3, 11);
            proc.execute();
            int sum = proc.getInt(1);
            Assertions.assertThat(sum).isEqualTo(111);
        }
    }

    /**
     * After calling procedure with {@link CallableStatement} syntax sum of
     * values is read from result set.
     *
     * @throws SQLException
     */
    @Test
    public void shouldCallProcedureReturnSingleValueWithResultSet() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{call num_sum(?, ?)}"))
        {
            proc.setInt(1, 100);
            proc.setInt(2, 11);
            try (ResultSet resultSet = proc.executeQuery())
            {
                Assertions.assertThat(resultSet.next()).isTrue();
                int sum = resultSet.getInt(1);
                Assertions.assertThat(sum).isEqualTo(111);
            }
        }
    }

    /**
     * Procedure with two input and two output parameteres is called with {@link CallableStatement
     * } and result of calculation is read from result set.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReadOutValuesFromResultSet() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{call modmul(?, ?)}"))
        {
            proc.setInt(1, 10);
            proc.setInt(2, 3);
            try (ResultSet resultSet = proc.executeQuery())
            {
                Assertions.assertThat(resultSet.next()).isTrue();
                int result = resultSet.getInt(1);
                int modulo = resultSet.getInt(2);
                Assertions.assertThat(result).isEqualTo(3);
                Assertions.assertThat(modulo).isEqualTo(1);
            }
        }
    }

    /**
     * Procedure with two input and two output parameteres is called with {@link CallableStatement
     * } and result of calculation is read from registered output parameters.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReadOutValuesFromParameters() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{call modmul(?, ?, ?, ?)}"))
        {
            proc.setInt(1, 10);
            proc.setInt(2, 3);
            proc.registerOutParameter(3, Types.INTEGER);
            proc.registerOutParameter(4, Types.INTEGER);
            proc.execute();

            int result = proc.getInt(3);
            int modulo = proc.getInt(4);
            Assertions.assertThat(result).isEqualTo(3);
            Assertions.assertThat(modulo).isEqualTo(1);
        }
    }

    /**
     * Procedure with one out parameter read like return statement.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReadOutParameterLikeReturn() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{? = call out_text()}"))
        {
            proc.registerOutParameter(1, Types.VARCHAR);
            proc.execute();
            String txt = proc.getString(1);
            Assertions.assertThat(txt).isEqualTo("out_text");
        }
    }

    /**
     * Procedure writes value to registered output parameter.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReadSingleOutputParam() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{call out_text(?)}"))
        {
            proc.registerOutParameter(1, Types.VARCHAR);
            proc.execute();
            String txt = proc.getString(1);
            Assertions.assertThat(txt).isEqualTo("out_text");
        }
    }

    /**
     * Value of output parameter read from result set.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReadSingleOutValueFromResultSet() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{call out_text()}"))
        {
            try (ResultSet resultSet = proc.executeQuery())
            {
                Assertions.assertThat(resultSet.next()).isTrue();
                String txt = resultSet.getString(1);
                Assertions.assertThat(txt).isEqualTo("out_text");
            }
        }
    }

    /**
     * List of integers read from result set.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReadListOfIntegersFromResultSet() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{call int_set(?)}"))
        {
            proc.setInt(1, 10);
            int index = 0;
            try (ResultSet resultSet = proc.executeQuery())
            {
                while (resultSet.next())
                {
                    index = resultSet.getRow();
                    Assertions.assertThat(resultSet.getInt(1)).isEqualTo(index * 100);
                }
            }
            Assertions.assertThat(index).isEqualTo(10);
        }
    }

    /**
     * List of records read from result set.
     *
     * @throws SQLException
     */
    @Test
    public void shouldReadListOfRecordsFromResultSet() throws SQLException
    {
        try (CallableStatement proc = pgCon.prepareCall("{call gen_rows(?)}"))
        {
            proc.setInt(1, 10);
            int index = 0;
            try (ResultSet resultSet = proc.executeQuery())
            {
                while (resultSet.next())
                {
                    index = resultSet.getRow();
                    Assertions.assertThat(resultSet.getString(1)).isEqualTo("ROW" + (index - 1));
                    Assertions.assertThat(resultSet.getInt(2)).isEqualTo(index);
                }
            }
            Assertions.assertThat(index).isEqualTo(10);
        }
    }

    @After
    public void releaseResources() throws SQLException
    {
        pgCon.close();
    }

}
