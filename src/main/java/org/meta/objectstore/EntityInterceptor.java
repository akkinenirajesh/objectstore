package org.meta.objectstore;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;


@Component
public class EntityInterceptor extends EmptyInterceptor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public void afterTransactionBegin(Transaction tx) {
		// TODO Auto-generated method stub
		super.afterTransactionBegin(tx);
	}
	
	@Override
	public void afterTransactionCompletion(Transaction tx) {
		// TODO Auto-generated method stub
		super.afterTransactionCompletion(tx);
	}
	@Override
	public void beforeTransactionCompletion(Transaction tx) {
		// TODO Auto-generated method stub
		super.beforeTransactionCompletion(tx);
	}
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		// TODO Auto-generated method stub
		return super.onSave(entity, id, state, propertyNames, types);
	}
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if(entity instanceof DatabaseObject) {
			EntityMutator mutator = EntityMutator.get();
			if(mutator!=null) {
				mutator.processOnLoad(entity);				
			}
		}
		return super.onLoad(entity, id, state, propertyNames, types);
	}
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		// TODO Auto-generated method stub
		super.onDelete(entity, id, state, propertyNames, types);
	}

}
