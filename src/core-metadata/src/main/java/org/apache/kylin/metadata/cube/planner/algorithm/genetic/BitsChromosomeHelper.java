/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kylin.metadata.cube.planner.algorithm.genetic;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.kylin.metadata.cube.planner.algorithm.CuboidStats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class BitsChromosomeHelper {

    public final double spaceLimit;
    private final CuboidStats cuboidStats;
    private final CuboidEncoder cuboidEncoder;

    public BitsChromosomeHelper(final double spaceLimit, final CuboidStats cuboidStats) {
        this.spaceLimit = spaceLimit;
        this.cuboidStats = cuboidStats;
        this.cuboidEncoder = new CuboidEncoder(cuboidStats.getAllCuboidsForSelection());
    }

    public ImmutableSet<BigInteger> getMandatoryCuboids() {
        return cuboidStats.getAllCuboidsForMandatory();
    }

    public List<BigInteger> toCuboidList(BitSet bits) {
        return cuboidEncoder.toCuboidList(bits);
    }

    public double getCuboidSize(Set<BigInteger> cuboids) {
        double ret = 0;
        for (BigInteger cuboid : cuboids) {
            ret += cuboidStats.getCuboidSize(cuboid);
        }
        return ret;
    }

    public double getCuboidSizeByBitIndex(int index) {
        return cuboidStats.getCuboidSize(cuboidEncoder.cuboidDomain.get(index));
    }

    public int getLength() {
        return cuboidEncoder.cuboidDomain.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BitsChromosomeHelper that = (BitsChromosomeHelper) o;

        return cuboidEncoder != null ? cuboidEncoder.equals(that.cuboidEncoder) : that.cuboidEncoder == null;

    }

    @Override
    public int hashCode() {
        return cuboidEncoder != null ? cuboidEncoder.hashCode() : 0;
    }

    private static class CuboidEncoder {
        public final ImmutableList<BigInteger> cuboidDomain;

        public CuboidEncoder(Set<BigInteger> cuboidSet) {
            List<BigInteger> cuboidList = Lists.newArrayList(cuboidSet);
            Collections.sort(cuboidList, Collections.reverseOrder());
            this.cuboidDomain = ImmutableList.copyOf(cuboidList);
        }

        public List<BigInteger> toCuboidList(BitSet bits) {
            List<BigInteger> cuboids = Lists.newArrayListWithExpectedSize(bits.cardinality());
            for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                cuboids.add(cuboidDomain.get(i));
            }
            return cuboids;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            CuboidEncoder that = (CuboidEncoder) o;

            return cuboidDomain != null ? cuboidDomain.equals(that.cuboidDomain) : that.cuboidDomain == null;

        }

        @Override
        public int hashCode() {
            return cuboidDomain != null ? cuboidDomain.hashCode() : 0;
        }
    }
}
