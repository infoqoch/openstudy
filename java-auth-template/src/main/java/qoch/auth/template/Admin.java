package qoch.auth.template;

public class Admin implements Auth<Long> {
    private final Long id;
    private final Type type;

    public enum Type{
        SUPER, BASIC
    }

    public Admin(Long id, Type type) {
        this.id = id;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean isTheSameIdWith(Long id) {
        return this.id.equals(id);
    }

    @Override
    public boolean isSuper() {
        return type == Type.SUPER;
    }
}
