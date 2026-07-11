/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package punchy.client.animation.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import punchy.client.animation.data.BoneTrack;
import punchy.client.animation.data.Keyframe;

public class AnimationClip {
    private final String name;
    private final float length;
    private final boolean loop;
    private final Map<String, BoneTrack> tracks = new HashMap<String, BoneTrack>();
    private final Set<String> effects = new HashSet<String>();
    private boolean holdOnLastFrame;
    private final TreeMap<Float, Set<String>> timelineEffects = new TreeMap();
    private final TreeMap<Float, Set<String>> soundEffects = new TreeMap();
    private boolean lengthDefined;
    private boolean loopDefined;

    public AnimationClip(String name, float length, boolean loop) {
        this.name = name;
        this.length = length;
        this.loop = loop;
    }

    public void addTrack(BoneTrack track) {
        this.tracks.put(track.getBoneName(), track);
    }

    public BoneTrack getTrack(String boneName) {
        return this.tracks.get(boneName);
    }

    public Map<String, BoneTrack> getTracks() {
        return this.tracks;
    }

    public float getLength() {
        return this.length;
    }

    public boolean isLoop() {
        return this.loop;
    }

    public String getName() {
        return this.name;
    }

    public void setHoldOnLastFrame(boolean holdOnLastFrame) {
        this.holdOnLastFrame = holdOnLastFrame;
    }

    public boolean isHoldOnLastFrame() {
        return this.holdOnLastFrame;
    }

    public void setLengthDefined(boolean lengthDefined) {
        this.lengthDefined = lengthDefined;
    }

    public boolean isLengthDefined() {
        return this.lengthDefined;
    }

    public void setLoopDefined(boolean loopDefined) {
        this.loopDefined = loopDefined;
    }

    public boolean isLoopDefined() {
        return this.loopDefined;
    }

    public void addEffect(String effect) {
        if (effect != null && !effect.isEmpty()) {
            this.effects.add(effect);
        }
    }

    public boolean hasEffect(String effect) {
        return effect != null && this.effects.contains(effect);
    }

    public Set<String> getEffects() {
        return Collections.unmodifiableSet(this.effects);
    }

    public void addTimelineEffect(float time, String effect) {
        if (effect == null || effect.isEmpty()) {
            return;
        }
        this.timelineEffects.computeIfAbsent(Float.valueOf(time), k -> new LinkedHashSet()).add(effect);
    }

    public void addSoundEffect(float time, String effect) {
        if (effect == null || effect.isEmpty()) {
            return;
        }
        this.soundEffects.computeIfAbsent(Float.valueOf(time), k -> new HashSet()).add(effect);
    }

    public NavigableMap<Float, Set<String>> getSoundEffectsBetween(float start, float end) {
        if (this.soundEffects.isEmpty()) {
            return Collections.emptyNavigableMap();
        }
        float safeStart = Math.max(0.0f, start);
        float safeEnd = Math.max(0.0f, end);
        if (safeEnd < safeStart) {
            return Collections.emptyNavigableMap();
        }
        return Collections.unmodifiableNavigableMap(this.soundEffects.subMap(Float.valueOf(safeStart), false, Float.valueOf(safeEnd), true));
    }

