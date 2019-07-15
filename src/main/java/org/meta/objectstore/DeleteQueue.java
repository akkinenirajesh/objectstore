package org.meta.objectstore;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

public class DeleteQueue {

	private boolean isInConversion;
	private EntityManager entityManager;
	private static ThreadLocal<Set<DatabaseObject>> queue = new ThreadLocal<Set<DatabaseObject>>();

	DeleteQueue(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void delete(DatabaseObject obj) {
		if (isInConversion) {
			getQueue().add(obj);
			return;
		}

		if (obj.isDeleted()) {
			return;
		}
		obj.setDeleted(true);
		this.entityManager.remove(obj);
	}

	private Set<DatabaseObject> getQueue() {
		Set<DatabaseObject> deleteQueue = queue.get();
		if (deleteQueue == null) {
			deleteQueue = new HashSet<>();
			queue.set(deleteQueue);
		}
		return deleteQueue;
	}

	public void finish() {
		Set<DatabaseObject> list = getQueue();
		Set<DatabaseObject> clone = new HashSet<DatabaseObject>(list);
		list.clear();
		for (DatabaseObject obj : clone) {
			entityManager.remove(obj);
		}
		if (!list.isEmpty()) {
			finish();
		}
	}

	public void conversionStart() {
		isInConversion = true;
	}

	public void converstionCompleted() {
		isInConversion = false;
		for (DatabaseObject obj : getQueue()) {
			delete(obj);
		}
	}

	public void clear() {
		getQueue().clear();
	}
}
