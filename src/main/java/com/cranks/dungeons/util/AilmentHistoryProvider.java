package com.cranks.dungeons.util;

import java.util.UUID;

public interface AilmentHistoryProvider {
    void cranks$recordBurnChain(UUID chainUuid);
    boolean cranks$wasHitByChain(UUID chainUuid);
}
