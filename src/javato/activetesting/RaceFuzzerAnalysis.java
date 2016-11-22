package javato.activetesting;
import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.CheckerAnalysisImpl;
import javato.activetesting.common.Parameters;
import org.omg.PortableInterceptor.ACTIVE;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class RaceFuzzerAnalysis extends CheckerAnalysisImpl {
//    private CommutativePair racePair;
    private Set<Integer> race;
    private AtomicBoolean raceFound;

    public void initialize() {
        ArrayList<Set<Integer>> races = null;
        if (Parameters.errorId >= 0) {
//    Your code goes here.
//    In my implementation I had the following code:
//            LinkedHashSet<CommutativePair> seenRaces = HybridRaceTracker.getRacesFromFile();
//            racePair = (CommutativePair) (seenRaces.toArray())[Parameters.errorId - 1];

            // http://www.vogella.com/tutorials/JavaSerialization/article.html
            FileInputStream fis = null;
            ObjectInputStream in = null;

            try {
                fis = new FileInputStream(Parameters.ERROR_LOG_FILE);
                in = new ObjectInputStream(fis);
                races = (ArrayList<Set<Integer>>) in.readObject();
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                races = new ArrayList<Set<Integer>>();
            }
        }
        race = races.get(Parameters.errorId-1);
        raceFound = new AtomicBoolean(false);
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
//  ignore this
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
//  ignore this
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
//  ignore this
    }

    public void readBefore(Integer iid, Integer thread, Long memory) {
//    Your code goes here.
//    In my implementation I had the following code:
//        if (racePair != null && racePair.contains(iid)) {
//            synchronized (ActiveChecker.lock) {
//                (new RaceChecker(memory, false, iid)).check();
//            }
//            ActiveChecker.blockIfRequired();
//        }
        if (race != null && race.contains(iid)) {
            synchronized(ActiveChecker.lock) {
                (new RaceChecker(memory, false, raceFound)).check();
            }
            ActiveChecker.blockIfRequired();
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory) {
//    Your code goes here.
//    In my implementation I had the following code:
//        if (racePair != null && racePair.contains(iid)) {
//            synchronized (ActiveChecker.lock) {
//                (new RaceChecker(memory, true, iid)).check();
//            }
//            ActiveChecker.blockIfRequired();
//        }
        if (race != null && race.contains(iid)) {
            synchronized (ActiveChecker.lock) {
                (new RaceChecker(memory, true, raceFound)).check();
            }
            ActiveChecker.blockIfRequired();
        }
    }

    public void finish() {
//  ignore this
        if (raceFound.get()) {
            System.out.println("FOUND RACE");
        } else {
            System.out.println("NO RACE");
        }
    }

    public static class RaceChecker extends ActiveChecker {
        private long location;
        private boolean isWrite;
        private AtomicBoolean flag;

        public RaceChecker(long location, boolean isWrite, AtomicBoolean flag) {
            this.location = location;
            this.isWrite = isWrite;
            this.flag = flag;
        }

        @Override
        public void check(Collection<ActiveChecker> checkers) {
            if (!flag.get()) { // If we've already found the race, then don't bother
                for (ActiveChecker ac : checkers) {
                    RaceChecker rc = (RaceChecker) ac;
                    if (rc.location == location) {
                        if (rc.isWrite || isWrite) {
                            flag.set(true);
                        }
                        // We should probably release both of them now
                        block(10);
                        rc.block(10);
                    }
                }
                block(0); // Wait for buddy to arrive
            }
        }
    }
}
