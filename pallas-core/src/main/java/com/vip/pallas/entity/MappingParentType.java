package com.vip.pallas.entity;

public enum MappingParentType {
    NORMAL((byte)0),NESTED((byte)1),OBJECT((byte)2),MULTI_FIELDS((byte)3);

    private Byte val;
    MappingParentType(Byte val) {
        this.val = val;
    }
    public Byte val(){
        return this.val;
    }
}
