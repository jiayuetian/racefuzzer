package javato.activetesting;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.analysis.Observer;
import javato.activetesting.common.Parameters;
import javato.activetesting.reentrant.IgnoreRentrantLock;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
MEM(σ, m, a, t, L) denotes that thread t performed an access a ∈ { WRITE, READ } to memory location m while holding the set of locks L and executing the statement σ.
• SND(g, t) denotes the sending of a message with unique id g by thread t.
• RCV(g,t) denotes the reception of a message with unique id g by thread t.
 */
public class HybridAnalysis extends AnalysisImpl {
//    need to declare data structures
//    In my implementation I had the following datastructure
//    private VectorClockTracker vcTracker;
//    private LockSet lsTracker;
//    private IgnoreRentrantLock ignoreRentrantLock;
//    private HybridRaceTracker eb;
    private VectorClockTracker vcTracker;

    private LockSetManager lockSetManager;

    private MemoryManager memoryManager;


    public void initialize() {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            vcTracker = new VectorClockTracker();
//            lsTracker = new LockSet();
//            ignoreRentrantLock = new IgnoreRentrantLock();
//            eb = new HybridRaceTracker();
            vcTracker = new VectorClockTracker();
            lockSetManager = new LockSetManager();
            memoryManager = new MemoryManager();
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            if (ignoreRentrantLock.lockBefore(thread, lock)) {
//                boolean isDeadlock = lsTracker.lockBefore(iid, thread, lock);
//            }
            lockSetManager.addLock(thread, lock);
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            if (ignoreRentrantLock.unlockAfter(thread, lock)) {
//                lsTracker.unlockAfter(thread);
//            }
            lockSetManager.removeLock(thread, lock);
        }
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
//  ignore this
    }

    public void methodEnterBefore(Integer iid) {
//  ignore this
    }

    public void methodExitAfter(Integer iid) {
//  ignore this
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            vcTracker.startBefore(parent, child);
            VectorClock vc = vcTracker.getClock(parent).copy();
            vcTracker.inc(parent);
            vcTracker.updateClock(child, vc);
            vcTracker.inc(child);
        }
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            vcTracker.waitAfter(thread, lock);
            vcTracker.wait(thread, lock);
            vcTracker.inc(thread);
        }
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            vcTracker.notifyBefore(thread, lock);
            // Create new message, and notify everyone
            vcTracker.notify(thread, lock);
            vcTracker.inc(thread);
        }
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            vcTracker.notifyBefore(thread, lock);
            vcTracker.notify(thread, lock);
            vcTracker.inc(thread);
        }
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            vcTracker.joinAfter(parent, child);
            vcTracker.updateClock(parent, vcTracker.getClock(child));
            vcTracker.inc(parent);
        }
    }

    public void readBefore(Integer iid, Integer thread, Long memory) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            LockSet ls = lsTracker.getLockSet(thread);
