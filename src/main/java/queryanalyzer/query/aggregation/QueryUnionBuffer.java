package queryanalyzer.query.aggregation;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Buffer;
import cascading.operation.BufferCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Katsiaryna Ramanouskaya
 * Date: 12/17/13
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryUnionBuffer extends BaseOperation implements Buffer {
    public  QueryUnionBuffer()
    {
        super(1, new Fields("urls","queries","count"));
    }

    public  QueryUnionBuffer(Fields fieldsDeclaration)
    {
        super(1, fieldsDeclaration);
    }


    @Override
    public void operate(FlowProcess flowProcess, BufferCall bufferCall) {
        String url;
        String queryUnion = "";

        // get all the current argument values for this grouping
        Iterator<TupleEntry> arguments = bufferCall.getArgumentsIterator();

        TupleEntry arg = new TupleEntry();

        int kol = 0;

        while( arguments.hasNext() )
        {
            arg = arguments.next();
            queryUnion += arg.getString(new Fields("query"))+",";
            kol++;
        }
        url = arg.getString("url");

        if (!("".equals(queryUnion))) {
            queryUnion = queryUnion.substring(0,queryUnion.length()-1);
        }

        //create a Tuple to hold our result values
        Tuple result = new Tuple(url,queryUnion, kol);

        // return the result Tuple
        bufferCall.getOutputCollector().add( result );
    }
}
