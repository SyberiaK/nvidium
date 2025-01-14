package me.cortex.nvidium.sodiumCompat;

import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

public class SodiumResultCompatibility {
    public static int getTotalGeometryQuadCount(ChunkBuildResult result) {
        return result.meshes.values().stream().mapToInt(a->(a.getVertexData().vertexBuffer().getLength()/20)/4).sum();
    }

    //Everything is /6*4 cause its in indices and we want verticies
    public static void uploadChunkGeometry(long uploadBuffer, short[] outOffsets, ChunkBuildResult result) {
        int formatSize = 20;
        int offset = 0;

        //Do translucent first
        var translucentData  = result.meshes.get(BlockRenderPass.TRANSLUCENT);
        if (translucentData != null) {
            for (int i = 0; i < 7; i++) {
                var segment = translucentData.getParts().get(ModelQuadFacing.values()[i]);
                if (segment != null) {
                    long srcVert = MemoryUtil.memAddress(translucentData.getVertexData().vertexBuffer().getDirectBuffer());
                    long srcIdx = segment.elementPointer() + MemoryUtil.memAddress(translucentData.getVertexData().indexBuffer().getDirectBuffer());
                    long dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6); j++) {
                        int a = MemoryUtil.memGetInt(srcIdx+4*6*j);
                        if (MemoryUtil.memGetInt(srcIdx+4*6*j+4) == a+1) {
                            MemoryUtil.memCopy(srcVert + (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize,
                                    dst,
                                    formatSize * 4);
                            dst += formatSize * 4;
                        } else {
                            long base = (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize + srcVert;
                            MemoryUtil.memCopy(base,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 3*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 2*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                        }
                    }

                    //Update the meta bits of the model format
                    dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6)*4; j++) {
                        short flags = (short) 0b000;//No mipping, No alpha cut
                        MemoryUtil.memPutShort(dst+ (long) j *formatSize+ 6L, flags);//Note: the 6 here is the offset into the vertex format
                    }

                    offset += (segment.elementCount()/6);
                }
            }
        }
        outOffsets[7] = (short) offset;


