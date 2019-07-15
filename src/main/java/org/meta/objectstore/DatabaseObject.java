package org.meta.objectstore;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.hibernate.CallbackException;
import org.hibernate.Session;

import com.sun.org.apache.xerces.internal.parsers.XMLParser;
import com.vimukti.ecgine.client.ApplicationException;
import com.vimukti.ecgine.server.ActivityThreadLocal;
import com.vimukti.ecgine.server.ApplicationThreadLocal;
import com.vimukti.ecgine.server.CachedSaveQueue;
import com.vimukti.ecgine.server.activitylog.ActivityRestore;
import com.vimukti.ecgine.server.activitylog.ObjectActivity;

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

	@Override
	public boolean onUpdate(Session s) throws CallbackException {
		getLog().setType(ObjectActivity.TYPE_UPDATE);
		return onSave(s);
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

	public void addFields(Map<String, Object> map) {
		map.put("Id", getId());
	}

	@Override
	public void onLoad(Session s, Serializable id) {
		isNew = false;
	}

	@Override
	public boolean onSave(Session arg0) throws CallbackException {
		onSaveCount++;
		if (onSaveCount > MAX_SAVE_COUNT - OBJ_LOG_COUNT) {
			Map<String, Object> map = new HashMap<String, Object>();
			addFields(map);
			logger.info(getObjectName() + " Object Data(" + onSaveCount + "):" + map);
		}
		if (onSaveCount == MAX_SAVE_COUNT) {
			needSaveProcess = false;
			throw new RuntimeException("Object saving more than " + MAX_SAVE_COUNT + " times");
		}
		project = ApplicationThreadLocal.getRootObj();
		return false;
	}

	@Override
	public boolean onDelete(Session arg0) throws CallbackException {
		getLog().setType(ObjectActivity.TYPE_DELETE);
		return false;
	}

	@Override
	public void exportToXml(XMLParser p) throws Exception {

	}

	public String getObjectName() {
		return null;
	}

	@Override
	public boolean canSkip() {
		return false;
	}

	@Override
	public void importFromXml(XMLParser parser, CachedSaveQueue queue) throws Exception {

	}

	public void doScheduleAction(int actionNo) throws ApplicationException {
		// TODO Auto-generated method stub

	}

	public boolean canDelete() throws ApplicationException {
		return true;
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

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public void markDirty() {
		isDirty = true;
		markNeedsUpdate();
	}

	@Override
	public void resetDirty() {
		isDirty = false;
	}

	public abstract int type();

	public void updateEntry(Session s) {
		needsUpdate = false;
		onUpdate(s);
		onLoad(s, getId());
	}

	@Override
	public boolean isGotId() {
		return isGotId;
	}

	@Override
	public void onSetId() throws ApplicationException {
		isGotId = true;
	}

	public boolean isNew() {
		return isNew;
	}

	@Override
	public boolean needsUpdate() {
		return needsUpdate;
	}

	@Override
	public void markNeedsUpdate() {
		needsUpdate = true;
	}

	public void setDeletedProcessed(boolean isDeleteProcessed) {
		this.isDeleteProcessed = isDeleteProcessed;
	}

	public boolean isDeleteProcessed() {
		return isDeleteProcessed;
	}

	public static <T extends DatabaseObject> void peformDeleteOrphan(Collection<T> oldList, Collection<T> newList) {
		List<T> deletedList = new ArrayList<T>();
		for (T t : oldList) {
			if (!newList.contains(t)) {
				deletedList.add(t);
			}
		}
		oldList.clear();
		oldList.addAll(newList);
		for (T t : deletedList) {
			DeleteQueue.delete(t);
		}
	}

	public IDeleteProcessor getDeleteProcessor() {
		return new IDeleteProcessor() {

			@Override
			public void processDelete() {
			}
		};
	}

	public ObjectActivity getLog() {
		if (log == null) {
			log = new ObjectActivity(this);
			log.setType(isNew ? ObjectActivity.TYPE_CREATE : ObjectActivity.TYPE_UPDATE);
			if (!isComponent()) {
				ActivityThreadLocal.getLog().add(log);
			}
		}
		return log;
	}

	public void restore(int fid, ActivityRestore ar) throws IOException {
	}

	public void recordLog() {
	}

	public DatabaseObject clone() {
		return null;
	}

	public void clone(DatabaseObject clone) {

	}

}
