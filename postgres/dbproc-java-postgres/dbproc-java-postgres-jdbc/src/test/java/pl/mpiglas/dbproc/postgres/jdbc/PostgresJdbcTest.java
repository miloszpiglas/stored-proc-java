/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.mpiglas.dbproc.postgres.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @After
    public void releaseResources() throws SQLException
    {
        pgCon.close();
    }

}
