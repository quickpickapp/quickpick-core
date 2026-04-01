package de.lukasbreuer.quickpick.core.application;

import de.lukasbreuer.quickpick.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ApplicationLaunchEvent extends Event {
}