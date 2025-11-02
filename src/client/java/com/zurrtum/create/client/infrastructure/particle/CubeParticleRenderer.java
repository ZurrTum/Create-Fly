package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.client.foundation.render.AllRenderPipelines;
import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BillboardParticle.RenderType;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Submittable;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

public class CubeParticleRenderer extends ParticleRenderer<CubeParticle> {
    public static final ParticleTextureSheet SHEET = new ParticleTextureSheet("create:cube");
    public static final RenderType RENDER_TYPE = new RenderType(false, PonderSpecialTextures.BLANK.getLocation(), AllRenderPipelines.CUBE);
    public CubeParticleSubmittable submittable = new CubeParticleSubmittable();

    public CubeParticleRenderer(ParticleManager particleManager) {
        super(particleManager);
        MinecraftClient.getInstance().getTextureManager().getTexture(PonderSpecialTextures.BLANK.getLocation());
    }

    @Override
    public Submittable render(Frustum frustum, Camera camera, float tickProgress) {
        for (CubeParticle particle : particles) {
            if (particle.shouldRender(frustum)) {
                try {
                    particle.render(submittable, camera, tickProgress);
                } catch (Throwable var9) {
                    CrashReport crashReport = CrashReport.create(var9, "Rendering Particle");
                    CrashReportSection crashReportSection = crashReport.addElement("Particle being rendered");
                    crashReportSection.add("Particle", particle::toString);
                    crashReportSection.add("Particle Type", RENDER_TYPE::toString);
                    throw new CrashException(crashReport);
                }
            }
        }
        return submittable;
    }
}
