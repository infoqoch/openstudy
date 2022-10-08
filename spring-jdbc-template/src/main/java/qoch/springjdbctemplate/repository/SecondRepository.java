package qoch.springjdbctemplate.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import qoch.springjdbctemplate.model.First;
import qoch.springjdbctemplate.model.Second;

import javax.sql.DataSource;
import java.util.List;

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

    public List<Second> findByFirstId(Long firstId) {

        String sql = "select * from second where first_id = :firstId";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("firstId", firstId);

        return template.query(sql, param, secondRowMapper());
    }

    private RowMapper<Second> secondRowMapper() {
        return BeanPropertyRowMapper.newInstance(Second.class);
    }
}
