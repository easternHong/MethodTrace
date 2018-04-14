/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;

class ProfileProvider {

    private MethodData[] mRoots;
    private TraceReader mReader;
    private String mColumnNames[] = {"Name",
            "Incl Cpu Time %", "Incl Cpu Time", "Excl Cpu Time %", "Excl Cpu Time",
            "Incl Real Time %", "Incl Real Time", "Excl Real Time %", "Excl Real Time",
            "Calls+Recur\nCalls/Total", "Cpu Time/Call", "Real Time/Call"};
    private int mColumnWidths[] = {370,
            100, 100, 100, 100,
            100, 100, 100, 100,
            100, 100, 100};
    private static final int COL_NAME = 0;
    private static final int COL_INCLUSIVE_CPU_TIME_PER = 1;
    private static final int COL_INCLUSIVE_CPU_TIME = 2;
    private static final int COL_EXCLUSIVE_CPU_TIME_PER = 3;
    private static final int COL_EXCLUSIVE_CPU_TIME = 4;
    private static final int COL_INCLUSIVE_REAL_TIME_PER = 5;
    private static final int COL_INCLUSIVE_REAL_TIME = 6;
    private static final int COL_EXCLUSIVE_REAL_TIME_PER = 7;
    private static final int COL_EXCLUSIVE_REAL_TIME = 8;
    private static final int COL_CALLS = 9;
    private static final int COL_CPU_TIME_PER_CALL = 10;
    private static final int COL_REAL_TIME_PER_CALL = 11;
    private long mTotalCpuTime;
    private long mTotalRealTime;
    private int mPrevMatchIndex = -1;

    public ProfileProvider(TraceReader reader) {
        mRoots = reader.getMethods();
        mReader = reader;
        mTotalCpuTime = reader.getTotalCpuTime();
        mTotalRealTime = reader.getTotalRealTime();
    }


    public static boolean hasUpperCaseCharacter(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private MethodData doMatchName(String name, int startIndex) {
        // Check if the given "name" has any uppercase letters
        boolean hasUpper = hasUpperCaseCharacter(name);
        for (int ii = startIndex; ii < mRoots.length; ++ii) {
            MethodData md = mRoots[ii];
            String fullName = md.getName();
            // If there were no upper case letters in the given name,
            // then ignore case when matching.
            if (!hasUpper)
                fullName = fullName.toLowerCase();
            if (fullName.indexOf(name) != -1) {
                mPrevMatchIndex = ii;
                return md;
            }
        }
        mPrevMatchIndex = -1;
        return null;
    }

    public MethodData findMatchingName(String name) {
        return doMatchName(name, 0);
    }

    public MethodData findNextMatchingName(String name) {
        return doMatchName(name, mPrevMatchIndex + 1);
    }


    public String[] getColumnNames() {
        return mColumnNames;
    }

    public int[] getColumnWidths() {
        int[] widths = Arrays.copyOf(mColumnWidths, mColumnWidths.length);
        if (!mReader.haveCpuTime()) {
            widths[COL_EXCLUSIVE_CPU_TIME] = 0;
            widths[COL_EXCLUSIVE_CPU_TIME_PER] = 0;
            widths[COL_INCLUSIVE_CPU_TIME] = 0;
            widths[COL_INCLUSIVE_CPU_TIME_PER] = 0;
            widths[COL_CPU_TIME_PER_CALL] = 0;
        }
        if (!mReader.haveRealTime()) {
            widths[COL_EXCLUSIVE_REAL_TIME] = 0;
            widths[COL_EXCLUSIVE_REAL_TIME_PER] = 0;
            widths[COL_INCLUSIVE_REAL_TIME] = 0;
            widths[COL_INCLUSIVE_REAL_TIME_PER] = 0;
            widths[COL_REAL_TIME_PER_CALL] = 0;
        }
        return widths;
    }


    public Object getRoot() {
        return "root";
    }
}
