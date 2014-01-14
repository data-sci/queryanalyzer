package queryanalyzer.query.aggregation;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Buffer;
import cascading.operation.BufferCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import queryanalyzer.util.TimeCompareUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 */
public class SessionCountBuffer extends BaseOperation implements Buffer {

    public  SessionCountBuffer()
    {
        super(1, new Fields("count", "session"));
    }

    public  SessionCountBuffer(Fields fieldsDeclaration)
    {
        super(1, fieldsDeclaration);
    }

    @Override
    public void operate(FlowProcess flowProcess, BufferCall bufferCall) {
        // get all the current argument values for this grouping
        Iterator<TupleEntry> arguments = bufferCall.getArgumentsIterator();

        TupleEntry arg = new TupleEntry();
        List<String> time = new ArrayList<String>();

        while( arguments.hasNext() )
        {
            arg = arguments.next();
            time.add(arg.getString("time"));

        }

        int count = 1;
        for (int i=1;i<time.size();i++){
           if (!TimeCompareUtil.compareTime(time.get(i - 1), time.get(i))){
                Tuple result = new Tuple(count, "session");
                bufferCall.getOutputCollector().add( result );
                count=1;
            } else {
                count++;
            }

        }
        Tuple result = new Tuple(count, "session");
        bufferCall.getOutputCollector().add( result );
    }
}