        var solid  = result.meshes.get(BlockRenderPass.SOLID);
        var cutout = result.meshes.get(BlockRenderPass.CUTOUT);
        var mipped = result.meshes.get(BlockRenderPass.CUTOUT_MIPPED);
        //Do all but translucent
        for (int i = 0; i < 7; i++) {
            int poff = offset;
            if (solid != null) {
                //TODO Optimize from .values()
                var segment = solid.getParts().get(ModelQuadFacing.values()[i]);
                if (segment != null) {
                    long srcVert = MemoryUtil.memAddress(solid.getVertexData().vertexBuffer().getDirectBuffer());
                    long srcIdx = segment.elementPointer() + MemoryUtil.memAddress(solid.getVertexData().indexBuffer().getDirectBuffer());
                    long dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6); j++) {
                        int a = MemoryUtil.memGetInt(srcIdx+4*6*j);
                        if (MemoryUtil.memGetInt(srcIdx+4*6*j+4) == a+1) {
                            MemoryUtil.memCopy(srcVert + (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize,
                                    dst,
                                    formatSize * 4);
                            dst += formatSize * 4;
                        } else {
                            long base = (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize + srcVert;
                            MemoryUtil.memCopy(base,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 3*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 2*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                        }
                    }
                    //Update the meta bits of the model format
                    dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6)*4; j++) {
                        short flags = (short) 0b100;//Yes mipping, No alpha cut
                        MemoryUtil.memPutShort(dst+ (long) j *formatSize+ 6L, flags);//Note: the 6 here is the offset into the vertex format
                    }

                    offset += (segment.elementCount()/6);
                }
            }
            if (cutout != null) {
                var segment = cutout.getParts().get(ModelQuadFacing.values()[i]);
                if (segment != null) {
                    long srcVert = MemoryUtil.memAddress(cutout.getVertexData().vertexBuffer().getDirectBuffer());
                    long srcIdx = segment.elementPointer() + MemoryUtil.memAddress(cutout.getVertexData().indexBuffer().getDirectBuffer());
                    long dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6); j++) {
                        int a = MemoryUtil.memGetInt(srcIdx+4*6*j);
                        if (MemoryUtil.memGetInt(srcIdx+4*6*j+4) == a+1) {
                            MemoryUtil.memCopy(srcVert + (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize,
                                    dst,
                                    formatSize * 4);
                            dst += formatSize * 4;
                        } else {
                            long base = (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize + srcVert;
                            MemoryUtil.memCopy(base,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 3*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 2*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                        }
                    }
                    //Update the meta bits of the model format
                    dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6)*4; j++) {
                        short flags = (short) 0b001;//No mipping, 0.1 alpha cut
                        MemoryUtil.memPutShort(dst+ (long) j *formatSize+ 6L, flags);//Note: the 6 here is the offset into the vertex format
                    }

                    offset += (segment.elementCount()/6);
                }
            }
            if (mipped != null) {
                var segment = mipped.getParts().get(ModelQuadFacing.values()[i]);
                if (segment != null) {
                    long srcVert = MemoryUtil.memAddress(mipped.getVertexData().vertexBuffer().getDirectBuffer());
                    long srcIdx = segment.elementPointer() + MemoryUtil.memAddress(mipped.getVertexData().indexBuffer().getDirectBuffer());
                    long dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6); j++) {
                        int a = MemoryUtil.memGetInt(srcIdx+4*6*j);
                        if (MemoryUtil.memGetInt(srcIdx+4*6*j+4) == a+1) {
                            MemoryUtil.memCopy(srcVert + (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize,
                                    dst,
                                    formatSize * 4);
                            dst += formatSize * 4;
                        } else {
                            long base = (long) MemoryUtil.memGetInt(srcIdx + 4 * 6 * j) * formatSize + srcVert;
                            MemoryUtil.memCopy(base,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 3*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + 2*formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                            MemoryUtil.memCopy(base + formatSize,
                                    dst,
                                    formatSize);
                            dst += formatSize;
                        }
                    }
                    //Update the meta bits of the model format
                    dst = uploadBuffer + offset * 4L * formatSize;
                    for (int j = 0; j < (segment.elementCount()/6)*4; j++) {
                        short flags = (short)(IrisCheck.IRIS_LOADED?0b101: 0b110);//mipping, 0.5 cut without iris 0.1 with iris
                        MemoryUtil.memPutShort(dst+ (long) j *formatSize+ 6L, flags);//Note: the 6 here is the offset into the vertex format
                    }

                    offset += (segment.elementCount()/6);
                }
            }
            outOffsets[i] = (short) (offset - poff);
        }
    }

    //TODO: FIXME: dont use these bounds as they are not accurate (e.g. grass can take up multiple blocks cause vertices extend outside of block)
    public static Vector3i getMinBounds(ChunkBuildResult result) {
        int mx = (int) (result.data.getBounds().x1 + 0.5f - result.render.getOriginX());
        int my = (int) (result.data.getBounds().y1 + 0.5f - result.render.getOriginY());
        int mz = (int) (result.data.getBounds().z1 + 0.5f - result.render.getOriginZ());
        mx = Math.min(15, mx);
        my = Math.min(15, my);
        mz = Math.min(15, mz);
        return new Vector3i(mx,my,mz);
    }

    //Note: this is adjusted since you cant ever have a size == 0 (the chunk would be air)
    // so its size -1
    public static Vector3i getSizeBounds(ChunkBuildResult result) {
        int sx = (int)Math.ceil(result.data.getBounds().x2-result.data.getBounds().x1-1);
        int sy = (int)Math.ceil(result.data.getBounds().y2-result.data.getBounds().y1-1);
        int sz = (int)Math.ceil(result.data.getBounds().z2-result.data.getBounds().z1-1);
        sx--;
        sy--;
        sz--;
        sx = Math.max(0, sx);
        sy = Math.max(0, sy);
        sz = Math.max(0, sz);
        sx = Math.min(15, sx);
        sy = Math.min(15, sy);
        sz = Math.min(15, sz);
        return new Vector3i(sx,sy,sz);
    }
}
