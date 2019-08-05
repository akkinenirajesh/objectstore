package org.meta.objectstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Scope("request")
@Service
public class EntityMutator {
	private final EntityManager manager;

	private final Map<String, EntityHelper<?>> entityHelpers;

	private Set<DatabaseObject> saveQueue;
	private Set<DatabaseObject> deleteQueue;
	private Set<DatabaseObject> saveDuplicates;
	private boolean isInConversion;

	private final Map<DatabaseObject, DatabaseObject> clones = new HashMap<>();

	private ValidationContextImpl context;

	static ThreadLocal<EntityMutator> threadLocalMutator = new ThreadLocal<>();

	static EntityMutator get() {
		return threadLocalMutator.get();
	}

	EntityMutator(EntityManager manager, Map<String, EntityHelper<?>> entityHelpers) {
		this.manager = manager;
		this.entityHelpers = entityHelpers;
		this.saveQueue = new HashSet<DatabaseObject>();
		this.saveDuplicates = new HashSet<DatabaseObject>();
		this.deleteQueue = new HashSet<DatabaseObject>();
		context = new ValidationContextImpl<>();
		threadLocalMutator.set(this);
	}

	public void save(DatabaseObject obj) {
		
	}
	
	public void update(DatabaseObject obj) {
		
	}
	
	public void saveOrUpdate(DatabaseObject obj) {
		if (obj instanceof CreatableObject) {
			CreatableObject creaObj = (CreatableObject) obj;
			if (creaObj.isInConvert() || creaObj.isDeleted()) {
				return;
			}
		}

		if (saveQueue.contains(obj)) {
			saveDuplicates.add(obj);
		} else {
			saveQueue.add(obj);
			saveInternal(obj);
		}
	}

	public boolean finish() {
		Iterator<DatabaseObject> iterator = new HashSet<DatabaseObject>(saveDuplicates).iterator();
		saveDuplicates.clear();
		while (iterator.hasNext()) {
			DatabaseObject next = iterator.next();
			if (!saveInternal(next)) {
				return false;
			}
		}
		if (!saveDuplicates.isEmpty()) {
			return finish();
		}

		Set<DatabaseObject> clone = new HashSet<DatabaseObject>(deleteQueue);
		deleteQueue.clear();
		for (DatabaseObject obj : clone) {
			manager.remove(obj);
		}
		if (!deleteQueue.isEmpty()) {
			return finish();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private <T extends DatabaseObject> boolean saveInternal(T entity) {
		if (entity.isDeleted()) {
			return true;
		}
		EntityHelper<T> helper = (EntityHelper<T>) this.entityHelpers.get(entity.getClass().getName());

		context.setEntity(entity);
		if (helper != null) {
			helper.setDefaults(entity);
			helper.compute(entity);
			helper.validate(context);
		}
		if (!context.hasErrors()) {
			if (helper != null) {
				if (entity.isNew()) {
					helper.onCreate(entity);
				} else {
					helper.onUpdate(entity, entity);
				}
			}
			manager.persist(entity);
			return true;
		}
		return false;
	}

	public <T extends DatabaseObject> boolean delete(T obj) {
		if (isInConversion) {
			deleteQueue.add(obj);
			return true;
		}
		return deleteInternal(obj);
	}

	public void conversionStart() {
		isInConversion = true;
	}

	public boolean converstionCompleted() {
		isInConversion = false;
		for (DatabaseObject obj : deleteQueue) {
			if (!deleteInternal(obj)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private <T extends DatabaseObject> boolean deleteInternal(T entity) {
		if (entity.isDeleted()) {
			return true;
		}
		entity.setDeleted(true);
		EntityHelper<T> helper = (EntityHelper<T>) this.entityHelpers.get(entity.getClass().getName());

		context.setEntity(entity);
		if (!context.hasErrors()) {
			if (helper != null) {
				if (!helper.onDelete(entity)) {
					return false;
				}
			}
			manager.remove(entity);
			return true;
		}
		return false;
	}

	public <T extends DatabaseObject> void peformDeleteOrphan(Collection<T> oldList, Collection<T> newList) {
		List<T> deletedList = new ArrayList<T>();
		for (T t : oldList) {
			if (!newList.contains(t)) {
				deletedList.add(t);
			}
		}
		oldList.clear();
		oldList.addAll(newList);
		for (T t : deletedList) {
			this.delete(t);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void processOnLoad(Object entity) {
		EntityHelper helper = (EntityHelper) this.entityHelpers.get(entity.getClass().getName());

		if (helper != null) {
			if (clones.containsKey(entity)) {
				return;
			}
			Object clone = helper.clone(entity);
			clones.put((DatabaseObject) entity, (DatabaseObject) clone);
		}

	}
}
