package org.springframework.samples.petclinic.system;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class MonitoringConfig {

	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

	@Bean
	public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
		return registry -> registry.config()
			.commonTags("application", "petclinic");
	}

	// JVM Metrics
	@Bean
	public JvmMemoryMetrics jvmMemoryMetrics() {
		return new JvmMemoryMetrics();
	}

	@Bean
	public JvmGcMetrics jvmGcMetrics() {
		return new JvmGcMetrics();
	}

	@Bean
	public JvmThreadMetrics jvmThreadMetrics() {
		return new JvmThreadMetrics();
	}

	@Bean
	public ClassLoaderMetrics classLoaderMetrics() {
		return new ClassLoaderMetrics();
	}

	// System Metrics
	@Bean
	public ProcessorMetrics processorMetrics() {
		return new ProcessorMetrics();
	}

	@Bean
	public UptimeMetrics uptimeMetrics() {
		return new UptimeMetrics();
	}
}
