package org.meta.objectstore;

import java.util.Collection;
import java.util.logging.Logger;

public abstract class DatabaseObject {

	protected long id;

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private static Logger logger = Logger.getLogger("DatabaseObject");
	private transient static int MAX_SAVE_COUNT = 20;
	private transient static int OBJ_LOG_COUNT = 5;
	private transient boolean isDeleted;
	public transient boolean isImportingFromXml;
	private transient boolean isDirty;
	private transient boolean isNew = true;
	private transient boolean needsUpdate;
	private transient boolean isDeleteProcessed;
	protected transient boolean needSaveProcess = true;
	protected transient boolean needDeleteProcess = true;
	private transient int onSaveCount;
	private transient boolean isGotId;


	public DatabaseObject() {
	}

	public boolean isDeleted() {
		return this.isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	

	protected Object toLogStr(Collection<?> col) {
		return col.size();
	}

	protected Object toLogStr(DatabaseObject db) {
		if (db == null) {
			return null;
		}
		return db.getId();
	}

	

	public String getObjectName() {
		return null;
	}

	
	protected void setInputs() {

	}

	public void setNeedDeleteProcess(boolean needDeleteProcess) {
		this.needDeleteProcess = needDeleteProcess;
	}

	protected boolean isComponent() {
		return false;
	}

	public void setNeedSaveProcess(boolean needSaveProcess) {
		this.needSaveProcess = needSaveProcess;
	}

	

	public abstract int type();

	
	public void setDeletedProcessed(boolean isDeleteProcessed) {
		this.isDeleteProcessed = isDeleteProcessed;
	}

	public boolean isDeleteProcessed() {
		return isDeleteProcessed;
	}

	


	public void recordLog() {
	}

	public DatabaseObject clone() {
		return null;
	}

	public void clone(DatabaseObject clone) {

	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

}