    public Set<String> getSoundEffectsAt(float time) {
        if (this.soundEffects.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> effects = this.soundEffects.get(Float.valueOf(time));
        if (effects == null || effects.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(effects);
    }

    public boolean hasEffectAny(String effect) {
        if (effect == null || effect.isEmpty()) {
            return false;
        }
        if (this.effects.contains(effect)) {
            return true;
        }
        for (Set<String> effectsAtTime : this.timelineEffects.values()) {
            if (!effectsAtTime.contains(effect)) continue;
            return true;
        }
        return false;
    }

    public boolean isEffectActive(String effect, float time) {
        if (effect == null || effect.isEmpty()) {
            return false;
        }
        if (this.effects.contains(effect)) {
            return true;
        }
        for (Map.Entry entry : this.timelineEffects.headMap(Float.valueOf(time), true).entrySet()) {
            if (!((Set)entry.getValue()).contains(effect)) continue;
            return true;
        }
        return false;
    }

    public Set<String> getActiveEffects(float time) {
        HashSet<String> active = new HashSet<String>(this.effects);
        for (Set effectsAtTime : this.timelineEffects.headMap(Float.valueOf(time), true).values()) {
            active.addAll(effectsAtTime);
        }
        return active;
    }

    public Iterable<Map.Entry<Float, Set<String>>> getTimelineEffectsUpTo(float time) {
        NavigableMap<Float, Set<String>> head = this.timelineEffects.headMap(Float.valueOf(time), true);
        return Collections.unmodifiableNavigableMap(head).entrySet();
    }

    public NavigableMap<Float, Set<String>> getTimelineEffects() {
        return Collections.unmodifiableNavigableMap(this.timelineEffects);
    }

    public boolean hasEffectTriggeredBetween(String effect, float startExclusive, float endInclusive) {
        if (effect == null || effect.isEmpty() || this.timelineEffects.isEmpty()) {
            return false;
        }
        if (endInclusive < startExclusive) {
            return this.hasEffectTriggeredBetween(effect, startExclusive, Float.MAX_VALUE) || this.hasEffectTriggeredBetween(effect, Float.NEGATIVE_INFINITY, endInclusive);
        }
        NavigableMap<Float, Set<String>> window = this.timelineEffects.subMap(Float.valueOf(startExclusive), false, Float.valueOf(endInclusive), true);
        for (Set effectsAtTime : window.values()) {
            if (!effectsAtTime.contains(effect)) continue;
            return true;
        }
        return false;
    }

    public float getLastEffectTime(String effect, float time) {
        if (effect == null || effect.isEmpty() || this.timelineEffects.isEmpty()) {
            return Float.NaN;
        }
        float last = Float.NaN;
        for (Map.Entry entry : this.timelineEffects.headMap(Float.valueOf(time), true).entrySet()) {
            if (!((Set)entry.getValue()).contains(effect)) continue;
            last = ((Float)entry.getKey()).floatValue();
        }
        return last;
    }

    public NavigableMap<Float, Set<String>> getSoundEffects() {
        return Collections.unmodifiableNavigableMap(this.soundEffects);
    }

    public static AnimationClip copyOf(AnimationClip source) {
        if (source == null) {
            return null;
        }
        AnimationClip copy = new AnimationClip(source.getName(), source.getLength(), source.isLoop());
        copy.setHoldOnLastFrame(source.isHoldOnLastFrame());
        copy.setLengthDefined(source.isLengthDefined());
        copy.setLoopDefined(source.isLoopDefined());
        for (BoneTrack boneTrack : source.getTracks().values()) {
            if (boneTrack == null) continue;
            copy.addTrack(AnimationClip.copyTrack(boneTrack));
        }
        for (String string : source.getEffects()) {
            copy.addEffect(string);
        }
        for (Map.Entry entry : source.getTimelineEffects().entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            for (String effect : (Set)entry.getValue()) {
                copy.addTimelineEffect(((Float)entry.getKey()).floatValue(), effect);
            }
        }
        for (Map.Entry entry : source.getSoundEffects().entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            for (String effect : (Set)entry.getValue()) {
                copy.addSoundEffect(((Float)entry.getKey()).floatValue(), effect);
            }
        }
        return copy;
    }

    public static AnimationClip merge(AnimationClip base, AnimationClip overlay) {
        if (overlay == null) {
            return AnimationClip.copyOf(base);
        }
        if (base == null) {
            return AnimationClip.copyOf(overlay);
        }
        String name = overlay.getName() != null && !overlay.getName().isBlank() ? overlay.getName() : base.getName();
        float length = overlay.isLengthDefined() ? overlay.getLength() : base.getLength();
        boolean loop = overlay.isLoopDefined() ? overlay.isLoop() : base.isLoop();
        AnimationClip merged = new AnimationClip(name, length, loop);
        merged.setLengthDefined(base.isLengthDefined() || overlay.isLengthDefined());
        merged.setLoopDefined(base.isLoopDefined() || overlay.isLoopDefined());
        merged.setHoldOnLastFrame(overlay.isLoopDefined() ? overlay.isHoldOnLastFrame() : base.isHoldOnLastFrame());
        for (BoneTrack track : base.getTracks().values()) {
            if (track == null) continue;
            merged.addTrack(AnimationClip.copyTrack(track));
        }
        for (BoneTrack track : overlay.getTracks().values()) {
            if (track == null) continue;
            merged.addTrack(AnimationClip.copyTrack(track));
        }
        for (String effect : base.getEffects()) {
            merged.addEffect(effect);
        }
        for (String effect : overlay.getEffects()) {
            merged.addEffect(effect);
        }
        AnimationClip.mergeTimedEffectsInto(merged, base.getTimelineEffects(), false);
        AnimationClip.mergeTimedEffectsInto(merged, overlay.getTimelineEffects(), false);
        AnimationClip.mergeTimedEffectsInto(merged, base.getSoundEffects(), true);
        AnimationClip.mergeTimedEffectsInto(merged, overlay.getSoundEffects(), true);
        return merged;
    }

    private static void mergeTimedEffectsInto(AnimationClip target, NavigableMap<Float, Set<String>> timedEffects, boolean sound) {
        if (target == null || timedEffects == null || timedEffects.isEmpty()) {
            return;
        }
        for (Map.Entry entry : timedEffects.entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
            for (String effect : (Set)entry.getValue()) {
                if (sound) {
                    target.addSoundEffect(((Float)entry.getKey()).floatValue(), effect);
                    continue;
                }
                target.addTimelineEffect(((Float)entry.getKey()).floatValue(), effect);
            }
        }
    }

    private static BoneTrack copyTrack(BoneTrack source) {
        BoneTrack copy = new BoneTrack(source.getBoneName());
        for (Keyframe keyframe : source.getKeyframes()) {
            copy.addKeyframe(AnimationClip.copyKeyframe(keyframe));
        }
        return copy;
    }

    private static Keyframe copyKeyframe(Keyframe source) {
        Vector3f position = source.position() == null ? null : new Vector3f((Vector3fc)source.position());
        Vector3f rotationEuler = source.rotationEuler() == null ? null : new Vector3f((Vector3fc)source.rotationEuler());
        Quaternionf rotation = source.rotation() == null ? null : new Quaternionf((Quaternionfc)source.rotation());
        Vector3f scale = source.scale() == null ? null : new Vector3f((Vector3fc)source.scale());
        return new Keyframe(source.timeStamp(), position, rotationEuler, rotation, scale, source.interpolation(), source.positionV2() == null ? null : new Vector3f((Vector3fc)source.positionV2()), source.rotationPreEulerV2() == null ? null : new Vector3f((Vector3fc)source.rotationPreEulerV2()), source.rotationPostEulerV2() == null ? null : new Vector3f((Vector3fc)source.rotationPostEulerV2()), source.positionExpression(), source.rotationExpression(), source.scaleExpression());
    }
}

