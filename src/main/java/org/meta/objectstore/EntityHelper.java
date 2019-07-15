package org.meta.objectstore;

public interface EntityHelper<T> {
	void setDefaults(T entity);

	void compute(T entity);

	void validate(ValidationContext<T> context);

	void callActions(T entity);

	boolean onCreate(T entity);
	boolean onUpdate(T entity);
	boolean onDelete(T entity);
}
