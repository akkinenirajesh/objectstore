package org.meta.objectstore;

public abstract class CreatableObject
    extends DatabaseObject
{

    protected int saveStatus;
    private transient boolean isInConvert;


    public boolean isInConvert() {
        return this.isInConvert;
    }

    public void setInConvert(boolean isInConvert) {
        this.isInConvert = isInConvert;
    }

   
}
