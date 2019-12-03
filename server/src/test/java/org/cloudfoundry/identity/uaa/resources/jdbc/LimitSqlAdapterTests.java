package org.cloudfoundry.identity.uaa.resources.jdbc;

import org.cloudfoundry.identity.uaa.test.JdbcTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LimitSqlAdapterTests extends JdbcTestBase {

    @Before
    public void setUpLimitSqlAdapterTests() {
        jdbcTemplate.update("create table delete_top_rows_test (id varchar(10), expires integer, payload varchar(20))");
        jdbcTemplate.update("insert into delete_top_rows_test values (?,?,?)", "X", 1, "some-data");
        jdbcTemplate.update("insert into delete_top_rows_test values (?,?,?)", "M", 2, "some-data");
        jdbcTemplate.update("insert into delete_top_rows_test values (?,?,?)", "K", 3, "some-data");
        jdbcTemplate.update("insert into delete_top_rows_test values (?,?,?)", "D", 4, "some-data");
        jdbcTemplate.update("insert into delete_top_rows_test values (?,?,?)", "A", 5, "some-data");
    }

    @After
    public void tearDown() {
        jdbcTemplate.update("drop table delete_top_rows_test");
    }

    @Test
    public void revocableTokenDeleteSyntax() {
        //tests that the query succeed, nothing else
        String query = limitSqlAdapter.getDeleteExpiredQuery("revocable_tokens", "token_id", "expires_at", 500);
        jdbcTemplate.update(query, System.currentTimeMillis());
    }

    @Test
    public void deleteTopRows() {
        assertEquals(1, (int) jdbcTemplate.queryForObject("select count(*) from delete_top_rows_test where id = 'X'", Integer.class));
        assertEquals(1, (int) jdbcTemplate.queryForObject("select count(*) from delete_top_rows_test where id = 'A'", Integer.class));
        jdbcTemplate.update(
                limitSqlAdapter.getDeleteExpiredQuery(
                        "delete_top_rows_test",
                        "id",
                        "expires",
                        2
                ),
                5
        );
        assertEquals(1, (int) jdbcTemplate.queryForObject("select count(*) from delete_top_rows_test where id = 'K'", Integer.class));
        assertEquals(1, (int) jdbcTemplate.queryForObject("select count(*) from delete_top_rows_test where id = 'D'", Integer.class));
        assertEquals(1, (int) jdbcTemplate.queryForObject("select count(*) from delete_top_rows_test where id = 'A'", Integer.class));
        assertEquals(3, (int) jdbcTemplate.queryForObject("select count(*) from delete_top_rows_test", Integer.class));
    }
}