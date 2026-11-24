package com.cranks.dungeons.client;

import com.cranks.dungeons.particle.ModParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public final class ModParticleProviders {
    private ModParticleProviders() {}

    public static void register() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.POISON_BUBBLE, sprites ->
                (parameters, world, x, y, z, vx, vy, vz) -> new ToxicBubble(world,sprites,x,y,z,vx,vy,vz));
        mote(ModParticles.POISON_DROPLET, true, false, .200f, 16);
        mote(ModParticles.BLEED_SPLASH_1, true, true, .080f, 16);
        mote(ModParticles.BLEED_SPLASH_2, true, true, .080f, 16);
        mote(ModParticles.BLEED_SPLASH_3, true, true, .080f, 16);
        mote(ModParticles.BLEED_SPLASH_4, true, true, .080f, 16);
        mote(ModParticles.SHOCK_AMBIENT_1, false, false, .100f, 8);
        mote(ModParticles.SHOCK_AMBIENT_2, false, false, .100f, 8);
        mote(ModParticles.SHOCK_AMBIENT_3, false, false, .100f, 8);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_1, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_2, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_3, EndRodParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SHOCK_SPARK_4, EndRodParticle.Factory::new);
    }

    private static void mote(SimpleParticleType type, boolean falling, boolean blood, float size, int lifetime) {
        boolean electric=type==ModParticles.SHOCK_AMBIENT_1 || type==ModParticles.SHOCK_AMBIENT_2 || type==ModParticles.SHOCK_AMBIENT_3;
        boolean poison=type==ModParticles.POISON_DROPLET;
        ParticleFactoryRegistry.getInstance().register(type, sprites -> (parameters, world, x, y, z, vx, vy, vz) ->
                new Mote(world, sprites, x, y, z, vx, vy, vz, falling, blood, electric, poison, size, lifetime));
    }

    /** Rise and pop in one particle, preserving the shared emission budget. */
    private static final class ToxicBubble extends SpriteBillboardParticle {
        private final SpriteProvider sprites;
        private final int riseTicks;
        private final float bubbleSize;

        ToxicBubble(ClientWorld world,SpriteProvider sprites,double x,double y,double z,double vx,double vy,double vz) {
            super(world,x,y,z);
            this.sprites=sprites;
            riseTicks=28+world.random.nextInt(13);
            maxAge=riseTicks+5;
            bubbleSize=.095f+world.random.nextFloat()*.04f;
            scale=bubbleSize;
            velocityX=vx;velocityY=Math.max(.035,vy);velocityZ=vz;
            velocityMultiplier=.99f;
            gravityStrength=0;
            collidesWithWorld=false;
            // The bubble already contains its green shading; tint only the vanilla pop frames.
            setAlpha(.85f);
            setSprite(sprites.getSprite(0,5));
            setBoundingBoxSpacing(.02f,.02f);
        }

        @Override public void tick() {
            super.tick();
            int popping=age-riseTicks;
            if(popping>=0) {
                setSprite(sprites.getSprite(Math.min(5,popping+1),5));
                setColor(.4f,.75f,.22f);
                scale=bubbleSize*(1+popping*.09f);
                setAlpha(.85f*(1-popping/5f));
                velocityY*=.55;
            }
        }
        @Override public ParticleTextureSheet getType() {return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;}
    }

    /** Actual liquid gravity and short fading lifetimes; no End Rod random upward drift. */
    private static final class Mote extends SpriteBillboardParticle {
        private final boolean electric;
        Mote(ClientWorld world, SpriteProvider sprites, double x, double y, double z,
             double vx, double vy, double vz, boolean falling, boolean blood, boolean electric, boolean poison, float size, int lifetime) {
            // Vanilla drip_fall's filled pixels sit above the sprite centre; align those pixels with the head source.
            super(world, x, poison?y-size*.625:y, z);
            this.electric=electric;
            setSprite(sprites);
            velocityX = vx;
            velocityY = vy;
            velocityZ = vz;
            gravityStrength = falling ? (blood?.60f:.45f) : 0;
            velocityMultiplier = falling ? .96f : .91f;
            scale = size;
            maxAge = lifetime + world.random.nextInt(4);
            if (blood) setColor(.82f, .52f, .52f);
            if (poison) setColor(.45f, .72f, .25f);
            setBoundingBoxSpacing(.02f, .02f);
        }

        @Override public void tick() {
            super.tick();
            setAlpha(Math.clamp((maxAge - age) / 6f,0,.85f));
        }

        @Override public ParticleTextureSheet getType() { return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT; }
        @Override public int getBrightness(float tickDelta) { return electric?15728880:super.getBrightness(tickDelta); }
    }
}
