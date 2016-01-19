/*
 *
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *
 *  contributor license agreements. See the NOTICE file distributed with
 *
 *  this work for additional information regarding copyright ownership.
 *
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *
 *  (the "License"); you may not use this file except in compliance with
 *
 *  the License. You may obtain a copy of the License at
 *
 *
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *
 *  Unless required by applicable law or agreed to in writing, software
 *
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *
 *  limitations under the License.
 *
 * /
 */
package org.apache.kylin.cube.util;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.kylin.common.KylinConfig;
import org.apache.kylin.common.hll.HyperLogLogPlusCounter;
import org.apache.kylin.common.util.ByteArray;
import org.apache.kylin.cube.CubeInstance;
import org.apache.kylin.cube.CubeSegment;
import org.apache.kylin.cube.cuboid.Cuboid;
import org.apache.kylin.cube.cuboid.CuboidScheduler;
import org.apache.kylin.cube.model.CubeDesc;
import org.apache.kylin.cube.model.CubeJoinedFlatTableDesc;
import org.apache.kylin.dict.*;
import org.apache.kylin.metadata.model.TblColRef;
import org.apache.kylin.source.ReadableTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class CubingUtils {

    private static Logger logger = LoggerFactory.getLogger(CubingUtils.class);

    public static Map<Long, HyperLogLogPlusCounter> sampling(CubeDesc cubeDesc, Iterable<List<String>> streams) {
        CubeJoinedFlatTableDesc intermediateTableDesc = new CubeJoinedFlatTableDesc(cubeDesc, null);
        final int rowkeyLength = cubeDesc.getRowkey().getRowKeyColumns().length;
        final List<Long> allCuboidIds = new CuboidScheduler(cubeDesc).getAllCuboidIds();
        final long baseCuboidId = Cuboid.getBaseCuboidId(cubeDesc);
        final Map<Long, Integer[]> allCuboidsBitSet = Maps.newHashMap();

        Lists.transform(allCuboidIds, new Function<Long, Integer[]>() {
            @Nullable
            @Override
            public Integer[] apply(@Nullable Long cuboidId) {
                BitSet bitSet = BitSet.valueOf(new long[] { cuboidId });
                Integer[] result = new Integer[bitSet.cardinality()];

                long mask = Long.highestOneBit(baseCuboidId);
                int position = 0;
                for (int i = 0; i < rowkeyLength; i++) {
                    if ((mask & cuboidId) > 0) {
                        result[position] = i;
                        position++;
                    }
                    mask = mask >> 1;
                }
                return result;
            }
        });
        final Map<Long, HyperLogLogPlusCounter> result = Maps.newHashMapWithExpectedSize(allCuboidIds.size());
        for (Long cuboidId : allCuboidIds) {
            result.put(cuboidId, new HyperLogLogPlusCounter(14));
            BitSet bitSet = BitSet.valueOf(new long[] { cuboidId });
            Integer[] cuboidBitSet = new Integer[bitSet.cardinality()];

            long mask = Long.highestOneBit(baseCuboidId);
            int position = 0;
            for (int i = 0; i < rowkeyLength; i++) {
                if ((mask & cuboidId) > 0) {
                    cuboidBitSet[position] = i;
                    position++;
                }
                mask = mask >> 1;
            }
            allCuboidsBitSet.put(cuboidId, cuboidBitSet);
        }

        HashFunction hf = Hashing.murmur3_32();
        ByteArray[] row_hashcodes = new ByteArray[rowkeyLength];
        for (int i = 0; i < rowkeyLength; i++) {
            row_hashcodes[i] = new ByteArray();
        }
        for (List<String> row : streams) {
            //generate hash for each row key column
            for (int i = 0; i < rowkeyLength; i++) {
                Hasher hc = hf.newHasher();
                final String cell = row.get(intermediateTableDesc.getRowKeyColumnIndexes()[i]);
                if (cell != null) {
                    row_hashcodes[i].set(hc.putString(cell).hash().asBytes());
                } else {
                    row_hashcodes[i].set(hc.putInt(0).hash().asBytes());
                }
            }

            for (Map.Entry<Long, HyperLogLogPlusCounter> longHyperLogLogPlusCounterEntry : result.entrySet()) {
                Long cuboidId = longHyperLogLogPlusCounterEntry.getKey();
                HyperLogLogPlusCounter counter = longHyperLogLogPlusCounterEntry.getValue();
                Hasher hc = hf.newHasher();
                final Integer[] cuboidBitSet = allCuboidsBitSet.get(cuboidId);
                for (int position = 0; position < cuboidBitSet.length; position++) {
                    hc.putBytes(row_hashcodes[cuboidBitSet[position]].array());
                }
                counter.add(hc.hash().asBytes());
            }
        }
        return result;
    }

    public static Map<TblColRef, Dictionary<?>> buildDictionary(final CubeInstance cubeInstance, Iterable<List<String>> recordList) throws IOException {
        final List<TblColRef> columnsNeedToBuildDictionary = cubeInstance.getDescriptor().listDimensionColumnsExcludingDerived();
        final HashMap<Integer, TblColRef> tblColRefMap = Maps.newHashMap();
        int index = 0;
        for (TblColRef column : columnsNeedToBuildDictionary) {
            tblColRefMap.put(index++, column);
        }

        HashMap<TblColRef, Dictionary<?>> result = Maps.newHashMap();

        HashMultimap<TblColRef, String> valueMap = HashMultimap.create();
        for (List<String> row : recordList) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (tblColRefMap.containsKey(i)) {
                    valueMap.put(tblColRefMap.get(i), cell);
                }
            }
        }
        for (TblColRef tblColRef : valueMap.keySet()) {
            final Collection<byte[]> bytes = Collections2.transform(valueMap.get(tblColRef), new Function<String, byte[]>() {
                @Nullable
                @Override
                public byte[] apply(String input) {
                    return input == null ? null : input.getBytes();
                }
            });
            final Dictionary<?> dict = DictionaryGenerator.buildDictionaryFromValueEnumerator(tblColRef.getType(), new IterableDictionaryValueEnumerator(bytes));
            result.put(tblColRef, dict);
        }
        return result;
    }

    public static Map<TblColRef, Dictionary<?>> writeDictionary(CubeSegment cubeSegment, Map<TblColRef, Dictionary<?>> dictionaryMap, long startOffset, long endOffset) {
        Map<TblColRef, Dictionary<?>> realDictMap = Maps.newHashMap();

        for (Map.Entry<TblColRef, Dictionary<?>> entry : dictionaryMap.entrySet()) {
            final TblColRef tblColRef = entry.getKey();
            final Dictionary<?> dictionary = entry.getValue();
            ReadableTable.TableSignature signature = new ReadableTable.TableSignature();
            signature.setLastModifiedTime(System.currentTimeMillis());
            signature.setPath(String.format("streaming_%s_%s", startOffset, endOffset));
            signature.setSize(endOffset - startOffset);
            DictionaryInfo dictInfo = new DictionaryInfo(tblColRef.getTable(), tblColRef.getName(), tblColRef.getColumnDesc().getZeroBasedIndex(), tblColRef.getDatatype(), signature);
            logger.info("writing dictionary for TblColRef:" + tblColRef.toString());
            DictionaryManager dictionaryManager = DictionaryManager.getInstance(KylinConfig.getInstanceFromEnv());
            try {
                DictionaryInfo realDict = dictionaryManager.trySaveNewDict(dictionary, dictInfo);
                cubeSegment.putDictResPath(tblColRef, realDict.getResourcePath());
                realDictMap.put(tblColRef, realDict.getDictionaryObject());
            } catch (IOException e) {
                throw new RuntimeException("error save dictionary for column:" + tblColRef, e);
            }
        }

        return realDictMap;
    }

}
