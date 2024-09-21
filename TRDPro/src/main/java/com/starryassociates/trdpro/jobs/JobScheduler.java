package com.starryassociates.trdpro.jobs;

import com.starryassociates.trdpro.jobs.model.JobInfo;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.List;

public class JobScheduler {

    private List<JobInfo> jobs;
    private JobService jobService;
    private final LambdaClient lambdaClient;
    private final SnsClient snsClient;

    // Public constructor to allow instantiation
    public JobScheduler(List<JobInfo> jobs, JobService jobService) {
        this.jobs = jobs;
        this.jobService = jobService;
        this.lambdaClient = LambdaClient.create(); // Initialize Lambda client
        this.snsClient = SnsClient.create(); // Initialize SNS client
    }

    public synchronized void runDueJobs() {
        if (!jobService.acquireGlobalLock()) {
            System.out.println("Previous job set is still running. Exiting.");
            return;
        }
        String status = "Failure";

        try {
            for (JobInfo job : jobs) {
                if (job.isDueForRun()) {
                    try {
                        job.startJob();  // Mark the job as running
                        status = triggerLambdaFunction(job);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    jobService.updateJobStatus(job, status);
                }
            }
        } finally {
            jobService.releaseGlobalLock();  // Release the lock once all jobs are done
        }
        return;
    }

    private String triggerLambdaFunction(JobInfo job) {
        System.out.println("Triggering Lambda function for job: " + job.getName());

        InvokeRequest request = InvokeRequest.builder()
                .functionName(job.getLambdaFunctionName())
                .build();

        InvokeResponse response = lambdaClient.invoke(request);
        int statusCode = response.statusCode();
        String status = statusCode == 200 ? "Success" : "Failure";

        if (job.isSnsEnabled() && job.getSnsArn() != null) {
            publishToSns(job, status);
        }
        return status;
    }

    private void publishToSns(JobInfo job, String status) {
        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(job.getSnsArn())
                    .message("Job " + job.getName() + " has been completed with status: " + status)
                    .build();

            PublishResponse publishResponse = snsClient.publish(publishRequest);
            System.out.println("SNS Message ID: " + publishResponse.messageId());
        } catch (Exception e) {
            System.out.println("SNS Message publish failed");
        }
    }

    public List<JobInfo> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobInfo> jobs) {
        this.jobs = jobs;
    }
}
