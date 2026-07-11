/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 *  net.minecraft.class_3300
 */
package punchy.client.modelparts;

import net.minecraft.class_2960;
import net.minecraft.class_3300;
import punchy.client.model.ResourcePackGeoRegistry;
import punchy.client.model.ResourcePackItemModelRegistry;
import punchy.client.model.SimpleBedrockGeo;
import punchy.client.model.SimpleBedrockGeoThirdPerson;
import punchy.util.ResourceLocationCompat;

final class ResourceStateMachine {
    private State state = State.UNLOADED;

    ResourceStateMachine() {
    }

    void load(class_3300 manager) {
        if (manager == null) {
            this.state = State.FAILED;
            return;
        }
        this.state = State.LOADING;
        try {
            class_2960 playerGeoId = ResourceLocationCompat.fromNamespaceAndPath("punchy", "geo/player.geo.json");
            SimpleBedrockGeo.load(playerGeoId);
            class_2960 thirdGeoId = ResourceLocationCompat.fromNamespaceAndPath("punchy", "geo/player_3_per.geo.json");
            SimpleBedrockGeoThirdPerson.load(thirdGeoId);
            ResourcePackGeoRegistry.load(manager);
            ResourcePackItemModelRegistry.load(manager);
            this.state = State.READY;
        }
        catch (Throwable t) {
            this.state = State.FAILED;
        }
    }

    State getState() {
        return this.state;
    }

    static enum State {
        UNLOADED,
        LOADING,
        READY,
        FAILED;

    }
}

