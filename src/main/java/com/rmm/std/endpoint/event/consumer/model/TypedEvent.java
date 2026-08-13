package com.rmm.std.endpoint.event.consumer.model;

import com.rmm.std.PojaGenerated;
import com.rmm.std.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
