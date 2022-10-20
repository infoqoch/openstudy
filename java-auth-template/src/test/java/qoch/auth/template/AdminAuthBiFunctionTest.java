package qoch.auth.template;

import qoch.auth.template.Admin;
import qoch.auth.template.AdminAuthBiFunction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminAuthBiFunctionTest {
    @Test
    void test() {
        // given
        final Map<Long, List<String>> repo = givenRepository();
        final AdminAuthBiFunction<Admin, Long, List<String>> findItemListByIdAfterCheckAuth = new AdminAuthBiFunction.Builder<Admin, Long, List<String>>()
                .supers((a, id) -> repo.get(id))
                .theSameId((a, id) -> repo.get(a.getId()))
                .elses((a, id) -> {throw new IllegalArgumentException("허용되지 않은 접근입니다. admin ["+a.getId()+"] => request ["+id+"]");})
                .build();

        // super
        final List<String> findOthersWithSuperAdmin = findItemListByIdAfterCheckAuth.compareTo(new Admin(999L, Admin.Type.SUPER), 1L);
        assertThat(findOthersWithSuperAdmin.size()).isEqualTo(2);
        assertThat(findOthersWithSuperAdmin).isEqualTo(repo.get(1L));

        // mine
        final List<String> findMine = findItemListByIdAfterCheckAuth.compareTo(new Admin(1L, Admin.Type.BASIC), 1L);
        assertThat(findMine.size()).isEqualTo(2);
        assertThat(findMine).isEqualTo(repo.get(1L));

        // others
        assertThatThrownBy(()->{
            findItemListByIdAfterCheckAuth.compareTo(new Admin(1L, Admin.Type.BASIC), 2L);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    Map<Long, List<String>> givenRepository(){
        Map<Long, List<String>> r = new HashMap<>();
        r.put(1L, new ArrayList<>());
        r.get(1L).add("apple");
        r.get(1L).add("orange");

        r.put(2L, new ArrayList<>());
        r.get(2L).add("mouse");
        r.get(2L).add("keyboard");
        r.get(2L).add("main-board");

        return r;
    }
}