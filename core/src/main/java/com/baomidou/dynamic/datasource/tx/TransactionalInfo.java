package com.baomidou.dynamic.datasource.tx;

public class TransactionalInfo {

    Class<? extends Throwable>[] rollbackFor;

    Class<? extends Throwable>[] noRollbackFor;

    DsPropagation propagation;

    public Class<? extends Throwable>[] getRollbackFor() {
        return rollbackFor;
    }

    public void setRollbackFor(Class<? extends Throwable>[] rollbackFor) {
        this.rollbackFor = rollbackFor;
    }

    public Class<? extends Throwable>[] getNoRollbackFor() {
        return noRollbackFor;
    }

    public void setNoRollbackFor(Class<? extends Throwable>[] noRollbackFor) {
        this.noRollbackFor = noRollbackFor;
    }

    public DsPropagation getPropagation() {
        return propagation;
    }

    public void setPropagation(DsPropagation propagation) {
        this.propagation = propagation;
    }
}
