package qoch.springjdbctemplate.domain;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

@Repository
public class FirstRepository {
    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public FirstRepository(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("first")
                .usingGeneratedKeyColumns("id");
    }

    public First save(First first){
        SqlParameterSource param = new BeanPropertySqlParameterSource(first);
        Number key = jdbcInsert.executeAndReturnKey(param);
        first.setId(key.longValue());
        return first;
    }

    public int updateStatusNewToDone(Long id) {
        String sql = "update first set status = 'DONE' where id = :id and status = 'NEW'";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id);
        return template.update(sql, param);
    }

    public Optional<First> findById(Long id) {
        String sql = "select * from first where id = :id";
        try {
            Map<String, Object> param = Map.of("id", id);
            First first = template.queryForObject(sql, param, firstRowMapper());
            return Optional.of(first);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int countByIdAndStatus(First.Status status, Long id) {
        String sql = "select count(*) from first where id = :id and status = :status";
        Map<String, Object> param = Map.of("id", id, "status", status.toString());
        return template.queryForObject(sql, param, Integer.class);
    }

    public int countByIdAndStatusForUpdate(First.Status status, Long id) {
        String sql = "select count(*) from first where id = :id and status = :status FOR UPDATE";
        Map<String, Object> param = Map.of("id", id, "status", status.toString());
        return template.queryForObject(sql, param, Integer.class);
    }

    private RowMapper<First> firstRowMapper() {
        return BeanPropertyRowMapper.newInstance(First.class);
    }
}
