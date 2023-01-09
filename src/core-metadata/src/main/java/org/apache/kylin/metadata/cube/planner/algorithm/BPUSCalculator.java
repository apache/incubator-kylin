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

package org.apache.kylin.metadata.cube.planner.algorithm;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BPUSCalculator implements BenefitPolicy {
    private static Logger logger = LoggerFactory.getLogger(BPUSCalculator.class);

    protected final CuboidStats cuboidStats;
    protected final ImmutableMap<BigInteger, Long> initCuboidAggCostMap;
    protected final Map<BigInteger, Long> processCuboidAggCostMap;

    public BPUSCalculator(CuboidStats cuboidStats) {
        this.cuboidStats = cuboidStats;
        this.initCuboidAggCostMap = ImmutableMap.copyOf(initCuboidAggCostMap());
        this.processCuboidAggCostMap = Maps.newHashMap(initCuboidAggCostMap);
    }

    protected BPUSCalculator(CuboidStats cuboidStats, ImmutableMap<BigInteger, Long> initCuboidAggCostMap) {
        this.cuboidStats = cuboidStats;
        this.initCuboidAggCostMap = initCuboidAggCostMap;
        this.processCuboidAggCostMap = Maps.newHashMap(initCuboidAggCostMap);
    }

    private Map<BigInteger, Long> initCuboidAggCostMap() {
        Map<BigInteger, Long> cuboidAggCostMap = Maps.newHashMap();
        //Initialize stats for mandatory cuboids
        for (BigInteger cuboid : cuboidStats.getAllCuboidsForMandatory()) {
            if (getCuboidCost(cuboid) != null) {
                cuboidAggCostMap.put(cuboid, getCuboidCost(cuboid));
            }
        }

        //Initialize stats for selection cuboids
        long baseCuboidCost = getCuboidCost(cuboidStats.getBaseCuboid());
        for (BigInteger cuboid : cuboidStats.getAllCuboidsForSelection()) {
            long leastCost = baseCuboidCost;
            for (Map.Entry<BigInteger, Long> cuboidTargetEntry : cuboidAggCostMap.entrySet()) {
                // use the equal to check two value
                if ((cuboid.or(cuboidTargetEntry.getKey())).equals(cuboidTargetEntry.getKey())) {
                    if (leastCost > cuboidTargetEntry.getValue()) {
                        leastCost = cuboidTargetEntry.getValue();
                    }
                }
            }
            cuboidAggCostMap.put(cuboid, leastCost);
        }
        return cuboidAggCostMap;
    }

    @Override
    public CuboidBenefitModel.BenefitModel calculateBenefit(BigInteger cuboid, Set<BigInteger> selected) {
        double totalCostSaving = 0;
        int benefitCount = 0;
        for (BigInteger descendant : cuboidStats.getAllDescendants(cuboid)) {
            if (!selected.contains(descendant)) {
                double costSaving = getCostSaving(descendant, cuboid);
                if (costSaving > 0) {
                    totalCostSaving += costSaving;
                    benefitCount++;
                }
            }
        }

        double spaceCost = calculateSpaceCost(cuboid);
        double benefitPerUnitSpace = totalCostSaving / spaceCost;
        return new CuboidBenefitModel.BenefitModel(benefitPerUnitSpace, benefitCount);
    }

    @Override
    public CuboidBenefitModel.BenefitModel calculateBenefitTotal(Set<BigInteger> cuboidsToAdd,
            Set<BigInteger> selected) {
        Set<BigInteger> selectedInner = Sets.newHashSet(selected);
        Map<BigInteger, Long> cuboidAggCostMapCopy = Maps.newHashMap(processCuboidAggCostMap);
        for (BigInteger cuboid : cuboidsToAdd) {
            selectedInner.add(cuboid);
            propagateAggregationCost(cuboid, selectedInner, cuboidAggCostMapCopy);
        }
        double totalCostSaving = 0;
        int benefitCount = 0;
        for (Map.Entry<BigInteger, Long> entry : cuboidAggCostMapCopy.entrySet()) {
            if (entry.getValue() < processCuboidAggCostMap.get(entry.getKey())) {
                totalCostSaving += processCuboidAggCostMap.get(entry.getKey()) - entry.getValue();
                benefitCount++;
            }
        }

        double benefitPerUnitSpace = totalCostSaving;
        return new CuboidBenefitModel.BenefitModel(benefitPerUnitSpace, benefitCount);
    }

    protected double getCostSaving(BigInteger descendant, BigInteger cuboid) {
        long cuboidCost = getCuboidCost(cuboid);
        long descendantAggCost = getCuboidAggregationCost(descendant);
        return (double) descendantAggCost - cuboidCost;
    }

    protected Long getCuboidCost(BigInteger cuboid) {
        return cuboidStats.getCuboidCount(cuboid);
    }

    private long getCuboidAggregationCost(BigInteger cuboid) {
        return processCuboidAggCostMap.get(cuboid);
    }

    @Override
    public boolean ifEfficient(CuboidBenefitModel best) {
        if (best.getBenefit() < getMinBenefitRatio()) {
            logger.info(String.format(Locale.ROOT, "The recommended cuboid %s doesn't meet minimum benifit ratio %f",
                    best, getMinBenefitRatio()));
            return false;
        }
        return true;
    }

    public double getMinBenefitRatio() {
        return cuboidStats.getBpusMinBenefitRatio();
    }

    @Override
    public void propagateAggregationCost(BigInteger cuboid, Set<BigInteger> selected) {
        propagateAggregationCost(cuboid, selected, processCuboidAggCostMap);
    }

    private void propagateAggregationCost(BigInteger cuboid, Set<BigInteger> selected,
            Map<BigInteger, Long> processCuboidAggCostMap) {
        long aggregationCost = getCuboidCost(cuboid);
        Set<BigInteger> childrenCuboids = cuboidStats.getAllDescendants(cuboid);
        for (BigInteger child : childrenCuboids) {
            if (!selected.contains(child) && (aggregationCost < getCuboidAggregationCost(child))) {
                processCuboidAggCostMap.put(child, aggregationCost);
            }
        }
    }

    /**
     * Return the space cost of building a cuboid.
     *
     */
    public double calculateSpaceCost(BigInteger cuboid) {
        return cuboidStats.getCuboidCount(cuboid);
    }

    @Override
    public BenefitPolicy getInstance() {
        return new BPUSCalculator(this.cuboidStats, this.initCuboidAggCostMap);
    }
}
