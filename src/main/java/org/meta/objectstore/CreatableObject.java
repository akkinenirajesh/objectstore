package org.meta.objectstore;



import java.io.IOException;
import java.sql.Timestamp;
import com.vimukti.ecgine.client.ApplicationException;
import com.vimukti.ecgine.client.core.ClientCreatableObject;
import com.vimukti.ecgine.server.ApplicationThreadLocal;
import com.vimukti.ecgine.server.activitylog.FieldType;
import com.vimukti.ecgine.server.network.NetworkChange;
import com.vimukti.ecgine.server.network.NetworkReader;
import com.vimukti.ecgine.server.network.NetworkWriter;
import com.vimukti.ecgine.server.utils.CalendarUtil;
import com.vimukti.ecgine.server.utils.CoreUtils;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import com.vimukti.ecgine.server.activitylog.ObjectActivity;
import java.util.Map;
import com.vimukti.ecgine.server.activitylog.ActivityRestore;

public abstract class CreatableObject
    extends DatabaseObject
{

    protected int saveStatus;
    protected int version;
    private transient boolean isInConvert;
    protected Timestamp createdDate;
    protected Timestamp lastModifiedDate;
    private ProjectMember createdBy;
    private ProjectMember lastModifiedBy;

    public void write(NetworkWriter w)
        throws IOException
    {
        super.write(w);
        w.writeInt(saveStatus);
        w.writeInt(version);
        w.writeBoolean(isInConvert);
        w.writeTimestamp(createdDate);
        w.writeTimestamp(lastModifiedDate);
        w.write(createdBy);
        w.write(lastModifiedBy);
    }

    public void read(NetworkReader r)
        throws IOException
    {
        super.read(r);
        saveStatus = r.readInt();
        version = r.readInt();
        isInConvert = r.readBoolean();
        createdDate = r.readTimestamp();
        lastModifiedDate = r.readTimestamp();
        createdBy = r.read(ProjectMember.class);
        lastModifiedBy = r.read(ProjectMember.class);
    }

    public void change(NetworkChange c) {
    }

    public int getSaveStatus() {
        return this.saveStatus;
    }

    public void setSaveStatus(int saveStatus) {
        if (CoreUtils.nullSafeEquals(this.getSaveStatus(), saveStatus)) {
            return ;
        }
        markDirty();
        if (CoreUtils.nullSafeEquals(this.getSaveStatus(), saveStatus)) {
            return ;
        }
        getLog().log(1, FieldType.INT, false, saveStatus, this.saveStatus);
        this.saveStatus = saveStatus;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        if (CoreUtils.nullSafeEquals(this.getVersion(), version)) {
            return ;
        }
        getLog().log(2, FieldType.INT, false, version, this.version);
        this.version = version;
    }

    public boolean isInConvert() {
        return this.isInConvert;
    }

    public void setInConvert(boolean isInConvert) {
        this.isInConvert = isInConvert;
    }

    public Timestamp getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        if (CalendarUtil.compare(this.getCreatedDate(), createdDate, 1)) {
            return ;
        }
        getLog().log(3, FieldType.DATE_TIME, false, createdDate, this.createdDate);
        this.createdDate = createdDate;
    }

    public String getCreatedDateAsString() {
        return CalendarUtil.getDateTimeAsString(createdDate, ((ApplicationThreadLocal.getShortDateFormat()+" ")+ ApplicationThreadLocal.getShortTimeFormat()), ApplicationThreadLocal.getRootObj().getProjectTimeZone());
    }

    public Timestamp getLastModifiedDate() {
        return this.lastModifiedDate;
    }

    public void setLastModifiedDate(Timestamp lastModifiedDate) {
        if (CalendarUtil.compare(this.getLastModifiedDate(), lastModifiedDate, 1)) {
            return ;
        }
        getLog().log(4, FieldType.DATE_TIME, false, lastModifiedDate, this.lastModifiedDate);
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedDateAsString() {
        return CalendarUtil.getDateTimeAsString(lastModifiedDate, ((ApplicationThreadLocal.getShortDateFormat()+" ")+ ApplicationThreadLocal.getShortTimeFormat()), ApplicationThreadLocal.getRootObj().getProjectTimeZone());
    }

    public ProjectMember getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(ProjectMember createdBy) {
        if (CoreUtils.checkIsDirty(this.getCreatedBy(), createdBy)) {
            return ;
        }
        getLog().log(5, FieldType.REF, false, createdBy, this.createdBy);
        this.createdBy = createdBy;
    }

    public ProjectMember getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public void setLastModifiedBy(ProjectMember lastModifiedBy) {
        if (CoreUtils.checkIsDirty(this.getLastModifiedBy(), lastModifiedBy)) {
            return ;
        }
        getLog().log(6, FieldType.REF, false, lastModifiedBy, this.lastModifiedBy);
        this.lastModifiedBy = lastModifiedBy;
    }

    public void fromClient(ClientCreatableObject client)
        throws ApplicationException
    {
        super.fromClient(client);
        version = client.version;
        saveStatus = client.saveStatus;
        id = client.id;
    }

    public ClientCreatableObject toClient(ClientCreatableObject client) {
        super.toClient(client);
        client.version = getVersion();
        client.saveStatus = getSaveStatus();
        client.id = getId();
        if (createdBy!= null) {
            client.createdBy = getCreatedBy().getId();
        }
        if (lastModifiedBy!= null) {
            client.lastModifiedBy = getLastModifiedBy().getId();
        }
        client.createdDate = getCreatedDate();
        client.lastModifiedDate = getLastModifiedDate();
        return client;
    }

    public boolean onSave(Session session)
        throws CallbackException
    {
        super.onSave(session);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        lastModifiedDate = currentTime;
        lastModifiedBy = ApplicationThreadLocal.getMember();
        if (getId() == 0) {
            createdDate = currentTime;
            createdBy = lastModifiedBy;
        }
        if ((saveStatus!= 1)&&(id == 0)) {
            version = 1;
        }
        return false;
    }

    public boolean onUpdate(Session session)
        throws CallbackException
    {
        if (saveStatus!= 1) {
            version += 1;
        }
        boolean ret = super.onUpdate(session);
        return ret;
    }

    public void updateComputedProperties() {
    }

    public void doRemainingTasks()
        throws ApplicationException
    {
    }

    public void validate()
        throws ApplicationException
    {
    }

    public ClientCreatableObject toClientLite() {
        return toClient();
    }

    public boolean canSkip() {
        return (saveStatus == 1);
    }
    

	@Override
	public void restore(int fid, ActivityRestore ar) throws IOException {
		super.restore(fid, ar);
		switch (fid) {
		case 1:
			saveStatus = ar.read(FieldType.INT);
			break;
		case 2:
			version = ar.read(FieldType.INT);
			break;
		case 3:
			createdDate = ar.read(FieldType.DATE_TIME);
			break;
		case 4:
			lastModifiedDate = ar.read(FieldType.DATE_TIME);
			break;
		case 5:
			createdBy = ar.read(FieldType.REF, 12);
			break;
		case 6:
			lastModifiedBy = ar.read(FieldType.REF, 12);
			break;
		default:
			break;
		}
	}

	@Override
	public void recordLog() {
		super.recordLog();
		ObjectActivity l = getLog();
		l.log(1, FieldType.INT, false, saveStatus, this.saveStatus);
		l.log(2, FieldType.INT, false, version, this.version);
		l.log(3, FieldType.DATE_TIME, false, createdDate, this.createdDate);
		l.log(4, FieldType.DATE_TIME, false, lastModifiedDate,
				this.lastModifiedDate);
		l.log(5, FieldType.REF, false, createdBy, this.createdBy);
		l.log(6, FieldType.REF, false, lastModifiedBy, this.lastModifiedBy);
	}

	@Override
	public void addFields(Map<String, Object> map) {
		super.addFields(map);
		map.put("Save Status", this.saveStatus);
		map.put("Version", this.version);
		map.put("Created Date", createdDate);
		map.put("Last Modified Date", lastModifiedDate);
		map.put("Created By", toLogStr(createdBy));
		map.put("Last Modified By", toLogStr(lastModifiedBy));
	}
}
