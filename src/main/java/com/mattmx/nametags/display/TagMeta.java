package com.mattmx.nametags.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class TagMeta {

    public enum BillboardConstraints {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER
    }

    private static final byte SHADOW = 0x01;
    private static final byte SEE_THROUGH = 0x02;
    private static final byte DEFAULT_BACKGROUND = 0x04;

    private Component text = Component.empty();
    private int lineWidth = 200;
    private int backgroundColor = 0x40000000;
    private byte textOpacity = -1;
    private byte styleFlags;
    private Vector3f translation = Vector3f.zero();
    private Vector3f scale = new Vector3f(1f, 1f, 1f);
    private BillboardConstraints billboardConstraints = BillboardConstraints.FIXED;
    private int brightnessOverride = -1;
    private float viewRange = 1f;
    private float shadowRadius;
    private float shadowStrength = 1f;
    private int transformationInterpolationDuration;
    private int positionRotationInterpolationDuration;
    private boolean hidden;
    private boolean sneaking;
    private int sneakOpacity = 0x46;
    private int textVersion;

    private int sentLineWidth = lineWidth;
    private int sentBackgroundColor = backgroundColor;
    private byte sentTextOpacity = textOpacity;
    private byte sentStyleFlags = styleFlags;
    private Vector3f sentTranslation = translation;
    private Vector3f sentScale = scale;
    private BillboardConstraints sentBillboardConstraints = billboardConstraints;
    private int sentBrightnessOverride = brightnessOverride;
    private float sentViewRange = viewRange;
    private float sentShadowRadius = shadowRadius;
    private float sentShadowStrength = shadowStrength;
    private int sentTransformationInterpolationDuration = transformationInterpolationDuration;
    private int sentPositionRotationInterpolationDuration = positionRotationInterpolationDuration;

    public synchronized @NotNull Component getText() {
        return text;
    }

    public synchronized void setText(@NotNull Component text) {
        if (!this.text.equals(text)) {
            this.text = text;
            this.textVersion++;
        }
    }

    synchronized int textVersion() {
        return textVersion;
    }

    public synchronized int getLineWidth() {
        return lineWidth;
    }

    public synchronized void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public synchronized int getBackgroundColor() {
        return backgroundColor;
    }

    public synchronized void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public synchronized byte getTextOpacity() {
        return textOpacity;
    }

    public synchronized void setTextOpacity(byte textOpacity) {
        this.textOpacity = textOpacity;
    }

    public synchronized boolean isShadow() {
        return (styleFlags & SHADOW) != 0;
    }

    public synchronized void setShadow(boolean shadow) {
        styleFlags = flag(styleFlags, SHADOW, shadow);
    }

    public synchronized boolean isSeeThrough() {
        return (styleFlags & SEE_THROUGH) != 0;
    }

    public synchronized void setSeeThrough(boolean seeThrough) {
        styleFlags = flag(styleFlags, SEE_THROUGH, seeThrough);
    }

    public synchronized boolean isUseDefaultBackground() {
        return (styleFlags & DEFAULT_BACKGROUND) != 0;
    }

    public synchronized void setUseDefaultBackground(boolean useDefaultBackground) {
        styleFlags = flag(styleFlags, DEFAULT_BACKGROUND, useDefaultBackground);
    }

    public synchronized @NotNull Vector3f getTranslation() {
        return translation;
    }

    public synchronized void setTranslation(@NotNull Vector3f translation) {
        this.translation = translation;
    }

    public synchronized @NotNull Vector3f getScale() {
        return scale;
    }

    public synchronized void setScale(@NotNull Vector3f scale) {
        this.scale = scale;
    }

    public synchronized @NotNull BillboardConstraints getBillboardConstraints() {
        return billboardConstraints;
    }

    public synchronized void setBillboardConstraints(@NotNull BillboardConstraints billboardConstraints) {
        this.billboardConstraints = billboardConstraints;
    }

    public synchronized int getBrightnessOverride() {
        return brightnessOverride;
    }

    public synchronized void setBrightnessOverride(int brightnessOverride) {
        this.brightnessOverride = brightnessOverride;
    }

    public synchronized float getViewRange() {
        return viewRange;
    }

    public synchronized void setViewRange(float viewRange) {
        this.viewRange = viewRange;
    }

    public synchronized float getShadowRadius() {
        return shadowRadius;
    }

    public synchronized void setShadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
    }

    public synchronized float getShadowStrength() {
        return shadowStrength;
    }

    public synchronized void setShadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
    }

    public synchronized int getTransformationInterpolationDuration() {
        return transformationInterpolationDuration;
    }

    public synchronized void setTransformationInterpolationDuration(int ticks) {
        this.transformationInterpolationDuration = ticks;
    }

    public synchronized int getPositionRotationInterpolationDuration() {
        return positionRotationInterpolationDuration;
    }

    public synchronized void setPositionRotationInterpolationDuration(int ticks) {
        this.positionRotationInterpolationDuration = ticks;
    }

    public synchronized boolean isHidden() {
        return hidden;
    }

    public synchronized void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public synchronized boolean isSneaking() {
        return sneaking;
    }

    public synchronized void setSneaking(boolean sneaking, int opacity) {
        this.sneaking = sneaking;
        this.sneakOpacity = opacity & 0xFF;
    }

    synchronized void collectAll(@NotNull List<EntityData<?>> out) {
        add(out, DisplayIndices.TRANSFORMATION_INTERPOLATION, EntityDataTypes.INT, transformationInterpolationDuration);
        if (DisplayIndices.POSITION_ROTATION_INTERPOLATION_PRESENT) {
            add(out, DisplayIndices.POSITION_ROTATION_INTERPOLATION, EntityDataTypes.INT, positionRotationInterpolationDuration);
        }
        add(out, DisplayIndices.TRANSLATION, EntityDataTypes.VECTOR3F, translation);
        add(out, DisplayIndices.SCALE, EntityDataTypes.VECTOR3F, scale);
        add(out, DisplayIndices.BILLBOARD, EntityDataTypes.BYTE, (byte) billboardConstraints.ordinal());
        add(out, DisplayIndices.BRIGHTNESS, EntityDataTypes.INT, brightnessOverride);
        add(out, DisplayIndices.VIEW_RANGE, EntityDataTypes.FLOAT, sentViewRange());
        add(out, DisplayIndices.SHADOW_RADIUS, EntityDataTypes.FLOAT, shadowRadius);
        add(out, DisplayIndices.SHADOW_STRENGTH, EntityDataTypes.FLOAT, shadowStrength);
        add(out, DisplayIndices.LINE_WIDTH, EntityDataTypes.INT, lineWidth);
        add(out, DisplayIndices.BACKGROUND, EntityDataTypes.INT, sentBackground());
        add(out, DisplayIndices.TEXT_OPACITY, EntityDataTypes.BYTE, sentTextOpacity());
        add(out, DisplayIndices.STYLE_FLAGS, EntityDataTypes.BYTE, styleFlags);
    }

    synchronized void collectChanges(@NotNull List<EntityData<?>> out) {
        if (transformationInterpolationDuration != sentTransformationInterpolationDuration) {
            sentTransformationInterpolationDuration = transformationInterpolationDuration;
            add(out, DisplayIndices.TRANSFORMATION_INTERPOLATION, EntityDataTypes.INT, transformationInterpolationDuration);
        }
        if (positionRotationInterpolationDuration != sentPositionRotationInterpolationDuration) {
            sentPositionRotationInterpolationDuration = positionRotationInterpolationDuration;
            if (DisplayIndices.POSITION_ROTATION_INTERPOLATION_PRESENT) {
                add(out, DisplayIndices.POSITION_ROTATION_INTERPOLATION, EntityDataTypes.INT, positionRotationInterpolationDuration);
            }
        }
        if (!Objects.equals(translation, sentTranslation)) {
            sentTranslation = translation;
            add(out, DisplayIndices.TRANSLATION, EntityDataTypes.VECTOR3F, translation);
        }
        if (!Objects.equals(scale, sentScale)) {
            sentScale = scale;
            add(out, DisplayIndices.SCALE, EntityDataTypes.VECTOR3F, scale);
        }
        if (billboardConstraints != sentBillboardConstraints) {
            sentBillboardConstraints = billboardConstraints;
            add(out, DisplayIndices.BILLBOARD, EntityDataTypes.BYTE, (byte) billboardConstraints.ordinal());
        }
        if (brightnessOverride != sentBrightnessOverride) {
            sentBrightnessOverride = brightnessOverride;
            add(out, DisplayIndices.BRIGHTNESS, EntityDataTypes.INT, brightnessOverride);
        }
        float viewRange = sentViewRange();
        if (Float.compare(viewRange, sentViewRange) != 0) {
            sentViewRange = viewRange;
            add(out, DisplayIndices.VIEW_RANGE, EntityDataTypes.FLOAT, viewRange);
        }
        if (Float.compare(shadowRadius, sentShadowRadius) != 0) {
            sentShadowRadius = shadowRadius;
            add(out, DisplayIndices.SHADOW_RADIUS, EntityDataTypes.FLOAT, shadowRadius);
        }
        if (Float.compare(shadowStrength, sentShadowStrength) != 0) {
            sentShadowStrength = shadowStrength;
            add(out, DisplayIndices.SHADOW_STRENGTH, EntityDataTypes.FLOAT, shadowStrength);
        }
        if (lineWidth != sentLineWidth) {
            sentLineWidth = lineWidth;
            add(out, DisplayIndices.LINE_WIDTH, EntityDataTypes.INT, lineWidth);
        }
        int background = sentBackground();
        if (background != sentBackgroundColor) {
            sentBackgroundColor = background;
            add(out, DisplayIndices.BACKGROUND, EntityDataTypes.INT, background);
        }
        byte opacity = sentTextOpacity();
        if (opacity != sentTextOpacity) {
            sentTextOpacity = opacity;
            add(out, DisplayIndices.TEXT_OPACITY, EntityDataTypes.BYTE, opacity);
        }
        if (styleFlags != sentStyleFlags) {
            sentStyleFlags = styleFlags;
            add(out, DisplayIndices.STYLE_FLAGS, EntityDataTypes.BYTE, styleFlags);
        }
    }

    private float sentViewRange() {
        return hidden ? 0f : viewRange;
    }

    private int sentBackground() {
        if (!sneaking || (backgroundColor >>> 24) == 0) {
            return backgroundColor;
        }
        return (backgroundColor & 0x00FFFFFF) | (sneakOpacity << 24);
    }

    private byte sentTextOpacity() {
        return sneaking ? (byte) sneakOpacity : textOpacity;
    }

    private static <T> void add(@NotNull List<EntityData<?>> out, int modernIndex, @NotNull EntityDataType<T> type, @NotNull T value) {
        out.add(new EntityData<>(DisplayIndices.onServer(modernIndex), type, value));
    }

    private static byte flag(byte flags, byte bit, boolean set) {
        return (byte) (set ? flags | bit : flags & ~bit);
    }
}
