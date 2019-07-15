package org.meta.objectstore;

public interface EntityHelper<T> {
	void setDefaults(T entity);

	void compute(T entity);

	void validate(ValidationContext<T> context);

	boolean onCreate(T obj);

	boolean onUpdate(T obj, T old);

	boolean onDelete(T obj);

	T clone(T entity);
}
