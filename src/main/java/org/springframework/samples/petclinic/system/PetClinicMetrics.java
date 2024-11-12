package org.springframework.samples.petclinic.system;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class PetClinicMetrics {
	private final Counter newPetsCounter;
	private final Counter newVisitsCounter;
	private final Counter newOwnersCounter;

	public PetClinicMetrics(MeterRegistry registry) {
		this.newPetsCounter = Counter.builder("petclinic.pets.created")
			.description("Number of new pets added")
			.register(registry);

		this.newVisitsCounter = Counter.builder("petclinic.visits.created")
			.description("Number of new visits scheduled")
			.register(registry);

		this.newOwnersCounter = Counter.builder("petclinic.owners.created")
			.description("Number of new owners registered")
			.register(registry);
	}

	public void recordNewPet() {
		newPetsCounter.increment();
	}

	public void recordNewVisit() {
		newVisitsCounter.increment();
	}

	public void recordNewOwner() {
		newOwnersCounter.increment();
	}
}
