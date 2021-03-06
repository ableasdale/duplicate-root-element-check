package com.marklogic.support.jobs;

import com.marklogic.support.providers.CounterProvider;
import com.marklogic.support.providers.MarkLogicContentSourceProvider;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.invoke.MethodHandles;

public class CountRootNodes implements Job {
    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void execute(JobExecutionContext context) {

        JobDataMap dataMap = context.getMergedJobDataMap();
        String uri = dataMap.getString("uri");

        Session s = MarkLogicContentSourceProvider.getInstance().getContentSource().newSession();
        ResultSequence rs = null;

        try {
            rs = s.submitRequest(s.newAdhocQuery(String.format("fn:count(fn:doc(\"%s\"))", uri)));
            int count = Integer.parseInt(rs.asString());
            if (count > 1) {
                long counter = CounterProvider.getInstance().getCounter().incrementAndGet();
                LOG.info(String.format("Doc count is greater than 1: %d - total count: %d", count, counter));
            } else {
                LOG.debug(String.format(String.format("Doc count: %d", count)));
            }

        } catch (RequestException e) {
            LOG.error("Failed XCC request:", e);
        } finally {
            rs.close();
            s.close();
        }
    }

}