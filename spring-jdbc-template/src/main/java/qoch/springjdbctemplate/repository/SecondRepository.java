package qoch.springjdbctemplate.repository;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import qoch.springjdbctemplate.model.Second;

import javax.sql.DataSource;

@Repository
public class SecondRepository {
    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public SecondRepository(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("second")
                .usingGeneratedKeyColumns("id");
    }

    public Second save(Second second) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(second);
        Number key = jdbcInsert.executeAndReturnKey(param);
        second.setId(key.longValue());
        return second;
    }
}
