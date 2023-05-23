package antessio.idempotency;

public class IdempotencyKeyDaoAdapter<T> extends IdempotencyKeyDao<T> {

    private Class<T> cls;

    public IdempotencyKeyDaoAdapter(Class<T> cls) {
        super();
        this.cls = cls;
    }


    @Override
    public T fromJson(String json) {
        return ObjectConverter.convertFromJson(json, cls);
    }

    @Override
    public String toJson(T target) {
        return ObjectConverter.convertToJson(target);
    }

}
