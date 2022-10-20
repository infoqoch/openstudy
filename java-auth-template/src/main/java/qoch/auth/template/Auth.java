package qoch.auth.template;

public interface Auth<U> {
    boolean isSuper();
    boolean isTheSameIdWith(U id);
}
