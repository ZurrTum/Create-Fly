package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.backend.Samplers;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.model.ModelBakery;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class CrumblingMap {
    private final Deque<Node> nodes;
    private final Deque<Deque<AbstractDraw>> draws;
    private final Deque<Node>[] map;
    private @UnknownNullability AbstractTexture[] textures;
    private int flag;

    @SuppressWarnings("unchecked")
    public CrumblingMap(Deque<Node> nodes, Deque<Deque<AbstractDraw>> draws, AbstractTexture[] textures) {
        this.nodes = nodes;
        this.draws = draws;
        map = (Deque<Node>[]) new Deque[ModelBakery.DESTROY_STAGE_COUNT];
        for (int i = 0; i < ModelBakery.DESTROY_STAGE_COUNT; i++) {
            map[i] = new ArrayDeque<>();
        }
        this.textures = textures;
    }

    public CrumblingMap update(AbstractTexture[] textures) {
        this.textures = textures;
        return this;
    }

    public int flag() {
        return flag;
    }

    public void add(int key, int flag, Collection<? extends AbstractDraw> draws, int index) {
        Node node = nodes.pollFirst();
        if (node == null) {
            node = new Node();
        }
        node.draws = draws;
        node.index = index;
        map[key].addLast(node);
        this.flag |= flag;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDraw> void drain(IntObjectBiConsumer<T> consumer) {
        int flag = this.flag;
        assert flag != 0;
        do {
            int i = Integer.numberOfTrailingZeros(flag);
            AbstractTexture texture = textures[i];
            TextureBinder.setupTexture(Samplers.CRUMBLING, texture.getTextureView(), texture.getSampler());
            Deque<Node> deque = map[i];
            Node node = deque.pollFirst();
            do {
                assert node != null;
                int index = node.index;
                for (T draw : (Collection<T>) node.draws) {
                    consumer.accept(index, draw);
                }
                node.draws = null;
                nodes.addLast(node);
                node = deque.pollFirst();
            } while (node != null);
            flag &= flag - 1;
        } while (flag != 0);
        this.flag = 0;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDraw> void drainOwned(IntObjectBiConsumer<T> consumer) {
        int flag = this.flag;
        assert flag != 0;
        do {
            int i = Integer.numberOfTrailingZeros(flag);
            AbstractTexture texture = textures[i];
            TextureBinder.setupTexture(Samplers.CRUMBLING, texture.getTextureView(), texture.getSampler());
            Deque<Node> deque = map[i];
            Node node = deque.pollFirst();
            do {
                assert node != null;
                int index = node.index;
                Deque<T> drain = (Deque<T>) node.draws;
                T draw = drain.pollFirst();
                do {
                    assert draw != null;
                    consumer.accept(index, draw);
                    draw = drain.pollFirst();
                } while (draw != null);
                draws.addLast((Deque<AbstractDraw>) drain);
                node.draws = null;
                nodes.addLast(node);
                node = deque.pollFirst();
            } while (node != null);
            flag &= flag - 1;
        } while (flag != 0);
        this.flag = 0;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDraw> void forEach(IntObjectBiConsumer<T> consumer) {
        int flag = this.flag;
        assert flag != 0;
        do {
            int i = Integer.numberOfTrailingZeros(flag);
            AbstractTexture texture = textures[i];
            TextureBinder.setupTexture(Samplers.CRUMBLING, texture.getTextureView(), texture.getSampler());
            for (Node node : map[i]) {
                int index = node.index;
                for (T draw : (Collection<T>) node.draws) {
                    consumer.accept(index, draw);
                }
            }
            flag &= flag - 1;
        } while (flag != 0);
    }

    public void clear() {
        int flag = this.flag;
        assert flag != 0;
        do {
            int i = Integer.numberOfTrailingZeros(flag);
            Deque<Node> deque = map[i];
            Node node = deque.pollFirst();
            do {
                assert node != null;
                node.draws = null;
                nodes.addLast(node);
                node = deque.pollFirst();
            } while (node != null);
            flag &= flag - 1;
        } while (flag != 0);
        this.flag = 0;
    }

    @SuppressWarnings("unchecked")
    public void clearOwned() {
        int flag = this.flag;
        assert flag != 0;
        do {
            int i = Integer.numberOfTrailingZeros(flag);
            Deque<Node> deque = map[i];
            Node node = deque.pollFirst();
            do {
                assert node != null;
                Collection<? extends AbstractDraw> draws = node.draws;
                draws.clear();
                this.draws.addLast((Deque<AbstractDraw>) draws);
                node.draws = null;
                nodes.addLast(node);
                node = deque.pollFirst();
            } while (node != null);
            flag &= flag - 1;
        } while (flag != 0);
        this.flag = 0;
    }

    public static class Node {
        public @UnknownNullability Collection<? extends AbstractDraw> draws;
        public int index;
    }
}
