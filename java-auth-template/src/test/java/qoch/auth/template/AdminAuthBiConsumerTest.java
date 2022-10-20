package qoch.auth.template;

import qoch.auth.template.Admin;
import qoch.auth.template.AdminAuthBiConsumer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static qoch.auth.template.Admin.Type.*;

class AdminAuthBiConsumerTest {
    @Test
    void test_basic() {
        // given
        final AdminAuthBiConsumer<Admin, Long> logAccessUser = new AdminAuthBiConsumer.Builder<Admin, Long>()
                .supers((a, id) -> System.out.println("슈퍼 유저에게 허용된 데이터 접근입니다^^. 접근 대상 : " + id))
                .theSameId((a, id) -> System.out.println("본인의 데이터에 접근합니다. 접근 대상 : " + id))
                .elses((a, id) -> {throw new IllegalArgumentException("허용되지 않은 접근입니다. admin ["+a.getId()+"] => request ["+id+"]");})
                .build();

        // super로 접근
        logAccessUser.compareTo(new Admin(999L, SUPER), 1L); // 슈퍼 유저에게 허용된 데이터 접근입니다^^. 접근 대상 : 1

        // 자신의 것을 접근
        logAccessUser.compareTo(new Admin(1L, BASIC), 1L); // 본인의 데이터에 접근합니다. 접근 대상 : 1

        // 다른 회원의 데이터 접근
        assertThatThrownBy(()->{
            logAccessUser.compareTo(new Admin(1L, BASIC), 2L); // 예외
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void test_spy() {
        assertExecute(new Admin(1L, SUPER), 2L, Result.SUPER);
        assertExecute(new Admin(1L, SUPER), 1L, Result.SUPER);
        assertExecute(new Admin(1L, BASIC), 1L, Result.SAME);
        assertExecute(new Admin(1L, BASIC), 2L, Result.ELSE);
    }

    private void assertExecute(Admin admin, Long target, Result result) {
        // given
        boolean[] spy = new boolean[3];
        AdminAuthBiConsumer<Admin, Long>  execute = new AdminAuthBiConsumer.Builder<Admin, Long>()
                .supers((a, id) -> spy[0] = true)
                .theSameId((a, id) -> spy[1] = true)
                .elses((a, id) -> spy[2] = true)
                .build();

        // when
        execute.compareTo(admin, target);

        // then
        assertThat(spy[0]).isEqualTo(result.superAdmin);
        assertThat(spy[1]).isEqualTo(result.theSameId);
        assertThat(spy[2]).isEqualTo(result.elese);
    }

    enum Result {
        SUPER(true, false, false), SAME(false, true, false), ELSE(false, false, true);

        private final boolean superAdmin;
        private final boolean theSameId;
        private final boolean elese;

        Result(boolean superAdmin, boolean theSameId, boolean elese) {
            this.superAdmin = superAdmin;
            this.theSameId = theSameId;
            this.elese = elese;
        }
    }
}