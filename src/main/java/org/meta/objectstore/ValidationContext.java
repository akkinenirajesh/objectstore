package org.meta.objectstore;

public interface ValidationContext<T> {

	void setEntity(T entity);

	boolean hasErrors();

	Object getErrors();

}
