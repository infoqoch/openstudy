package qoch.springjdbctemplate.domain;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;

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

    public int countByFirstId(Long firstId) {
        String sql = "select count(*) from second where first_id = :first_id";
        Map<String, Object> param = Map.of("first_id", firstId);
        return template.queryForObject(sql, param, Integer.class);
    }

    public int countByFirstIdForUpdate(Long firstId) {
        String sql = "select count(*) from second where first_id = :first_id FOR UPDATE";
        Map<String, Object> param = Map.of("first_id", firstId);
        return template.queryForObject(sql, param, Integer.class);
    }
}
