package qoch.auth.template;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 데이터를 생성한 어드민과 슈퍼 어드민만 접근권한이 있는 저장소가 있다고 가정한다.
// 어드민의 ID값과 요청한 어드민의 정보를 기반으로 저장소에 데이터 접근을 한다.
// 권한과 아이디에 따라 추출 가능 여부와 추출된 데이터를 결정한다.
class AdminAuthBiFunctionTest {
    final Repository repo = Repository.sample();

    final AdminAuthBiFunction<Admin, Long, List<String>> findListById =
            new AdminAuthBiFunction.Builder<Admin, Long, List<String>>()
                    .supers((a, id) -> repo.findById(id))
                    .theSameId((a, id) -> repo.findById(a.getId()))
                    .elses((a, id) -> {
                        throw new IllegalArgumentException("허용되지 않은 접근입니다. admin [" + a.getId() + "] => request [" + id + "]");
                    })
                    .build();

    @Test
    void when_super_then_success() {
        final Admin supers = new Admin(999L, Admin.Type.SUPER);
        final List<String> found = findListById.compareTo(supers, 1L);
        assertThat(found.size()).isEqualTo(2);
        assertThat(found).isEqualTo(repo.findById(1L));
    }

    @Test
    void when_register_then_success() {
        final Admin author = new Admin(1L, Admin.Type.BASIC);
        final List<String> findMine = findListById.compareTo(author, 1L);
        assertThat(findMine.size()).isEqualTo(2);
        assertThat(findMine).isEqualTo(repo.findById(1L));
    }

    @Test
    void when_other_then_failed() {
        final Admin other = new Admin(2L, Admin.Type.BASIC);

        assertThatThrownBy(() -> {
            findListById.compareTo(other, 1L);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    static class Repository {
        Map<Long, List<String>> r = new HashMap<>();

        static Repository sample() {
            final Repository repository = new Repository();
            repository.save(1L, "apple");
            repository.save(1L, "orange");
            repository.save(2L, "mouse");
            repository.save(2L, "keyboard");
            repository.save(2L, "main-board");
            return repository;
        }

        void save(long id, String item) {
            getIdsRepo(id).add(item);
        }

        private List<String> getIdsRepo(long id) {
            if (r.containsKey(id)) {
                return r.get(id);
            }
            List<String> result = new ArrayList<>();
            r.put(id, result);
            return result;
        }

        public List<String> findById(long id) {
            return r.get(id);
        }
    }
}