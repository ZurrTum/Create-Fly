package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.client.foundation.render.AllRenderPipelines;
import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle.Layer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;

public class CubeParticleRenderer extends ParticleGroup<CubeParticle> {
    public static final ParticleRenderType SHEET = new ParticleRenderType("create:cube");
    public static final Layer RENDER_TYPE = new Layer(false, PonderSpecialTextures.BLANK.getLocation(), AllRenderPipelines.CUBE);
    public CubeParticleSubmittable submittable = new CubeParticleSubmittable();

    public CubeParticleRenderer(ParticleEngine particleManager) {
        super(particleManager);
        Minecraft.getInstance().getTextureManager().getTexture(PonderSpecialTextures.BLANK.getLocation());
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float tickProgress) {
        for (CubeParticle particle : particles) {
            if (particle.shouldRender(frustum)) {
                try {
                    particle.render(submittable, camera, tickProgress);
                } catch (Throwable var9) {
                    CrashReport crashReport = CrashReport.forThrowable(var9, "Rendering Particle");
                    CrashReportCategory crashReportSection = crashReport.addCategory("Particle being rendered");
                    crashReportSection.setDetail("Particle", particle::toString);
                    crashReportSection.setDetail("Particle Type", RENDER_TYPE::toString);
                    throw new ReportedException(crashReport);
                }
            }
        }
        return submittable;
    }
}
