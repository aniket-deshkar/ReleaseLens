package dev.releaselens.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(ReleaseLensProperties.class)
public class AppConfiguration {
    @Bean(name = "analysisExecutor")
    AsyncTaskExecutor analysisExecutor() { return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor()); }
}
