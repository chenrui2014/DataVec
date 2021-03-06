/*-
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.spark.transform.misc;

import org.apache.spark.api.java.function.Function;
import org.datavec.api.writable.Writable;
import org.datavec.api.transform.sequence.merge.SequenceMerge;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Spark function for merging multiple sequences, using a {@link SequenceMerge} instance.<br>
 *
 * Typical usage:<br>
 * <pre>
 * {@code
 * JavaPairRDD<SomeKey,List<List<Writable>>> myData = ...;
 * SequenceComparator comparator = ...;
 * SequenceMergeFunction<String> sequenceMergeFunction = new SequenceMergeFunction<>(new SequenceMerge(comparator));
 * JavaRDD<List<List<Writable>>> merged = myData.groupByKey().map(sequenceMergeFunction);
 * }
 * </pre>
 *
 * @author Alex Black
 */
public class SequenceMergeFunction<T>
                implements Function<Tuple2<T, Iterable<List<List<Writable>>>>, List<List<Writable>>> {

    private SequenceMerge sequenceMerge;

    public SequenceMergeFunction(SequenceMerge sequenceMerge) {
        this.sequenceMerge = sequenceMerge;
    }

    @Override
    public List<List<Writable>> call(Tuple2<T, Iterable<List<List<Writable>>>> t2) throws Exception {
        List<List<List<Writable>>> sequences = new ArrayList<>();
        for (List<List<Writable>> l : t2._2()) {
            sequences.add(l);
        }

        return sequenceMerge.mergeSequences(sequences);
    }
}
