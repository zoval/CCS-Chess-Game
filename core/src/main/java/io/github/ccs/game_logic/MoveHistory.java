package io.github.ccs.game_logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Time-indexed history of {@link MoveSnapshot}s. Element 0 is the initial
 * position; each successful move pushes the next snapshot. Supports
 * view-only stepping back and forward; pushing after stepping back discards
 * the redo states.
 */
public final class MoveHistory {

    private final List<MoveSnapshot> snapshots = new ArrayList<MoveSnapshot>();
    private int currentIndex;

    /** Starts a fresh history whose only element is the initial snapshot. */
    public void reset(MoveSnapshot initial) {
        snapshots.clear();
        snapshots.add(initial);
        currentIndex = 0;
    }

    /** Replaces the whole history (used when loading a saved game). */
    public void replaceAll(List<MoveSnapshot> snapshots, int index) {
        this.snapshots.clear();
        this.snapshots.addAll(snapshots);
        currentIndex = Math.max(0, Math.min(index, this.snapshots.size() - 1));
    }

    /** Appends the snapshot as the new live state, discarding redo states. */
    public void push(MoveSnapshot snapshot) {
        while (snapshots.size() > currentIndex + 1) {
            snapshots.remove(snapshots.size() - 1);
        }
        snapshots.add(snapshot);
        currentIndex = snapshots.size() - 1;
    }

    /** @return the snapshot at the current view index. */
    public MoveSnapshot current() {
        return snapshots.get(currentIndex);
    }

    /** @return the previous snapshot and steps to it, or null if at the start. */
    public MoveSnapshot stepBack() {
        if (currentIndex <= 0) {
            return null;
        }
        return snapshots.get(--currentIndex);
    }

    /** @return the next snapshot and steps to it, or null if at the live state. */
    public MoveSnapshot stepForward() {
        if (currentIndex >= snapshots.size() - 1) {
            return null;
        }
        return snapshots.get(++currentIndex);
    }

    /** @return true if there is an earlier position to step back to. */
    public boolean canStepBack() {
        return currentIndex > 0;
    }

    /** @return true if there is a newer position to step forward to. */
    public boolean canStepForward() {
        return currentIndex < snapshots.size() - 1;
    }

    /** @return true if the current view is not the newest (live) state. */
    public boolean isViewingHistory() {
        return currentIndex < snapshots.size() - 1;
    }

    /** @return the current view index. */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /** @return the number of stored snapshots. */
    public int size() {
        return snapshots.size();
    }

    /** @return the snapshot at the given index. */
    public MoveSnapshot at(int index) {
        return snapshots.get(index);
    }

    /** @return an unmodifiable view of the full history. */
    public List<MoveSnapshot> snapshots() {
        return Collections.unmodifiableList(snapshots);
    }
}
