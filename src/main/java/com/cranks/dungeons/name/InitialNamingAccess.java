package com.cranks.dungeons.name;

/** Persisted per animal so release, retaming and ownership transfer never renew the free name. */
public interface InitialNamingAccess {
    boolean cranks$wasNamingOffered();
    void cranks$setNamingOffered(boolean offered);
}