//            eb.checkRace(iid, thread, memory, true, vcTracker.getVectorClock(thread), ls);
//            eb.addEvent(iid, thread, memory, true, vcTracker.getVectorClock(thread), ls);

            LockSet ls = lockSetManager.getLockSet(thread);
            // Check if adding this event would cause a race condition
            MemoryAccess memoryAccess = new MemoryAccess(
                    iid, RWTYPE.READ, memory, thread, ls.copy(), vcTracker.getClock(thread).copy()
            );
            memoryManager.reportRaces(memoryAccess);
            memoryManager.addAccess(memoryAccess);
            vcTracker.inc(thread);
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory) {
        synchronized (ActiveChecker.lock) {
//    Your code goes here.
//    In my implementation I had the following code:
//            LockSet ls = lsTracker.getLockSet(thread);
//            eb.checkRace(iid, thread, memory, false, vcTracker.getVectorClock(thread), ls);
//            eb.addEvent(iid, thread, memory, false, vcTracker.getVectorClock(thread), ls);

            LockSet ls = lockSetManager.getLockSet(thread);
            // Check if adding this event would cause a race condition
            MemoryAccess memoryAccess = new MemoryAccess(
                    iid, RWTYPE.WRITE, memory, thread, ls.copy(), vcTracker.getClock(thread).copy()
            );
            memoryManager.reportRaces(memoryAccess);
            memoryManager.addAccess(memoryAccess);
            vcTracker.inc(thread);
        }
    }

    public void finish() {
        synchronized (ActiveChecker.lock) {
            int nRaces = memoryManager.races.size(); // nRaces must be equal to the number races detected by the hybrid race detector
//    Your code goes here.
//    In my implementation I had the following code:
//            nRaces = eb.dumpRaces();
//    The following method call creates a file "error.list" containing the list of numbers "1,2,3,...,nRaces"
//    This file is used by run.xml to initialize Parameters.errorId with a number from from the list.
//    Parameters.errorId tells RaceFuzzer the id of the race that RaceFuzzer should try to create
            Parameters.writeIntegerList(Parameters.ERROR_LIST_FILE, nRaces);

            // Code snipped from http://www.vogella.com/tutorials/JavaSerialization/article.html
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            try {
                fos = new FileOutputStream(Parameters.ERROR_LOG_FILE);
                out = new ObjectOutputStream(fos);
                out.writeObject(new ArrayList<Set<Integer>>(memoryManager.races));
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class VectorClock extends HashMap<Integer, Integer> {
        int getOrZero(int tid) {
            if (this.containsKey(tid)) return this.get(tid);
            return 0;
        }

        VectorClock copy() {
            VectorClock vc = new VectorClock();
            vc.putAll(this);
            return vc;
        }
    }

    private static class VectorClockTracker {
        // Map[i] is the VC for thread i
        private Map<Integer, VectorClock> clocks = new HashMap<Integer, VectorClock>();
        private Map<Integer, VectorClock> notifications = new HashMap<Integer, VectorClock>();

        public void notify(int thread, int key) {
            notifications.put(key, getClock(thread).copy());
        }

        public void wait(int thread, int key) {
            updateClock(thread, notifications.get(key));
        }

        public void inc(int thread) {
            VectorClock vectorClock = getClock(thread);
            vectorClock.put(thread,vectorClock.getOrZero(thread) + 1);
        }

        public VectorClock getClock(int thread) {
            if (!clocks.containsKey(thread)) {
                clocks.put(thread, new VectorClock());
            }
            return clocks.get(thread);
        }

        public void updateClock(int thread, VectorClock vc) {

            VectorClock vectorClock = getClock(thread);
            for (Integer tid : vc.keySet()) {
                int val = vectorClock.containsKey(tid) ? vectorClock.get(tid) : 0;  // default to 0
                int newVal = vc.get(tid);
                vectorClock.put(tid, Math.max(val, newVal));
            }
        }
    }

    private static class LockSet extends LinkedHashSet<Integer> {
        public LockSet copy() {
            LockSet ls = new LockSet();
            ls.addAll(this);
            return ls;
        }

        public LockSet intersection(LockSet other) {
            LockSet ls = this.copy();
            ls.retainAll(other);
            return ls;
        }
    }

    private static class LockSetManager {
        private Map<Integer, LockSet> lockSetMap = new HashMap<Integer, LockSet>();
        public boolean addLock(int thread, int lock) {
            LockSet lockSet = getLockSet(thread);
            if (lockSet.contains(lock)) {
                return false;
            }
            lockSet.add(lock);
            return true;
        }

        public boolean removeLock(int thread, int lock) {
            LockSet lockSet = getLockSet(thread);
            if (!lockSet.contains(lock)) {
                return false;
            }
            lockSet.remove(lock);
            return true;
        }

        public LockSet getLockSet(int thread) {
            if (!lockSetMap.containsKey(thread)) {
                lockSetMap.put(thread, new LockSet());
            }
            return lockSetMap.get(thread);
        }
    }

    public enum RWTYPE {READ, WRITE}

    private static class MemoryAccess {
        public int iid;
        public RWTYPE rwtype;
        public long location;
        public int tid;
        public LockSet lockSet;
        public VectorClock vectorClock;

        public MemoryAccess(int iid, RWTYPE rwtype, long location, int tid, LockSet lockSet, VectorClock vectorClock) {
            this.iid = iid;
            this.rwtype = rwtype;
            this.location = location;
            this.tid = tid;
            this.lockSet = lockSet;
            this.vectorClock = vectorClock;
        }

        public boolean happensBefore(MemoryAccess other) {
            // whether or not we happen before other
            // True if our access is less than their vector clock timestamp
            int accessTime = this.vectorClock.getOrZero(tid);
            int lastRecv = other.vectorClock.getOrZero(tid);

            return accessTime < lastRecv;
        }
    }
    private static class MemoryManager {
        private Map<Long, List<MemoryAccess>> writeMap = new HashMap<Long, List<MemoryAccess>>(); // When this location is written to
        private Map<Long, List<MemoryAccess>> readMap = new HashMap<Long, List<MemoryAccess>>(); // When this location is read from
        // Report locations of races
        private Set<Set<Integer>> races = new HashSet<Set<Integer>>();

        private void _reportRaces(MemoryAccess memoryAccess, Map<Long, List<MemoryAccess>> set) {
            // Check for WRITE-READ collisions.
            // set is set of previous events to check against
            if (set.containsKey(memoryAccess.location)) {
                for (MemoryAccess previous : set.get(memoryAccess.location)) {
                    if (previous.tid == memoryAccess.tid) continue;
                    if (previous.lockSet.intersection(memoryAccess.lockSet).size() != 0) continue;
                    // a -> b iff a sent a message that b received.
                    if (!previous.happensBefore(memoryAccess) && !memoryAccess.happensBefore(previous)) {
                        // Actually a race
                        Set<Integer> newSet = new HashSet<Integer>();
                        newSet.add(previous.iid);
                        newSet.add(memoryAccess.iid);
//                        if (!races.contains(newSet)) {
//                            System.out.println(Observer.getIidToLine(previous.iid));
//                            System.out.println(Observer.getIidToLine(memoryAccess.iid));
//                            System.out.println(previous.vectorClock);
//                            System.out.println(memoryAccess.vectorClock);
//                            System.out.println("\n");
//                        }
                        races.add(newSet);
                    }
                }
            }
        }
        public void reportRaces(MemoryAccess memoryAccess) {
            if (memoryAccess.rwtype == RWTYPE.WRITE) {
                _reportRaces(memoryAccess, readMap);
            }
            _reportRaces(memoryAccess, writeMap);
        }

        public void addAccess(MemoryAccess memoryAccess) {
            Map<Long, List<MemoryAccess>> insert = (memoryAccess.rwtype == RWTYPE.WRITE) ? writeMap : readMap;
            if (!insert.containsKey(memoryAccess.location)) insert.put(memoryAccess.location, new LinkedList<MemoryAccess>());
            insert.get(memoryAccess.location).add(memoryAccess);
        }
    }
}
