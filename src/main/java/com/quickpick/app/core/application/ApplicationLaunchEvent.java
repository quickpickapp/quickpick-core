package com.quickpick.app.core.application;

import com.quickpick.app.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ApplicationLaunchEvent extends Event {
}