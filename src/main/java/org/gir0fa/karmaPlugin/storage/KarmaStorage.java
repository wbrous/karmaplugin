package org.gir0fa.karmaPlugin.storage;

import java.util.UUID;

public interface KarmaStorage {
    int getKarma(UUID playerId);
    void setKarma(UUID playerId, int karma);
    void saveAsync();
    void saveSync();
}


