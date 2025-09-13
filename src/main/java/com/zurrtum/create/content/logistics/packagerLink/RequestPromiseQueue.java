package com.zurrtum.create.content.logistics.packagerLink;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

public class RequestPromiseQueue {
    public static final Codec<RequestPromiseQueue> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.list(RequestPromise.CODEC)
        .fieldOf("List").forGetter(i -> i.flatten(false))).apply(instance, RequestPromiseQueue::new));
    private final Map<Item, List<RequestPromise>> promisesByItem;
    private Runnable onChanged;

    public RequestPromiseQueue(Runnable onChanged) {
        promisesByItem = new IdentityHashMap<>();
        this.onChanged = onChanged;
    }

    private RequestPromiseQueue(List<RequestPromise> list) {
        this((Runnable) null);
        list.forEach(promise -> promisesByItem.computeIfAbsent(promise.promisedStack.stack.getItem(), $ -> new LinkedList<>()).add(promise));
    }

    public void add(RequestPromise promise) {
        promisesByItem.computeIfAbsent(promise.promisedStack.stack.getItem(), $ -> new LinkedList<>()).add(promise);
        onChanged.run();
    }

    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public int getTotalPromisedAndRemoveExpired(ItemStack stack, int expiryTime) {
        int promised = 0;
        List<RequestPromise> list = promisesByItem.get(stack.getItem());
        if (list == null)
            return promised;

        for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext(); ) {
            RequestPromise promise = iterator.next();
            if (!ItemStack.areItemsAndComponentsEqual(promise.promisedStack.stack, stack))
                continue;
            if (expiryTime != -1 && promise.ticksExisted >= expiryTime) {
                iterator.remove();
                onChanged.run();
                continue;
            }

            promised += promise.promisedStack.count;
        }
        return promised;
    }

    public void forceClear(ItemStack stack) {
        List<RequestPromise> list = promisesByItem.get(stack.getItem());
        if (list == null)
            return;

        for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext(); ) {
            RequestPromise promise = iterator.next();
            if (!ItemStack.areItemsAndComponentsEqual(promise.promisedStack.stack, stack))
                continue;
            iterator.remove();
            onChanged.run();
        }

        if (list.isEmpty())
            promisesByItem.remove(stack.getItem());
    }

    public void itemEnteredSystem(ItemStack stack, int amount) {
        List<RequestPromise> list = promisesByItem.get(stack.getItem());
        if (list == null)
            return;

        for (Iterator<RequestPromise> iterator = list.iterator(); iterator.hasNext(); ) {
            RequestPromise requestPromise = iterator.next();
            if (!ItemStack.areItemsAndComponentsEqual(requestPromise.promisedStack.stack, stack))
                continue;

            int toSubtract = Math.min(amount, requestPromise.promisedStack.count);
            amount -= toSubtract;
            requestPromise.promisedStack.count -= toSubtract;

            if (requestPromise.promisedStack.count <= 0) {
                iterator.remove();
                onChanged.run();
            }
            if (amount <= 0)
                break;
        }

        if (list.isEmpty())
            promisesByItem.remove(stack.getItem());
    }

    public List<RequestPromise> flatten(boolean sorted) {
        List<RequestPromise> all = new ArrayList<>();
        promisesByItem.forEach((key, list) -> all.addAll(list));
        if (sorted)
            all.sort(RequestPromise.ageComparator());
        return all;
    }

    public void tick() {
        promisesByItem.forEach((key, list) -> list.forEach(RequestPromise::tick)); // delete old entries?
    }

    public boolean isEmpty() {
        return promisesByItem.isEmpty();
    }
}
