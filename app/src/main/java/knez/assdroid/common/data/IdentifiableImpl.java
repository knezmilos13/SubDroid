package knez.assdroid.common.data;

public abstract class IdentifiableImpl implements Identifiable {

    private final long id;

    public IdentifiableImpl(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

}
