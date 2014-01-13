package queryanalyzer;


import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.operation.*;
import cascading.operation.aggregator.Count;
import cascading.operation.aggregator.First;
import cascading.operation.filter.Limit;
import cascading.operation.regex.RegexParser;
import cascading.pipe.*;
import cascading.pipe.assembly.AverageBy;
import cascading.pipe.assembly.CountBy;
import cascading.pipe.assembly.Discard;
import cascading.pipe.joiner.InnerJoin;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import queryanalyzer.query.aggregation.SessionCountBuffer;

import java.util.Properties;

/**
 * User: frontijer
 */
public class QueryProcessing {
    public static void main(String[] args) {

        //input query data
        Fields queryField = new Fields("sid", "ip","browser", "time", "query");
        String inQueryPath = "/home/hadoop/workspace/projects/queryanalyzer/resources/analyzer/logs.txt";
        Tap inQueryTap = new Hfs(new TextDelimited(queryField, false, "\t"), inQueryPath);

        //input url data
        String inUrlPath = "/home/hadoop/workspace/projects/queryanalyzer/resources/analyzer/url.txt";
        Fields urlField = new Fields("uid", "url");
        Tap inUrlTap = new Hfs(new TextDelimited(urlField, false, "   "), inUrlPath);

        //output file of top the best query
        String outBestQuery = "/home/hadoop/workspace/projects/queryanalyzer/resources/analyzer/output/topQuery";
        Tap outTopQyeryTap = new Hfs(new TextDelimited(false,"\t"),outBestQuery, SinkMode.REPLACE);

        //output more  usefull time for query
        String outActiveTime = "/home/hadoop/workspace/projects/queryanalyzer/resources/analyzer/output/activeTime";
        Tap outActiveTimeTap = new Hfs(new TextDelimited(false,"\t"),outActiveTime, SinkMode.REPLACE);

        //output average request in session
        String outAvrgReq = "/home/hadoop/workspace/projects/queryanalyzer/resources/analyzer/output/avrgReq";
        Tap outAvrgReqTap = new Hfs(new TextDelimited(false,"\t"),outAvrgReq, SinkMode.REPLACE);

        //output top urls and the qieries
        String outTopUrl = "/home/hadoop/workspace/projects/queryanalyzer/resources/analizer/output/topUrl";
        Tap outTopUrlTap = new Hfs(new TextDelimited(false,"\t"),outTopUrl, SinkMode.REPLACE);

        //reading from input
        Pipe queries = new Pipe("query");
        Pipe clicks = new Pipe("click");

        //processing top 25 the best queries
        Pipe topQ = new GroupBy("top queries",queries, new Fields("query"));
        topQ = new Every(topQ, new Count(new Fields("count")));
        topQ = new GroupBy(topQ, new Fields("count"), true);
        topQ = new Each(topQ, new Limit(25));

        //processing average count queries in session
        Pipe avgQ = new GroupBy("average queries", queries, new Fields("ip","browser"), new Fields("time"), false);
        avgQ = new Every(avgQ, new Fields("ip","time"), new SessionCountBuffer(),Fields.RESULTS);
        //avgQ = new Each(avgQ, DebugLevel.VERBOSE, new Debug());
        //Fields temp = new Fields("temp");//for using AverageBy I add temp fields
        //Insert insertFunction = new Insert(temp,"temp");
        //avgQ = new Each(avgQ, insertFunction, Fields.ALL);
        //avgQ = new Each(avgQ, DebugLevel.VERBOSE, new Debug());
        avgQ = new AverageBy(avgQ, new Fields("session"), new Fields("count"), new Fields("average"));
        avgQ = new Discard(avgQ, new Fields("session"));
        avgQ = new Each(avgQ, DebugLevel.VERBOSE, new Debug());

        //processing activity users
        Pipe timeActivity = new Pipe("time activity", queries);
        Fields dataParser = new Fields("d", "m", "y", "hh", "mm", "ss", "t");
        String dataRegex = "(\\d{2})\\/(\\w{3})\\/(\\d{4}):(\\d{2}):(\\d{2}):(\\d{2})\\s-(\\d{4})";
        RegexParser parser = new RegexParser(dataParser, dataRegex);
        timeActivity = new Each(timeActivity, new Fields("time"), parser);
        timeActivity = new GroupBy(timeActivity, new Fields("hh"));
        timeActivity = new Every(timeActivity, new Count(new Fields("count")));

        //top queries per url
        Pipe topU = new CoGroup("joined", clicks, new Fields("uid"), queries, new Fields("sid"), new InnerJoin());
        topU = new CountBy(topU, new Fields("url", "query"), new Fields("count"));
        topU = new GroupBy(topU, new Fields("url"), new Fields("count"), true);
        topU = new Every(topU, Fields.FIRST, new First(Fields.ARGS, 10), Fields.ARGS);

        FlowDef flowDef = FlowDef.flowDef()
                //.addSource(clicks, inUrlTap)
                .addSource(queries, inQueryTap)
                .addTailSink(topQ, outTopQyeryTap)
                //.addTailSink(avgQ, outAvrgReqTap)
                .addTailSink(timeActivity, outActiveTimeTap)
                //.addTailSink(topU, outTopUrlTap)
                ;
        flowDef.setDebugLevel( DebugLevel.VERBOSE );
        getHadoopFlowConnector().connect(flowDef)
                .writeDOT("dot/queryanalyzer/query.dot");

        getHadoopFlowConnector().connect(flowDef) .complete();
    }

    static HadoopFlowConnector getHadoopFlowConnector() {
        Properties properties = new Properties();
        AppProps.setApplicationJarClass(properties, QueryProcessing.class);
        return new HadoopFlowConnector(properties);
    }
}
