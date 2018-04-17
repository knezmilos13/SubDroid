package knez.assdroid.common.data;

public abstract class IdentifiableImpl implements Identifiable {

    private final int id;

    public IdentifiableImpl(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
