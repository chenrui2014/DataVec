package org.datavec.spark.transform.join;

import com.google.common.collect.Iterables;
import org.datavec.api.transform.join.Join;
import org.datavec.api.writable.Writable;
import org.datavec.spark.functions.FlatMapFunctionAdapter;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Execute a join
 *
 * @author Alex Black
 */
public class ExecuteJoinFromCoGroupFlatMapFunctionAdapter implements
                FlatMapFunctionAdapter<Tuple2<List<Writable>, Tuple2<Iterable<List<Writable>>, Iterable<List<Writable>>>>, List<Writable>> {

    private final Join join;

    public ExecuteJoinFromCoGroupFlatMapFunctionAdapter(Join join) {
        this.join = join;
    }

    @Override
    public Iterable<List<Writable>> call(
                    Tuple2<List<Writable>, Tuple2<Iterable<List<Writable>>, Iterable<List<Writable>>>> t2)
                    throws Exception {

        Iterable<List<Writable>> leftList = t2._2()._1();
        Iterable<List<Writable>> rightList = t2._2()._2();

        List<List<Writable>> ret = new ArrayList<>();
        Join.JoinType jt = join.getJoinType();
        switch (jt) {
            case Inner:
                //Return records where key columns appear in BOTH
                //So if no values from left OR right: no return values
                for (List<Writable> jvl : leftList) {
                    for (List<Writable> jvr : rightList) {
                        List<Writable> joined = join.joinExamples(jvl, jvr);
                        ret.add(joined);
                    }
                }
                break;
            case LeftOuter:
                //Return all records from left, even if no corresponding right value (NullWritable in that case)
                for (List<Writable> jvl : leftList) {
                    if (Iterables.size(rightList) == 0) {
                        List<Writable> joined = join.joinExamples(jvl, null);
                        ret.add(joined);
                    } else {
                        for (List<Writable> jvr : rightList) {
                            List<Writable> joined = join.joinExamples(jvl, jvr);
                            ret.add(joined);
                        }
                    }
                }
                break;
            case RightOuter:
                //Return all records from right, even if no corresponding left value (NullWritable in that case)
                for (List<Writable> jvr : rightList) {
                    if (Iterables.size(leftList) == 0) {
                        List<Writable> joined = join.joinExamples(null, jvr);
                        ret.add(joined);
                    } else {
                        for (List<Writable> jvl : leftList) {
                            List<Writable> joined = join.joinExamples(jvl, jvr);
                            ret.add(joined);
                        }
                    }
                }
                break;
            case FullOuter:
                //Return all records, even if no corresponding left/right value (NullWritable in that case)
                if (Iterables.size(leftList) == 0) {
                    //Only right values
                    for (List<Writable> jvr : rightList) {
                        List<Writable> joined = join.joinExamples(null, jvr);
                        ret.add(joined);
                    }
                } else if (Iterables.size(rightList) == 0) {
                    //Only left values
                    for (List<Writable> jvl : leftList) {
                        List<Writable> joined = join.joinExamples(jvl, null);
                        ret.add(joined);
                    }
                } else {
                    //Records from both left and right
                    for (List<Writable> jvl : leftList) {
                        for (List<Writable> jvr : rightList) {
                            List<Writable> joined = join.joinExamples(jvl, jvr);
                            ret.add(joined);
                        }
                    }
                }
                break;
        }

        return ret;
    }
}
