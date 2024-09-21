package com.starryassociates.trdpro.jobs;

import com.amazonaws.services.lambda.runtime.Context;
import com.starryassociates.trdpro.jobs.model.JobInfo;

import java.util.List;
import java.util.Map;

public class JobSchedulerLambda {

    private JobService jobService;

    public JobSchedulerLambda() {
        this.jobService = new JobService(); // Initialize your JobService here
    }

    public void handleRequest(Map<String, Object> event, Context context) {
        // Fetch the list of jobs from the JobService or other storage
        List<JobInfo> jobs = jobService.getAllJobs();

        JobScheduler jobScheduler = new JobScheduler(jobs, jobService);
        jobScheduler.runDueJobs();
    }
}
