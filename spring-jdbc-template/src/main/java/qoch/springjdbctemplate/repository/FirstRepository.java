package qoch.springjdbctemplate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import qoch.springjdbctemplate.model.First;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
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
        log.info("first = {}", first);
        return first;
    }

    public int updateStatus(First first) {
        String sql = "update first set status = :status where id = :id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("status", first.getStatus().toString())
                .addValue("id", first.getId());
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

    public List<First> findAll() {
        String sql = "select * from first";
        return template.query(sql, firstRowMapper());
    }

    public int countByIdAndStatus(Long id, First.Status status) {
        String sql = "select count(*) from first where id = :id and status = :status";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status.toString());

        return template.queryForObject(sql, param, Integer.class);
    }

    private RowMapper<First> firstRowMapper() {
        return BeanPropertyRowMapper.newInstance(First.class);
    }
}
