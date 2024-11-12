package org.springframework.samples.petclinic.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class WebMetrics {

	private final MeterRegistry meterRegistry;
	private final Timer requestTimer;
	// counter

	public WebMetrics(final MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		this.requestTimer = Timer.builder("request.timer.duration")
			.tag("type", "http")
			.register(meterRegistry);
	}

	public void recordTimer(long timeMs, int stausCode) {

		requestTimer.record(timeMs, TimeUnit.MILLISECONDS);
		if (stausCode >= 400) {
			// counter.increment();
		}
	}


}
