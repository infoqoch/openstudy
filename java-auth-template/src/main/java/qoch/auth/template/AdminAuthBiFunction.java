package qoch.auth.template;

import java.util.function.BiFunction;

public class AdminAuthBiFunction<E extends Auth, U , R> {
    private final BiFunction<E, U, R> supers;
    private final BiFunction<E, U, R> theSameId;
    private final BiFunction<E, U, R> elses;

    public AdminAuthBiFunction(Builder<E, U, R> builder) {
        supers = builder.supers;
        theSameId = builder.theSameId;
        elses = builder.elses;
    }

    public R compareTo(E target, U u) {
        if(target.isSuper()) {
            return supers.apply(target, u);
        }else if(target.isTheSameIdWith(u)) {
            return theSameId.apply(target, u);
        }else {
            return elses.apply(target, u);
        }
    }

    // 빌더 패턴을 통하여 불변식 구현
    // 함수형 인터페이스를 통해 간단한 함수 처리
    public static class Builder<E extends Auth, U, R> {
        private BiFunction<E, U, R> supers;
        private BiFunction<E, U, R> theSameId;
        private BiFunction<E, U, R> elses;

        public Builder() {
        }

        public Builder<E, U, R> supers(BiFunction<E, U, R> function){
            this.supers = function;
            return this;
        }

        public Builder<E,U, R> theSameId(BiFunction<E, U, R> function){
            this.theSameId = function;
            return this;
        }

        public Builder<E,U,R> elses(BiFunction<E, U, R> function){
            this.elses = function;
            return this;
        }

        public AdminAuthBiFunction<E, U, R> build() {
            return new AdminAuthBiFunction<E, U, R>(this);
        }
    }
}

