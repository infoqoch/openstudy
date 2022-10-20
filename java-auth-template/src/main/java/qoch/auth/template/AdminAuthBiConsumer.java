package qoch.auth.template;

import java.util.function.BiConsumer;

public class AdminAuthBiConsumer<E extends Auth, U> {
    private final BiConsumer<E, U> supers;
    private final BiConsumer<E, U> theSameId;
    private final BiConsumer<E, U> elses;

    public AdminAuthBiConsumer(Builder<E, U> builder) {
        supers = builder.supers;
        theSameId = builder.theSameId;
        elses = builder.elses;
    }

    public void compareTo(E target, U u) {
        if(target.isSuper()) {
            supers.accept(target, u);
        }else if(target.isTheSameIdWith(u)) {
            theSameId.accept(target, u);
        }else {
            elses.accept(target, u);
        }
    }

    public static class Builder<E extends Auth, U> {
        private BiConsumer<E, U> supers;
        private BiConsumer<E, U> theSameId;
        private BiConsumer<E, U> elses;

        public Builder() {
        }

        public Builder<E, U> supers(BiConsumer<E, U> consumer){
            this.supers = consumer;
            return this;
        }

        public Builder<E, U> theSameId(BiConsumer<E, U> consumer){
            this.theSameId = consumer;
            return this;
        }

        public Builder<E, U> elses(BiConsumer<E, U> consumer){
            this.elses = consumer;
            return this;
        }

        public AdminAuthBiConsumer<E, U> build() {
            return new AdminAuthBiConsumer<E, U>(this);
        }
    }
}

