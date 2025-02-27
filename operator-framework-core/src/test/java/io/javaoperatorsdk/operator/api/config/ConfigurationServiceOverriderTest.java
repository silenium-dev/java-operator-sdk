package io.javaoperatorsdk.operator.api.config;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.javaoperatorsdk.operator.api.monitoring.Metrics;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ConfigurationServiceOverriderTest {

  private static final Metrics METRICS = new Metrics() {};
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final LeaderElectionConfiguration LEADER_ELECTION_CONFIGURATION =
      new LeaderElectionConfiguration("foo", "fooNS");

  private static final Cloner CLONER = new Cloner() {
    @Override
    public <R extends HasMetadata> R clone(R object) {
      return null;
    }
  };

  @Test
  void overrideShouldWork() {
    final var config = new BaseConfigurationService(null) {
      @Override
      public boolean checkCRDAndValidateLocalModel() {
        return false;
      }

      @Override
      public Config getClientConfiguration() {
        return new ConfigBuilder().withNamespace("namespace").build();
      }

      @Override
      public int concurrentReconciliationThreads() {
        return -1;
      }

      @Override
      public int getTerminationTimeoutSeconds() {
        return -1;
      }

      @Override
      public Metrics getMetrics() {
        return METRICS;
      }

      @Override
      public ExecutorService getExecutorService() {
        return null;
      }

      @Override
      public boolean closeClientOnStop() {
        return true;
      }

      @Override
      public ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
      }

      @Override
      public Cloner getResourceCloner() {
        return CLONER;
      }

      @Override
      public Optional<LeaderElectionConfiguration> getLeaderElectionConfiguration() {
        return Optional.of(LEADER_ELECTION_CONFIGURATION);
      }
    };
    final var overridden = new ConfigurationServiceOverrider(config)
        .withClientConfiguration(new ConfigBuilder().withNamespace("newNS").build())
        .checkingCRDAndValidateLocalModel(true)
        .withExecutorService(Executors.newSingleThreadExecutor())
        .withWorkflowExecutorService(Executors.newFixedThreadPool(4))
        .withCloseClientOnStop(false)
        .withObjectMapper(new ObjectMapper())
        .withResourceCloner(new Cloner() {
          @Override
          public <R extends HasMetadata> R clone(R object) {
            return null;
          }
        })
        .withConcurrentReconciliationThreads(25)
        .withTerminationTimeoutSeconds(100)
        .withMetrics(new Metrics() {})
        .withLeaderElectionConfiguration(new LeaderElectionConfiguration("newLease", "newLeaseNS"))
        .withInformerStoppedHandler((informer, ex) -> {
        })
        .build();

    assertNotEquals(config.closeClientOnStop(), overridden.closeClientOnStop());
    assertNotEquals(config.checkCRDAndValidateLocalModel(),
        overridden.checkCRDAndValidateLocalModel());
    assertNotEquals(config.concurrentReconciliationThreads(),
        overridden.concurrentReconciliationThreads());
    assertNotEquals(config.getTerminationTimeoutSeconds(),
        overridden.getTerminationTimeoutSeconds());
    assertNotEquals(config.getClientConfiguration(), overridden.getClientConfiguration());
    assertNotEquals(config.getExecutorService(), overridden.getExecutorService());
    assertNotEquals(config.getWorkflowExecutorService(), overridden.getWorkflowExecutorService());
    assertNotEquals(config.getMetrics(), overridden.getMetrics());
    assertNotEquals(config.getObjectMapper(), overridden.getObjectMapper());
    assertNotEquals(config.getLeaderElectionConfiguration(),
        overridden.getLeaderElectionConfiguration());
    assertNotEquals(config.getInformerStoppedHandler(),
        overridden.getLeaderElectionConfiguration());
  }
}
