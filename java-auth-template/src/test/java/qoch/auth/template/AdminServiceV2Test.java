package qoch.auth.template;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminServiceV2Test {

    static class AdminAuthorizationConsumer<E extends AdminAuth> {
        private final E target;
        private final Consumer<E> supers;
        private final Consumer<E> theSameId;
        private final Consumer<E> elses;

        public AdminAuthorizationConsumer(Builder<E> builder) {
            supers = builder.supers;
            theSameId = builder.theSameId;
            elses = builder.elses;
            target = builder.target;
        }

        // 탬플릿 패턴을 통하여 if문의 흐름 (슈퍼 여부 -> 회원 일치 여부 -> 그 외)를 강제한다.
        public void compareTo(String id) {
            if(target.isSuper()) { // 대상이 되는 객체의 super 여부를 interface 로 구현한다.
                supers.accept(target);
            }else if(target.isTheSameIdWith(id)) { // 대상이 되는 객체와 제공하는 아이디 간 일치여부를 interface 로 구현한다.
                theSameId.accept(target);
            }else { // 위의 상황이 모두 false 일 경우
                elses.accept(target);
            }
        }

        // 빌더 패턴을 통하여 불변식으로 구현하였다.
        // 스트림 형태로 함수형 인터페이스를 구현하기 때문에 실제 구현 시 보기 좋다.
        public static class Builder<E extends AdminAuth> {
            private final E target;
            private Consumer<E> supers;
            private Consumer<E> theSameId;
            private Consumer<E> elses;

            public Builder(E e) {
                this.target = e;
            }

            public Builder<E> superAdmin(Consumer<E> consumer){
                this.supers = consumer;
                return this;
            }

            public Builder<E> theSameId(Consumer<E> consumer){
                this.theSameId = consumer;
                return this;
            }

            public Builder<E> elses(Consumer<E> consumer){
                this.elses = consumer;
                return this;
            }

            public AdminAuthorizationConsumer<E> build() {
                return new AdminAuthorizationConsumer<E>(this);
            }
        }
    }

    interface AdminAuth {
        boolean isSuper();
        boolean isTheSameIdWith(String id);
    }

    static class Admin implements AdminAuth {
        private final String id;
        private final Type type;

        enum Type{
            SUPER, BASIC
        }

        Admin(String id, Type type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public boolean isTheSameIdWith(String id) {
            return this.id.equals(id);
        }

        @Override
        public boolean isSuper() {
            return type == Type.SUPER;
        }
    }

    @Test
    void test1(){
        Admin admin = new Admin("kim", Admin.Type.BASIC);
        AdminAuthorizationConsumer<Admin> auth = new AdminAuthorizationConsumer.Builder<Admin>(admin)
                .superAdmin(a -> System.out.println("슈퍼유저!"))
                .theSameId(a -> System.out.println("아이디가 일치하네!"))
                .elses(a -> System.out.println("아이디가 불일치하네!"))
                .build();

        auth.compareTo("kim"); // 아이디가 일치하네!
    }

    @Test
    void test2() {
        assertExecute(new Admin("kim", Admin.Type.SUPER), "abc", Result.SUPER);
        assertExecute(new Admin("kim", Admin.Type.SUPER), "kim", Result.SUPER);
        assertExecute(new Admin("kim", Admin.Type.BASIC), "kim", Result.SAME);
        assertExecute(new Admin("kim", Admin.Type.BASIC), "lee", Result.ELSE);
    }

    private void assertExecute(Admin admin, String target, Result result) {
        // given
        boolean[] spy = new boolean[3];
        AdminAuthorizationConsumer<Admin> builder = new AdminAuthorizationConsumer.Builder<Admin>(admin)
                .superAdmin(a -> spy[0] = true)
                .theSameId(a -> spy[1] = true)
                .elses(a -> spy[2] = true)
                .build();

        // when
        builder.compareTo(target);

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
